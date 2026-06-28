# PRD — T-108: Concorrência no Payment (isolar o saldo mutável)

| Campo | Valor |
|---|---|
| **Task** | `T-108` (hardening de `T-103`) |
| **Fase do roadmap** | `Fase 1 — Domínio (hardening)` |
| **Status** | `Em revisão (PR)` |
| **Branch** | `task/T-108-payment-concurrency` |
| **PR** | [#36](https://github.com/mclovin137/Horus/pull/36) |
| **Depende de** | `T-103` (domínio Payment) |
| **Requisitos atendidos** | `RNF-002` (integridade), reforço de `RF-012/013/014` |
| **ADRs relacionados** | `ADR-0002` (sem auth no domínio — N/A), `ADR-0005` |
| **Data** | `2026-06-28` |

## Objetivo

Eliminar a **race condition** no saldo da carteira (estado mutável compartilhado). O fluxo
anterior `findById → modifica saldo → commit` não tinha controle de concorrência: dois
débitos simultâneos na mesma carteira podiam causar **lost update** ou **saldo negativo**.
Isolar esse estado com **lock pessimista** no nível do banco.

## Escopo (o que entra)

- `CarteiraService.aplicarPorId(...)`: carrega a carteira com `LockModeType.PESSIMISTIC_WRITE`
  (`SELECT … FOR UPDATE`) e aplica a movimentação — serializa transações sobre a mesma linha.
- `movimentar(...)` passa a usar `aplicarPorId`.
- `PagamentoService.aprovar/estornar`: debitam/creditam via `aplicarPorId` (carteira sob lock
  na mesma transação que muda o status do pagamento → atômico).
- Teste de concorrência (`PaymentConcurrencyTest`, Testcontainers): 20 débitos simultâneos
  sem *lost update*; e saldo nunca negativo (exatamente 1 sucesso quando só há saldo para 1).

## Fora do escopo

- Optimistic locking (`@Version`) — alternativa documentada; o lock pessimista basta para o
  hot path de baixa contenção e é determinístico de testar.
- Concorrência em outros agregados (consulta/NF) — sem estado numérico compartilhado crítico.

## Premissas e dependências

- Postgres (READ COMMITTED) — `SELECT … FOR UPDATE` serializa as transações concorrentes.
- Lock de **uma linha** por operação → sem risco de deadlock por ordem de aquisição.

## Critérios de aceite

- [x] Débitos concorrentes na mesma carteira não perdem atualização (saldo final exato).
- [x] Concorrência nunca deixa o saldo negativo (excesso → 409, exatamente 1 sucesso).
- [x] Fluxo existente (aprovar/estornar/histórico) permanece verde.
- [x] `mvn -pl services/payment test` verde (Testcontainers).

## Riscos

| Risco | Mitigação |
|---|---|
| Lock segura a linha por toda a transação | Transações curtas; lock de 1 linha; sem chamadas externas dentro do lock. |
| Contenção alta numa carteira "quente" | Aceitável no escopo; se virar gargalo, avaliar fila/particionamento. |

## Referências

- [`./PLAN.md`](./PLAN.md) · `docs/tasks/T-103/`
