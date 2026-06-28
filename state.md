# state.md — Estado do Projeto Horus

> **Fonte única de verdade do "onde estamos".** Este arquivo é atualizado **a cada entrega**.
> Regra registrada em [`docs/ROLES.md`](./docs/ROLES.md) (§ Responsabilidades de manutenção) e em [`CLAUDE.md`](./CLAUDE.md).
> Fluxo completo em [`docs/WORKFLOW.md`](./docs/WORKFLOW.md).

| Campo | Valor |
|---|---|
| **Última atualização** | 2026-06-28 |
| **Branch atual** | `task/T-502-correlation-model` |
| **Fase do roadmap** | Fase 5 — Horus core (correlação + ciclo de vida) |
| **Task ativa** | `T-502` — Modelo de correlação por `trace_id` (em revisão (PR)) |

---

## 🟢 Última entrega (mergeada)

**`T-403` — OTel no worker + propagação HTTP→AMQP (entregue via [PR #34](https://github.com/mclovin137/Horus/pull/34), mergeada em `main`).** `telemetry.rs` (OTLP/HTTP + bridge tracing→OTel + W3C); `extrair_contexto` lê `traceparent` dos headers AMQP → span filho do publicador. Verificado em container `rust:1-slim` (test/fmt/clippy). **Fase 4 (telemetria) completa.** **CI + ai-review verdes.**

> 🔄 **Em revisão:** **`T-502`** — Modelo de correlação por `trace_id` (RF-H-001/002/004): `correlation/CorrelationModel` + `CorrelationService` costuram trace (spans por serviço) + logs + fronteira de mensageria (`report-worker`/publish/relatorios) + contagem de erros num `RequestCorrelation`; `GET /horus/correlation/trace/{id}`. Construído sobre as portas T-501, testado com mocks. `mvn -pl horus test` verde (35/35, 3 novos).

## ▶️ Próxima ação

Fase 5 core: **`T-503/504`** (APIs de ciclo de vida — waterfall de spans da request / statement de query sanitizado) e **`T-505`** (agregação de logs de erro correlacionados) — todos sobre o modelo T-502. Depois **`T-506`** (mapa de serviços) / **`T-507`** (visualização de SAGA) e Fase 7 **`T-702`** (waterfall UI). **`T-405`** (validação ponta a ponta com stack real) e **`T-606`** (anomalias, dep T-505) ficam para quando o ambiente estiver de pé / após T-505. 💡 Rust verificável localmente via Docker (`rust:1-slim`).

> ⚙️ **Pendência sua (agora relevante p/ IA):** a IA real (T-601+) exige a secret `ANTHROPIC_API_KEY` em **Settings → Secrets and variables → Actions** e `horus.ai.enabled=true`. Sem isso, o Horus roda com o `StubLlmEngine` (placeholder) — não bloqueia build/CI.

> ⚠️ **Nota técnica (vale p/ T-103/104):** entidades Panache geram id via **sequência `<tabela>_seq`** (PooledLo, INCREMENT 50) — as migrações Flyway devem criar a sequência (não usar coluna IDENTITY), senão `INSERT` falha em `nextval`.

> ⚙️ **Pendência sua (não bloqueia o roadmap):** configurar a secret `ANTHROPIC_API_KEY` em **Settings → Secrets and variables → Actions** para o review por IA rodar de fato (o `gate` o pula com status neutro). O pipeline de **CI (build/test) não usa segredo** e roda sempre.

---

## 📒 Log de entregas (mais recente primeiro)

| Data | Task | Entrega | Branch | PR | Status |
|---|---|---|---|---|---|
| 2026-06-28 | `T-403` | OTel no worker + propagação HTTP→AMQP (RF-029/RF-H-004): `telemetry.rs` (provider+recurso `report-worker`/`medrec`, OTLP/HTTP sem protoc, bridge tracing→OTel, propagador W3C); `extrair_contexto` lê `traceparent` dos headers AMQP e o span de processamento vira filho do publicador; verificado em container `rust:1-slim` (test 12, fmt, clippy) | `task/T-403-worker-otel-amqp` | [#34](https://github.com/mclovin137/Horus/pull/34) | ✅ Entregue |
| 2026-06-28 | `T-502` | Modelo de correlação por `trace_id` (RF-H-001/002/004): `CorrelationModel`+`CorrelationService` costuram spans-por-serviço + logs + fronteira mensageria/worker + erros num `RequestCorrelation`; `GET /horus/correlation/trace/{id}`; sobre portas T-501, testes mockados; `mvn -pl horus test` verde (35/35, 3 novos) | `task/T-502-correlation-model` | [#35](https://github.com/mclovin137/Horus/pull/35) | 🔄 Em revisão (PR) |
| 2026-06-28 | `T-303` | Worker Rust de relatórios (RF-022..024/RNF-012): crate `worker/` (tokio+lapin+lettre+serde+tracing) consome exchange `relatorios`/`nota-fiscal`, gera relatório e envia e-mail; `EmailSender` (SMTP + stub de log), idempotência por `id`, ack/nack; job de CI `build-worker` (fmt+clippy+test); validação via CI | `task/T-303-rust-worker` | [#33](https://github.com/mclovin137/Horus/pull/33) | ✅ Entregue |
| 2026-06-28 | `T-402` | Instrumentação OTel da borda (RF-029): `telemetry.py` (provider+recurso canônico `loadtest-api`/`medrec`+OTLP), FastAPI instrumentada (gated `HORUS_OTEL_ENABLED`), Locust injeta W3C `traceparent` (`events.init`+`RequestsInstrumentor`), LB access log JSON com `traceparent`; testes offline (exporter em memória); `pytest` verde (21/21) | `task/T-402-otel-loadtest-edge` | [#32](https://github.com/mclovin137/Horus/pull/32) | ✅ Entregue |
| 2026-06-28 | `T-203` | Cenários Locust (RF-004/RNF-016): `app/locustfile.py` (usuários por domínio + SAGA, fluxos encadeados, fração de falhas) + `LocustRunner` (`locust --headless`, start/stop) selecionável por `HORUS_LOADTEST_RUNNER`; +4 correções do review por IA; testes offline isolam import de `locust`/gevent em subprocesso; `pytest` verde (18/18) | `task/T-203-locust-scenarios` | [#31](https://github.com/mclovin137/Horus/pull/31) | ✅ Entregue |
| 2026-06-28 | `T-202` | API FastAPI de controle de teste de carga (RF-001..003): `POST /load-tests`, `GET /load-tests/{id}`, `POST /load-tests/{id}/stop` (+`GET /load-tests`/`/health`); Pydantic + `LoadTestManager` (porta `LoadRunner` no-op→Locust em T-203); job de CI Python `build-loadtest` (uv+pytest); `pytest` verde (10/10) | `task/T-202-fastapi-control-api` | [#30](https://github.com/mclovin137/Horus/pull/30) | ✅ Entregue |
| 2026-06-28 | `T-704` | RBAC do Horus (RNF-H-010, 1ª fatia): `HorusRole`×`Capability` (matriz de `ROLES.md`), `HorusRbacFilter` autoriza por papel via cabeçalho `X-Horus-Role`; públicos isentos; off por padrão (`horus.rbac.enabled=false`); OIDC/JWT em fatia seguinte; `mvn -pl horus test` verde (32/32, 6 novos) | `task/T-704-horus-rbac` | [#29](https://github.com/mclovin137/Horus/pull/29) | ✅ Entregue |
| 2026-06-28 | `T-701` | Painel Horus (RF-H-012, 1ª fatia): API `GET /horus/panel/overview` (saúde IA+cache+backends+capacidades) + página estática `horus-panel.html` (saúde, ask, busca por trace explain/RCA, capacidades); waterfall (T-702) e busca avançada nas fatias seguintes; `mvn -pl horus test` verde (26/26, 2 novos) | `task/T-701-horus-panel` | [#28](https://github.com/mclovin137/Horus/pull/28) | ✅ Entregue |
| 2026-06-28 | `T-608` | Salvaguardas de custo/latência (RNF-H-003/004): cache de respostas do LLM via CDI decorator `CachingLlmEngine` + `LlmResponseCache` (LRU, on/off), `GET /horus/ai/cache/stats`; consolida seleção de modelo (`ModelTier`) + orçamento (T-602); `mvn -pl horus test` verde (24/24, hit verificado) | `task/T-608-llm-cache` | [#27](https://github.com/mclovin137/Horus/pull/27) | 🔄 Em revisão (PR) |
| 2026-06-28 | `T-607` | Agente NL Query "pergunte ao Horus" (RF-H-010, 1ª fatia): `NlQueryAgent` responde NL fundamentado na telemetria (camada BALANCED), `POST /horus/ai/ask`; planejamento autônomo (tool-calling) é fatia seguinte; `mvn -pl horus test` verde (20/20) | `task/T-607-nl-query-agent` | [#26](https://github.com/mclovin137/Horus/pull/26) | 🔄 Em revisão (PR) |
| 2026-06-28 | `T-605` | Agente Root-Cause Analyst (RF-H-007): `RootCauseAnalyst` faz RCA dos 3 sinais (`assembleForIncident` trace+logs+métricas) com causa provável+passos, camada DEEP/Opus; `GET /horus/ai/rca/trace/{id}`; `mvn -pl horus test` verde (17/17) | `task/T-605-rca-agent` | [#25](https://github.com/mclovin137/Horus/pull/25) | 🔄 Em revisão (PR) |
| 2026-06-28 | `T-604` | Agente Trace Explainer (RF-H-006): `TraceExplainer` narra o caminho da request (contexto T-602 + `LlmEngine` BALANCED/Sonnet); `GET /horus/ai/explain/trace/{id}`; `mvn -pl horus test` verde (15/15) | `task/T-604-trace-explainer` | [#24](https://github.com/mclovin137/Horus/pull/24) | 🔄 Em revisão (PR) |
| 2026-06-28 | `T-603` | Agente Summarizer (RF-H-005, 1ª fatia): `StateSummarizer` (contexto T-602 + `LlmEngine` FAST) resume estado em NL; sob demanda `GET /horus/ai/summary/trace/{id}` + agendado `ScheduledStateSummary` (cron off default); `mvn -pl horus test` verde (13/13) | `task/T-603-summarizer-agent` | [#23](https://github.com/mclovin137/Horus/pull/23) | 🔄 Em revisão (PR) |
| 2026-06-28 | `T-602` | Montador de contexto da IA (RNF-H-003/006, 1ª fatia): `ContextAssembler` (trace+logs+métricas→prompt) com `TokenBudget` (orçamento+truncamento) e `PromptSanitizer` (PII na fronteira); `assembleForTrace` + `assemble(sinais)`; `mvn -pl horus test` verde (11/11) | `task/T-602-context-assembler` | [#22](https://github.com/mclovin137/Horus/pull/22) | 🔄 Em revisão (PR) |
| 2026-06-28 | `T-601` | Camada de IA (ADR-0011, 1ª fatia): porta desacoplada `LlmEngine` + `ModelTier` (Haiku/Sonnet/Opus), `StubLlmEngine` (default, sem chave) e `LangChain4jLlmEngine` (`quarkus-langchain4j-anthropic` 1.1.0, flag de build), API `/horus/ai/*`; extensão validada sob Quarkus 3.37 + JDK 25, `mvn -pl horus test` verde (8/8) | `task/T-601-llm-anthropic-integration` | [#21](https://github.com/mclovin137/Horus/pull/21) | 🔄 Em revisão (PR) |
| 2026-06-27 | `T-501` | Serviço Horus (1ª fatia): camada de consulta aos backends — portas Trace/Log/Metric + adapters REST-client (Jaeger/Loki/Prometheus), API `GET /horus/query/*`, testes com portas mockadas; `mvn -pl horus test` verde (6/6) | `task/T-501-horus-query-adapters` | [#20](https://github.com/mclovin137/Horus/pull/20) | ✅ Entregue |
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
