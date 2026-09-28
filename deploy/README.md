# deploy/ — Empacotamento e orquestração

Artefatos de implantação do Horus e do sistema observado.

## Desenvolvimento local — `docker-compose` (T-003, RF-032)

`docker-compose.yml` sobe a **infraestrutura local**; rode pela raiz do repo:

```bash
make up      # sobe tudo em background
make ps      # status
make logs    # segue os logs
make down    # derruba (mantém os dados)
make clean   # derruba e APAGA os volumes (dados dos bancos)
```

### Serviços e portas

| Serviço | Imagem | Porta(s) host | Função |
|---|---|---|---|
| `postgres-prontuario` | `postgres:17.2` | `5432` | Banco `prontuario_db` |
| `postgres-payment` | `postgres:17.2` | `5433` | Banco `payment_db` |
| `postgres-invoice` | `postgres:17.2` | `5434` | Banco `invoice_db` |
| `postgres-saga` | `postgres:17.2` | `5435` | Banco `saga_db` |
| `rabbitmq` | `rabbitmq:4.0-management` | `5672`, `15672` (UI) | Mensageria de relatórios (ADR-0004) |
| `otel-collector` | `otelcol-contrib:0.118` | `4317` (gRPC), `4318` (HTTP) | Ingestão OTLP única (ADR-0010) |
| `jaeger` | `jaeger:2.2` | `16686` (UI) | Traces (RF-031) |
| `loki` | `loki:3.4` | `3100` | Logs (RF-H-003) |
| `prometheus` | `prometheus:v3.2` | `9090` (UI) | Métricas |

**Credenciais de dev** (não-secretas, locais): cada Postgres usa credencial segregada do
serviço (`*_svc`/`*_pw`); RabbitMQ usa `horus`/`horus`.

### Fluxo de telemetria

As aplicações instrumentadas emitem **só via OTLP** para o Collector (`localhost:4317/4318`), que roteia:
traces → Jaeger, logs → Loki, métricas → Prometheus (remote_write). Configs em `deploy/telemetry/`.
A redação/sanitização de borda (PII) é aprofundada em **T-404/T-406** — ver [`docs/telemetry/CONTRACT.md`](../docs/telemetry/CONTRACT.md).

### Aplicações + Load Balancer — profile `apps` (T-1002)

O mesmo compose sobe o **sistema completo** com o profile `apps` (fora do `make up` padrão):

```bash
make up-apps   # ./mvnw package + compose --profile apps up --build --wait
make e2e       # scripts/e2e.sh — SAGA feliz e compensada pelo LB, verificadas no Horus
make down-apps
```

| Serviço | Porta host | Observação |
|---|---|---|
| `load-balancer` | `8088` | NGINX (`lb/nginx.conf`) — entrada da carga e do e2e (RF-005) |
| `horus` | `8080` | Painel em `/horus-panel.html`; IA real se `ANTHROPIC_API_KEY` exportada |
| `prontuario-service` / `payment-service` / `invoice-service` / `saga-orchestrator` | `8081`–`8084` | Profile `prod` do Quarkus (hosts do compose) |
| `report-worker` | — | Consome `relatorios` no RabbitMQ; OTLP/HTTP no Collector |
| `loadtest` | `8000` | FastAPI + Locust; alvo padrão `http://load-balancer` |

As imagens Quarkus do profile copiam o `target/quarkus-app` já empacotado no host (sem
refazer o download de dependências dentro do Docker); para imagens publicáveis e
autocontidas use os Dockerfiles de T-801 (`make docker-images`).

> ⚠️ Não exporte `ANTHROPIC_API_KEY` vazia: o Quarkus trata `""` como valor definido e o Horus
> não sobe. Sem chave, o compose passa o placeholder e a IA roda em modo stub.

## Imagens Docker dos executáveis (T-801, RF-032/RNF-004)

Cada executável tem um Dockerfile reproduzível (build multi-stage, roda como usuário
não-root). Construa pela raiz do repo:

```bash
make docker-images     # todas as 7 imagens (5 Quarkus + worker + loadtest)
make docker-quarkus    # só os 5 módulos Quarkus
make docker-worker     # só o worker Rust
make docker-loadtest   # só a API de carga (FastAPI)
# tag customizada: make docker-images IMAGE_TAG=0.1.0
```

| Executável | Dockerfile | Contexto | Imagem | Porta |
|---|---|---|---|---|
| Horus | `horus/src/main/docker/Dockerfile.jvm` | raiz | `horus/horus` | 8080 |
| Prontuário | `services/prontuario/src/main/docker/Dockerfile.jvm` | raiz | `horus/prontuario` | 8080 |
| Payment | `services/payment/src/main/docker/Dockerfile.jvm` | raiz | `horus/payment` | 8080 |
| Invoice | `services/invoice/src/main/docker/Dockerfile.jvm` | raiz | `horus/invoice` | 8080 |
| SAGA | `services/saga-orchestrator/src/main/docker/Dockerfile.jvm` | raiz | `horus/saga-orchestrator` | 8080 |
| Worker | `worker/Dockerfile` | `worker/` | `horus/report-worker` | — |
| Loadtest | `loadtest/Dockerfile` | `loadtest/` | `horus/loadtest` | 8000 |

> As imagens Quarkus constroem a partir da **raiz** (precisam do reator Maven); por isso o
> `-f <módulo>/.../Dockerfile.jvm .`. São imagens **JVM** (Temurin 25); imagem nativa é otimização futura.

## Outras entregas (futuras)

- **Manifests / Helm** para Kubernetes (T-802, RF-033): deployments, services, configmaps, secrets.
- **Topologia:** worker e Horus implantados **separados** dos serviços de domínio (ADR-0012, T-804); escala horizontal independente por componente (RNF-005, T-803).

## Tasks

`T-003` (compose dev) · `T-404` (Collector + backends, config aprofundada) · `T-801`..`T-804` (Docker/K8s).
