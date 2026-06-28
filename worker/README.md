# worker/ — Report & Email Worker (Rust)

Worker **assíncrono** em **Rust**, de responsabilidade limitada: consome solicitações de relatório do RabbitMQ, gera o relatório e envia o e-mail. Fica **fora** do caminho síncrono de negócio (ADR-0005).

- **Stack:** Rust (1.85+), cliente AMQP (lapin), SMTP, OpenTelemetry.
- **Requisitos:** RF-022..024, RNF-012.

## Responsabilidades

- **Consumir** mensagens de relatório do RabbitMQ (exchange `relatorios`, routing key `nota-fiscal`). ✅ T-303.
- **Gerar** o relatório solicitado. ✅ T-303.
- **Enviar e-mail** com o resultado (`lettre`; stub de log quando sem `SMTP_HOST`). ✅ T-303.

## Desenvolvimento

> Requer toolchain Rust (`rustup`, stable) e um **linker C** (`build-essential`/`gcc`).

```bash
cd worker
cargo test                 # lógica (parse, relatório, idempotência, e-mail) — offline
cargo run                  # conecta no RabbitMQ e consome (usa as envs abaixo)
```

Configuração por ambiente (defaults = compose de dev):
`AMQP_URL` (`amqp://horus:horus@localhost:5672/%2f`), `RELATORIOS_EXCHANGE` (`relatorios`),
`RELATORIOS_QUEUE` (`relatorios.worker`), `RELATORIOS_ROUTING_KEY` (`nota-fiscal`),
`SMTP_HOST` (sem ela → e-mails apenas registrados), `EMAIL_FROM`, `EMAIL_TO`.

## Observabilidade

Instrumentado com OpenTelemetry e **continua o trace** propagado nos headers AMQP (HTTP→AMQP), para correlação ponta a ponta no mesmo `trace_id` (T-403, RF-H-004).

## Tasks

`T-301` (contrato da mensagem) · `T-303` (worker) · `T-403` (OTel + propagação AMQP).
