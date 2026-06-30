# PRD — T-507: Visualização de SAGA

| Campo | Valor |
|---|---|
| **Task** | `T-507` |
| **Fase** | Fase 5 — Horus core (correlação + ciclo de vida) |
| **Requisitos** | RF-H-016, ADR-0013 |
| **Dependências** | T-502 (correlação), T-107 (SAGA orchestrator), T-005 (contrato de telemetria) |
| **Branch** | `task/T-507-saga-visualization` |

## Objetivo

Exibir, para um `traceId`, o **ciclo de vida de uma SAGA**: seus **passos**, as
**compensações** e o **desfecho** (sucesso / compensada), reconstruídos a partir dos
spans do trace conforme o contrato de telemetria (T-005, ADR-0013). É a peça que
torna o padrão SAGA observável no Horus (RF-H-016), correlacionada por `trace_id`.

## Base no contrato de telemetria (T-005)

Spans de SAGA seguem convenção de nome de operação:
- **passo**: `saga.{flow}.{step}` (ex.: `saga.pay-then-invoice.reserve-payment`);
- **compensação**: `saga.{flow}.{step}.compensate`.

Como o modelo de span da camada de consulta (`SpanRef`, T-501) é enxuto e **não
carrega atributos crus**, a visualização deriva `flow`, `step` e o caráter de
compensação **do nome da operação** — sem depender de atributos OTel adicionais.

## Escopo

- `SagaVisualizationModel` — DTOs REST: passo de SAGA e a SAGA correlacionada.
- `SagaVisualizationService` — projeta um `TraceResult` na linha do tempo da SAGA:
  - filtra spans cuja operação começa com `saga.`;
  - extrai `flow`, `step` e flag `compensation` do nome;
  - ordena por tempo de início, com `offsetMicros` relativo ao 1º passo e duração;
  - **desfecho**: `compensated` se houve ao menos uma compensação; `completed` se
    houve passos sem compensação; `none` se o trace não tem SAGA.
- `HorusSagaResource` — `GET /horus/lifecycle/saga/{traceId}`
  (valida hex 32 chars; 404 se trace ausente; 400 se inválido).
- Teste REST `@QuarkusTest` com `TraceQueryPort` mockado.

## Fora de escopo

- Renderização gráfica (front-end) — fase de UI (T-701/T-702).
- Detecção de falha por **status do span**/atributos crus — `SpanRef` não os expõe;
  a falha é inferida pela presença de compensação (desfecho `compensated`).
- Correlação com logs/métricas da SAGA — coberto por T-502/T-505.

## Critérios de aceitação

1. `GET /horus/lifecycle/saga/{traceId}` lista os passos da SAGA (flow, step, ordem,
   duração) e marca as compensações.
2. Desfecho `compensated` quando há compensação; `completed` quando só há passos;
   `none` quando o trace não tem spans de SAGA.
3. Spans não-SAGA são ignorados.
4. 404 para trace inexistente; 400 para `traceId` malformado (sem consultar backend).
5. `./mvnw -pl horus test` verde.
