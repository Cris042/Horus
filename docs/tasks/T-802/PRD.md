# PRD — T-802: Manifests/Helm Kubernetes

| Campo | Valor |
|---|---|
| **Task** | `T-802` |
| **Fase** | Fase 8 — Containerização e Kubernetes |
| **Requisitos** | RF-033 |
| **Dependências** | T-801 (imagens Docker) |
| **Branch** | `task/T-802-k8s-manifests` |

## Objetivo

Declarar os **workloads de aplicação** do Horus em Kubernetes — `Deployment` + `Service`
de cada executável, `ConfigMap` compartilhado e `Secret` (credenciais/chaves) — como uma
base **Kustomize** aplicável com `kubectl apply -k`.

## Escopo

- `deploy/k8s/namespace.yaml` — namespace `horus`.
- `deploy/k8s/config.yaml` — `ConfigMap horus-config` (OTLP/backends, `HORUS_ENV`).
- `deploy/k8s/secrets.example.yaml` — template de Secrets (DB por serviço + `ANTHROPIC_API_KEY`).
- `deploy/k8s/apps/*.yaml` — `Deployment`(+`Service`) dos 7 executáveis, com:
  - imagem `horus/*:dev`, `imagePullPolicy: IfNotPresent`;
  - credenciais de banco via `secretKeyRef`; config via `configMapRef`;
  - probes `/q/health/{ready,live}` (Quarkus) / `/health` (loadtest); worker sem porta/probe HTTP;
  - `resources` requests/limits; labels `app.kubernetes.io/*` (component = domain/orchestrator/platform/worker/loadtest).
- `deploy/k8s/kustomization.yaml` — agrega recursos + transformer `images` (tag).
- `deploy/k8s/.gitignore` — ignora `secrets.yaml` (valores reais).
- `deploy/k8s/README.md` — topologia (ADR-0012), pré-requisitos e passos de `apply`.

## Fora de escopo

- Overlay de **infraestrutura** K8s (Postgres/RabbitMQ/observabilidade) — pré-requisito no
  cluster (em dev, via compose). Entra em fatia seguinte.
- **HPA / escala horizontal** por componente — **T-803**.
- Ingress/Gateway, NetworkPolicies, PDB, Helm chart empacotado.

## Critérios de aceitação

1. Cada executável tem `Deployment` (e `Service`, exceto o worker) no namespace `horus`.
2. Config não-secreta vem de `ConfigMap`; credenciais/chaves de `Secret` (com template de exemplo).
3. Probes e `resources` definidos; Horus e worker separados do domínio (ADR-0012).
4. `kubectl apply -k deploy/k8s/` referencia todos os recursos (base Kustomize válida).
5. Todos os manifestos são YAML válido (validado) — ver PLAN.
