# Plano de Execução — T-1006: Análises pesadas assíncronas

> ⚠️ **Regra R2 ([WORKFLOW](../../WORKFLOW.md)).** Sempre que **criar ou modificar** um arquivo desta tabela, atualize **na mesma hora** o seu `Status` e `Atualizado`, e registre a mudança em *Registro de alterações*. Arquivo não previsto que precise ser tocado deve ser **adicionado** à tabela.

| Campo | Valor |
|---|---|
| **Task** | `T-1006` |
| **Branch** | `claude/beautiful-cray-sfblsr` |
| **PRD** | [`./PRD.md`](./PRD.md) |
| **Progresso** | `11/11` arquivos |

## Arquivos a criar/modificar

| # | Arquivo | Ação | Propósito | Status | Atualizado |
|---|---|---|---|---|---|
| 1 | `horus/src/main/java/org/example/horus/ai/job/AiJobService.java` | Criar | Pool/fila limitados, ciclo de vida, expiração | ✅ Concluído | 2026-09-28 |
| 2 | `horus/src/main/java/org/example/horus/ai/job/AiJob.java` | Criar | Estado do job + visão serializável | ✅ Concluído | 2026-09-28 |
| 3 | `horus/src/main/java/org/example/horus/api/HorusAiJobResource.java` | Criar | `/horus/ai/jobs` (rca, state-summary, get, list) | ✅ Concluído | 2026-09-28 |
| 4 | `horus/src/main/java/org/example/horus/panel/HorusPanelResource.java` | Modificar | Endpoints no overview | ✅ Concluído | 2026-09-28 |
| 5 | `horus/src/main/resources/META-INF/resources/horus-panel.html` | Modificar | RCA via job com polling | ✅ Concluído | 2026-09-28 |
| 6 | `horus/src/main/resources/application.properties` | Modificar | `horus.ai.jobs.*` | ✅ Concluído | 2026-09-28 |
| 7 | `horus/src/test/java/org/example/horus/ai/job/AiJobServiceTest.java` | Criar | Ciclo de vida, falha, fila cheia, expiração | ✅ Concluído | 2026-09-28 |
| 8 | `horus/src/test/java/org/example/horus/api/HorusAiJobResourceTest.java` | Criar | 202 → SUCCEEDED; 400; 404 | ✅ Concluído | 2026-09-28 |
| 9 | `docs/tasks/T-1006/PRD.md` | Criar | PRD | ✅ Concluído | 2026-09-28 |
| 10 | `docs/tasks/T-1006/PLAN.md` | Criar | Este plano | ✅ Concluído | 2026-09-28 |
| 11 | `state.md` | Modificar | R1 | ✅ Concluído | 2026-09-28 |

**Legenda:** ⬜ Pendente · 🟡 Em progresso · ✅ Concluído

## Verificação / testes

- [x] `./mvnw -pl horus test` (JDK 25) → **110 testes, 0 falhas**.

## Registro de alterações

| Data | Arquivo | Mudança |
|---|---|---|
| `2026-09-28` | (todos acima) | Jobs assíncronos de IA (T-1006) |
