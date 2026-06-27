# state.md — Estado do Projeto Horus

> **Fonte única de verdade do "onde estamos".** Este arquivo é atualizado **a cada entrega**.
> Regra registrada em [`docs/ROLES.md`](./docs/ROLES.md) (§ Responsabilidades de manutenção) e em [`CLAUDE.md`](./CLAUDE.md).
> Fluxo completo em [`docs/WORKFLOW.md`](./docs/WORKFLOW.md).

| Campo | Valor |
|---|---|
| **Última atualização** | 2026-06-27 |
| **Branch atual** | `task/T-001-estrutura-monorepo` (em revisão — PR) |
| **Fase do roadmap** | Fase 0 — Fundação |
| **Task ativa** | `T-001` (em revisão — PR) |

---

## 🟢 Última entrega

**`T-001` — Estrutura de monorepo.**
- Diretórios-raiz por componente criados, cada um com `README.md` (papel, stack, requisitos e tasks relacionadas): `services/{prontuario,payment,invoice}`, `worker/`, `loadtest/`, `horus/`, `deploy/`.
- `Makefile` com alvos placeholder `up`/`down`/`build` (+ `help`) e `.editorconfig` para padronização de estilo.
- `README.md` raiz augmentado com a seção *Estrutura do repositório* (mapa de diretórios + nota de migração do scaffold para `horus/` em T-002).
- `pom.xml` e `src/` raiz **intocados** (serão realocados em T-002).

## ▶️ Próxima ação

Revisar e **mergear o PR de `T-001`**. Em seguida iniciar **`T-002` — Bootstrap Quarkus do Horus** (substituir `src/main/java/org/example/Main.java` e reorganizar o `pom.xml`, movendo o build para `horus/`), na branch `task/T-002-bootstrap-quarkus`.
*Pré-requisito para os workflows de IA:* configurar o secret `ANTHROPIC_API_KEY` (e, para as rotinas de logs/banco, `LOKI_URL` / `DB_DSNS`) em **Settings → Secrets → Actions**.

---

## 📒 Log de entregas (mais recente primeiro)

| Data | Task | Entrega | Branch | PR | Status |
|---|---|---|---|---|---|
| 2026-06-27 | `T-001` | Estrutura de monorepo (diretórios + READMEs por componente, `Makefile`, `.editorconfig`, mapa no README) | `task/T-001-estrutura-monorepo` | _(a abrir)_ | 🟡 Em revisão (PR) |
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
