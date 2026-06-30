# Plano de Execução — T-505: Agregação de logs de erro correlacionados

> ⚠️ **Regra R2 ([WORKFLOW](../../WORKFLOW.md)).** Sempre que **criar ou modificar** um arquivo desta tabela, atualize **na mesma hora** o seu `Status` e `Atualizado`, e registre a mudança em *Registro de alterações*. Arquivo não previsto que precise ser tocado deve ser **adicionado** à tabela.

| Campo | Valor |
|---|---|
| **Task** | `T-505` |
| **Branch** | `task/T-505-error-log-aggregation` |
| **PRD** | [`./PRD.md`](./PRD.md) |
| **Progresso** | `7/7` arquivos |

## Arquivos a criar/modificar

| # | Arquivo | Ação | Propósito | Status | Atualizado |
|---|---|---|---|---|---|
| 1 | `docs/tasks/T-505/PRD.md` | Criar | PRD da task | ✅ Concluído | 2026-06-28 |
| 2 | `docs/tasks/T-505/PLAN.md` | Criar | Plano arquivo por arquivo | ✅ Concluído | 2026-06-28 |
| 3 | `horus/src/main/java/org/example/horus/lifecycle/ErrorLogAggregationModel.java` | Criar | Modelo REST dos grupos de erro | ✅ Concluído | 2026-06-28 |
| 4 | `horus/src/main/java/org/example/horus/lifecycle/ErrorLogAggregationService.java` | Criar | Agregar logs de erro correlacionados | ✅ Concluído | 2026-06-28 |
| 5 | `horus/src/main/java/org/example/horus/api/HorusErrorLogResource.java` | Criar | `GET /horus/lifecycle/errors/{traceId}` | ✅ Concluído | 2026-06-28 |
| 6 | `horus/src/test/java/org/example/horus/lifecycle/ErrorLogAggregationResourceTest.java` | Criar | Testes REST com `TraceQueryPort`/`LogQueryPort` mockados | ✅ Concluído | 2026-06-28 |
| 7 | `state.md` | Modificar | Alinhar estado ao progresso de T-505 | ✅ Concluído | 2026-06-28 |

**Legenda:** ⬜ Pendente · 🟡 Em progresso · ✅ Concluído

## Passos de implementação

1. Escrever testes da API de agregação de erros.
2. Implementar modelo/serviço/resource da T-505.
3. Atualizar este plano e, ao final da entrega, o `state.md`.
4. Rodar `./mvnw -pl horus test`.

## Verificação / testes

- [x] `./mvnw -pl horus test` verde (41/41).

## Registro de alterações

| Data | Arquivo | Mudança |
|---|---|---|
| `2026-06-28` | `docs/tasks/T-505/PRD.md`, `docs/tasks/T-505/PLAN.md` | PRD e plano de execução criados |
| `2026-06-28` | `horus/src/test/java/org/example/horus/lifecycle/ErrorLogAggregationResourceTest.java` | Teste do contrato REST de agregação de erros criado |
| `2026-06-28` | `ErrorLogAggregation*`, `api/HorusErrorLogResource.java` | Implementacao inicial da agregação de logs de erro |
| `2026-06-28` | `state.md` | Estado do projeto alinhado ao progresso de T-505 |
