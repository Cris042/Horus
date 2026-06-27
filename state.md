# state.md — Estado do Projeto Horus

> **Fonte única de verdade do "onde estamos".** Este arquivo é atualizado **a cada entrega**.
> Regra registrada em [`docs/ROLES.md`](./docs/ROLES.md) (§ Responsabilidades de manutenção) e em [`CLAUDE.md`](./CLAUDE.md).
> Fluxo completo em [`docs/WORKFLOW.md`](./docs/WORKFLOW.md).

| Campo | Valor |
|---|---|
| **Última atualização** | 2026-06-27 |
| **Branch atual** | `task/T-002-bootstrap-quarkus` (em revisão — [PR #3](https://github.com/mclovin137/Horus/pull/3)) |
| **Fase do roadmap** | Fase 0 — Fundação |
| **Task ativa** | `T-002` (em revisão — PR) |

---

## 🟢 Última entrega

**`T-002` — Bootstrap Quarkus do Horus (em revisão via PR).**
- `pom.xml` raiz convertido em **parent/aggregator** (`packaging=pom`): Java 25, BOM do Quarkus 3.20 em `dependencyManagement`, módulo `horus`.
- `horus/` agora é um **app Quarkus**: extensões de bootstrap (`quarkus-rest`, `rest-jackson`, `smallrye-health`, `arc`), endpoint `GET /horus/info`, health em `/q/health` e teste de fumaça (`@QuarkusTest`).
- `Main.java` placeholder **removido** (e `src/` raiz eliminado); features de preview do Java 25 abandonadas (Quarkus gerencia o entry point).
- Docs alinhadas: `CLAUDE.md`, `README.md`, `horus/README.md`, `lib.md`.

> ⚠️ **Não verificável localmente:** sem `mvn` e sem rede neste ambiente, o build real (`mvn package` / `quarkus:dev`) não rodou — POMs validados por well-formedness/estrutura. **T-004 (CI) fechará essa lacuna** ao compilar o módulo em pipeline.

## ▶️ Próxima ação

Concluir a **Fase 0**. Recomendado iniciar por **`T-004` — CI: build/test por componente** (branch `task/T-004-ci-build`), pois compila o módulo `horus` em pipeline e **valida o bootstrap de T-002** (que não pôde ser buildado localmente). Depois: **`T-003`** (docker-compose de dev) e **`T-005`** (convenções de logging/spans). O caminho crítico segue para **`T-101`** (scaffold dos 3 serviços Quarkus).

> ⚙️ **Pendência sua (não bloqueia o roadmap):** configurar a secret `ANTHROPIC_API_KEY` em **Settings → Secrets and variables → Actions** para o review por IA rodar de fato (hoje o `gate` o pula com status neutro).

---

## 📒 Log de entregas (mais recente primeiro)

| Data | Task | Entrega | Branch | PR | Status |
|---|---|---|---|---|---|
| 2026-06-27 | `T-002` | Bootstrap Quarkus do Horus (parent/aggregator + módulo `horus` REST/health; remove `Main.java`) | `task/T-002-bootstrap-quarkus` | [#3](https://github.com/mclovin137/Horus/pull/3) | 🟡 Em revisão (PR) |
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
