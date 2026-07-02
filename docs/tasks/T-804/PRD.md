# PRD — T-804: Implantar worker e Horus separados dos serviços de domínio

| Campo | Valor |
|---|---|
| **Task** | `T-804` |
| **Fase** | Fase 8 — Containerização e Kubernetes |
| **Requisitos** | ADR-0012 |
| **Dependências** | T-802 (manifests K8s) |
| **Branch** | `task/T-804-separate-platform` |

## Objetivo

Garantir que o **worker Rust** e o **Horus** sejam implantados **separados** dos serviços
de domínio (ADR-0012) — reforçando a não intrusividade (RNF-H-008) e a escala independente.
A separação lógica já existe (Deployments distintos + labels `component`); esta task a torna
**topológica e explícita** no agendamento.

## Escopo

- Adicionar `nodeAffinity` **preferida** (soft) a cada Deployment, por pool de nós
  (`horus.io/tier`):
  - `horus` e `report-worker` → `platform`;
  - `prontuario-service` / `payment-service` / `invoice-service` / `saga-orchestrator` → `domain`.
- Soft (`preferredDuringSchedulingIgnoredDuringExecution`) para não bloquear em clusters
  sem nós rotulados (dev/single-node), mas separar de fato quando há pools.
- Documentar a topologia e como rotular os nós no `deploy/k8s/README.md`.

## Fora de escopo

- Afinidade **rígida** (`required...`) / taints+tolerations — fica como endurecimento de produção.
- Namespaces separados por tier; NetworkPolicies entre tiers.

## Critérios de aceitação

1. `horus` e `report-worker` preferem o pool `platform`; serviços de domínio preferem `domain`.
2. A preferência é soft (não quebra clusters sem labels).
3. Manifestos validados como YAML; Deployments íntegros.
4. README documenta a separação (ADR-0012) e a rotulagem de nós.
