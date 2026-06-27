# Plano de Execução — T-001: Estrutura de monorepo

> ⚠️ **Regra R2 ([WORKFLOW](../../WORKFLOW.md)).** Sempre que **criar ou modificar** um arquivo desta tabela, atualize **na mesma hora** o seu `Status` e `Atualizado`, e registre em *Registro de alterações*.

| Campo | Valor |
|---|---|
| **Task** | `T-001` |
| **Branch** | `task/T-001-estrutura-monorepo` |
| **PRD** | [`./PRD.md`](./PRD.md) |
| **Progresso** | `11/11` arquivos |

## Arquivos a criar/modificar

| # | Arquivo | Ação | Propósito | Status | Atualizado |
|---|---|---|---|---|---|
| 1 | `README.md` | Atualizar | Mapa do repositório; links para `docs/`, `state.md`, `lib.md` | ✅ Concluído | 2026-06-27 |
| 2 | `Makefile` | Criar | Alvos de conveniência `up`/`down`/`build` (placeholders) | ✅ Concluído | 2026-06-27 |
| 3 | `.editorconfig` | Criar | Padronização de estilo (charset, indentação, EOL) | ✅ Concluído | 2026-06-27 |
| 4 | `services/README.md` | Criar | Papel da camada de serviços de domínio (Quarkus) | ✅ Concluído | 2026-06-27 |
| 5 | `services/prontuario/README.md` | Criar | Escopo do Prontuário Service | ✅ Concluído | 2026-06-27 |
| 6 | `services/payment/README.md` | Criar | Escopo do Payment Service | ✅ Concluído | 2026-06-27 |
| 7 | `services/invoice/README.md` | Criar | Escopo do Invoice Service | ✅ Concluído | 2026-06-27 |
| 8 | `worker/README.md` | Criar | Papel do Report & Email Worker (Rust) | ✅ Concluído | 2026-06-27 |
| 9 | `loadtest/README.md` | Criar | Papel da API de carga (FastAPI + Locust) | ✅ Concluído | 2026-06-27 |
| 10 | `horus/README.md` | Criar | Papel da plataforma Horus (destino do código em T-002) | ✅ Concluído | 2026-06-27 |
| 11 | `deploy/README.md` | Criar | Onde ficarão `docker-compose` (T-003) e manifests K8s (Fase 8) | ✅ Concluído | 2026-06-27 |

**Legenda:** ⬜ Pendente · 🟡 Em progresso · ✅ Concluído

> **Nota sobre o item 1:** o `README.md` raiz já existia (visão geral/fluxo/skills); a ação foi **augmentar** (não recriar), adicionando a seção *Estrutura do repositório* com o mapa de diretórios e os links exigidos pelo critério de aceite. Por isso a *Ação* foi ajustada de `Criar` para `Atualizar`.

## Passos de implementação

1. ✅ Criar a branch `task/T-001-estrutura-monorepo` a partir de `main`.
2. ✅ Criar os diretórios e os `README.md` de cada componente (itens 4-11).
3. ✅ Criar `Makefile` e `.editorconfig` e augmentar o `README.md` raiz (itens 1-3).
4. ✅ Marcar cada item como ✅ neste plano.
5. ⬜ Atualizar `state.md` e abrir o PR `T-001: estrutura de monorepo`.

## Verificação / testes

- [x] `find` lista todos os READMEs esperados — componentes em profundidade 2-3 (`./services/README.md` e `./services/{prontuario,payment,invoice}/README.md`, além de `worker/`, `loadtest/`, `horus/`, `deploy/`).
- [ ] `make build` executa sem erro — **não verificável neste ambiente:** `make` não está instalado. Makefile validado por **inspeção** (receitas com `\t`, alvos `up`/`down`/`build` + `help` default).
- [x] `pom.xml` e `src/` raiz permanecem inalterados (`git diff main -- pom.xml src/` vazio).

## Registro de alterações

| Data | Arquivo | Mudança |
|---|---|---|
| 2026-06-27 | `services/`, `worker/`, `loadtest/`, `horus/`, `deploy/` (8 `README.md`) | Criados os diretórios de componente e seus READMEs (papel, stack, requisitos e tasks relacionadas). |
| 2026-06-27 | `Makefile` | Criado com alvos placeholder `up`/`down`/`build` + `help` (default goal). |
| 2026-06-27 | `.editorconfig` | Criado (charset utf-8, LF, indentação por tipo de arquivo, tab em Makefile). |
| 2026-06-27 | `README.md` | Augmentado com a seção *Estrutura do repositório* (mapa de diretórios + nota de migração para `horus/` em T-002). |
