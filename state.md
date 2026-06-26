# state.md — Estado do Projeto Horus

> **Fonte única de verdade do "onde estamos".** Este arquivo é atualizado **a cada entrega**.
> Regra registrada em [`docs/ROLES.md`](./docs/ROLES.md) (§ Responsabilidades de manutenção) e em [`CLAUDE.md`](./CLAUDE.md).
> Fluxo completo em [`docs/WORKFLOW.md`](./docs/WORKFLOW.md).

| Campo | Valor |
|---|---|
| **Última atualização** | 2026-06-26 |
| **Branch atual** | `main` (publicado no GitHub) |
| **Fase do roadmap** | Fase 0 — Fundação |
| **Task ativa** | `T-001` (planejada — PRD e plano prontos) |

---

## 🟢 Última entrega

**Automações de CI/CD + IA, README e adoção de SAGA.**
- **CI/CD**: review automatizado por IA em cada PR (`.github/workflows/code-review.yml` + rubrica) e **3 rotinas diárias** (auditoria de dependências→PR, análise de logs, análise de banco/queries). Skills instaladas via `skillfish`.
- **README.md** com visão geral, fluxo de desenvolvimento, skills e estratégias.
- **Decisão de SAGA revertida**: `ADR-0006` substituído por `ADR-0013` (**adotar SAGA desde o início**, orquestração via MicroProfile LRA), pois o foco é observabilidade. Cascateado em PRD, ROADMAP (T-107/T-507), lib.md, README e rubrica de review.
- **Projeto publicado no Git/GitHub.**

## ▶️ Próxima ação

Executar **`T-001` — Estrutura de monorepo** na branch `task/T-001-estrutura-monorepo`, seguindo [`docs/tasks/T-001/PLAN.md`](./docs/tasks/T-001/PLAN.md), e abrir PR ao final.
*Pré-requisito para os workflows de IA:* configurar o secret `ANTHROPIC_API_KEY` (e, para as rotinas de logs/banco, `LOKI_URL` / `DB_DSNS`) em **Settings → Secrets → Actions**.

---

## 📒 Log de entregas (mais recente primeiro)

| Data | Task | Entrega | Branch | PR | Status |
|---|---|---|---|---|---|
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
