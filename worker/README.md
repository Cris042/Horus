# worker/ — Report & Email Worker (Rust)

Worker **assíncrono** em **Rust**, de responsabilidade limitada: consome solicitações de relatório do RabbitMQ, gera o relatório e envia o e-mail. Fica **fora** do caminho síncrono de negócio (ADR-0005).

- **Stack:** Rust (1.85+), cliente AMQP (lapin), SMTP, OpenTelemetry.
- **Requisitos:** RF-022..024, RNF-012.

## Responsabilidades

- **Consumir** mensagens de relatório do RabbitMQ.
- **Gerar** o relatório solicitado.
- **Enviar e-mail** com o resultado.

## Observabilidade

Instrumentado com OpenTelemetry e **continua o trace** propagado nos headers AMQP (HTTP→AMQP), para correlação ponta a ponta no mesmo `trace_id` (T-403, RF-H-004).

## Tasks

`T-301` (contrato da mensagem) · `T-303` (worker) · `T-403` (OTel + propagação AMQP).
