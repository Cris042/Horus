# state.md — Estado do Projeto Horus

> **Fonte única de verdade do "onde estamos".** Este arquivo é atualizado **a cada entrega**.
> Regra registrada em [`docs/ROLES.md`](./docs/ROLES.md) (§ Responsabilidades de manutenção) e em [`CLAUDE.md`](./CLAUDE.md).
> Fluxo completo em [`docs/WORKFLOW.md`](./docs/WORKFLOW.md).

| Campo | Valor |
|---|---|
| **Última atualização** | 2026-06-27 |
| **Branch atual** | `main` |
| **Fase do roadmap** | Fase 0 — Fundação |
| **Task ativa** | `T-002` (planejada) — próxima do roadmap |

---

## 🟢 Última entrega

**Correção de CI — review por IA destravado ([PR #2](https://github.com/mclovin137/Horus/pull/2)).**
- `code-review.yml`: adicionado `id-token: write` (corrige o erro de OIDC do `claude-code-action@v1`) e um job `gate` que **pula** o review (status neutro) quando `ANTHROPIC_API_KEY` está ausente, em vez de falhar o PR.
- Validado no próprio PR #2: `gate` passou, `ai-review` pulado, estado `CLEAN`.

**Anterior — `T-001` — Estrutura de monorepo ([PR #1](https://github.com/mclovin137/Horus/pull/1), mergeado).** Diretórios por componente + READMEs, `Makefile`, `.editorconfig`, mapa no README; `pom.xml`/`src/` intocados (realocação em T-002).

## ▶️ Próxima ação

Iniciar **`T-002` — Bootstrap Quarkus do Horus** (branch `task/T-002-bootstrap-quarkus`): criar `docs/tasks/T-002/{PRD,PLAN}.md` a partir do template, substituir `src/main/java/org/example/Main.java` pelo bootstrap Quarkus e reorganizar o `pom.xml`, movendo o build para `horus/` (RNF-014). Atenção ao Java 25 + features de preview citadas em `CLAUDE.md`.

> ⚙️ **Pendência sua (não bloqueia o roadmap):** configurar a secret `ANTHROPIC_API_KEY` em **Settings → Secrets and variables → Actions** para o review por IA rodar de fato (hoje o `gate` o pula com status neutro).

---

## 📒 Log de entregas (mais recente primeiro)

| Data | Task | Entrega | Branch | PR | Status |
|---|---|---|---|---|---|
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
