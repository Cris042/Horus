# Plano de Execução — T-001: Estrutura de monorepo

> ⚠️ **Regra R2 ([WORKFLOW](../../WORKFLOW.md)).** Sempre que **criar ou modificar** um arquivo desta tabela, atualize **na mesma hora** o seu `Status` e `Atualizado`, e registre em *Registro de alterações*.

| Campo | Valor |
|---|---|
| **Task** | `T-001` |
| **Branch** | `task/T-001-estrutura-monorepo` |
| **PRD** | [`./PRD.md`](./PRD.md) |
| **Progresso** | `0/11` arquivos |

## Arquivos a criar/modificar

| # | Arquivo | Ação | Propósito | Status | Atualizado |
|---|---|---|---|---|---|
| 1 | `README.md` | Criar | Mapa do repositório; links para `docs/`, `state.md`, `lib.md` | ⬜ Pendente | — |
| 2 | `Makefile` | Criar | Alvos de conveniência `up`/`down`/`build` (placeholders) | ⬜ Pendente | — |
| 3 | `.editorconfig` | Criar | Padronização de estilo (charset, indentação, EOL) | ⬜ Pendente | — |
| 4 | `services/README.md` | Criar | Papel da camada de serviços de domínio (Quarkus) | ⬜ Pendente | — |
| 5 | `services/prontuario/README.md` | Criar | Escopo do Prontuário Service | ⬜ Pendente | — |
| 6 | `services/payment/README.md` | Criar | Escopo do Payment Service | ⬜ Pendente | — |
| 7 | `services/invoice/README.md` | Criar | Escopo do Invoice Service | ⬜ Pendente | — |
| 8 | `worker/README.md` | Criar | Papel do Report & Email Worker (Rust) | ⬜ Pendente | — |
| 9 | `loadtest/README.md` | Criar | Papel da API de carga (FastAPI + Locust) | ⬜ Pendente | — |
| 10 | `horus/README.md` | Criar | Papel da plataforma Horus (destino do código em T-002) | ⬜ Pendente | — |
| 11 | `deploy/README.md` | Criar | Onde ficarão `docker-compose` (T-003) e manifests K8s (Fase 8) | ⬜ Pendente | — |

**Legenda:** ⬜ Pendente · 🟡 Em progresso · ✅ Concluído

## Passos de implementação

1. Criar a branch `task/T-001-estrutura-monorepo` a partir de `main`.
2. Criar os diretórios e os `README.md` de cada componente (itens 4-11).
3. Criar `README.md` raiz, `Makefile` e `.editorconfig` (itens 1-3).
4. Marcar cada item como ✅ neste plano ao criá-lo.
5. Atualizar `state.md` e abrir o PR `T-001: estrutura de monorepo`.

## Verificação / testes

- [ ] `find . -maxdepth 2 -name README.md` lista os READMEs esperados.
- [ ] `make build` executa sem erro (mesmo que no-op nos placeholders).
- [ ] `pom.xml` e `src/` raiz permanecem inalterados.

## Registro de alterações

| Data | Arquivo | Mudança |
|---|---|---|
| — | — | (ainda não iniciado) |
