# Plano de Execução — T-503: API do ciclo de vida da request

> ⚠️ **Regra R2 ([WORKFLOW](../../WORKFLOW.md)).** Sempre que **criar ou modificar** um arquivo desta tabela, atualize **na mesma hora** o seu `Status` e `Atualizado`, e registre a mudança em *Registro de alterações*. Arquivo não previsto que precise ser tocado deve ser **adicionado** à tabela.

| Campo | Valor |
|---|---|
| **Task** | `T-503` |
| **Branch** | `task/T-503-request-lifecycle-api` |
| **PRD** | [`./PRD.md`](./PRD.md) |
| **Progresso** | `9/9` arquivos |

## Arquivos a criar/modificar

| # | Arquivo | Ação | Propósito | Status | Atualizado |
|---|---|---|---|---|---|
| 1 | `docs/tasks/T-503/PRD.md` | Criar | PRD da task | ✅ Concluído | 2026-06-28 |
| 2 | `docs/tasks/T-503/PLAN.md` | Criar | Plano arquivo por arquivo | ✅ Concluído | 2026-06-28 |
| 3 | `horus/src/main/java/org/example/horus/query/QueryModel.java` | Modificar | Enriquecer `SpanRef` com timestamp/parent/kind | ✅ Concluído | 2026-06-28 |
| 4 | `horus/src/main/java/org/example/horus/query/backend/JaegerTraceAdapter.java` | Modificar | Extrair campos de waterfall do Jaeger | ✅ Concluído | 2026-06-28 |
| 5 | `horus/src/main/java/org/example/horus/lifecycle/RequestLifecycleModel.java` | Criar | Modelo da timeline/waterfall | ✅ Concluído | 2026-06-28 |
| 6 | `horus/src/main/java/org/example/horus/lifecycle/RequestLifecycleService.java` | Criar | Transformar trace em lifecycle | ✅ Concluído | 2026-06-28 |
| 7 | `horus/src/main/java/org/example/horus/api/HorusRequestLifecycleResource.java` | Criar | `GET /horus/lifecycle/requests/{traceId}` | ✅ Concluído | 2026-06-28 |
| 8 | `horus/src/test/java/org/example/horus/lifecycle/RequestLifecycleResourceTest.java` | Criar | Testes REST com trace mockado | ✅ Concluído | 2026-06-28 |
| 9 | `STATE.md` | Modificar | Registrar T-502 entregue e T-503 ativa | ✅ Concluído | 2026-06-28 |

**Legenda:** ⬜ Pendente · 🟡 Em progresso · ✅ Concluído

## Passos de implementação

1. Escrever testes da API lifecycle (sucesso, trace inexistente e `traceId` inválido).
2. Estender `SpanRef` sem quebrar chamadas existentes.
3. Implementar modelo/serviço de waterfall com ordenação, offsets, profundidade e categorias.
4. Criar resource REST e ajustar adapter Jaeger.
5. Atualizar `STATE.md` e este plano.
6. Rodar `./mvnw -pl horus test`.

## Verificação / testes

- [x] `./mvnw -pl horus test` verde (38/38).

## Registro de alterações

| Data | Arquivo | Mudança |
|---|---|---|
| `2026-06-28` | `docs/tasks/T-503/PRD.md`, `docs/tasks/T-503/PLAN.md` | PRD e plano de execução criados |
| `2026-06-28` | `horus/src/test/java/org/example/horus/lifecycle/RequestLifecycleResourceTest.java` | Teste do contrato REST de lifecycle criado |
| `2026-06-28` | `QueryModel.java`, `JaegerTraceAdapter.java`, `lifecycle/*`, `api/HorusRequestLifecycleResource.java` | Implementacao inicial da API waterfall de request |
| `2026-06-28` | `STATE.md` | Estado do projeto alinhado ao merge de T-502 e ao progresso de T-503 |
