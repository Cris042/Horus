# Plano de Execução — T-802: Manifests/Helm Kubernetes

> ⚠️ **Regra R2 ([WORKFLOW](../../WORKFLOW.md)).** Sempre que **criar ou modificar** um arquivo desta tabela, atualize **na mesma hora** o seu `Status` e `Atualizado`, e registre a mudança em *Registro de alterações*.

| Campo | Valor |
|---|---|
| **Task** | `T-802` |
| **Branch** | `task/T-802-k8s-manifests` |
| **PRD** | [`./PRD.md`](./PRD.md) |
| **Progresso** | `14/14` arquivos |

## Arquivos a criar/modificar

| # | Arquivo | Ação | Status | Atualizado |
|---|---|---|---|---|
| 1 | `docs/tasks/T-802/PRD.md` | Criar | ✅ Concluído | 2026-06-30 |
| 2 | `docs/tasks/T-802/PLAN.md` | Criar | ✅ Concluído | 2026-06-30 |
| 3 | `deploy/k8s/namespace.yaml` | Criar | ✅ Concluído | 2026-06-30 |
| 4 | `deploy/k8s/config.yaml` | Criar | ✅ Concluído | 2026-06-30 |
| 5 | `deploy/k8s/secrets.example.yaml` | Criar | ✅ Concluído | 2026-06-30 |
| 6 | `deploy/k8s/apps/prontuario-service.yaml` | Criar | ✅ Concluído | 2026-06-30 |
| 7 | `deploy/k8s/apps/payment-service.yaml` | Criar | ✅ Concluído | 2026-06-30 |
| 8 | `deploy/k8s/apps/invoice-service.yaml` | Criar | ✅ Concluído | 2026-06-30 |
| 9 | `deploy/k8s/apps/saga-orchestrator.yaml` | Criar | ✅ Concluído | 2026-06-30 |
| 10 | `deploy/k8s/apps/horus.yaml` | Criar | ✅ Concluído | 2026-06-30 |
| 11 | `deploy/k8s/apps/report-worker.yaml` | Criar | ✅ Concluído | 2026-06-30 |
| 12 | `deploy/k8s/apps/loadtest.yaml` | Criar | ✅ Concluído | 2026-06-30 |
| 13 | `deploy/k8s/kustomization.yaml` | Criar | ✅ Concluído | 2026-06-30 |
| 14 | `deploy/k8s/README.md` + `.gitignore` | Criar | ✅ Concluído | 2026-06-30 |

> `state.md` atualizado ao final da entrega.

## Verificação / testes

- [x] Todos os manifestos validados como YAML (`python yaml.safe_load_all`).
- [ ] `kubectl apply -k` / `kubeconform` — não há cluster/ferramenta no ambiente; roda na validação ponta a ponta (T-405) ou em cluster real.

## Registro de alterações

| Data | Arquivo | Mudança |
|---|---|---|
| `2026-06-30` | `docs/tasks/T-802/*` | PRD e plano criados |
| `2026-06-30` | `deploy/k8s/**` | Base Kustomize: namespace, config, secret-exemplo, 7 workloads, README, gitignore |
| `2026-06-30` | `state.md` | Estado alinhado ao progresso de T-802 |
