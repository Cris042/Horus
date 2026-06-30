# Plano de Execução — T-504: API do ciclo de vida da query

> ⚠️ **Regra R2 ([WORKFLOW](../../WORKFLOW.md)).** Sempre que **criar ou modificar** um arquivo desta tabela, atualize **na mesma hora** o seu `Status` e `Atualizado`, e registre a mudança em *Registro de alterações*. Arquivo não previsto que precise ser tocado deve ser **adicionado** à tabela.

| Campo | Valor |
|---|---|
| **Task** | `T-504` |
| **Branch** | `task/T-504-query-lifecycle-api` |
| **PRD** | [`./PRD.md`](./PRD.md) |
| **Progresso** | `9/9` arquivos |

## Arquivos a criar/modificar

| # | Arquivo | Ação | Propósito | Status | Atualizado |
|---|---|---|---|---|---|
| 1 | `docs/tasks/T-504/PRD.md` | Criar | PRD da task | ✅ Concluído | 2026-06-28 |
| 2 | `docs/tasks/T-504/PLAN.md` | Criar | Plano arquivo por arquivo | ✅ Concluído | 2026-06-28 |
| 3 | `horus/src/main/java/org/example/horus/query/QueryModel.java` | Modificar | Enriquecer `SpanRef` com metadados `db.*` | ✅ Concluído | 2026-06-28 |
| 4 | `horus/src/main/java/org/example/horus/query/backend/JaegerTraceAdapter.java` | Modificar | Extrair atributos de query do Jaeger | ✅ Concluído | 2026-06-28 |
| 5 | `horus/src/main/java/org/example/horus/lifecycle/QueryLifecycleModel.java` | Criar | Modelo REST das queries correlacionadas | ✅ Concluído | 2026-06-28 |
| 6 | `horus/src/main/java/org/example/horus/lifecycle/QueryLifecycleService.java` | Criar | Filtrar/projetar spans SQL do trace | ✅ Concluído | 2026-06-28 |
| 7 | `horus/src/main/java/org/example/horus/api/HorusQueryLifecycleResource.java` | Criar | `GET /horus/lifecycle/queries/{traceId}` | ✅ Concluído | 2026-06-28 |
| 8 | `horus/src/test/java/org/example/horus/lifecycle/QueryLifecycleResourceTest.java` | Criar | Testes REST com `TraceQueryPort` mockado | ✅ Concluído | 2026-06-28 |
| 9 | `state.md` | Modificar | Alinhar estado ao merge de T-503 e ao início de T-504 | ✅ Concluído | 2026-06-28 |

**Legenda:** ⬜ Pendente · 🟡 Em progresso · ✅ Concluído

## Passos de implementação

1. Escrever testes da API de query lifecycle.
2. Estender `SpanRef` e o adapter Jaeger com atributos `db.*`.
3. Implementar modelo/serviço/resource da T-504.
4. Atualizar este plano e, ao final da entrega, o `state.md`.
5. Rodar `./mvnw -pl horus test`.

## Verificação / testes

- [x] `./mvnw -pl horus test` verde (42/42).

## Registro de alterações

| Data | Arquivo | Mudança |
|---|---|---|
| `2026-06-28` | `docs/tasks/T-504/PRD.md`, `docs/tasks/T-504/PLAN.md` | PRD e plano de execução criados |
| `2026-06-28` | `horus/src/test/java/org/example/horus/lifecycle/QueryLifecycleResourceTest.java` | Teste do contrato REST de query lifecycle criado |
| `2026-06-28` | `QueryModel.java`, `JaegerTraceAdapter.java`, `QueryLifecycle*`, `HorusQueryLifecycleResource.java` | Implementacao inicial da API de query lifecycle |
| `2026-06-28` | `state.md` | Estado do projeto alinhado ao merge de T-503 e ao progresso de T-504 |
