# PRD — T-803: Escalabilidade horizontal independente por componente

| Campo | Valor |
|---|---|
| **Task** | `T-803` |
| **Fase** | Fase 8 — Containerização e Kubernetes |
| **Requisitos** | RNF-005 |
| **Dependências** | T-802 (manifests K8s) |
| **Branch** | `task/T-803-hpa` |

## Objetivo

Permitir que cada componente **escale horizontalmente de forma independente** (RNF-005),
via `HorizontalPodAutoscaler` (autoscaling/v2) dirigido por utilização de CPU — aproveitando
os `resources.requests` definidos nos Deployments (T-802).

## Escopo

- `deploy/k8s/autoscaling/<componente>-hpa.yaml` para os componentes escaláveis:
  - domínio: `prontuario-service`, `payment-service`, `invoice-service` (2→6, CPU 70%);
  - `saga-orchestrator` (2→5, 70%); `horus` (1→3, 75%); `report-worker` (1→4, 75%).
- `behavior` com janelas de estabilização (scale-up rápido, scale-down conservador).
- Remover `replicas` estático dos Deployments geridos por HPA (evita conflito Kustomize×HPA);
  `loadtest` (controlador, não escalável) mantém réplica fixa.
- Incluir os HPAs na base Kustomize; documentar no `deploy/k8s/README.md`.

## Fora de escopo

- Escala por **métricas customizadas** (ex.: profundidade de fila RabbitMQ para o worker via
  KEDA/Prometheus Adapter) — CPU é a primeira fatia; custom metrics ficam para evolução.
- Cluster Autoscaler (nós), VPA, PodDisruptionBudget.

## Critérios de aceitação

1. Cada componente escalável tem um HPA com min/max e alvo de CPU.
2. Deployments geridos por HPA não fixam `replicas` (sem conflito); `loadtest` mantém réplica.
3. HPAs entram na base Kustomize (`kubectl apply -k`).
4. Manifestos validados como YAML.
