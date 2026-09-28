# PRD — T-1010: Robustez da SAGA

| Campo | Valor |
|---|---|
| **Task** | `T-1010` |
| **Fase do roadmap** | `Fase 10 — Produto utilizável` |
| **Status** | `Entregue` |
| **Branch** | `claude/beautiful-cray-sfblsr` |
| **PR** | — |
| **Depende de** | `T-107`, `T-507`, `T-1002` |
| **Requisitos atendidos** | `RNF-019`, `RF-H-016` |
| **ADRs relacionados** | `ADR-0013` |
| **Data** | `2026-09-28` |

## Objetivo

Fechar os dois pontos deixados em aberto: o risco da T-107 ("falha do orquestrador entre passos
deixa SAGA pendurada; retry fica para hardening futuro") e o limite da T-507 (falha só era inferida
pela presença de compensação, porque o `SpanRef` não expunha o status do span).

## Achado

`SagaService.pagarEEmitir` rodava em **uma única transação** atravessando as chamadas HTTP: um crash
no meio desfazia o registro da SAGA, mas o pagamento **já aprovado** no payment-service ficava
órfão — sem nenhum estado que uma recuperação pudesse encontrar. Além disso, a conexão de banco
ficava presa durante as chamadas remotas.

## Escopo (o que entra)

- **Orquestrador:** `SagaStore` grava cada transição em transação própria (`REQUIRES_NEW`); o id do
  pagamento é gravado **antes** da aprovação (falha na aprovação agora também é compensada).
- **Recuperação automática:** `SagaRecovery` (`saga.recovery.interval` 1m, `saga.recovery.timeout`
  5m) compensa SAGAs paradas em `INICIADA`/`PAGAMENTO_APROVADO`, marcando `COMPENSADA` com motivo
  "timeout"; span de compensação com `horus.saga.recovery=true` (contrato atualizado).
- **Horus:** `SpanRef.error` (status do span vindo do Jaeger); visualização da SAGA com
  `steps[].failed`, `failedStep`, desfecho `failed` (falhou sem compensação — antes aparecia como
  `completed`) e `recovered` (trace da recuperação automática).
- e2e exige `failedStep=issue-invoice` na SAGA compensada.

## Fora do escopo

- Retry automático dos passos (a política continua "falhou → compensa").
- Idempotência por chave no payment-service (o estorno já é idempotente por estado).

## Critérios de aceite

- [x] SAGA interrompida há mais que o timeout é compensada pela recuperação; recente não é tocada.
- [x] Falha na aprovação do pagamento compensa (id gravado antes).
- [x] Fluxos feliz/compensado inalterados.
- [x] Visualização aponta o passo que falhou; falha sem compensação = `failed`; trace de recuperação = `recovered`.
- [x] `./mvnw -pl services/saga-orchestrator test` (5) e `-pl horus test` (126) verdes.

## Riscos

| Risco | Mitigação |
|---|---|
| Recuperação compensar SAGA ainda em andamento lento | Timeout (5m) muito acima da duração normal (< 1s); configurável |
| Várias réplicas do orquestrador recuperando a mesma SAGA | Estorno idempotente; `SKIP` por réplica; evolução: `SELECT … FOR UPDATE SKIP LOCKED` |

## Referências

- [`../T-107/PRD.md`](../T-107/PRD.md) · [`../T-507/PRD.md`](../T-507/PRD.md) · [`./PLAN.md`](./PLAN.md)
