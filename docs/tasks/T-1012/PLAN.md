# Plano de Execução — T-1012: Endurecimentos menores

> ⚠️ **Regra R2 ([WORKFLOW](../../WORKFLOW.md)).** Sempre que **criar ou modificar** um arquivo desta tabela, atualize **na mesma hora** o seu `Status` e `Atualizado`, e registre a mudança em *Registro de alterações*. Arquivo não previsto que precise ser tocado deve ser **adicionado** à tabela.

| Campo | Valor |
|---|---|
| **Task** | `T-1012` |
| **Branch** | `claude/beautiful-cray-sfblsr` |
| **PRD** | [`./PRD.md`](./PRD.md) |
| **Progresso** | `13/13` arquivos |

## Arquivos a criar/modificar

| # | Arquivo | Ação | Propósito | Status | Atualizado |
|---|---|---|---|---|---|
| 1 | `horus/src/main/java/org/example/horus/ai/LlmResponseCache.java` | Modificar | TTL | ✅ Concluído | 2026-09-28 |
| 2 | `horus/src/test/java/org/example/horus/ai/LlmResponseCacheTest.java` | Modificar | Teste de expiração | ✅ Concluído | 2026-09-28 |
| 3 | `horus/src/main/resources/application.properties` | Modificar | `horus.ai.cache.ttl` | ✅ Concluído | 2026-09-28 |
| 4 | `deploy/k8s/optional/keda/report-worker-scaledobject.yaml` | Criar | KEDA por fila | ✅ Concluído | 2026-09-28 |
| 5 | `deploy/k8s/README.md` | Modificar | KEDA + TLS interno | ✅ Concluído | 2026-09-28 |
| 6 | `docs/PRD.md` | Modificar | SAGA sem LRA | ✅ Concluído | 2026-09-28 |
| 7 | `docs/ROADMAP.md` | Modificar | T-107 sem LRA; progresso | ✅ Concluído | 2026-09-28 |
| 8 | `docs/adr/ADR-0013-padrao-saga.md` | Modificar | Adendo | ✅ Concluído | 2026-09-28 |
| 9 | `lib.md` | Modificar | Remove LRA/coordenador | ✅ Concluído | 2026-09-28 |
| 10 | `CLAUDE.md` | Modificar | Status do código atualizado | ✅ Concluído | 2026-09-28 |
| 11 | `docs/tasks/T-1012/PRD.md` | Criar | PRD | ✅ Concluído | 2026-09-28 |
| 12 | `docs/tasks/T-1012/PLAN.md` | Criar | Este plano | ✅ Concluído | 2026-09-28 |
| 13 | `state.md` | Modificar | R1 + fechamento da Fase 10 | ✅ Concluído | 2026-09-28 |

**Legenda:** ⬜ Pendente · 🟡 Em progresso · ✅ Concluído

## Verificação / testes

- [x] `./mvnw -pl horus test` (JDK 25) → 128/128.
- [x] `kubeconform` do ScaledObject.

## Registro de alterações

| Data | Arquivo | Mudança |
|---|---|---|
| `2026-09-28` | (todos acima) | Endurecimentos menores (T-1012) |
