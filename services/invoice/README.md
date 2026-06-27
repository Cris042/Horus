# Invoice Service

Microsserviço de **faturamento / nota fiscal** do sistema observado.

- **Stack:** Quarkus (Java 25), REST, Hibernate Panache, Flyway.
- **Banco:** `invoice_db` (PostgreSQL dedicado).
- **Requisitos:** RF-016..020, RF-027.

## Responsabilidades

- Receber pedidos de **emissão** de nota fiscal.
- Gerar **NF simulada** e registrar o resultado.
- Listar notas e **reprocessar** falhas.
- **Publicar** solicitações de relatório no RabbitMQ (consumidas pelo worker Rust).

## SAGA

Etapa final do fluxo **pagar → emitir NF**; expõe compensação (cancelar/estornar NF) coordenada via LRA (ADR-0013, T-107).

## Observabilidade

Traces (HTTP + JDBC), logs e métricas via OTLP; **propaga o contexto de tracing** para o RabbitMQ ao publicar relatórios (T-403, RF-H-004).

## Tasks

`T-101` · `T-104` · `T-105` · `T-107` · `T-301`/`T-302` (relatório) · `T-401`.
