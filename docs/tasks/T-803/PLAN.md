# Plano de Execução — T-803: Escalabilidade horizontal (HPA)

> ⚠️ **Regra R2 ([WORKFLOW](../../WORKFLOW.md)).** Atualize o status ao tocar cada arquivo.

| Campo | Valor |
|---|---|
| **Task** | `T-803` |
| **Branch** | `task/T-803-hpa` |
| **PRD** | [`./PRD.md`](./PRD.md) |
| **Progresso** | `11/11` arquivos |

## Arquivos a criar/modificar

| # | Arquivo | Ação | Status | Atualizado |
|---|---|---|---|---|
| 1 | `docs/tasks/T-803/PRD.md` | Criar | ✅ Concluído | 2026-06-30 |
| 2 | `docs/tasks/T-803/PLAN.md` | Criar | ✅ Concluído | 2026-06-30 |
| 3 | `deploy/k8s/autoscaling/prontuario-service-hpa.yaml` | Criar | ✅ Concluído | 2026-06-30 |
| 4 | `deploy/k8s/autoscaling/payment-service-hpa.yaml` | Criar | ✅ Concluído | 2026-06-30 |
| 5 | `deploy/k8s/autoscaling/invoice-service-hpa.yaml` | Criar | ✅ Concluído | 2026-06-30 |
| 6 | `deploy/k8s/autoscaling/saga-orchestrator-hpa.yaml` | Criar | ✅ Concluído | 2026-06-30 |
| 7 | `deploy/k8s/autoscaling/horus-hpa.yaml` | Criar | ✅ Concluído | 2026-06-30 |
| 8 | `deploy/k8s/autoscaling/report-worker-hpa.yaml` | Criar | ✅ Concluído | 2026-06-30 |
| 9 | `deploy/k8s/kustomization.yaml` | Modificar | ✅ Concluído | 2026-06-30 |
| 10 | `deploy/k8s/apps/*.yaml` (6) | Modificar | ✅ Concluído | 2026-06-30 |
| 11 | `deploy/k8s/README.md` | Modificar | ✅ Concluído | 2026-06-30 |

> `state.md` atualizado ao final.

## Verificação / testes

- [x] Manifestos validados como YAML (`python yaml.safe_load_all`); Deployments íntegros.
- [ ] `kubectl apply -k` / métricas reais — em cluster com metrics-server (T-405).

## Registro de alterações

| Data | Arquivo | Mudança |
|---|---|---|
| `2026-06-30` | `docs/tasks/T-803/*` | PRD e plano criados |
| `2026-06-30` | `deploy/k8s/autoscaling/*` | HPAs por componente (CPU) |
| `2026-06-30` | `deploy/k8s/kustomization.yaml` | Inclui os HPAs |
| `2026-06-30` | `deploy/k8s/apps/*` | Remove `replicas` estático dos Deployments geridos por HPA |
| `2026-06-30` | `deploy/k8s/README.md` | Seção de escala horizontal |
| `2026-06-30` | `state.md` | Estado alinhado ao progresso de T-803 |
