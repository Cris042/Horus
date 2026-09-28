# Plano de Execução — T-1009: Overlay de infraestrutura no Kubernetes + LB

> ⚠️ **Regra R2 ([WORKFLOW](../../WORKFLOW.md)).** Sempre que **criar ou modificar** um arquivo desta tabela, atualize **na mesma hora** o seu `Status` e `Atualizado`, e registre a mudança em *Registro de alterações*. Arquivo não previsto que precise ser tocado deve ser **adicionado** à tabela.

| Campo | Valor |
|---|---|
| **Task** | `T-1009` |
| **Branch** | `claude/beautiful-cray-sfblsr` |
| **PRD** | [`./PRD.md`](./PRD.md) |
| **Progresso** | `17/17` arquivos |

## Arquivos a criar/modificar

| # | Arquivo | Ação | Propósito | Status | Atualizado |
|---|---|---|---|---|---|
| 1 | `deploy/kustomization.yaml` | Criar | Stack completo (infra + apps) + configMapGenerator das configs de telemetria | ✅ Concluído | 2026-09-28 |
| 2 | `deploy/k8s/infra/postgres.yaml` | Criar | Postgres ×4 (StatefulSet + PVC + Service) | ✅ Concluído | 2026-09-28 |
| 3 | `deploy/k8s/infra/rabbitmq.yaml` | Criar | RabbitMQ | ✅ Concluído | 2026-09-28 |
| 4 | `deploy/k8s/infra/telemetry.yaml` | Criar | Collector, Jaeger, Loki, Prometheus | ✅ Concluído | 2026-09-28 |
| 5 | `deploy/k8s/infra/prometheus-k8s.yml` | Criar | Config do Prometheus no cluster | ✅ Concluído | 2026-09-28 |
| 6 | `deploy/k8s/apps/load-balancer.yaml` | Criar | LB NGINX (Deployment + Service) | ✅ Concluído | 2026-09-28 |
| 7 | `deploy/k8s/lb/nginx.conf` | Criar | Variante K8s do nginx.conf | ✅ Concluído | 2026-09-28 |
| 8 | `deploy/k8s/lb/locations.conf` | Criar | Rotas com upstream estático | ✅ Concluído | 2026-09-28 |
| 9 | `deploy/k8s/kustomization.yaml` | Modificar | LB + configMapGenerator | ✅ Concluído | 2026-09-28 |
| 10 | `deploy/k8s/apps/report-worker.yaml` | Modificar | `AMQP_URL` (era `RABBITMQ_ADDR`) + OTLP | ✅ Concluído | 2026-09-28 |
| 11 | `deploy/k8s/apps/loadtest.yaml` | Modificar | Runner locust + OTel | ✅ Concluído | 2026-09-28 |
| 12 | `deploy/docker-compose.yml` | Modificar | Nomes de imagem `horus/*:dev` | ✅ Concluído | 2026-09-28 |
| 13 | `deploy/k8s/README.md` | Modificar | Stack completo + guia kind | ✅ Concluído | 2026-09-28 |
| 14 | `.github/workflows/ci.yml` | Modificar | Job `e2e-k8s` | ✅ Concluído | 2026-09-28 |
| 15 | `docs/tasks/T-1009/PRD.md` | Criar | PRD | ✅ Concluído | 2026-09-28 |
| 16 | `docs/tasks/T-1009/PLAN.md` | Criar | Este plano | ✅ Concluído | 2026-09-28 |
| 17 | `state.md` | Modificar | R1 | ✅ Concluído | 2026-09-28 |

**Legenda:** ⬜ Pendente · 🟡 Em progresso · ✅ Concluído

## Verificação / testes

- [x] `kubectl kustomize deploy/ | kubeconform -strict -kubernetes-version 1.33.0` → 52/52 válidos.
- [x] `nginx -t` da variante K8s.
- [x] `docker compose --profile apps config --images` → `horus/*:dev`.
- [ ] Cluster real — kind não roda neste sandbox (runc aninhado); job `e2e-k8s` do CI.

## Registro de alterações

| Data | Arquivo | Mudança |
|---|---|---|
| `2026-09-28` | (todos acima) | Stack completo no K8s + LB (T-1009) |
