# Plano de Execução — T-1004: Auditoria de prompts + sanitização obrigatória

> ⚠️ **Regra R2 ([WORKFLOW](../../WORKFLOW.md)).** Sempre que **criar ou modificar** um arquivo desta tabela, atualize **na mesma hora** o seu `Status` e `Atualizado`, e registre a mudança em *Registro de alterações*. Arquivo não previsto que precise ser tocado deve ser **adicionado** à tabela.

| Campo | Valor |
|---|---|
| **Task** | `T-1004` |
| **Branch** | `claude/beautiful-cray-sfblsr` |
| **PRD** | [`./PRD.md`](./PRD.md) |
| **Progresso** | `14/14` arquivos |

## Arquivos a criar/modificar

| # | Arquivo | Ação | Propósito | Status | Atualizado |
|---|---|---|---|---|---|
| 1 | `horus/src/main/java/org/example/horus/ai/AuditingLlmEngine.java` | Criar | Decorator obrigatório: sanitiza + audita | ✅ Concluído | 2026-09-28 |
| 2 | `horus/src/main/java/org/example/horus/ai/LlmAuditTrail.java` | Criar | Trilha em memória + totais | ✅ Concluído | 2026-09-28 |
| 3 | `horus/src/main/java/org/example/horus/api/HorusAuditResource.java` | Criar | `GET /horus/ai/audit` | ✅ Concluído | 2026-09-28 |
| 4 | `horus/src/main/java/org/example/horus/ai/LlmEngine.java` | Modificar | `LlmRequest.purpose`; Javadoc da fronteira | ✅ Concluído | 2026-09-28 |
| 5 | `horus/src/main/java/org/example/horus/ai/context/PromptSanitizer.java` | Modificar | `sanitizeCounting` | ✅ Concluído | 2026-09-28 |
| 6 | `horus/src/main/java/org/example/horus/ai/agent/*.java + anomaly/ErrorClusterer + alert/AlertService + api/HorusAiResource` | Modificar | `purpose` em cada chamada | ✅ Concluído | 2026-09-28 |
| 7 | `horus/src/main/java/org/example/horus/panel/HorusPanelResource.java` | Modificar | Endpoint no overview | ✅ Concluído | 2026-09-28 |
| 8 | `horus/src/main/resources/application.properties` | Modificar | `horus.ai.audit.max-entries` | ✅ Concluído | 2026-09-28 |
| 9 | `horus/src/test/java/org/example/horus/ai/AuditingLlmEngineTest.java` | Criar | Fronteira + trilha (unitário) | ✅ Concluído | 2026-09-28 |
| 10 | `horus/src/test/java/org/example/horus/api/HorusAuditResourceTest.java` | Criar | Wiring real do CDI | ✅ Concluído | 2026-09-28 |
| 11 | `docs/tasks/T-904/PRD.md` | Modificar | Risco fechado | ✅ Concluído | 2026-09-28 |
| 12 | `docs/tasks/T-1004/PRD.md` | Criar | PRD | ✅ Concluído | 2026-09-28 |
| 13 | `docs/tasks/T-1004/PLAN.md` | Criar | Este plano | ✅ Concluído | 2026-09-28 |
| 14 | `state.md` | Modificar | R1 | ✅ Concluído | 2026-09-28 |

**Legenda:** ⬜ Pendente · 🟡 Em progresso · ✅ Concluído

## Passos de implementação

1. `purpose` no `LlmRequest` (construtor de 3 argumentos mantido) e em todos os agentes.
2. `PromptSanitizer.sanitizeCounting`; `LlmAuditTrail`; decorator `AuditingLlmEngine` externo ao cache.
3. API `/horus/ai/audit`; testes unitário + `@QuarkusTest`.

## Verificação / testes

- [x] `./mvnw -pl horus test` (JDK 25) → **103 testes, 0 falhas**.

## Registro de alterações

| Data | Arquivo | Mudança |
|---|---|---|
| `2026-09-28` | (todos acima) | Fronteira obrigatória + trilha do LLM (T-1004) |
