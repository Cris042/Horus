# Plano de Execução — T-108: Concorrência no Payment

> ⚠️ **Regra R2 ([WORKFLOW](../../WORKFLOW.md)).** Atualize `Status`/`Atualizado` e o *Registro de alterações* ao criar/modificar cada arquivo.

| Campo | Valor |
|---|---|
| **Task** | `T-108` |
| **Branch** | `task/T-108-payment-concurrency` |
| **PRD** | [`./PRD.md`](./PRD.md) |
| **Progresso** | `3/3` arquivos |

## Arquivos a criar/modificar

| # | Arquivo | Ação | Propósito | Status | Atualizado |
|---|---|---|---|---|---|
| 1 | `services/payment/src/main/java/org/example/payment/service/CarteiraService.java` | Modificar | `aplicarPorId` com `PESSIMISTIC_WRITE`; `movimentar` usa-o | ✅ Concluído | 2026-06-28 |
| 2 | `services/payment/src/main/java/org/example/payment/service/PagamentoService.java` | Modificar | aprovar/estornar debitam via `aplicarPorId` (carteira sob lock) | ✅ Concluído | 2026-06-28 |
| 3 | `services/payment/src/test/java/org/example/payment/api/PaymentConcurrencyTest.java` | Criar | Teste de concorrência (sem lost update / sem saldo negativo) | ✅ Concluído | 2026-06-28 |

**Legenda:** ⬜ Pendente · 🟡 Em progresso · ✅ Concluído

## Passos de implementação

1. `aplicarPorId` carrega a carteira com `LockModeType.PESSIMISTIC_WRITE` e aplica a movimentação.
2. `movimentar` e `PagamentoService` (aprovar/estornar) passam a usar `aplicarPorId`.
3. Teste de concorrência com `ExecutorService` + `CountDownLatch` (20 threads, Testcontainers).

## Verificação / testes

- [x] `mvn -pl services/payment test` verde (7/7, 2 novos de concorrência).

## Registro de alterações

| Data | Arquivo | Mudança |
|---|---|---|
| `2026-06-28` | `CarteiraService.java` | `aplicarPorId` com lock pessimista; `movimentar` delega |
| `2026-06-28` | `PagamentoService.java` | aprovar/estornar via `aplicarPorId` |
| `2026-06-28` | `PaymentConcurrencyTest.java` | teste de débitos concorrentes |
