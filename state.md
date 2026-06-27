# state.md — Estado do Projeto Horus

> **Fonte única de verdade do "onde estamos".** Este arquivo é atualizado **a cada entrega**.
> Regra registrada em [`docs/ROLES.md`](./docs/ROLES.md) (§ Responsabilidades de manutenção) e em [`CLAUDE.md`](./CLAUDE.md).
> Fluxo completo em [`docs/WORKFLOW.md`](./docs/WORKFLOW.md).

| Campo | Valor |
|---|---|
| **Última atualização** | 2026-06-27 |
| **Branch atual** | `task/T-005-telemetry-conventions` |
| **Fase do roadmap** | Fase 0 — Fundação |
| **Task ativa** | `T-005` — Contrato de telemetria (em revisão (PR)) |

---

## 🟢 Última entrega (mergeada)

**`T-004` — CI de build/test + Maven Wrapper (entregue via [PR #4](https://github.com/mclovin137/Horus/pull/4), mergeada em `main`).**
- **Maven Wrapper commitado** (`mvnw`, `mvnw.cmd`, `.mvn/wrapper/maven-wrapper.properties`) — variante `only-script` fixando **Maven 3.9.9**; build reproduzível sem `mvn` no host (prefira `./mvnw`).
- **`.github/workflows/ci.yml`**: job `build-java` (setup-java **25** + cache Maven + `./mvnw … verify` + upload de relatórios surefire), em push para `main`, em todo PR e via `workflow_dispatch`. **Sem segredos**.
- **Build validado local + em CI**: **Java 25.0.3 + Quarkus 3.20.0 → BUILD SUCCESS**, `Tests run: 2, Failures: 0`. Par **JDK 25 / Quarkus 3.20 confirmado**.

> 🔄 **Em revisão:** **`T-005`** — contrato de telemetria (`docs/telemetry/CONTRACT.md`): convenções de logging/spans, propagação W3C (HTTP **e** headers AMQP), sanitização de PII. Só-docs; guia a instrumentação OTel das fases seguintes.

## ▶️ Próxima ação

**`T-005`** (contrato de telemetria) está **em revisão** (PR `#<n>`). Ao mergear, resta da **Fase 0** apenas **`T-003`** (docker-compose de dev: Postgres ×3, RabbitMQ, Collector, Jaeger, Loki, Prometheus). Em seguida o caminho crítico abre **`T-101`** (scaffold dos 3 serviços Quarkus), já guiado pelo contrato de telemetria.

> ⚙️ **Pendência sua (não bloqueia o roadmap):** configurar a secret `ANTHROPIC_API_KEY` em **Settings → Secrets and variables → Actions** para o review por IA rodar de fato (o `gate` o pula com status neutro). O pipeline de **CI (build/test) não usa segredo** e roda sempre.

---

## 📒 Log de entregas (mais recente primeiro)

| Data | Task | Entrega | Branch | PR | Status |
|---|---|---|---|---|---|
| 2026-06-27 | `T-005` | Contrato de telemetria (`docs/telemetry/CONTRACT.md`): logging/spans, propagação HTTP+AMQP, métricas mínimas, PII | `task/T-005-telemetry-conventions` | `#<n>` | 🔄 Em revisão (PR) |
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
