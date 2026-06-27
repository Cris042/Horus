# Payment Service

Microsserviço de **pagamentos** do sistema observado.

- **Stack:** Quarkus (Java 25), REST, Hibernate Panache, Flyway.
- **Banco:** `payment_db` (PostgreSQL dedicado).
- **Requisitos:** RF-011..015, RF-026.

## Responsabilidades

- Carteira e **saldo** por cliente.
- Registrar **movimentações** financeiras.
- **Aprovar**, **rejeitar** e **estornar** pagamentos.
- Histórico de transações.

## SAGA

Participa do fluxo distribuído **pagar → emitir NF** como participante LRA, com **compensação** (estorno) idempotente em caso de falha downstream (ADR-0013, T-107).

## Observabilidade

Traces (HTTP + JDBC), logs e métricas via OTLP. Valores e identificadores sensíveis sanitizados na borda (RNF-H-002).

## Tasks

`T-101` · `T-103` · `T-105` · `T-107` (SAGA) · `T-401`.
