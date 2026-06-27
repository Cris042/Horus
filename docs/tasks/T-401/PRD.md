# PRD — T-401: Instrumentação OTel dos 3 serviços Quarkus

| Campo | Valor |
|---|---|
| **Task** | `T-401` |
| **Fase do roadmap** | Fase 4 — Telemetria |
| **Status** | `Em revisão (PR)` |
| **Branch** | `task/T-401-otel-services` |
| **PR** | [#11](https://github.com/mclovin137/Horus/pull/11) |
| **Depende de** | `T-102`/`T-103`/`T-104` (domínio), `T-003` (Collector), `T-005` (contrato) |
| **Requisitos atendidos** | RF-029, RF-030, RF-H-002 (RNF-H-002 parcial) |
| **ADRs relacionados** | ADR-0007 (OTel), ADR-0009 (request/query), ADR-0010 (pipeline) |
| **Data** | 2026-06-27 |

## Objetivo

Instrumentar os três serviços Quarkus (Prontuário, Payment, Invoice) com **OpenTelemetry**: traces de **HTTP** e de **JDBC/Hibernate** (um span por query, `db.query.text` parametrizado), **logs JSON** correlacionados por `trace_id`/`span_id`, e exportação **OTLP** para o Collector (T-003/ADR-0010). Operacionaliza o contrato de telemetria (T-005) na camada de serviços — base da correlação ponta a ponta do Horus.

## Escopo (o que entra)

- **Extensões** por serviço: `quarkus-opentelemetry` (traces HTTP + propagação W3C) e `quarkus-logging-json` (logs estruturados).
- **Config** (`application.properties`) por serviço:
  - `service.name` canônico (= `quarkus.application.name`) + `service.namespace=medrec` + `deployment.environment.name`;
  - exportador **OTLP** por perfil (`%dev` → `localhost:4317`, `%prod` → `otel-collector:4317`);
  - **`quarkus.datasource.jdbc.telemetry=true`** → span por execução de SQL com texto parametrizado (RF-H-002);
  - `quarkus.log.console.json=true` (logs JSON; OTel injeta `trace_id`/`span_id`);
  - `%test.quarkus.otel.sdk.disabled=true` (sem Collector em teste — não afeta os testes de fluxo).

## Fora do escopo

- **FastAPI/Load Balancer** (T-402) e **worker Rust/AMQP** (T-403).
- **Collector + backends** (config aprofundada) e **validação ponta a ponta** → T-404/T-405.
- **Sanitização/redação de PII** na borda → T-406 (o contrato já define as regras; aqui só garantimos `db.query.text` parametrizado e não-captura de binds).
- **Métricas customizadas** além do que o OTel/Micrometer fornecem por padrão.

## Premissas e dependências

- Os 3 serviços de domínio (T-102/103/104) entregues.
- Em dev, `make up` (T-003) provê o Collector em `localhost:4317`.
- Testes rodam via Dev Services; o SDK OTel é desligado em `%test` (sem Collector no CI).

## Critérios de aceite

- [ ] Cada serviço exporta traces HTTP via OTLP, com `service.name` canônico e propagação W3C (RF-029).
- [ ] Há **um span por execução de SQL** com `db.query.text` **parametrizado** (RF-H-002).
- [ ] Logs em **JSON** com `trace_id`/`span_id` quando há trace ativo (correlação).
- [ ] Build/test verde (SDK OTel desligado em teste); `PLAN.md` 100% ✅ e `state.md` atualizado.

## Riscos

| Risco | Mitigação |
|---|---|
| Exporter tentando conectar ao Collector em teste | `%test.quarkus.otel.sdk.disabled=true`. |
| `db.query.text` capturar valores ligados (PII) | Instrumentação JDBC usa o statement parametrizado (`?`); binds não são capturados — reforço de borda em T-406. |
| Nomes de propriedade OTel mudarem entre versões | Fixado ao Quarkus 3.37; confirmar ao evoluir (nota em `lib.md`). |

## Referências

- [`../../telemetry/CONTRACT.md`](../../telemetry/CONTRACT.md) — §§1-4 (recurso, propagação, spans, logs)
- [`../../PRD.md`](../../PRD.md) — RF-029/030, RF-H-002; ADR-0007/0009/0010
- [`./PLAN.md`](./PLAN.md)
