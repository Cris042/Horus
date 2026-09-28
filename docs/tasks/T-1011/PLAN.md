# Plano de Execução — T-1011: UI de service-map e SAGA

> ⚠️ **Regra R2 ([WORKFLOW](../../WORKFLOW.md)).** Sempre que **criar ou modificar** um arquivo desta tabela, atualize **na mesma hora** o seu `Status` e `Atualizado`, e registre a mudança em *Registro de alterações*. Arquivo não previsto que precise ser tocado deve ser **adicionado** à tabela.

| Campo | Valor |
|---|---|
| **Task** | `T-1011` |
| **Branch** | `claude/beautiful-cray-sfblsr` |
| **PRD** | [`./PRD.md`](./PRD.md) |
| **Progresso** | `8/8` arquivos |

## Arquivos a criar/modificar

| # | Arquivo | Ação | Propósito | Status | Atualizado |
|---|---|---|---|---|---|
| 1 | `horus/src/main/resources/META-INF/resources/horus-waterfall.html` | Modificar | Cards de mapa de serviços (SVG) e SAGA | ✅ Concluído | 2026-09-28 |
| 2 | `horus/src/main/resources/META-INF/resources/horus-panel.html` | Modificar | Filtro ocultar ruído | ✅ Concluído | 2026-09-28 |
| 3 | `horus/src/main/java/org/example/horus/api/HorusTraceSearchResource.java` | Modificar | `minSpans` | ✅ Concluído | 2026-09-28 |
| 4 | `horus/src/test/java/org/example/horus/api/HorusTraceSearchResourceTest.java` | Modificar | Teste de `minSpans` | ✅ Concluído | 2026-09-28 |
| 5 | `docs/tasks/T-1011/waterfall-saga-compensada.png` | Criar | Evidência com dados reais | ✅ Concluído | 2026-09-28 |
| 6 | `docs/tasks/T-1011/PRD.md` | Criar | PRD | ✅ Concluído | 2026-09-28 |
| 7 | `docs/tasks/T-1011/PLAN.md` | Criar | Este plano | ✅ Concluído | 2026-09-28 |
| 8 | `state.md` | Modificar | R1 | ✅ Concluído | 2026-09-28 |

**Legenda:** ⬜ Pendente · 🟡 Em progresso · ✅ Concluído

## Verificação / testes

- [x] Stack real + e2e verde; screenshot (Playwright/Chromium) do waterfall de uma SAGA compensada.
- [x] `./mvnw -pl horus test` → 127/127.

## Registro de alterações

| Data | Arquivo | Mudança |
|---|---|---|
| `2026-09-28` | (todos acima) | UI de mapa de serviços e SAGA (T-1011) |
