# Plano de Execução — T-507: Visualização de SAGA

> ⚠️ **Regra R2 ([WORKFLOW](../../WORKFLOW.md)).** Sempre que **criar ou modificar** um arquivo desta tabela, atualize **na mesma hora** o seu `Status` e `Atualizado`, e registre a mudança em *Registro de alterações*. Arquivo não previsto que precise ser tocado deve ser **adicionado** à tabela.

| Campo | Valor |
|---|---|
| **Task** | `T-507` |
| **Branch** | `task/T-507-saga-visualization` |
| **PRD** | [`./PRD.md`](./PRD.md) |
| **Progresso** | `7/7` arquivos |

## Arquivos a criar/modificar

| # | Arquivo | Ação | Propósito | Status | Atualizado |
|---|---|---|---|---|---|
| 1 | `docs/tasks/T-507/PRD.md` | Criar | PRD da task | ✅ Concluído | 2026-06-30 |
| 2 | `docs/tasks/T-507/PLAN.md` | Criar | Plano arquivo por arquivo | ✅ Concluído | 2026-06-30 |
| 3 | `horus/src/main/java/org/example/horus/lifecycle/SagaVisualizationModel.java` | Criar | DTOs REST da SAGA | ✅ Concluído | 2026-06-30 |
| 4 | `horus/src/main/java/org/example/horus/lifecycle/SagaVisualizationService.java` | Criar | Projetar trace na linha do tempo da SAGA | ✅ Concluído | 2026-06-30 |
| 5 | `horus/src/main/java/org/example/horus/api/HorusSagaResource.java` | Criar | `GET /horus/lifecycle/saga/{traceId}` | ✅ Concluído | 2026-06-30 |
| 6 | `horus/src/test/java/org/example/horus/lifecycle/SagaVisualizationResourceTest.java` | Criar | Testes REST com `TraceQueryPort` mockado | ✅ Concluído | 2026-06-30 |
| 7 | `state.md` | Modificar | Alinhar estado ao progresso de T-507 | ✅ Concluído | 2026-06-30 |

**Legenda:** ⬜ Pendente · 🟡 Em progresso · ✅ Concluído

## Passos de implementação

1. Escrever testes da API de visualização de SAGA.
2. Implementar modelo/serviço/resource da T-507.
3. Atualizar este plano e, ao final da entrega, o `state.md`.
4. Rodar `./mvnw -pl horus test`.

## Verificação / testes

- [x] `./mvnw -pl horus test` verde.

## Registro de alterações

| Data | Arquivo | Mudança |
|---|---|---|
| `2026-06-30` | `docs/tasks/T-507/PRD.md`, `docs/tasks/T-507/PLAN.md` | PRD e plano de execução criados |
| `2026-06-30` | `SagaVisualizationModel.java`, `SagaVisualizationService.java`, `api/HorusSagaResource.java` | Implementação da visualização de SAGA |
| `2026-06-30` | `SagaVisualizationResourceTest.java` | Testes do contrato REST da SAGA |
| `2026-06-30` | `state.md` | Estado do projeto alinhado ao progresso de T-507 |
