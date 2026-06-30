# PRD — T-801: Imagens Docker de todos os executáveis

| Campo | Valor |
|---|---|
| **Task** | `T-801` |
| **Fase** | Fase 8 — Containerização e Kubernetes |
| **Requisitos** | RF-032, RNF-004 |
| **Dependências** | Fases 1-7 (componentes existentes) |
| **Branch** | `task/T-801-docker-images` |

## Objetivo

Empacotar **todos os executáveis** do Horus em imagens Docker reproduzíveis: os 5
módulos Quarkus (Horus + Prontuário + Payment + Invoice + SAGA), o worker Rust e a
API de carga (FastAPI). Base para o compose de aplicações e os manifests K8s (T-802).

## Escopo

- **Quarkus (×5):** `src/main/docker/Dockerfile.jvm` em cada módulo — build multi-stage
  (Temurin 25 JDK → JRE), build do módulo via wrapper Maven a partir da **raiz** do
  repositório (`-pl <mod> -am`, precisa do reator), camadas `quarkus-app`, usuário
  não-root, porta 8080, `JAVA_OPTS` com `MaxRAMPercentage`.
- **Worker Rust:** `worker/Dockerfile` — multi-stage (`rust:1-slim` → `debian:bookworm-slim`),
  cache de dependências, `ca-certificates`, usuário não-root, binário `horus-report-worker`.
- **Loadtest Python:** `loadtest/Dockerfile` — `python:3.13-slim`, instala o pacote via
  `pyproject`, usuário não-root, `uvicorn app.main:app` na porta 8000.
- **`.dockerignore`** (raiz + worker + loadtest) excluindo `target/`, `.git`, caches.
- **Makefile:** alvos `docker-images` / `docker-quarkus` / `docker-worker` /
  `docker-loadtest` (tag via `IMAGE_TAG`, default `dev`).
- **`deploy/README.md`:** seção de build de imagens.

## Fora de escopo

- Compose de **aplicações** (subir os apps junto da infra) e manifests/Helm — **T-802**.
- Imagens **nativas** (GraalVM) — esta fatia entrega JVM (mais simples/portável); nativo
  fica como otimização posterior.
- Push para registry / versionamento de release das imagens — depende de T-802/CI.

## Critérios de aceitação

1. Cada executável tem um Dockerfile reproduzível e documentado.
2. Imagens rodam como **usuário não-root** e expõem a porta correta.
3. `make docker-images` constrói todas as imagens; alvos individuais funcionam.
4. `.dockerignore` mantém `target/`/`.git` fora do contexto.
5. Builds validados localmente (`docker build`) — ver registro no PLAN.
