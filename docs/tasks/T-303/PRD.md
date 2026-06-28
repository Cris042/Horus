# PRD — T-303: Worker Rust de relatórios (consumir → gerar → e-mail)

| Campo | Valor |
|---|---|
| **Task** | `T-303` |
| **Fase do roadmap** | `Fase 3 — Mensageria e worker Rust` |
| **Status** | `Em revisão (PR)` |
| **Branch** | `task/T-303-rust-worker` |
| **PR** | `#<n>` (preencher ao abrir) |
| **Depende de** | `T-301` (contrato da mensagem) |
| **Requisitos atendidos** | `RF-022`, `RF-023`, `RF-024`, `RNF-012` |
| **ADRs relacionados** | `ADR-0004` (RabbitMQ restrito a relatórios), `ADR-0005` (worker isolado) |
| **Data** | `2026-06-28` |

## Objetivo

Implementar o **worker Rust** de responsabilidade limitada (ADR-0005): consome solicitações
de relatório do RabbitMQ, gera o relatório e envia o e-mail (RF-022..024). Fecha o fluxo
assíncrono `invoice → RabbitMQ → worker → e-mail` e é pré-requisito de T-403 (OTel/AMQP).

## Escopo (o que entra)

- Projeto Cargo `worker/` (Rust 2021): `tokio`, `lapin` (AMQP), `lettre` (SMTP), `serde`,
  `tracing` (log JSON), `anyhow`/`thiserror`.
- Consumo: declara/binda fila durável à exchange `relatorios` (topic, routing key `nota-fiscal`);
  ack em sucesso, nack sem requeue em erro.
- `message.rs`: `RelatorioMensagem` (espelha o contrato T-301, JSON).
- `report.rs`: geração do relatório (texto).
- `email.rs`: porta `EmailSender` com `SmtpSender` (lettre) e `LogSender` (stub, padrão sem SMTP).
- `consumer.rs`: orquestração idempotente por `id` (RNF-012), testável offline.
- Testes unitários (`cargo test`) + job de CI `build-worker` (fmt + clippy + test).

## Fora do escopo

- **Propagação/continuação do trace HTTP→AMQP** (extrair `traceparent` dos headers) → **T-403**.
- Geração de relatório "real" (PDF/planilha) — basta o conteúdo textual simulado.
- TLS no SMTP (endurecimento posterior); dev usa transporte plaintext/stub.

## Premissas e dependências

- Contrato e roteamento de T-301: exchange `relatorios`, routing key `nota-fiscal`, corpo JSON.
- Sem SMTP configurado (`SMTP_HOST` ausente) o worker usa o `LogSender` (não falha em dev/CI).
- ⚠️ Verificação local exigiu `gcc` (linker C) ausente no ambiente → validação via **CI** (job `build-worker`, ubuntu).

## Critérios de aceite

- [ ] `cargo build`/`cargo test` verdes (no CI `build-worker`).
- [ ] Desserialização do `RelatorioMensagem` (JSON do invoice) coberta por teste.
- [ ] Idempotência por `id` (mensagem repetida não reenvia) coberta por teste.
- [ ] `fmt --check` e `clippy -D warnings` limpos.

## Riscos

| Risco | Mitigação |
|---|---|
| Sem broker/SMTP em CI | Lógica pura (parse/relatório/idempotência) testada offline; AMQP/SMTP só no `main`. |
| `BigDecimal` do produtor como número | `valor: f64` desserializa o número JSON; revisitar se vier como string. |
| Linker C ausente no dev | CI valida; documentado para instalar `build-essential` no ambiente local. |

## Referências

- [`../../architecture/report-message.md`](../../architecture/report-message.md) — exchange/routing/headers
- [`../../telemetry/CONTRACT.md`](../../telemetry/CONTRACT.md) §2 (AMQP — base para T-403)
- [`./PLAN.md`](./PLAN.md)
