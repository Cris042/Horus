# PRD — T-107: SAGA (orquestração) — fluxo pagar→emitir NF

| Campo | Valor |
|---|---|
| **Task** | `T-107` |
| **Fase do roadmap** | Fase 1 — Microsserviços de domínio |
| **Status** | `Em revisão (PR)` |
| **Branch** | `task/T-107-saga-orchestrator` |
| **PR** | [#13](https://github.com/mclovin137/Horus/pull/13) |
| **Depende de** | `T-102`, `T-103`, `T-104` |
| **Requisitos atendidos** | RNF-019 (+ base para RF-H-016) |
| **ADRs relacionados** | ADR-0013 (SAGA), ADR-0002 (banco por serviço), ADR-0007/0009 (OTel) |
| **Data** | 2026-06-27 |

## Objetivo

Entregar o **primeiro fluxo SAGA cross-service** (`pagar → emitir NF`) por **orquestração** (ADR-0013): um orquestrador com **estado persistido** conduz os passos via HTTP e dispara **compensações** idempotentes em caso de falha, com cada passo/compensação correlacionado por `trace_id` (OTel). Dá ao Horus material real de fluxo distribuído (sucesso, falha, compensação) para observar.

## Decisão de implementação

Das duas opções do ADR-0013, escolhida (pelo usuário) a **alternativa de orquestrador próprio com estado persistido em PostgreSQL** (em vez de MicroProfile LRA + coordinator): sem infra extra, totalmente testável em um `@QuarkusTest` com os participantes mockados, mantendo a semântica SAGA (passos idempotentes + compensação).

## Escopo (o que entra)

- **Novo módulo `services/saga-orchestrator`** (Quarkus): REST + REST Client + Panache/Flyway + OTel/logs JSON; banco dedicado **`saga_db`** (compose: `postgres-saga`, 5435).
- **Estado da SAGA** (`Saga` + enum `StatusSaga`: INICIADA→PAGAMENTO_APROVADO→CONCLUIDA / COMPENSADA), migração `V1__saga.sql`.
- **Clients REST** `PaymentClient` (criar/aprovar/estornar) e `InvoiceClient` (emitir).
- **Orquestração** `SagaService.pagarEEmitir`: passo 1 aprova pagamento (compensa por estorno); passo 2 emite NF; falha de emissão → compensa o passo 1; estado persistido a cada transição.
- **REST** `POST /sagas/pagar-e-emitir` (201 concluída / 200 compensada), `GET /sagas/{id}` (RF-H-016).
- **Teste `@QuarkusTest`** com `@InjectMock @RestClient`: caminho feliz conclui (sem estorno) e falha de emissão compensa (estorno disparado).

## Fora do escopo

- **MicroProfile LRA + coordinator** (alternativa não escolhida).
- **Recuperação/retry assíncrono** de SAGAs interrompidas e timeouts.
- **Visualização da SAGA no Horus** → T-507 (RF-H-016).
- Cancelamento de NF já emitida (fluxo de 2 passos com NF como último passo não exige).

## Premissas e dependências

- Serviços de domínio T-102/103/104 entregues (endpoints usados pelos clients).
- Em dev, `make up` provê `payment-service`/`invoice-service` e `postgres-saga`.
- Testes: clients mockados (sem HTTP real); `saga_db` via Dev Services (CI com Docker).

## Critérios de aceite

- [ ] `POST /sagas/pagar-e-emitir` executa pagamento+emissão e persiste o estado da SAGA.
- [ ] Falha na emissão → **compensação** (estorno) idempotente; SAGA termina `COMPENSADA`.
- [ ] Cada passo/compensação é chamada HTTP instrumentada (mesmo `trace_id`).
- [ ] Build/test verde; `PLAN.md` 100% ✅ e `state.md` atualizado.

## Riscos

| Risco | Mitigação |
|---|---|
| Falha do orquestrador entre passos deixa SAGA "pendurada" | Estado persistido permite diagnóstico/recuperação; retry automático fica para hardening futuro. |
| Compensação falhar (serviço indisponível) | Logada; estado registra a tentativa — reconciliação manual/observável (foco do Horus). |
| Chamadas externas dentro de uma transação longa | Aceitável no incremento; revisão de fronteiras transacionais em hardening. |

## Referências

- [ADR-0013](../../adr/ADR-0013-padrao-saga.md) · [`../../PRD.md`](../../PRD.md) (RNF-019, RF-H-016)
- [`../../telemetry/CONTRACT.md`](../../telemetry/CONTRACT.md) (§3.4 atributos `horus.saga.*` — adoção futura)
- [`./PLAN.md`](./PLAN.md)
