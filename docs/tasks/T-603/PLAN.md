# Plano de Execução — T-603: Agente Summarizer

> ⚠️ **Regra R2 ([WORKFLOW](../../WORKFLOW.md)).** Atualize `Status`/`Atualizado` e o registro ao criar/modificar cada arquivo.

| Campo | Valor |
|---|---|
| **Task** | `T-603` |
| **Branch** | `task/T-603-summarizer-agent` |
| **PRD** | [`./PRD.md`](./PRD.md) |
| **Progresso** | `8/8` arquivos |

## Arquivos a criar/modificar

| # | Arquivo | Ação | Propósito | Status | Atualizado |
|---|---|---|---|---|---|
| 1 | `horus/pom.xml` | Modificar | + `quarkus-scheduler` | ✅ Concluído | 2026-06-28 |
| 2 | `horus/.../ai/agent/StateSummarizer.java` | Criar | agente Summarizer (FAST tier) | ✅ Concluído | 2026-06-28 |
| 3 | `horus/.../api/HorusSummaryResource.java` | Criar | endpoint sob demanda | ✅ Concluído | 2026-06-28 |
| 4 | `horus/.../ai/agent/ScheduledStateSummary.java` | Criar | resumo agendado (cron off default) | ✅ Concluído | 2026-06-28 |
| 5 | `horus/src/main/resources/application.properties` | Modificar | cron + promql do resumo | ✅ Concluído | 2026-06-28 |
| 6 | `horus/.../ai/agent/StateSummarizerTest.java` | Criar | teste do agente (engine falso) | ✅ Concluído | 2026-06-28 |
| 7 | `horus/.../api/HorusSummaryResourceTest.java` | Criar | teste do endpoint (assembler mockado) | ✅ Concluído | 2026-06-28 |
| 8 | `docs/tasks/T-603/{PRD,PLAN}.md` | Criar | docs da task | ✅ Concluído | 2026-06-28 |

**Legenda:** ⬜ Pendente · 🟡 Em progresso · ✅ Concluído

## Passos de implementação

1. Adicionar `quarkus-scheduler`.
2. `StateSummarizer`: system prompt + chamada ao `LlmEngine` (FAST).
3. Endpoint sob demanda `GET /horus/ai/summary/trace/{traceId}`.
4. Job agendado `@Scheduled(cron={...:off})` best-effort.
5. Config + testes.

## Verificação / testes

- [x] `./mvnw -pl horus test` → **BUILD SUCCESS** (13 testes; 2 novos).

## Registro de alterações

| Data | Arquivo | Mudança |
|---|---|---|
| `2026-06-28` | (todos acima) | Agente Summarizer (T-603) — sob demanda + agendado |
