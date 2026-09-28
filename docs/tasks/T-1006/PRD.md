# PRD — T-1006: Análises pesadas assíncronas

| Campo | Valor |
|---|---|
| **Task** | `T-1006` |
| **Fase do roadmap** | `Fase 10 — Produto utilizável` |
| **Status** | `Entregue` |
| **Branch** | `claude/beautiful-cray-sfblsr` |
| **PR** | — |
| **Depende de** | `T-605`, `T-1001` |
| **Requisitos atendidos** | `RNF-H-004` |
| **ADRs relacionados** | `ADR-0011` |
| **Data** | `2026-09-28` |

## Objetivo

RNF-H-004 pede "análises pesadas executadas de forma assíncrona". A RCA (camada DEEP / Opus) e o
resumo de uma janela inteira rodavam **síncronos** no request HTTP — com a IA real, dezenas de
segundos com a conexão presa (e timeouts de proxy/LB).

## Escopo (o que entra)

- `AiJobService`: pool limitado de workers (virtual threads) com **fila limitada** — fila cheia
  → **429** (contrapressão, não acumula custo); jobs `PENDING → RUNNING → SUCCEEDED | FAILED`;
  concluídos expiram após `horus.ai.jobs.retention` (1h).
- API: `POST /horus/ai/jobs/rca` e `POST /horus/ai/jobs/state-summary` → **202** + `Location`;
  `GET /horus/ai/jobs/{id}` (404 se inexistente/expirado); `GET /horus/ai/jobs`.
- Painel: botão **RCA** usa o job com polling e mostra o progresso.
- Endpoints síncronos mantidos (compatibilidade).

## Fora do escopo

- Persistência dos jobs (sobrevivem só ao processo) e distribuição entre réplicas — com HPA do
  Horus (T-803) o `GET` precisa cair na mesma réplica; documentado como limitação (sticky session
  ou armazenamento compartilhado numa evolução).
- Push (SSE/WebSocket) do resultado — polling basta.

## Critérios de aceite

- [x] RCA assíncrona: 202 + `Location`, depois `SUCCEEDED` com o mesmo resultado do síncrono.
- [x] Falha do job → `FAILED` com a mensagem; job inexistente → 404; corpo inválido → 400.
- [x] Fila cheia rejeita sem registrar o job.
- [x] Expiração dos jobs concluídos.
- [x] `./mvnw -pl horus test` verde (110 testes).

## Riscos

| Risco | Mitigação |
|---|---|
| Jobs em memória com várias réplicas | Documentado; retenção curta; evolução com store compartilhado |
| Explosão de custo por muitos jobs | Workers e fila limitados, 429 |

## Referências

- [`./PLAN.md`](./PLAN.md)
