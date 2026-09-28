# Plano de Execução — T-1010: Robustez da SAGA

> ⚠️ **Regra R2 ([WORKFLOW](../../WORKFLOW.md)).** Sempre que **criar ou modificar** um arquivo desta tabela, atualize **na mesma hora** o seu `Status` e `Atualizado`, e registre a mudança em *Registro de alterações*. Arquivo não previsto que precise ser tocado deve ser **adicionado** à tabela.

| Campo | Valor |
|---|---|
| **Task** | `T-1010` |
| **Branch** | `claude/beautiful-cray-sfblsr` |
| **PRD** | [`./PRD.md`](./PRD.md) |
| **Progresso** | `16/16` arquivos |

## Arquivos a criar/modificar

| # | Arquivo | Ação | Propósito | Status | Atualizado |
|---|---|---|---|---|---|
| 1 | `services/saga-orchestrator/src/main/java/org/example/saga/service/SagaStore.java` | Criar | Uma transação por transição | ✅ Concluído | 2026-09-28 |
| 2 | `services/saga-orchestrator/src/main/java/org/example/saga/service/SagaService.java` | Modificar | Sem transação longa; pagamento gravado antes de aprovar; compensação reutilizável | ✅ Concluído | 2026-09-28 |
| 3 | `services/saga-orchestrator/src/main/java/org/example/saga/service/SagaRecovery.java` | Criar | Recuperação agendada por timeout | ✅ Concluído | 2026-09-28 |
| 4 | `services/saga-orchestrator/pom.xml` | Modificar | `quarkus-scheduler` | ✅ Concluído | 2026-09-28 |
| 5 | `services/saga-orchestrator/src/main/resources/application.properties` | Modificar | `saga.recovery.*` | ✅ Concluído | 2026-09-28 |
| 6 | `services/saga-orchestrator/src/test/java/org/example/saga/api/SagaFlowTest.java` | Modificar | Recuperação + falha na aprovação | ✅ Concluído | 2026-09-28 |
| 7 | `horus/src/main/java/org/example/horus/query/QueryModel.java` | Modificar | `SpanRef.error` | ✅ Concluído | 2026-09-28 |
| 8 | `horus/src/main/java/org/example/horus/query/backend/JaegerTraceAdapter.java` | Modificar | Preenche `error` | ✅ Concluído | 2026-09-28 |
| 9 | `horus/src/main/java/org/example/horus/lifecycle/SagaVisualizationModel.java` | Modificar | `failed`, `failedStep`, `recovered` | ✅ Concluído | 2026-09-28 |
| 10 | `horus/src/main/java/org/example/horus/lifecycle/SagaVisualizationService.java` | Modificar | Desfecho por status do span | ✅ Concluído | 2026-09-28 |
| 11 | `horus/src/test/java/org/example/horus/lifecycle/SagaVisualizationResourceTest.java` | Modificar | 3 casos novos | ✅ Concluído | 2026-09-28 |
| 12 | `scripts/e2e.sh` | Modificar | Exige `failedStep` | ✅ Concluído | 2026-09-28 |
| 13 | `docs/telemetry/CONTRACT.md` | Modificar | `horus.saga.recovery`; `horus.saga.id` real | ✅ Concluído | 2026-09-28 |
| 14 | `docs/tasks/T-1010/PRD.md` | Criar | PRD | ✅ Concluído | 2026-09-28 |
| 15 | `docs/tasks/T-1010/PLAN.md` | Criar | Este plano | ✅ Concluído | 2026-09-28 |
| 16 | `state.md` | Modificar | R1 | ✅ Concluído | 2026-09-28 |

**Legenda:** ⬜ Pendente · 🟡 Em progresso · ✅ Concluído

## Verificação / testes

- [x] `./mvnw -pl services/saga-orchestrator test` (JDK 25, Testcontainers) → 5/5.
- [x] `./mvnw -pl horus test` → 126/126.
- [x] e2e ao vivo — rodado no fechamento da fase (ver `state.md`).

## Registro de alterações

| Data | Arquivo | Mudança |
|---|---|---|
| `2026-09-28` | (todos acima) | SAGA durável + recuperação + passo que falhou (T-1010) |
