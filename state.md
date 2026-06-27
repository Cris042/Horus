# state.md — Estado do Projeto Horus

> **Fonte única de verdade do "onde estamos".** Este arquivo é atualizado **a cada entrega**.
> Regra registrada em [`docs/ROLES.md`](./docs/ROLES.md) (§ Responsabilidades de manutenção) e em [`CLAUDE.md`](./CLAUDE.md).
> Fluxo completo em [`docs/WORKFLOW.md`](./docs/WORKFLOW.md).

| Campo | Valor |
|---|---|
| **Última atualização** | 2026-06-27 |
| **Branch atual** | `task/T-004-ci-build` |
| **Fase do roadmap** | Fase 0 — Fundação |
| **Task ativa** | `T-004` — CI build/test + Maven Wrapper (em revisão (PR)) |

---

## 🟢 Última entrega

**`T-004` — CI de build/test + Maven Wrapper (em revisão via PR [#4](https://github.com/mclovin137/Horus/pull/4), branch `task/T-004-ci-build`).**
- **Maven Wrapper commitado** (`mvnw`, `mvnw.cmd`, `.mvn/wrapper/maven-wrapper.properties`) — variante `only-script` fixando **Maven 3.9.9**; build reproduzível sem `mvn` no host (prefira `./mvnw`).
- **`.github/workflows/ci.yml`**: job `build-java` (setup-java **25** + cache Maven + `./mvnw … verify` + upload de relatórios surefire), disparando em push para `main`, em todo PR e via `workflow_dispatch`. **Sem segredos** — roda sempre.
- **Build validado localmente** (fecha a lacuna de T-002): **Java 25.0.3 + Quarkus 3.20.0 → BUILD SUCCESS**, `Tests run: 2, Failures: 0`. O par **JDK 25 / Quarkus 3.20 está confirmado** — o fallback Java 21 **não** foi necessário.
- Docs alinhadas: `CLAUDE.md` (build via `./mvnw`), `lib.md` (wrapper adicionado), `README.md` (badge de CI).

## ▶️ Próxima ação

Acompanhar a **CI do PR de T-004** e **mergear** quando verde (Actions/badge confirmam o build em pipeline). Em seguida, concluir a **Fase 0**: **`T-003`** (docker-compose de dev) e **`T-005`** (convenções de logging/spans). O caminho crítico segue para **`T-101`** (scaffold dos 3 serviços Quarkus).

> ⚙️ **Pendência sua (não bloqueia o roadmap):** configurar a secret `ANTHROPIC_API_KEY` em **Settings → Secrets and variables → Actions** para o review por IA rodar de fato (o `gate` o pula com status neutro). O pipeline de **CI (build/test) não usa segredo** e roda sempre.

---

## 📒 Log de entregas (mais recente primeiro)

| Data | Task | Entrega | Branch | PR | Status |
|---|---|---|---|---|---|
| 2026-06-27 | `T-004` | CI de build/test (`ci.yml`) + **Maven Wrapper** (3.9.9); build validado localmente (Java 25 + Quarkus 3.20 → SUCCESS, testes verdes) | `task/T-004-ci-build` | [#4](https://github.com/mclovin137/Horus/pull/4) | 🔄 Em revisão (PR) |
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
