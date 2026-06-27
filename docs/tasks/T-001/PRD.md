# PRD — T-001: Estrutura de monorepo

| Campo | Valor |
|---|---|
| **Task** | `T-001` |
| **Fase do roadmap** | Fase 0 — Fundação |
| **Status** | `Planejada` |
| **Branch** | `task/T-001-estrutura-monorepo` |
| **PR** | [`#1`](https://github.com/mclovin137/Horus/pull/1) |
| **Depende de** | — |
| **Requisitos atendidos** | base para RF-032 / RNF-014 |
| **ADRs relacionados** | ADR-0001, ADR-0012 |
| **Data** | 2026-06-26 |

## Objetivo

Estabelecer o esqueleto de diretórios do monorepo, com um lugar claro para cada componente (serviços Quarkus, worker Rust, API de carga Python, plataforma Horus e artefatos de deploy), de modo que as próximas tasks tenham onde aterrissar o código.

## Escopo (o que entra)

- Diretórios-raiz por componente: `services/{prontuario,payment,invoice}`, `worker/`, `loadtest/`, `horus/`, `deploy/`.
- `README.md` por componente explicando seu papel.
- `README.md` raiz do projeto (mapa do repositório, apontando para `docs/`, `state.md`, `lib.md`).
- `Makefile` com alvos de conveniência (placeholders: `up`, `down`, `build`).
- `.editorconfig` para padronização básica de estilo.

## Fora do escopo

- Bootstrap do Quarkus do Horus e reorganização do `pom.xml`/`src/` atuais → **T-002**.
- `docker-compose` real de desenvolvimento → **T-003** (aqui só o diretório/placeholder).
- Qualquer código de domínio.

## Premissas e dependências

- O `pom.xml` e `src/main/java/org/example/Main.java` atuais permanecem **intocados** nesta task; serão realocados para `horus/` em T-002.
- Versionamento git inicializado e branch criada conforme `docs/WORKFLOW.md`.

## Critérios de aceite

- [ ] Todos os diretórios de componente existem e contêm um `README.md` descrevendo seu papel.
- [ ] `README.md` raiz descreve o repositório e linka `docs/`, `state.md` e `lib.md`.
- [ ] `Makefile` existe com os alvos `up`, `down`, `build` (ainda que placeholders).
- [ ] `PLAN.md` desta task está 100% ✅ e `state.md` atualizado.

## Riscos

| Risco | Mitigação |
|---|---|
| Conflito futuro entre o `pom.xml` raiz e a estrutura `horus/` | Realocação tratada explicitamente em T-002; não mover nada agora |

## Referências

- [`../../PRD.md`](../../PRD.md), [`../../ROADMAP.md`](../../ROADMAP.md)
- [`./PLAN.md`](./PLAN.md)
- ADR-0001 (microsserviços), ADR-0012 (Docker/K8s)
