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

/// Converte os headers AMQP em um mapa de strings (só valores textuais interessam para a
/// propagação W3C: `traceparent`/`tracestate`/`baggage`).
fn headers_para_mapa(headers: &FieldTable) -> HashMap<String, String> {
    let mut mapa = HashMap::new();
    for (k, v) in headers.inner() {
        if let AMQPValue::LongString(s) = v {
            mapa.insert(k.to_string(), s.to_string());
        } else if let AMQPValue::ShortString(s) = v {
            mapa.insert(k.to_string(), s.to_string());
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
}
