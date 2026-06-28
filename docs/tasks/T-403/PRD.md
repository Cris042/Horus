# PRD — T-403: OTel no worker Rust + propagação HTTP→AMQP

| Campo | Valor |
|---|---|
| **Task** | `T-403` |
| **Fase do roadmap** | `Fase 4 — Telemetria base (OpenTelemetry)` |
| **Status** | `Entregue` |
| **Branch** | `task/T-403-worker-otel-amqp` |
| **PR** | [#34](https://github.com/mclovin137/Horus/pull/34) |
| **Depende de** | `T-303` (worker Rust) |
| **Requisitos atendidos** | `RF-029`, `RF-H-004` |
| **ADRs relacionados** | `ADR-0007` (propagação de contexto) |
| **Data** | `2026-06-28` |

## Objetivo

Instrumentar o worker Rust com OpenTelemetry e **continuar o trace na fronteira HTTP→AMQP**
(RF-H-004): extrair o `traceparent`/`tracestate` (W3C) dos **headers da mensagem** e abrir o
span de processamento como **filho** do span publicador, preservando o `trace_id` ponta a
ponta. É o último elo antes de validar a correlação completa (T-405).

## Escopo (o que entra)

- Deps OTel (Rust): `opentelemetry`, `opentelemetry_sdk` (rt-tokio), `opentelemetry-otlp`
  (OTLP/HTTP — sem protoc/gRPC), `opentelemetry-semantic-conventions`, `tracing-opentelemetry`.
- `telemetry.rs`: init do provider (recurso canônico `report-worker`/`medrec`) + exportador
  OTLP/HTTP + bridge `tracing→OTel`; propagador global **W3C**.
- `extrair_contexto(headers)`: `Extractor` sobre os headers AMQP → `opentelemetry::Context`.
- `main`: por mensagem, abre `info_span!` e faz `set_parent(contexto_extraído)`.
- Testes (em container Rust): extração de `trace_id` do `traceparent`; ausência → contexto inválido.

## Fora do escopo

- Validação ponta a ponta da correlação (todos os componentes no mesmo `trace_id`) → **T-405**.
- Métricas/logs OTel além de traces (logs do worker já são JSON via `tracing`).

## Premissas e dependências

- Produtor (invoice) injeta `traceparent` nos headers AMQP (T-301/contrato T-005 §2).
- Endpoint OTLP via `OTEL_EXPORTER_OTLP_ENDPOINT` (padrão do OTel; Collector do compose).
- Verificação local feita **em container `rust:1-slim`** (host sem `gcc`); CI valida no job `build-worker`.

## Critérios de aceite

- [ ] `extrair_contexto` recupera o `trace_id` de um `traceparent` W3C (teste).
- [ ] Sem `traceparent`, o contexto é inválido/raiz (teste).
- [ ] Worker compila e `cargo test`/clippy/fmt verdes (container + CI).

## Riscos

| Risco | Mitigação |
|---|---|
| Matriz de versões OTel Rust acoplada | Conjunto 0.27/0.28 alinhado; verificado por compilação em container. |
| `opentelemetry-otlp` puxar protoc/openssl | OTLP/**HTTP** (`http-proto` + `reqwest-client`), sem gRPC/tonic. |

## Referências

- [`../../telemetry/CONTRACT.md`](../../telemetry/CONTRACT.md) §2 (propagação AMQP)
- [`../../architecture/report-message.md`](../../architecture/report-message.md)
- [`./PLAN.md`](./PLAN.md)
