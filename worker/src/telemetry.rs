//! Telemetria OTel do worker — T-403, RF-029 / RF-H-004.
//!
//! Duas responsabilidades:
//! 1. **Init**: provider de traces (recurso canônico `report-worker`/`medrec`), exportador
//!    OTLP/HTTP e bridge `tracing → OpenTelemetry`; propagador global **W3C**.
//! 2. **Propagação HTTP→AMQP**: extrai o `traceparent` dos **headers da mensagem** para que
//!    o span de processamento continue o trace do publicador (a fronteira que mais quebra
//!    correlação — RF-H-004).

use std::collections::HashMap;

use anyhow::Result;
use lapin::types::{AMQPValue, FieldTable};
use opentelemetry::propagation::{Extractor, TextMapPropagator};
use opentelemetry::Context;
use opentelemetry_sdk::propagation::TraceContextPropagator;

/// Nome canônico do recurso (contrato T-005 §1).
pub const SERVICE_NAME: &str = "report-worker";
pub const SERVICE_NAMESPACE: &str = "medrec";

/// Reduz o byte de `trace-flags` do `traceparent` ao bit "sampled" (0x01) que o propagador
/// desta versão do `opentelemetry_sdk` (0.27) reconhece.
///
/// **Achado em T-405 (validação ponta a ponta):** o OTel Java do Quarkus injeta bits extras
/// no `trace-flags` (ex.: `03` — o "random trace id flag" do W3C Trace Context Level 2, além
/// do bit `sampled`). O parser do `opentelemetry_sdk` 0.27 rejeita qualquer valor de
/// `trace-flags` fora de `00`/`01`, descartando o `SpanContext` inteiro — o span de
/// processamento silenciosamente virava raiz e a correlação HTTP→AMQP quebrava (RF-H-004),
/// mesmo com o publicador injetando o header corretamente. Os demais bits são reservados pelo
/// W3C (não usados para o parenteamento local do span aqui), então normalizá-los é seguro.
fn sanitizar_trace_flags(traceparent: &str) -> String {
    match traceparent.rsplit_once('-') {
        Some((prefixo, flags)) if flags.len() == 2 => {
            let bits = u8::from_str_radix(flags, 16).unwrap_or(0);
            let sampled = if bits & 0x01 != 0 { "01" } else { "00" };
            format!("{prefixo}-{sampled}")
        }
        _ => traceparent.to_string(),
    }
}

/// Converte os headers AMQP em um mapa de strings (só valores textuais interessam para a
/// propagação W3C: `traceparent`/`tracestate`/`baggage`).
fn headers_para_mapa(headers: &FieldTable) -> HashMap<String, String> {
    let mut mapa = HashMap::new();
    for (k, v) in headers.inner() {
        let valor = match v {
            AMQPValue::LongString(s) => Some(s.to_string()),
            AMQPValue::ShortString(s) => Some(s.to_string()),
            _ => None,
        };
        if let Some(valor) = valor {
            let chave = k.to_string();
            let valor = if chave == "traceparent" {
                sanitizar_trace_flags(&valor)
            } else {
                valor
            };
            mapa.insert(chave, valor);
        }
    }
    mapa
}

/// Adaptador de `Extractor` sobre o mapa de headers.
struct MapExtractor<'a>(&'a HashMap<String, String>);

impl Extractor for MapExtractor<'_> {
    fn get(&self, key: &str) -> Option<&str> {
        self.0.get(key).map(String::as_str)
    }

    fn keys(&self) -> Vec<&str> {
        self.0.keys().map(String::as_str).collect()
    }
}

/// Extrai o contexto de trace propagado nos headers AMQP (W3C). Sem `traceparent`,
/// retorna um contexto vazio (o span de processamento vira raiz).
pub fn extrair_contexto(headers: &FieldTable) -> Context {
    let mapa = headers_para_mapa(headers);
    let propagator = TraceContextPropagator::new();
    propagator.extract(&MapExtractor(&mapa))
}

/// Grava `trace_id`/`span_id` como campos do span (contrato T-005 §4.1 — obrigatório em logs
/// quando há trace ativo; RF-H-004: correlação log↔worker). O span precisa declarar esses
/// campos vazios na criação (`tracing::field::Empty`) para poderem ser gravados aqui.
///
/// Requer o feature `tracing_unstable`/registro do `tracing-opentelemetry` já ativo (T-403);
/// sem trace ativo (span raiz sem propagação), não grava nada.
pub fn registrar_trace_context(span: &tracing::Span) {
    use opentelemetry::trace::TraceContextExt;
    use tracing_opentelemetry::OpenTelemetrySpanExt;

    let sc = span.context().span().span_context().clone();
    if sc.is_valid() {
        span.record("trace_id", format!("{:032x}", sc.trace_id()));
        span.record("span_id", format!("{:016x}", sc.span_id()));
    }
}

/// Inicializa o pipeline OTel (exportador OTLP/HTTP) e o bridge tracing→OTel.
///
/// Endpoint via `OTEL_EXPORTER_OTLP_ENDPOINT` (padrão do OTel). Idempotente o suficiente
/// para o `main`; retorna o provider para `shutdown` no encerramento.
pub fn init() -> Result<opentelemetry_sdk::trace::TracerProvider> {
    use opentelemetry::trace::TracerProvider as _;
    use opentelemetry::KeyValue;
    use tracing_subscriber::prelude::*;

    opentelemetry::global::set_text_map_propagator(TraceContextPropagator::new());

    let exporter = opentelemetry_otlp::SpanExporter::builder()
        .with_http()
        .build()?;

    let resource = opentelemetry_sdk::Resource::new(vec![
        KeyValue::new("service.name", SERVICE_NAME),
        KeyValue::new("service.namespace", SERVICE_NAMESPACE),
    ]);

    let provider = opentelemetry_sdk::trace::TracerProvider::builder()
        .with_batch_exporter(exporter, opentelemetry_sdk::runtime::Tokio)
        .with_resource(resource)
        .build();

    let tracer = provider.tracer(SERVICE_NAME);
    let otel_layer = tracing_opentelemetry::layer().with_tracer(tracer);

    tracing_subscriber::registry()
        .with(
            tracing_subscriber::EnvFilter::try_from_default_env().unwrap_or_else(|_| "info".into()),
        )
        .with(tracing_subscriber::fmt::layer().json())
        .with(otel_layer)
        .init();

    opentelemetry::global::set_tracer_provider(provider.clone());
    Ok(provider)
}

#[cfg(test)]
mod tests {
    use super::*;
    use lapin::types::{AMQPValue, FieldTable, LongString};
    use opentelemetry::trace::TraceContextExt;

    fn headers_com(traceparent: &str) -> FieldTable {
        let mut ft = FieldTable::default();
        ft.insert(
            "traceparent".into(),
            AMQPValue::LongString(LongString::from(traceparent)),
        );
        ft
    }

    #[test]
    fn extrai_trace_id_do_traceparent() {
        // traceparent W3C: version-traceid-spanid-flags
        let tp = "00-0af7651916cd43dd8448eb211c80319c-b7ad6b7169203331-01";
        let ctx = extrair_contexto(&headers_com(tp));
        let sc = ctx.span().span_context().clone();
        assert!(sc.is_valid(), "deveria extrair um SpanContext válido");
        assert_eq!(
            format!("{:032x}", sc.trace_id()),
            "0af7651916cd43dd8448eb211c80319c"
        );
        assert!(sc.is_remote(), "contexto extraído deve ser remoto");
    }

    #[test]
    fn sem_traceparent_contexto_invalido() {
        let ctx = extrair_contexto(&FieldTable::default());
        assert!(!ctx.span().span_context().is_valid());
    }

    #[test]
    fn traceparent_com_bits_reservados_no_trace_flags_e_extraido() {
        // T-405: reproduz o traceparent real injetado pelo OTel Java do Quarkus (flags=03 —
        // sampled + random trace id flag), que o parser do opentelemetry_sdk 0.27 rejeitava
        // sem a sanitização.
        let tp = "00-e8ffad7619030025c644045465ef46ff-5e2d5d8afcb211c5-03";
        let ctx = extrair_contexto(&headers_com(tp));
        let sc = ctx.span().span_context().clone();
        assert!(
            sc.is_valid(),
            "deveria extrair um SpanContext válido mesmo com flags=03"
        );
        assert_eq!(
            format!("{:032x}", sc.trace_id()),
            "e8ffad7619030025c644045465ef46ff"
        );
        assert!(sc.is_remote());
    }

    #[test]
    fn sanitizar_trace_flags_preserva_sampled_e_zera_bits_extras() {
        assert_eq!(
            sanitizar_trace_flags("00-0af7651916cd43dd8448eb211c80319c-b7ad6b7169203331-03"),
            "00-0af7651916cd43dd8448eb211c80319c-b7ad6b7169203331-01"
        );
        assert_eq!(
            sanitizar_trace_flags("00-0af7651916cd43dd8448eb211c80319c-b7ad6b7169203331-02"),
            "00-0af7651916cd43dd8448eb211c80319c-b7ad6b7169203331-00"
        );
        assert_eq!(
            sanitizar_trace_flags("00-0af7651916cd43dd8448eb211c80319c-b7ad6b7169203331-01"),
            "00-0af7651916cd43dd8448eb211c80319c-b7ad6b7169203331-01"
        );
    }
}
