# PRD — T-1009: Overlay de infraestrutura no Kubernetes + LB

| Campo | Valor |
|---|---|
| **Task** | `T-1009` |
| **Fase do roadmap** | `Fase 10 — Produto utilizável` |
| **Status** | `Entregue` (validação em cluster real pendente — ver Verificação) |
| **Branch** | `claude/beautiful-cray-sfblsr` |
| **PR** | — |
| **Depende de** | `T-802`, `T-1002`, `T-1005` |
| **Requisitos atendidos** | `RF-033`, `RF-005`, `RNF-005`, PRD §10 critério 7 |
| **ADRs relacionados** | `ADR-0002`, `ADR-0010`, `ADR-0012` |
| **Data** | `2026-09-28` |

## Objetivo

No T-905 os pods ficaram `Pending`/falhando porque a infraestrutura nunca existiu no cluster
(`UnknownHostException: postgres-prontuario`), e o LB nunca teve Deployment em K8s. Esta task
entrega o **stack completo** aplicável com um comando.

## Escopo (o que entra)

- `deploy/kustomization.yaml` (stack completo): base de apps (`deploy/k8s`) + infra em
  `deploy/k8s/infra/`: Postgres ×4 (StatefulSets com PVC, credenciais do `horus-db-secret`),
  RabbitMQ (StatefulSet), OTel Collector, Jaeger (Badger + PVC, `fsGroup`), Loki (PVC), Prometheus
  (PVC, config K8s própria). Collector/Jaeger/Loki usam **as mesmas configs do compose** via
  `configMapGenerator` (por isso a kustomization fica na raiz de `deploy/`). Retenções da T-1005.
- **LB NGINX** (`apps/load-balancer.yaml`, 2 réplicas): o `http://load-balancer` que a API de carga
  usa — variante de config K8s (`k8s/lb/`) com upstreams nos Services (ClusterIP) em vez do DNS
  do Docker.
- **Bugs corrigidos nos manifests existentes:** o `report-worker` recebia `RABBITMQ_ADDR`, mas lê
  `AMQP_URL` (tentaria `localhost:5672` — nunca consumiria) e não tinha
  `OTEL_EXPORTER_OTLP_ENDPOINT` (sem traces); o `loadtest` rodava o runner no-op sem OTel.
- Imagens do compose com os mesmos nomes dos manifests (`horus/<componente>:dev`).
- Job `e2e-k8s` no CI (kind + `kubectl apply -k deploy/` + `scripts/e2e.sh` pelo LB do cluster),
  **não-bloqueante** até a primeira execução verde no GitHub.
- Guia de cluster local em `deploy/k8s/README.md`.

## Fora do escopo

- Operadores/Helm charts gerenciados (Postgres HA, RabbitMQ cluster), backups.
- `metrics-server`/ingress-controller (dependências do cluster, já documentadas em T-802/T-901).

## Critérios de aceite

- [x] `kubectl kustomize deploy/` renderiza 52 objetos; `kubeconform -strict` (K8s 1.33) → 52 válidos.
- [x] Config NGINX K8s: `nginx -t` ok.
- [x] Bugs de env do worker/loadtest corrigidos.
- [ ] Pods prontos num cluster real + e2e pelo LB — **não executável no ambiente da sessão** (kind
  sobe o nó, mas o runc aninhado falha ao criar sandboxes: `can't get final child's PID`);
  coberto pelo job `e2e-k8s` do CI.

## Riscos

| Risco | Mitigação |
|---|---|
| Stack não validado em cluster real | Job `e2e-k8s` (não-bloqueante até ficar verde); schemas validados estritamente |
| Requests de CPU (2,45 vCPU) > nó pequeno | Comando `kubectl set resources` documentado e usado no CI |
| Jaeger/Loki com PVC RWO | `strategy: Recreate` (nunca dois pods no mesmo volume) |

## Referências

- [`../T-905/PRD.md`](../T-905/PRD.md) · [`../../../deploy/k8s/README.md`](../../../deploy/k8s/README.md) · [`./PLAN.md`](./PLAN.md)
