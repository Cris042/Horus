# PRD — T-501: Serviço Horus (Quarkus) — adaptadores de consulta aos backends

| Campo | Valor |
|---|---|
| **Task** | `T-501` |
| **Fase do roadmap** | `Fase 5 — Horus core` |
| **Status** | `Entregue` |
| **Branch** | `task/T-501-horus-query-adapters` |
| **PR** | `#20` |
| **Depende de** | `T-404` |
| **Requisitos atendidos** | `RF-H-014` |
| **ADRs relacionados** | `ADR-0009`, `ADR-0010` |
| **Data** | `2026-06-27` |

## Objetivo

Primeira fatia do serviço Horus: a **camada de consulta (read-side)** aos três backends de
telemetria — Jaeger (traces), Loki (logs), Prometheus (métricas) — atrás de **portas**
desacopladas, com adaptadores REST-client e uma API REST fina. É a base sobre a qual o
**modelo de correlação por `trace_id`** (T-502) e os agentes de IA (Fase 6) operam.

## Escopo (o que entra)

- Portas: `TraceQueryPort`, `LogQueryPort`, `MetricQueryPort` + DTOs neutros (`QueryModel`).
- Adaptadores REST-client: `JaegerTraceAdapter`, `LokiLogAdapter`, `PrometheusMetricAdapter`
  (parsing via `JsonNode`, sem acoplar ao schema completo de cada backend).
- API REST `GET /horus/query/traces/{id}`, `/horus/query/logs?traceId=`, `/horus/query/metrics?query=`.
- Config das URLs dos backends (default = nomes de serviço do compose) + template LogQL.
- Testes `@QuarkusTest` com as portas **mockadas** (verde sem backends reais).

## Fora do escopo

- **Receptor OTLP** do próprio Horus (o Horus emitir/ingerir sua telemetria) — fatia posterior.
- Modelo de correlação que costura request↔query↔log↔mensagem (T-502).
- APIs de ciclo de vida de request/query, agregação de erros, mapa de serviços (T-503..506).
- Validação contra backends reais — depende do stack de pé (T-405/T-801).

## Premissas e dependências

- `T-404` entregou Jaeger/Loki/Prometheus no compose com URLs estáveis.
- O rótulo/seletor LogQL de correlação é **configurável** (`horus.query.loki.logql-template`)
  porque o mapeamento OTLP→Loki só é finalizado na validação ponta a ponta (T-405).

## Critérios de aceite

- [ ] Três portas + adaptadores REST-client compilando e injetáveis (CDI).
- [ ] API REST expõe traces/logs/métricas; 404 em trace inexistente.
- [ ] `./mvnw -pl horus test` verde (portas mockadas).

## Riscos

| Risco | Mitigação |
|---|---|
| Schema de resposta dos backends diferente do esperado | Parsing tolerante via `JsonNode` (campos ausentes → default), não POJOs rígidos |
| Seletor LogQL errado (rótulo de trace_id) | Template configurável; finalizado em T-405 com stack real |

## Referências

- [`../../PRD.md`](../../PRD.md) — PRD global
- [`./PLAN.md`](./PLAN.md) — plano de execução
- [`../../telemetry/CONTRACT.md`](../../telemetry/CONTRACT.md) — contrato de telemetria
- `ADR-0009` (correlação por trace_id), `ADR-0010` (OTel/Collector)
