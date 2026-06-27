# SAGA Orchestrator

Orquestrador de **SAGA por orquestração** (ADR-0013) — conduz fluxos cross-service via HTTP, com **estado persistido** e **compensações**, e é justamente o tipo de fluxo distribuído rico que o Horus observa.

- **Stack:** Quarkus (Java 25), REST + REST Client, Hibernate Panache, Flyway, OTel.
- **Banco:** `saga_db` (PostgreSQL dedicado — porta 5435 no compose de dev).
- **Requisitos:** RNF-019, RF-H-016 · ADR-0013.

## Fluxo `pay-then-invoice` (T-107)

`POST /sagas/pagar-e-emitir` `{carteiraId, valor, simularFalhaNota?}`:

1. **Passo 1 — pagamento** (payment-service): cria + **aprova** um pagamento. Compensação: **estorno** (RF-014).
2. **Passo 2 — nota fiscal** (invoice-service): **emite** a NF. Se a emissão falhar, a SAGA **compensa** o passo 1.

Resultado: `CONCLUIDA` (201) ou `COMPENSADA` (200). `GET /sagas/{id}` consulta o estado (RF-H-016).

- **Idempotência:** a compensação só estorna se houve aprovação; estado persistido em `saga`.
- **Observabilidade:** cada passo/compensação é uma chamada HTTP instrumentada pelo OTel — todos no mesmo `trace_id` (ADR-0009).

## Tasks

`T-107` (este orquestrador) · `T-507` (visualização da SAGA no Horus).
