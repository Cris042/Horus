# Plano de Execução — T-602: Montador de contexto da IA

> ⚠️ **Regra R2 ([WORKFLOW](../../WORKFLOW.md)).** Atualize `Status`/`Atualizado` e o registro ao criar/modificar cada arquivo.

| Campo | Valor |
|---|---|
| **Task** | `T-602` |
| **Branch** | `task/T-602-context-assembler` |
| **PRD** | [`./PRD.md`](./PRD.md) |
| **Progresso** | `7/7` arquivos |

## Arquivos a criar/modificar

| # | Arquivo | Ação | Propósito | Status | Atualizado |
|---|---|---|---|---|---|
| 1 | `horus/.../ai/context/TokenBudget.java` | Criar | estimativa + orçamento de tokens | ✅ Concluído | 2026-06-28 |
| 2 | `horus/.../ai/context/PromptSanitizer.java` | Criar | guarda final de PII | ✅ Concluído | 2026-06-28 |
| 3 | `horus/.../ai/context/PromptContext.java` | Criar | resultado (texto + metadados) | ✅ Concluído | 2026-06-28 |
| 4 | `horus/.../ai/context/ContextAssembler.java` | Criar | montagem trace/logs/métricas com budget | ✅ Concluído | 2026-06-28 |
| 5 | `horus/src/main/resources/application.properties` | Modificar | `horus.ai.context.max-tokens` | ✅ Concluído | 2026-06-28 |
| 6 | `horus/.../ai/context/ContextAssemblerTest.java` | Criar | testes (budget/truncamento/PII) | ✅ Concluído | 2026-06-28 |
| 7 | `docs/tasks/T-602/{PRD,PLAN}.md` | Criar | docs da task | ✅ Concluído | 2026-06-28 |

**Legenda:** ⬜ Pendente · 🟡 Em progresso · ✅ Concluído

## Passos de implementação

1. `TokenBudget` (estimativa ~4 chars/token, `fits`/`remaining`).
2. `PromptSanitizer` (regex e-mail/CPF/cartão).
3. `ContextAssembler` + `PromptContext`: prioridade trace→logs→métricas, truncamento por linha.
4. Config do orçamento.
5. Testes unitários do overload `assemble(...)` (sem backends).

## Verificação / testes

- [x] `./mvnw -pl horus test` → **BUILD SUCCESS** (11 testes; 3 novos).

## Registro de alterações

| Data | Arquivo | Mudança |
|---|---|---|
| `2026-06-28` | (todos acima) | Montador de contexto da IA (T-602) |
