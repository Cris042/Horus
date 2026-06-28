# PRD — T-202: API FastAPI de controle de teste de carga

| Campo | Valor |
|---|---|
| **Task** | `T-202` |
| **Fase do roadmap** | `Fase 2 — Entrada e teste de carga` |
| **Status** | `Em revisão (PR)` |
| **Branch** | `task/T-202-fastapi-control-api` |
| **PR** | [#30](https://github.com/mclovin137/Horus/pull/30) |
| **Depende de** | `T-201` (Load Balancer) |
| **Requisitos atendidos** | `RF-001`, `RF-002`, `RF-003` |
| **ADRs relacionados** | — |
| **Data** | `2026-06-28` |

## Objetivo

Entregar a **API de controle** dos testes de carga (FastAPI): iniciar, consultar e
interromper testes (RF-001..003). É o ponto a partir do qual a carga (Locust, T-203) e a
propagação de tracing na borda (T-402) serão acopladas, no caminho crítico até T-405.

## Escopo (o que entra)

- App FastAPI (`app/`): `POST /load-tests` (RF-001), `GET /load-tests/{id}` (RF-002),
  `POST /load-tests/{id}/stop` (RF-003), `GET /load-tests` (auxiliar), `GET /health`.
- Modelos Pydantic (`LoadTestRequest`, `LoadTest`, `LoadTestStatus`) com validação.
- `LoadTestManager` thread-safe (ciclo de vida em memória) com porta `LoadRunner` (no-op aqui;
  Locust pluga em T-203).
- `pyproject.toml` (uv) + testes pytest; job de CI Python (`build-loadtest`).

## Fora do escopo

- Geração real de carga / cenários Locust (RF-004) → **T-203**.
- Instrumentação OpenTelemetry e início do contexto de tracing na borda → **T-402**.
- Persistência dos testes (estado é em memória nesta fatia).

## Premissas e dependências

- O Load Balancer (T-201) é o alvo default (`http://load-balancer`).
- CI Java não cobre Python; adicionado job `build-loadtest` (uv + pytest) ao `ci.yml`.

## Critérios de aceite

- [x] `POST /load-tests` cria teste em `running` (201) e valida parâmetros (422 inválido).
- [x] `GET /load-tests/{id}` retorna o teste (200) ou 404.
- [x] `POST /load-tests/{id}/stop` encerra um teste em execução (200), 404 se inexistente, 409 se já encerrado.
- [x] `pytest` verde; job de CI Python adicionado.

## Riscos

| Risco | Mitigação |
|---|---|
| Estado em memória se perde no restart | Aceitável na fatia; persistência fora do escopo. |
| Acoplamento futuro ao Locust | Isolado atrás da porta `LoadRunner` (no-op→Locust em T-203). |

## Referências

- [`../../../loadtest/README.md`](../../../loadtest/README.md)
- [`../../PRD.md`](../../PRD.md) — RF-001..003
- [`./PLAN.md`](./PLAN.md)
