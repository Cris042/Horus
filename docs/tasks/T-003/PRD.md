# PRD — T-003: `docker-compose` de desenvolvimento

| Campo | Valor |
|---|---|
| **Task** | `T-003` |
| **Fase do roadmap** | Fase 0 — Fundação |
| **Status** | `Entregue` |
| **Branch** | `task/T-003-docker-compose-dev` |
| **PR** | [#6](https://github.com/mclovin137/Horus/pull/6) |
| **Depende de** | `T-001` |
| **Requisitos atendidos** | RF-032 (+ guia RF-031, RF-H-003, ADR-0010) |
| **ADRs relacionados** | ADR-0004, ADR-0010, ADR-0012 |
| **Data** | 2026-06-27 |

## Objetivo

Entregar a **infraestrutura local de desenvolvimento** via `docker-compose`, de modo que `make up` suba, com um comando, os **Postgres ×3** (um por serviço de domínio), o **RabbitMQ**, o **OTel Collector** e os backends de telemetria **Jaeger/Loki/Prometheus**. É a base sobre a qual as Fases 1+ (serviços, worker, instrumentação) rodam localmente.

## Escopo (o que entra)

- **`deploy/docker-compose.yml`** — Postgres ×3 (`prontuario_db`/`payment_db`/`invoice_db`, portas 5432/5433/5434), RabbitMQ 4.0 (5672 + 15672), OTel Collector contrib (4317/4318), Jaeger 2 (16686), Loki 3 (3100), Prometheus 3 (9090); rede, volumes nomeados e healthchecks.
- **`deploy/telemetry/otel-collector-config.yaml`** — pipeline OTLP→Jaeger/Loki/Prometheus (ADR-0010).
- **`deploy/telemetry/prometheus.yml`**, **`deploy/telemetry/loki-config.yaml`** — configs mínimas de dev.
- **`Makefile`** — `up`/`down`/`ps`/`logs`/`restart`/`clean` reais (substituem os placeholders).
- **`deploy/README.md`** — tabela de serviços/portas e fluxo de telemetria.

## Fora do escopo

- **Aplicações** (Quarkus/Rust/Python) no compose → Fases 1+.
- **Config aprofundada** do Collector/redação de PII na borda → T-404/T-406.
- **Migrações** de schema dos bancos → T-101+ (Flyway).
- **Kubernetes/Helm e imagens publicáveis** → Fase 8 (T-801..804).

## Premissas e dependências

- T-001 entregue (diretório `deploy/`).
- Docker + Compose v2 disponíveis no host. Versões de imagem fixadas conforme `lib.md` (confirmar ao evoluir).
- Alinhado ao contrato de telemetria (T-005): nomes de banco e o Collector como ponto único de ingestão.

## Critérios de aceite

- [ ] `docker compose -f deploy/docker-compose.yml config` valida sem erro.
- [ ] `make up` sobe os 8 contêineres; bancos e RabbitMQ ficam *healthy*.
- [ ] UIs acessíveis: Jaeger `:16686`, RabbitMQ `:15672`, Prometheus `:9090`.
- [ ] Collector aceita OTLP em `:4317`/`:4318` e roteia para os três backends.
- [ ] `make down`/`make clean` derrubam (e, no clean, removem volumes).
- [ ] `PLAN.md` 100% ✅ e `state.md` atualizado.

## Riscos

| Risco | Mitigação |
|---|---|
| Grafias de config do Collector/Loki mudam entre versões | Versões **fixadas**; config mínima e documentada; validar com `compose config` e subida real. |
| Conflito de portas no host (5432 etc.) | Postgres mapeados em 5432/5433/5434; documentado no README para ajuste local. |
| Loki/Prometheus exigirem flags novas para OTLP/remote_write | `--web.enable-remote-write-receiver` no Prometheus; Loki com `allow_structured_metadata`. |

## Referências

- [`../../PRD.md`](../../PRD.md), [`../../ROADMAP.md`](../../ROADMAP.md) — RF-032
- [`../../telemetry/CONTRACT.md`](../../telemetry/CONTRACT.md) — contrato de telemetria (T-005)
- ADR-0004 (mensageria), ADR-0010 (pipeline de telemetria), ADR-0012 (topologia)
- [`./PLAN.md`](./PLAN.md)
