# Plano de Execução — T-801: Imagens Docker de todos os executáveis

> ⚠️ **Regra R2 ([WORKFLOW](../../WORKFLOW.md)).** Sempre que **criar ou modificar** um arquivo desta tabela, atualize **na mesma hora** o seu `Status` e `Atualizado`, e registre a mudança em *Registro de alterações*.

| Campo | Valor |
|---|---|
| **Task** | `T-801` |
| **Branch** | `task/T-801-docker-images` |
| **PRD** | [`./PRD.md`](./PRD.md) |
| **Progresso** | `13/13` arquivos |

## Arquivos a criar/modificar

| # | Arquivo | Ação | Propósito | Status | Atualizado |
|---|---|---|---|---|---|
| 1 | `docs/tasks/T-801/PRD.md` | Criar | PRD da task | ✅ Concluído | 2026-06-30 |
| 2 | `docs/tasks/T-801/PLAN.md` | Criar | Plano arquivo por arquivo | ✅ Concluído | 2026-06-30 |
| 3 | `horus/src/main/docker/Dockerfile.jvm` | Criar | Imagem JVM do Horus | ✅ Concluído | 2026-06-30 |
| 4 | `services/prontuario/src/main/docker/Dockerfile.jvm` | Criar | Imagem JVM do Prontuário | ✅ Concluído | 2026-06-30 |
| 5 | `services/payment/src/main/docker/Dockerfile.jvm` | Criar | Imagem JVM do Payment | ✅ Concluído | 2026-06-30 |
| 6 | `services/invoice/src/main/docker/Dockerfile.jvm` | Criar | Imagem JVM do Invoice | ✅ Concluído | 2026-06-30 |
| 7 | `services/saga-orchestrator/src/main/docker/Dockerfile.jvm` | Criar | Imagem JVM do SAGA | ✅ Concluído | 2026-06-30 |
| 8 | `worker/Dockerfile` | Criar | Imagem do worker Rust | ✅ Concluído | 2026-06-30 |
| 9 | `loadtest/Dockerfile` | Criar | Imagem da API de carga (FastAPI) | ✅ Concluído | 2026-06-30 |
| 10 | `.dockerignore` | Criar | Exclusões do contexto (raiz) | ✅ Concluído | 2026-06-30 |
| 11 | `worker/.dockerignore`, `loadtest/.dockerignore` | Criar | Exclusões dos contextos worker/loadtest | ✅ Concluído | 2026-06-30 |
| 12 | `Makefile` | Modificar | Alvos `docker-images`/`docker-*` | ✅ Concluído | 2026-06-30 |
| 13 | `deploy/README.md` | Modificar | Seção de build de imagens | ✅ Concluído | 2026-06-30 |

> `state.md` atualizado ao final da entrega.

## Verificação / testes

- [x] `docker build` da imagem **loadtest** — ver registro.
- [x] `docker build` da imagem **worker** — ver registro.
- [x] `docker build` da imagem **horus** (Quarkus, representa as 5) — ver registro.

## Registro de alterações

| Data | Arquivo | Mudança |
|---|---|---|
| `2026-06-30` | `docs/tasks/T-801/*` | PRD e plano criados |
| `2026-06-30` | `**/Dockerfile.jvm`, `worker/Dockerfile`, `loadtest/Dockerfile` | Dockerfiles dos 7 executáveis |
| `2026-06-30` | `.dockerignore`, `worker/.dockerignore`, `loadtest/.dockerignore` | Exclusões de contexto |
| `2026-06-30` | `Makefile` | Alvos de build de imagens |
| `2026-06-30` | `deploy/README.md` | Seção de build de imagens |
| `2026-06-30` | `state.md` | Estado alinhado ao progresso de T-801 |
