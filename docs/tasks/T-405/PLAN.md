# Plano de Execução — T-405: Validar correlação ponta a ponta

> ⚠️ **Regra R2 ([WORKFLOW](../../WORKFLOW.md)).** Sempre que **criar ou modificar** um arquivo desta tabela, atualize **na mesma hora** o seu `Status` e `Atualizado`, e registre a mudança em *Registro de alterações*. Arquivo não previsto que precise ser tocado deve ser **adicionado** à tabela.

| Campo | Valor |
|---|---|
| **Task** | `T-405` |
| **Branch** | `task/T-405-e2e-correlation` |
| **PRD** | [`./PRD.md`](./PRD.md) |
| **Progresso** | `6/6` arquivos |

## Arquivos a criar/modificar

| # | Arquivo | Ação | Propósito | Status | Atualizado |
|---|---|---|---|---|---|
| 1 | `docs/tasks/T-405/PRD.md` | Criar | PRD + evidência da validação + bugs encontrados/corrigidos | ✅ Concluído | 2026-07-02 |
| 2 | `docs/tasks/T-405/PLAN.md` | Criar | Plano de execução desta task | ✅ Concluído | 2026-07-02 |
| 3 | `services/invoice/src/main/java/org/example/invoice/relatorio/RelatorioPublisher.java` | Modificar | Injeta `traceparent`/`tracestate` nos headers AMQP (Bug 1) | ✅ Concluído | 2026-07-02 |
| 4 | `services/invoice/src/test/java/org/example/invoice/relatorio/RelatorioPublicacaoTest.java` | Modificar | Regressão: mensagem deve carregar `traceparent` | ✅ Concluído | 2026-07-02 |
| 5 | `services/invoice/src/test/java/org/example/invoice/relatorio/OtelEnabledTestProfile.java` | Criar | Reativa o SDK OTel (exportador `none`) só para o teste acima | ✅ Concluído | 2026-07-02 |
| 6 | `worker/src/telemetry.rs` | Modificar | `sanitizar_trace_flags` (Bug 2) + `registrar_trace_context` (gap de log) + testes | ✅ Concluído | 2026-07-02 |
| 7 | `worker/src/main.rs` | Modificar | Declara campos `trace_id`/`span_id` no span e chama `registrar_trace_context` | ✅ Concluído | 2026-07-02 |

> `state.md` atualizado ao final.

## Passos de implementação

1. Subir infra real (`docker compose up -d postgres-invoice rabbitmq otel-collector jaeger`).
2. Buildar e rodar `invoice-service` (JAR Quarkus, perfil dev) e `worker` (container `rust:1`,
   rede do compose) de verdade.
3. Exercitar `POST /notas` + `POST /notas/{id}/relatorio`; extrair `trace_id` do log do
   invoice; consultar `GET /api/traces/{trace_id}` no Jaeger.
4. **Bug 1 encontrado** (span do worker sem nenhuma relação — trace novo): corrigir
   `RelatorioPublisher` para injetar o `traceparent` nos headers AMQP via
   `OutgoingRabbitMQMetadata`.
5. Rebuild + reteste → **Bug 2 encontrado** (header chega, mas o worker ainda abre trace novo):
   depurar via header cru da fila (API do RabbitMQ) + teste unitário reproduzindo o
   `trace-flags=03` real; corrigir com `sanitizar_trace_flags` no worker.
6. Rebuild + reteste → span do worker aparece corretamente como filho no Jaeger.
7. **Gap adicional encontrado** (logs do worker sem `trace_id`/`span_id`): corrigir com
   `registrar_trace_context` + campos declarados no span em `main.rs`.
8. Rebuild + reteste final → logs de `invoice-service` e `worker` com o mesmo `trace_id`.
9. Adicionar testes de regressão (Java + Rust) para os dois bugs corrigidos.
10. `cargo fmt` / `cargo clippy -- -D warnings` / `cargo test` (worker) e
    `./mvnw -pl services/invoice -am test` (invoice) verdes.
11. Desmontar o ambiente de teste.

## Verificação / testes

- [x] `./mvnw -pl services/invoice -am test` → 8/8 (inclui o novo teste de header AMQP).
- [x] `cargo test --release` (worker, em container `rust:1`) → 14/14 (inclui os 2 novos).
- [x] `cargo fmt --check` / `cargo clippy --release -- -D warnings` → limpos.
- [x] Validação real via Jaeger: `processar_relatorio` (report-worker) aparece como filho de
      `relatorios publish` (invoice-service) sob o mesmo `trace_id`.
- [x] Logs estruturados de ambos os serviços com o mesmo `trace_id`.

## Registro de alterações

| Data | Arquivo | Mudança |
|---|---|---|
| `2026-07-02` | `docs/tasks/T-405/*` | PRD (com evidência completa) e plano criados |
| `2026-07-02` | `services/invoice/.../RelatorioPublisher.java` | Injeta `traceparent` nos headers AMQP (Bug 1) |
| `2026-07-02` | `services/invoice/.../RelatorioPublicacaoTest.java` | Regressão: header `traceparent` presente na mensagem publicada |
| `2026-07-02` | `services/invoice/.../OtelEnabledTestProfile.java` | Perfil de teste com SDK OTel ligado (exportador `none`) |
| `2026-07-02` | `worker/src/telemetry.rs` | `sanitizar_trace_flags` (Bug 2) + `registrar_trace_context` (gap de log) + 2 testes novos |
| `2026-07-02` | `worker/src/main.rs` | Campos `trace_id`/`span_id` no span + chamada a `registrar_trace_context` |
