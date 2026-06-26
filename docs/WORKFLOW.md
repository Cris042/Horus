# WORKFLOW — Fluxo de Trabalho do Projeto

| Campo | Valor |
|---|---|
| **Versão** | 1.0 |
| **Data** | 2026-06-26 |
| **Relacionado** | [`../state.md`](../state.md) · [`ROADMAP.md`](./ROADMAP.md) · [`tasks/`](./tasks/) |

Este documento define **como o trabalho flui** no projeto Horus. É de cumprimento obrigatório para qualquer pessoa ou agente (inclusive o Claude Code) que execute tarefas no repositório.

## O fluxo

```
ROADMAP  ──►  TASK  ──►  PRD da task  ──►  PLANO DE EXECUÇÃO  ──►  branch  ──►  PR  ──►  merge
(visão     (item     (objetivo e        (arquivo por arquivo)   (task/...)         (atualiza
 macro)     T-xxx)    critérios)                                                    state.md)
```

1. **ROADMAP** ([`ROADMAP.md`](./ROADMAP.md)) — lista macro das tasks `T-xxx`, em fases, com dependências e rastreabilidade aos requisitos do [`PRD.md`](./PRD.md).
2. **TASK** — uma unidade de trabalho do roadmap. Ao iniciá-la, cria-se a pasta `docs/tasks/T-xxx/`.
3. **PRD da task** (`docs/tasks/T-xxx/PRD.md`) — objetivo, escopo, requisitos atendidos e critérios de aceite **daquela task** (derivado do `_TEMPLATE`).
4. **PLANO DE EXECUÇÃO** (`docs/tasks/T-xxx/PLAN.md`) — o **plano detalhado arquivo por arquivo**: cada arquivo a criar/modificar, seu propósito e seu status.
5. **BRANCH** — toda task é desenvolvida em **branch própria**.
6. **PR** — a entrega ocorre por **Pull Request**; o merge fecha a task.
7. **state.md** — atualizado **a cada entrega**.

## Passo a passo para executar uma task

1. **Selecionar** a próxima task no `ROADMAP.md` (respeitando dependências e o caminho crítico).
2. **Criar a branch:** `git checkout -b task/T-xxx-<slug-curto>` (a partir de `main` atualizado).
3. **Criar o PRD da task** copiando `docs/tasks/_TEMPLATE/PRD.md` para `docs/tasks/T-xxx/PRD.md` e preenchendo.
4. **Criar o plano de execução** copiando `docs/tasks/_TEMPLATE/PLAN.md` para `docs/tasks/T-xxx/PLAN.md`, listando **cada arquivo** a criar/modificar.
5. **Implementar**, marcando cada arquivo no `PLAN.md` como concluído **assim que o criar/modificar** (ver regra abaixo).
6. **Validar** os critérios de aceite do PRD da task.
7. **Abrir o PR:** `gh pr create` com título `T-xxx: <descrição>` e corpo referenciando o PRD/plano da task.
8. **Merge** após revisão.
9. **Atualizar o `state.md`**: última entrega, próxima ação, log.

## Regras obrigatórias de atualização

> ⚠️ Estas duas regras são a essência do sistema de governança.

- **R1 — `state.md` a cada entrega.** Sempre que uma task é concluída (ou um marco relevante é entregue), atualize `state.md` (seção *Última entrega*, *Próxima ação* e *Log de entregas*).
- **R2 — `PLAN.md` a cada arquivo.** Sempre que **criar ou modificar** um arquivo que conste no `PLAN.md` da task, atualize **na mesma hora** o status daquele arquivo (`⬜ → ✅`), a data e o *Registro de alterações* do plano. Se um arquivo novo (não previsto) precisar ser tocado, **adicione-o** ao plano antes/junto da mudança.

## Convenção de branches

| Tipo | Padrão | Exemplo |
|---|---|---|
| Task | `task/T-xxx-<slug>` | `task/T-001-estrutura-monorepo` |
| Correção pontual | `fix/<slug>` | `fix/otel-context-rabbitmq` |
| Documentação | `docs/<slug>` | `docs/atualiza-roadmap` |

- Base: `main`. `main` permanece sempre integrável.
- Uma task = uma branch = um PR.

## Convenção de PR

- **Título:** `T-xxx: <descrição curta>`.
- **Corpo:** link para `docs/tasks/T-xxx/PRD.md` e `PLAN.md`; checklist dos critérios de aceite; requisitos atendidos (`RF-*`/`RNF-*`).
- **Definition of Done:** plano 100% ✅, critérios de aceite do PRD satisfeitos, `state.md` atualizado no PR.

## Definição de "entrega"

Uma **entrega** é qualquer uma destas, e cada uma dispara a regra **R1**:
- Uma task concluída e mergeada.
- Um marco do roadmap atingido.
- Uma mudança estrutural relevante (ex.: este próprio bootstrap de governança).
