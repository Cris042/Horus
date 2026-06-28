# Plano de Execução — T-403: OTel no worker + propagação HTTP→AMQP

> ⚠️ **Regra R2 ([WORKFLOW](../../WORKFLOW.md)).** Atualize `Status`/`Atualizado` e o *Registro de alterações* ao criar/modificar cada arquivo.

| Campo | Valor |
|---|---|
| **Task** | `T-403` |
| **Branch** | `task/T-403-worker-otel-amqp` |
| **PRD** | [`./PRD.md`](./PRD.md) |
| **Progresso** | `4/4` arquivos |

## Arquivos a criar/modificar

| # | Arquivo | Ação | Propósito | Status | Atualizado |
|---|---|---|---|---|---|
| 1 | `worker/Cargo.toml` | Modificar | Deps OTel (opentelemetry/sdk/otlp-http/semconv/tracing-otel) | ✅ Concluído | 2026-06-28 |
| 2 | `worker/src/telemetry.rs` | Criar | Init OTel + `extrair_contexto` (W3C dos headers AMQP) | ✅ Concluído | 2026-06-28 |
| 3 | `worker/src/main.rs` | Modificar | Init telemetria; span filho por mensagem (`set_parent`) | ✅ Concluído | 2026-06-28 |
| 4 | `docs/tasks/T-403/*` | Criar | PRD + plano | ✅ Concluído | 2026-06-28 |

**Legenda:** ⬜ Pendente · 🟡 Em progresso · ✅ Concluído

## Passos de implementação

1. Adicionar deps OTel (OTLP/HTTP, sem gRPC) ao `Cargo.toml`.
2. `telemetry.rs`: provider+exportador+bridge; `Extractor` sobre headers AMQP; propagador W3C.
3. `main`: por mensagem, `set_parent(extrair_contexto(headers))` no span de processamento.

## Verificação / testes

- [ ] `cargo fmt --check`, `cargo clippy -D warnings`, `cargo test` verdes (container `rust:1-slim` + CI `build-worker`).
- [ ] Testes de propagação (extrai `trace_id`; ausência → inválido).

## Registro de alterações

| Data | Arquivo | Mudança |
|---|---|---|
| `2026-06-28` | `worker/Cargo.toml` | Deps OTel |
| `2026-06-28` | `worker/src/telemetry.rs` | Init OTel + extração W3C dos headers AMQP |
| `2026-06-28` | `worker/src/main.rs` | Span de processamento filho do publicador |
