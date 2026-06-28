# PRD — T-502: Modelo de correlação por `trace_id`

| Campo | Valor |
|---|---|
| **Task** | `T-502` |
| **Fase do roadmap** | `Fase 5 — Horus core` |
| **Status** | `Em revisão (PR)` |
| **Branch** | `task/T-502-correlation-model` |
| **PR** | [#35](https://github.com/mclovin137/Horus/pull/35) |
| **Depende de** | `T-501` (camada de consulta), `T-403` (propagação ponta a ponta) |
| **Requisitos atendidos** | `RF-H-001`, `RF-H-002`, `RF-H-004` |
| **ADRs relacionados** | `ADR-0007`, `ADR-0008` |
| **Data** | `2026-06-28` |

## Objetivo

Costurar, por `trace_id`, os sinais hoje separados nos backends (request/spans, queries,
logs, fronteira de mensageria → worker) num **modelo de correlação** único — a fundação das
APIs de ciclo de vida (T-503/504/505), do mapa de serviços (T-506) e da visualização de
SAGA (T-507).

## Escopo (o que entra)

- `correlation/CorrelationModel`: `RequestCorrelation` (serviços envolvidos, logs, contagem
  de erros, flags de mensageria/worker, duração somada) + `ServiceInvolvement`.
- `correlation/CorrelationService` (`@ApplicationScoped`): monta o modelo a partir das portas
  `TraceQueryPort` + `LogQueryPort` (T-501); agrupa spans por serviço, detecta a fronteira de
  mensageria (`publish`/`receive`/`relatorios`) e a presença do `report-worker`, conta erros.
- `api/HorusCorrelationResource`: `GET /horus/correlation/trace/{traceId}` (404 se inexistente).
- Testes `@QuarkusTest` com portas mockadas (cross-service + mensageria + worker + erro + 404).

## Fora do escopo

- APIs de ciclo de vida detalhado (waterfall de spans, statement SQL) → **T-503/504**.
- Agregação dedicada de logs de erro → **T-505**.
- **Validação ponta a ponta com stack real** (mesmo `trace_id` de fato) → **T-405** (precisa do ambiente de pé).

## Premissas e dependências

- A propagação ponta a ponta já existe (T-401/402/403); aqui consome-se o resultado via T-501.
- Modelo neutro de backend — sem vazar schema de Jaeger/Loki; PII fora (CONTRACT §6).

## Critérios de aceite

- [x] `correlate(traceId)` agrupa spans por serviço (contagem + duração) ordenados por duração.
- [x] Detecta `workerInvolved` (`report-worker`) e `messagingInvolved` (publish/receive/relatorios).
- [x] Conta logs de erro (label `level=error` ou linha com `ERROR`).
- [x] `GET /horus/correlation/trace/{id}` → 200 com o modelo, 404 se inexistente.
- [x] `mvn -pl horus test` verde.

## Riscos

| Risco | Mitigação |
|---|---|
| Heurística de mensageria por nome de operação | Reforçada pela presença do `report-worker`; refinável quando T-405 rodar com dados reais. |
| Duração somada ≠ duração da request | Métrica aproximada; a árvore/waterfall real vem em T-503. |

## Referências

- [`../../telemetry/CONTRACT.md`](../../telemetry/CONTRACT.md) §1/§2
- [`./PLAN.md`](./PLAN.md)
