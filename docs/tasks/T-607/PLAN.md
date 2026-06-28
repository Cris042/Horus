# Plano de Execução — T-607: Agente NL Query ("pergunte ao Horus")

> ⚠️ **Regra R2 ([WORKFLOW](../../WORKFLOW.md)).** Atualize `Status`/`Atualizado` e o registro ao criar/modificar cada arquivo.

| Campo | Valor |
|---|---|
| **Task** | `T-607` |
| **Branch** | `task/T-607-nl-query-agent` |
| **PRD** | [`./PRD.md`](./PRD.md) |
| **Progresso** | `5/5` arquivos |

## Arquivos a criar/modificar

| # | Arquivo | Ação | Propósito | Status | Atualizado |
|---|---|---|---|---|---|
| 1 | `horus/.../ai/agent/NlQueryAgent.java` | Criar | agente NL Query (BALANCED) | ✅ Concluído | 2026-06-28 |
| 2 | `horus/.../api/HorusAskResource.java` | Criar | endpoint `POST /horus/ai/ask` | ✅ Concluído | 2026-06-28 |
| 3 | `horus/.../ai/agent/NlQueryAgentTest.java` | Criar | teste do agente (engine falso) | ✅ Concluído | 2026-06-28 |
| 4 | `horus/.../api/HorusAskResourceTest.java` | Criar | teste do endpoint (assembler mockado) | ✅ Concluído | 2026-06-28 |
| 5 | `docs/tasks/T-607/{PRD,PLAN}.md` | Criar | docs da task | ✅ Concluído | 2026-06-28 |

**Legenda:** ⬜ Pendente · 🟡 Em progresso · ✅ Concluído

## Passos de implementação

1. `NlQueryAgent`: system prompt grounded + chamada ao `LlmEngine` (BALANCED).
2. Endpoint `POST /horus/ai/ask` com recorte de escopo e validação.
3. Testes (agente + endpoint, incl. 400 sem pergunta).

## Verificação / testes

- [x] `./mvnw -pl horus test` → **BUILD SUCCESS** (20 testes; 3 novos).

## Registro de alterações

| Data | Arquivo | Mudança |
|---|---|---|
| `2026-06-28` | (todos acima) | Agente NL Query (T-607) — fatia grounded |
