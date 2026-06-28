# Plano de Execução — T-604: Agente Trace Explainer

> ⚠️ **Regra R2 ([WORKFLOW](../../WORKFLOW.md)).** Atualize `Status`/`Atualizado` e o registro ao criar/modificar cada arquivo.

| Campo | Valor |
|---|---|
| **Task** | `T-604` |
| **Branch** | `task/T-604-trace-explainer` |
| **PRD** | [`./PRD.md`](./PRD.md) |
| **Progresso** | `5/5` arquivos |

## Arquivos a criar/modificar

| # | Arquivo | Ação | Propósito | Status | Atualizado |
|---|---|---|---|---|---|
| 1 | `horus/.../ai/agent/TraceExplainer.java` | Criar | agente Explainer (BALANCED tier) | ✅ Concluído | 2026-06-28 |
| 2 | `horus/.../api/HorusExplainResource.java` | Criar | endpoint "explique este trace" | ✅ Concluído | 2026-06-28 |
| 3 | `horus/.../ai/agent/TraceExplainerTest.java` | Criar | teste do agente (engine falso) | ✅ Concluído | 2026-06-28 |
| 4 | `horus/.../api/HorusExplainResourceTest.java` | Criar | teste do endpoint (assembler mockado) | ✅ Concluído | 2026-06-28 |
| 5 | `docs/tasks/T-604/{PRD,PLAN}.md` | Criar | docs da task | ✅ Concluído | 2026-06-28 |

**Legenda:** ⬜ Pendente · 🟡 Em progresso · ✅ Concluído

## Passos de implementação

1. `TraceExplainer`: system prompt de narração + chamada ao `LlmEngine` (BALANCED).
2. Endpoint `GET /horus/ai/explain/trace/{traceId}`.
3. Testes (agente + endpoint).

## Verificação / testes

- [x] `./mvnw -pl horus test` → **BUILD SUCCESS** (15 testes; 2 novos).

## Registro de alterações

| Data | Arquivo | Mudança |
|---|---|---|
| `2026-06-28` | (todos acima) | Agente Trace Explainer (T-604) |
