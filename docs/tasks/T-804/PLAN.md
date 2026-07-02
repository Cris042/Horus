# Plano de Execução — T-804: worker e Horus separados do domínio

> ⚠️ **Regra R2 ([WORKFLOW](../../WORKFLOW.md)).** Atualize o status ao tocar cada arquivo.

| Campo | Valor |
|---|---|
| **Task** | `T-804` |
| **Branch** | `task/T-804-separate-platform` |
| **PRD** | [`./PRD.md`](./PRD.md) |
| **Progresso** | `9/9` arquivos |

## Arquivos a criar/modificar

| # | Arquivo | Ação | Status | Atualizado |
|---|---|---|---|---|
| 1 | `docs/tasks/T-804/PRD.md` | Criar | ✅ Concluído | 2026-06-30 |
| 2 | `docs/tasks/T-804/PLAN.md` | Criar | ✅ Concluído | 2026-06-30 |
| 3 | `deploy/k8s/apps/horus.yaml` | Modificar (nodeAffinity platform) | ✅ Concluído | 2026-06-30 |
| 4 | `deploy/k8s/apps/report-worker.yaml` | Modificar (nodeAffinity platform) | ✅ Concluído | 2026-06-30 |
| 5 | `deploy/k8s/apps/prontuario-service.yaml` | Modificar (nodeAffinity domain) | ✅ Concluído | 2026-06-30 |
| 6 | `deploy/k8s/apps/payment-service.yaml` | Modificar (nodeAffinity domain) | ✅ Concluído | 2026-06-30 |
| 7 | `deploy/k8s/apps/invoice-service.yaml` | Modificar (nodeAffinity domain) | ✅ Concluído | 2026-06-30 |
| 8 | `deploy/k8s/apps/saga-orchestrator.yaml` | Modificar (nodeAffinity domain) | ✅ Concluído | 2026-06-30 |
| 9 | `deploy/k8s/README.md` | Modificar (separação/rotulagem) | ✅ Concluído | 2026-06-30 |

> `state.md` atualizado ao final.

## Verificação / testes

- [x] Manifestos validados como YAML; affinity por tier conferida via parser.
- [ ] Agendamento real por pool — em cluster com nós rotulados (T-405).

## Registro de alterações

| Data | Arquivo | Mudança |
|---|---|---|
| `2026-06-30` | `docs/tasks/T-804/*` | PRD e plano criados |
| `2026-06-30` | `deploy/k8s/apps/*` | nodeAffinity soft por tier (platform/domain) |
| `2026-06-30` | `deploy/k8s/README.md` | Documenta separação (ADR-0012) e rotulagem de nós |
| `2026-06-30` | `state.md` | Estado alinhado ao progresso de T-804 |
