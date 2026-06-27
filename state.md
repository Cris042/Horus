# state.md — Estado do Projeto Horus

> **Fonte única de verdade do "onde estamos".** Este arquivo é atualizado **a cada entrega**.
> Regra registrada em [`docs/ROLES.md`](./docs/ROLES.md) (§ Responsabilidades de manutenção) e em [`CLAUDE.md`](./CLAUDE.md).
> Fluxo completo em [`docs/WORKFLOW.md`](./docs/WORKFLOW.md).

| Campo | Valor |
|---|---|
| **Última atualização** | 2026-06-27 |
| **Branch atual** | `task/T-302-saga-report-publication` |
| **Fase do roadmap** | Fase 3 — Mensageria e worker |
| **Task ativa** | `T-302` — publicação de relatório pelos serviços (em revisão (PR)) |

---

## 🟢 Última entrega (mergeada)

**`T-301` — RabbitMQ + contrato da mensagem de relatório (entregue via [PR #15](https://github.com/mclovin137/Horus/pull/15), mergeada em `main`).** Contrato `RelatorioMensagem` + publisher no invoice + doc (RF-021/RNF-011). **CI verde.**

> 🔄 **Em revisão:** **`T-302`** — publicação de relatório **por fluxo** (RF-021): ao concluir a SAGA `pagar→emitir NF` (T-107), o `saga-orchestrator` solicita o relatório da NF (`POST /notas/{id}/relatorio`) como passo final **best-effort** (falha não compensa). Fluxo `pagar → emitir → solicitar relatório` no mesmo `trace_id`.

## ▶️ Próxima ação

Fase 3: **`T-303`** (worker Rust: consumir/gerar relatório/e-mail — RF-022..024, componente novo "G"). Caminho crítico de telemetria: **`T-404`** (Collector/backends — config aprofundada) e **`T-405`** (correlação ponta a ponta por `trace_id`). Restam ainda **`T-402`/`T-403`** (FastAPI/LB e worker Rust) e **`T-507`** (visualização da SAGA — RF-H-016).

> ⚠️ **Nota técnica (vale p/ T-103/104):** entidades Panache geram id via **sequência `<tabela>_seq`** (PooledLo, INCREMENT 50) — as migrações Flyway devem criar a sequência (não usar coluna IDENTITY), senão `INSERT` falha em `nextval`.

> ⚙️ **Pendência sua (não bloqueia o roadmap):** configurar a secret `ANTHROPIC_API_KEY` em **Settings → Secrets and variables → Actions** para o review por IA rodar de fato (o `gate` o pula com status neutro). O pipeline de **CI (build/test) não usa segredo** e roda sempre.

---

## 📒 Log de entregas (mais recente primeiro)

| Data | Task | Entrega | Branch | PR | Status |
|---|---|---|---|---|---|
| 2026-06-27 | `T-302` | Publicação de relatório por fluxo (RF-021): SAGA solicita o relatório da NF após conclusão (best-effort); `InvoiceClient.solicitarRelatorio`, teste atualizado | `task/T-302-saga-report-publication` | `#<n>` | 🔄 Em revisão (PR) |
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
