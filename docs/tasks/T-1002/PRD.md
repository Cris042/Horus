# PRD — T-1002: Compose com as aplicações + e2e no CI

| Campo | Valor |
|---|---|
| **Task** | `T-1002` |
| **Fase do roadmap** | `Fase 10 — Produto utilizável` |
| **Status** | `Entregue` |
| **Branch** | `claude/beautiful-cray-sfblsr` |
| **PR** | — |
| **Depende de** | `T-801`, `T-905` |
| **Requisitos atendidos** | `RF-005`, `RF-032`, PRD §10 (critérios 2, 4, 9, 10, 16) |
| **ADRs relacionados** | `ADR-0002`, `ADR-0012`, `ADR-0013` |
| **Data** | `2026-09-28` |

## Objetivo

Até aqui o `docker-compose.yml` subia só a infraestrutura; o Load Balancer nunca foi um serviço
padrão e todas as validações ponta a ponta (T-405, T-905) foram manuais — foi assim que o bug do
T-507 (spans de SAGA nunca emitidos) passou despercebido. Esta task coloca **todo o sistema** no
compose e cria um **e2e automatizado no CI** que exercita o caminho real
LB → serviços → SAGA → RabbitMQ → worker → OTel → Jaeger/Loki → Horus.

## Escopo (o que entra)

- Profile `apps` no `deploy/docker-compose.yml` (fora do `make up` padrão): 4 serviços de
  domínio/SAGA, Horus, worker Rust, API de carga e o **Load Balancer NGINX** (`:8088`).
  Imagens Quarkus via `dockerfile_inline` sobre o `target/quarkus-app` empacotado no host;
  healthchecks `/q/health/ready` (bash `/dev/tcp`, a imagem JRE não tem curl/wget).
- `make package` / `make up-apps` / `make down-apps` / `make e2e`.
- `scripts/e2e.sh`: injeta `traceparent` conhecido, roda uma SAGA feliz e uma compensada pelo
  LB e verifica no Horus: SAGA (desfecho/compensação), queries SQL, serviços no mesmo trace,
  HTTP→RabbitMQ→worker, waterfall, busca por janela (T-1001, com e sem `error=true`) e resumo
  de estado. Espera ativa (pipeline OTLP é assíncrono) com timeout.
- Job `e2e` no `ci.yml` (após `build-java`/`build-worker`), com dump de logs em falha.
- **Correções encontradas ao rodar o stack real:**
  1. Horus **não subia** com `ANTHROPIC_API_KEY` vazia (Quarkus trata `""` como definido e o
     default `dummy-key` não se aplica) — afetava também o `secrets.example.yaml` do K8s, que
     definia a chave como `""`. Compose passa o placeholder; o exemplo K8s deixa a chave comentada.
  2. **A SAGA compensada não tinha nenhum span em erro**: a checagem `FALHA` da NF ficava fora do
     span do passo `issue-invoice`, que terminava `OK` — a busca por erro não encontrava a SAGA
     e o RCA não via a falha. A checagem foi movida para dentro do passo.

## Fora do escopo

- Overlay de infraestrutura no Kubernetes → `T-1009`.
- Grafana/exporters/cAdvisor e a API de carga no e2e (não são necessários para as verificações).
- IA real no e2e (roda em modo stub; `T-1003`).

## Critérios de aceite

- [x] `make up` continua subindo só a infra; `--profile apps` sobe o sistema inteiro saudável.
- [x] Tráfego entra pelo LB e o `traceparent` injetado é preservado ponta a ponta.
- [x] `scripts/e2e.sh` passa contra o stack real (9/9 verificações + 3 fluxos).
- [x] Job `e2e` no CI, com logs do stack em falha.
- [x] Os dois defeitos acima corrigidos e cobertos pelo e2e.

## Riscos

| Risco | Mitigação |
|---|---|
| e2e instável pela latência de ingestão | Espera ativa por verificação (padrão 120s) em vez de `sleep` fixo |
| Rate limit do Docker Hub no CI | Só imagens necessárias; `--wait-timeout 600` |
| Imagens do profile dependem do `target/` do host | Documentado; `make up-apps` empacota antes; T-801 segue para imagens publicáveis |

## Referências

- [`./PLAN.md`](./PLAN.md) · [`../T-905/PRD.md`](../T-905/PRD.md) · [`../../../deploy/README.md`](../../../deploy/README.md)
