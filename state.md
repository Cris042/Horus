# state.md — Estado do Projeto Horus

> **Fonte única de verdade do "onde estamos".** Este arquivo é atualizado **a cada entrega**.
> Regra registrada em [`docs/ROLES.md`](./docs/ROLES.md) (§ Responsabilidades de manutenção) e em [`CLAUDE.md`](./CLAUDE.md).
> Fluxo completo em [`docs/WORKFLOW.md`](./docs/WORKFLOW.md).

| Campo | Valor |
|---|---|
| **Última atualização** | 2026-06-27 |
| **Branch atual** | `task/T-501-horus-query-adapters` |
| **Fase do roadmap** | Fase 5 — Horus core |
| **Task ativa** | `T-501` — Serviço Horus: adaptadores de consulta aos backends (em revisão (PR)) |

---

## 🟢 Última entrega (mergeada)

**`T-406` — Redação de PII na borda do Collector (entregue via [PR #19](https://github.com/mclovin137/Horus/pull/19), mergeada em `main`).** Processor `transform/pii` (OTTL) em traces+logs: remove chaves proibidas e mascara e-mail/CPF/cartão (2ª camada; origem = T-401). Doc `PII-REDACTION.md`. `otelcol validate` exit 0. **CI verde.** *(Fase 4 de telemetria concluída exceto T-402/T-403, que dependem de componentes Python/Rust ainda não construídos.)*

> 🔄 **Em revisão:** **`T-501`** (1ª fatia) — Serviço Horus: camada de consulta (read-side) aos backends. Portas `TraceQueryPort`/`LogQueryPort`/`MetricQueryPort` + adapters REST-client (Jaeger/Loki/Prometheus, parsing `JsonNode`) + API `GET /horus/query/{traces,logs,metrics}`; testes `@QuarkusTest` com portas mockadas. `./mvnw -pl horus test` verde (6/6). Receptor OTLP próprio e correlação (T-502) ficam para fatias seguintes.

## ▶️ Próxima ação

**`T-502`** (modelo de correlação por `trace_id`: costurar request↔query↔log↔mensagem↔worker) — depende de `T-405`, melhor após `T-801` (stack reprodutível). Em paralelo, desbloqueadas: **`T-601`** (LangChain4j/Anthropic, depende só de T-501), Fase 3 **`T-303`** (worker Rust), Fase 2 **`T-202`** (API FastAPI de carga). Pendentes de telemetria: **`T-402`**/**`T-403`** (instrumentar FastAPI/LB e worker).

> ⚠️ **Nota técnica (vale p/ T-103/104):** entidades Panache geram id via **sequência `<tabela>_seq`** (PooledLo, INCREMENT 50) — as migrações Flyway devem criar a sequência (não usar coluna IDENTITY), senão `INSERT` falha em `nextval`.

> ⚙️ **Pendência sua (não bloqueia o roadmap):** configurar a secret `ANTHROPIC_API_KEY` em **Settings → Secrets and variables → Actions** para o review por IA rodar de fato (o `gate` o pula com status neutro). O pipeline de **CI (build/test) não usa segredo** e roda sempre.

---

## 📒 Log de entregas (mais recente primeiro)

| Data | Task | Entrega | Branch | PR | Status |
|---|---|---|---|---|---|
| 2026-06-27 | `T-501` | Serviço Horus (1ª fatia): camada de consulta aos backends — portas Trace/Log/Metric + adapters REST-client (Jaeger/Loki/Prometheus), API `GET /horus/query/*`, testes com portas mockadas; `mvn -pl horus test` verde (6/6) | `task/T-501-horus-query-adapters` | [#20](https://github.com/mclovin137/Horus/pull/20) | 🔄 Em revisão (PR) |
| 2026-06-27 | `T-406` | Redação de PII na borda do Collector (RNF-010/RNF-H-002): processor `transform/pii` (OTTL) em traces+logs — remove chaves proibidas e mascara e-mail/CPF/cartão (2ª camada; origem = T-401), doc `PII-REDACTION.md`; `otelcol validate` ok | `task/T-406-pii-redaction` | [#19](https://github.com/mclovin137/Horus/pull/19) | ✅ Entregue |
| 2026-06-27 | `T-404` | OTel Collector + backends (RF-031/RF-H-014): `health_check` no Collector, healthchecks/`depends_on` no compose, Grafana com datasources provisionados (Prometheus/Loki/Jaeger), doc `telemetry/README.md`; `compose config` + `otelcol validate` ok | `task/T-404-otel-collector-backends` | [#18](https://github.com/mclovin137/Horus/pull/18) | ✅ Entregue |
| 2026-06-27 | `T-201` | Load Balancer de entrada (NGINX): roteamento por prefixo aos 3 serviços + orquestrador, passthrough de `traceparent`, `resolver`+variável; `nginx -t` ok (config+docs; ativação em T-801) | `task/T-201-load-balancer` | [#17](https://github.com/mclovin137/Horus/pull/17) | ✅ Entregue |
| 2026-06-27 | `T-302` | Publicação de relatório por fluxo (RF-021): SAGA solicita o relatório da NF após conclusão (best-effort); `InvoiceClient.solicitarRelatorio`, teste atualizado | `task/T-302-saga-report-publication` | [#16](https://github.com/mclovin137/Horus/pull/16) | ✅ Entregue |
| 2026-06-27 | `T-301` | RabbitMQ + contrato da mensagem de relatório (RF-021/RNF-011): `RelatorioMensagem` + publisher no invoice, `POST /notas/{id}/relatorio`, teste in-memory, doc `report-message.md` | `task/T-301-rabbitmq-report-contract` | [#15](https://github.com/mclovin137/Horus/pull/15) | ✅ Entregue |
| 2026-06-27 | `T-106` | Isolamento de persistência (RNF-002/003): credenciais segregadas por serviço (compose + datasources), healthcheck por `$POSTGRES_USER`, doc `db-isolation.md` | `task/T-106-db-isolation` | [#14](https://github.com/mclovin137/Horus/pull/14) | ✅ Entregue |
| 2026-06-27 | `T-107` | SAGA (orquestração) — fluxo pagar→emitir NF: módulo `saga-orchestrator` (estado persistido em `saga_db`, clients REST, compensação por estorno, teste feliz+compensação) | `task/T-107-saga-orchestrator` | [#13](https://github.com/mclovin137/Horus/pull/13) | ✅ Entregue |
| 2026-06-27 | `T-105` | Migrações Flyway por banco (RF-028/RNF-015): hardening (`validate-on-migrate`, `clean-disabled`) nos 3 serviços + doc da estratégia | `task/T-105-flyway-migrations` | [#12](https://github.com/mclovin137/Horus/pull/12) | ✅ Entregue |
| 2026-06-27 | `T-401` | Instrumentação OTel dos 3 serviços (HTTP + JDBC/Hibernate, logs JSON correlacionados, OTLP→Collector); contrato T-005 na camada de serviços | `task/T-401-otel-services` | [#11](https://github.com/mclovin137/Horus/pull/11) | ✅ Entregue |
| 2026-06-27 | `T-104` | Invoice Service (domínio): receber emissão, gerar NF simulada, registrar resultado, listar, reprocessar (RF-016..020); entidade `NotaFiscal`, REST+validação, teste de fluxo | `task/T-104-invoice-domain` | [#10](https://github.com/mclovin137/Horus/pull/10) | ✅ Entregue |
| 2026-06-27 | `T-103` | Payment Service (domínio): carteira/saldo, movimentações, aprovar/rejeitar/estornar, histórico (RF-011..015); entidades `Movimentacao`/`Pagamento`, migração `V2`, REST+validação, teste de fluxo | `task/T-103-payment-domain` | [#9](https://github.com/mclovin137/Horus/pull/9) | ✅ Entregue |
| 2026-06-27 | `T-102` | Prontuário Service (domínio): criar/consultar prontuário, registrar/atualizar/finalizar consulta (RF-006..010); entidade `Consulta`, migração `V2`, REST+validação, teste de fluxo | `task/T-102-prontuario-domain` | [#8](https://github.com/mclovin137/Horus/pull/8) | ✅ Entregue |
| 2026-06-27 | `T-101` | Scaffold Quarkus dos 3 serviços (REST + Panache + Flyway, banco por serviço, smoke test); **Quarkus 3.20→3.37** (Panache sob JDK 25) | `task/T-101-scaffold-services` | [#7](https://github.com/mclovin137/Horus/pull/7) | ✅ Entregue |
| 2026-06-27 | `T-003` | `docker-compose` de dev (Postgres ×3, RabbitMQ, OTel Collector, Jaeger, Loki, Prometheus) + `Makefile` (`make up`) + configs de telemetria | `task/T-003-docker-compose-dev` | [#6](https://github.com/mclovin137/Horus/pull/6) | ✅ Entregue |
| 2026-06-27 | `T-005` | Contrato de telemetria (`docs/telemetry/CONTRACT.md`): logging/spans, propagação HTTP+AMQP, métricas mínimas, PII | `task/T-005-telemetry-conventions` | [#5](https://github.com/mclovin137/Horus/pull/5) | ✅ Entregue |
| 2026-06-27 | `T-004` | CI de build/test (`ci.yml`) + **Maven Wrapper** (3.9.9); build validado local + CI (Java 25 + Quarkus 3.20 → SUCCESS, testes verdes) | `task/T-004-ci-build` | [#4](https://github.com/mclovin137/Horus/pull/4) | ✅ Entregue |
| 2026-06-27 | `T-002` | Bootstrap Quarkus do Horus (parent/aggregator + módulo `horus` REST/health; remove `Main.java`) | `task/T-002-bootstrap-quarkus` | [#3](https://github.com/mclovin137/Horus/pull/3) | ✅ Entregue |
| 2026-06-27 | — (fix) | `fix(ci)`: `id-token: write` + job `gate` de secret no review por IA | `fix/ai-review-oidc-permission` | [#2](https://github.com/mclovin137/Horus/pull/2) | ✅ Entregue |
| 2026-06-27 | `T-001` | Estrutura de monorepo (diretórios + READMEs por componente, `Makefile`, `.editorconfig`, mapa no README) | `task/T-001-estrutura-monorepo` | [#1](https://github.com/mclovin137/Horus/pull/1) | ✅ Entregue |
| 2026-06-26 | — | Publicação do projeto no Git/GitHub | `main` | — | ✅ Entregue |
| 2026-06-26 | — | Reversão de SAGA: ADR-0006 → ADR-0013 (adotar SAGA) + cascata | `main` | — | ✅ Entregue |
| 2026-06-26 | — | CI/CD: review por IA em PR + 3 rotinas diárias de monitoramento + skills | `main` | — | ✅ Entregue |
| 2026-06-26 | — | `README.md` (visão geral, fluxo, skills, estratégias, automações) | `main` | — | ✅ Entregue |
| 2026-06-26 | `T-000` | Bootstrap de governança: `state.md`, `WORKFLOW.md`, templates, PRD+plano de T-001 | `main` | — | ✅ Entregue |
| 2026-06-26 | — | Documentação inicial de planejamento: `PRD`, 12 `ADR`, `ROLES`, `ROADMAP`, `lib.md` | `main` | — | ✅ Entregue |

> `T-000` é o bootstrap que **estabelece** o fluxo; por isso não passou por branch/PR. A partir de `T-001`, **toda** task segue branch-por-task + PR.

---

## Como este arquivo funciona

1. **A cada entrega** (task concluída, ou marco relevante), atualizar: *Última atualização*, *Branch atual*, *Task ativa*, a seção **Última entrega**, a **Próxima ação** e adicionar uma linha no **Log de entregas**.
2. O *status* de cada task vive em dois lugares: aqui (visão macro) e no `PLAN.md` da task (visão arquivo por arquivo).
3. Status possíveis de task: `Planejada` · `Em progresso` · `Em revisão (PR)` · `Entregue` · `Bloqueada`.
