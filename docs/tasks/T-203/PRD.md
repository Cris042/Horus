# PRD — T-203: Cenários Locust (geração de carga)

| Campo | Valor |
|---|---|
| **Task** | `T-203` |
| **Fase do roadmap** | `Fase 2 — Entrada e teste de carga` |
| **Status** | `Em revisão (PR)` |
| **Branch** | `task/T-203-locust-scenarios` |
| **PR** | [#31](https://github.com/mclovin137/Horus/pull/31) |
| **Depende de** | `T-202` (API de controle) |
| **Requisitos atendidos** | `RF-004`, `RNF-016` |
| **ADRs relacionados** | — |
| **Data** | `2026-06-28` |

## Objetivo

Gerar **tráfego concorrente e realista** contra o Load Balancer (RF-004/RNF-016): cenários
Locust por domínio (Prontuário, Payment, Invoice) e SAGA, encadeando chamadas dependentes
para produzir traces/queries/logs ricos para o Horus observar. Acoplado à API de controle
(T-202) por um runner Locust.

## Escopo (o que entra)

- `app/locustfile.py`: `HttpUser`s por domínio + SAGA, com `@task`/pesos, corpos válidos e
  uma fração de falhas (ex.: `simularFalha`) para gerar logs de erro.
- `app/runner.py`: `LocustRunner` (implementa a porta `LoadRunner`) — monta o comando
  `locust --headless` e gerencia o processo (start/stop); `build_command` isolado e testável.
- Seleção de runner por ambiente em `main.py` (`HORUS_LOADTEST_RUNNER=locust`; padrão no-op).
- Testes pytest offline (cenários bem-formados, comando correto, ciclo de vida do processo).

## Fora do escopo

- Instrumentação OTel / início do contexto de tracing na borda → **T-402**.
- Métricas/relatórios agregados persistidos do Locust (saída padrão do Locust basta aqui).
- Orquestração de carga distribuída (master/worker Locust).

## Premissas e dependências

- Rotas do LB (T-201): `/prontuarios`, `/consultas`, `/carteiras`, `/pagamentos`, `/notas`, `/sagas`.
- Padrão da API permanece no-op para não exigir rede em testes/CI.

## Critérios de aceite

- [x] `locustfile` define usuários para os 3 domínios + SAGA, todos com tasks.
- [x] `LocustRunner.build_command` produz `--headless -u/-r/--run-time -H -f` corretos.
- [x] `start`/`stop` gerenciam o processo (stop idempotente).
- [x] `pytest` verde; CI Python cobre os novos testes.

## Riscos

| Risco | Mitigação |
|---|---|
| Endpoints do domínio mudam de caminho | Nomes de rota centralizados no `locustfile`; alinhados ao `nginx.conf`. |
| Subprocesso Locust órfão | `stop` envia SIGINT e faz `kill` após timeout. |

## Referências

- [`../../../loadtest/app/locustfile.py`](../../../loadtest/app/locustfile.py)
- [`../../../deploy/lb/nginx.conf`](../../../deploy/lb/nginx.conf) — rotas do LB
- [`./PLAN.md`](./PLAN.md)
