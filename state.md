# state.md — Estado do Projeto Horus

> **Fonte única de verdade do "onde estamos".** Este arquivo é atualizado **a cada entrega**.
> Regra registrada em [`docs/ROLES.md`](./docs/ROLES.md) (§ Responsabilidades de manutenção) e em [`CLAUDE.md`](./CLAUDE.md).
> Fluxo completo em [`docs/WORKFLOW.md`](./docs/WORKFLOW.md).

| Campo | Valor |
|---|---|
| **Última atualização** | 2026-06-27 |
| **Branch atual** | `task/T-102-prontuario-domain` |
| **Fase do roadmap** | Fase 1 — Microsserviços de domínio |
| **Task ativa** | `T-102` — Prontuário Service (domínio) (em revisão (PR)) |

---

## 🟢 Última entrega (mergeada)

**`T-101` — scaffold Quarkus dos 3 serviços de domínio (entregue via [PR #7](https://github.com/mclovin137/Horus/pull/7), mergeada em `main`).**
- Módulos `services/{prontuario,payment,invoice}`: **REST + Hibernate Panache + Flyway**, banco dedicado por serviço, `service.name` canônico (contrato T-005), porta própria, migração `V1`, entidade Panache mínima, endpoint `/info` e teste de fumaça `@QuarkusTest` (Dev Services PostgreSQL).
- **Quarkus 3.20.0 → 3.37.0** no parent (Panache não lê bytecode JDK 25 no 3.20). **CI verde**.

> 🔄 **Em revisão:** **`T-102`** — domínio do Prontuário Service: criar/consultar prontuário (RF-006/007), registrar/atualizar/finalizar consulta (RF-008..010), persistindo só em `prontuario_db` (RF-025). Entidade `Consulta`+enum, migração `V2`, serviços transacionais (imutabilidade pós-finalização), REST+DTOs+validação (404/409/400) e teste de fluxo.

## ▶️ Próxima ação

**`T-102`** está **em revisão** (PR); build de produção verde. Depois: **`T-103`** (Payment — carteira/saldo, movimentações, aprovar/rejeitar/estornar) e **`T-104`** (Invoice — emitir NF simulada, listar, reprocessar), ambos sobre o scaffold. Em seguida **`T-105`** (migrações Flyway consolidadas), **`T-107`** (SAGA) e **`T-401`** (instrumentação OTel HTTP+JDBC).

> ⚙️ **Pendência sua (não bloqueia o roadmap):** configurar a secret `ANTHROPIC_API_KEY` em **Settings → Secrets and variables → Actions** para o review por IA rodar de fato (o `gate` o pula com status neutro). O pipeline de **CI (build/test) não usa segredo** e roda sempre.

---

## 📒 Log de entregas (mais recente primeiro)

| Data | Task | Entrega | Branch | PR | Status |
|---|---|---|---|---|---|
| 2026-06-27 | `T-102` | Prontuário Service (domínio): criar/consultar prontuário, registrar/atualizar/finalizar consulta (RF-006..010); entidade `Consulta`, migração `V2`, REST+validação, teste de fluxo | `task/T-102-prontuario-domain` | [#8](https://github.com/mclovin137/Horus/pull/8) | 🔄 Em revisão (PR) |
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
