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

**`T-001` — Estrutura de monorepo (entregue via [PR #1](https://github.com/mclovin137/Horus/pull/1), mergeado em `main`).**
- Diretórios-raiz por componente, cada um com `README.md` (papel, stack, requisitos, tasks): `services/{prontuario,payment,invoice}`, `worker/`, `loadtest/`, `horus/`, `deploy/`.
- `Makefile` (alvos placeholder `up`/`down`/`build` + `help`) e `.editorconfig`.
- `README.md` raiz augmentado com *Estrutura do repositório* (mapa + nota de migração para `horus/` em T-002).
- `pom.xml` e `src/` raiz **intocados** (realocação fica para T-002).

> ⚠️ **Descoberta durante o PR #1:** o workflow `code-review.yml` (review por IA) **falhou** por dois motivos de infraestrutura — (1) secret `ANTHROPIC_API_KEY` vazia e (2) faltava `id-token: write` nas permissões (erro de OIDC do `claude-code-action`). Nenhum achado sobre o conteúdo de T-001. Por isso o PR #1 foi mergeado sob a política **auto-merge de baixo risco** (docs/infra).

## ▶️ Próxima ação

1. **Correção de CI** (branch `fix/ai-review-oidc-permission`): adicionar `id-token: write` às permissões de `code-review.yml` para destravar o review por IA em **todos** os PRs futuros. *(A pendência do secret `ANTHROPIC_API_KEY` permanece: precisa ser configurada por você em **Settings → Secrets → Actions**; sem ela o review é pulado/limitado.)*
2. Iniciar **`T-002` — Bootstrap Quarkus do Horus** (branch `task/T-002-bootstrap-quarkus`): substituir `src/main/java/org/example/Main.java` e reorganizar o `pom.xml`, movendo o build para `horus/` (RNF-014).

---

## 📒 Log de entregas (mais recente primeiro)

| Data | Task | Entrega | Branch | PR | Status |
|---|---|---|---|---|---|
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
