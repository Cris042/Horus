# PRD — T-404: OTel Collector + backends (Jaeger / Loki / Prometheus)

| Campo | Valor |
|---|---|
| **Task** | `T-404` |
| **Fase do roadmap** | `Fase 4 — Telemetria e correlação` |
| **Status** | `Em revisão (PR)` |
| **Branch** | `task/T-404-otel-collector-backends` |
| **PR** | `#18` |
| **Depende de** | `T-003` |
| **Requisitos atendidos** | `RF-031`, `RF-H-014` |
| **ADRs relacionados** | `ADR-0010` (OTel/Collector), `ADR-0009` (backends) |
| **Data** | `2026-06-27` |

## Objetivo

Endurecer e validar o pipeline de observabilidade que o `T-003` plumbou (Collector → Jaeger/Loki/Prometheus), tornando-o **operacionalmente verificável** e **consultável por uma única lente**: adiciona *health checks* a todos os componentes de telemetria e um **Grafana** provisionado com os três datasources (Prometheus, Loki, Jaeger). Essa base habilita `T-405` (validação de correlação ponta a ponta) e `T-501` (serviço Horus consumindo os backends).

## Escopo (o que entra)

- Extensão `health_check` no OTel Collector (`:13133`) + `healthcheck` no compose nos componentes com shell/`wget` (Loki, Prometheus, Grafana). Jaeger 2.x e o Collector são imagens *distroless* (sem `wget`): a ordenação de boot fica por `depends_on` + a extensão de liveness (consumível por probes externos em k8s, T-8xx).
- Serviço **Grafana** no compose com **datasources provisionados** (Prometheus, Loki, Jaeger) — visualização unificada (RF-031).
- Doc de validação do pipeline (`deploy/telemetry/README.md`): portas, fluxo OTLP→backends e roteiro de verificação ponta a ponta (insumo do `T-405`).

## Fora do escopo

- Instrumentação de apps (já em `T-401`; FastAPI/worker em `T-402`/`T-403`).
- Sanitização/redação de PII na borda do Collector (`T-406`).
- Serviço Horus / adaptadores de consulta (`T-501`).
- Dashboards de negócio prontos (apenas datasources; dashboards vêm com a fase de visualização).

## Premissas e dependências

- `T-003` já entregou Collector, Jaeger, Loki, Prometheus no `deploy/docker-compose.yml` e suas configs em `deploy/telemetry/`.
- Ativação real dos apps containerizados é `T-801`; aqui validamos o plano de telemetria isoladamente (`docker compose config` + subida dos backends).

## Critérios de aceite

- [ ] Collector expõe `health_check` (`:13133`); compose tem `healthcheck` funcional em Loki/Prometheus/Grafana e ordenação por `depends_on`.
- [ ] Grafana sobe com datasources Prometheus, Loki e Jaeger já provisionados (sem cliques manuais).
- [ ] `docker compose -f deploy/docker-compose.yml config` valida sem erro.
- [ ] `deploy/telemetry/README.md` descreve portas, fluxo e roteiro de verificação.

## Riscos

| Risco | Mitigação |
|---|---|
| Versões de imagem incompatíveis com OTLP/remote_write | Pinar versões já validadas no `T-003`; Grafana em linha estável (11.x) |
| Datasource Jaeger exige plugin extra no Grafana | Usar o datasource nativo `jaeger` (incluído no core do Grafana) |

## Referências

- [`../../PRD.md`](../../PRD.md) — PRD global
- [`./PLAN.md`](./PLAN.md) — plano de execução desta task
- [`../../telemetry/CONTRACT.md`](../../telemetry/CONTRACT.md) — contrato de telemetria (T-005)
- `ADR-0009`, `ADR-0010`
