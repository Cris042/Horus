# PRD — T-301: RabbitMQ + contrato da mensagem de relatório

| Campo | Valor |
|---|---|
| **Task** | `T-301` |
| **Fase do roadmap** | Fase 3 — Mensageria e worker |
| **Status** | `Entregue` |
| **Branch** | `task/T-301-rabbitmq-report-contract` |
| **PR** | [#15](https://github.com/mclovin137/Horus/pull/15) |
| **Depende de** | `T-101` |
| **Requisitos atendidos** | RF-021, RNF-011 (base p/ RF-022/T-303) |
| **ADRs relacionados** | ADR-0004 (RabbitMQ restrito a relatórios), ADR-0007/0009 (tracing) |
| **Data** | 2026-06-27 |

## Objetivo

Definir o **contrato da mensagem de relatório** e a **infraestrutura de publicação** no RabbitMQ (RF-021): um serviço publica uma solicitação de relatório de forma **assíncrona** (RNF-011), com o **contexto de tracing** (W3C `traceparent`) nos **headers** da mensagem (RF-029). Habilita o worker Rust (T-303) e exercita a fronteira **HTTP→AMQP** central à observabilidade do Horus.

## Escopo (o que entra)

- **Contrato:** `RelatorioMensagem` (`id`, `tipo`, `notaId`, `valor`, `numero`, `solicitadoEm`) + doc `docs/architecture/report-message.md`.
- **Publicação** no `invoice-service` (`quarkus-messaging-rabbitmq`): canal de saída `relatorios` (exchange `relatorios`, routing key `nota-fiscal`); `RelatorioPublisher` (assíncrono).
- **Endpoint** `POST /notas/{id}/relatorio` (NF **emitida** → 202; senão 409) como gatilho/mecanismo.
- **Config** de conexão RabbitMQ por perfil (dev: compose; test: conector **in-memory**).
- **Teste `@QuarkusTest`** com conector in-memory: verifica a publicação (1 mensagem, contrato correto) e 409 p/ NF não emitida.

## Fora do escopo

- **Publicação integrada em outros fluxos/serviços** → T-302.
- **Worker Rust** (consumir/gerar relatório/e-mail) → T-303 (RF-022..024).
- **Validação HTTP→AMQP ponta a ponta** no worker → T-403.

## Premissas e dependências

- RabbitMQ no compose (T-003); ADR-0004 mantém o broker restrito a relatórios.
- OTel já presente no invoice (T-401) injeta `traceparent` no span PRODUCER/headers.
- Teste sem RabbitMQ real (conector in-memory) — CI-safe.

## Critérios de aceite

- [ ] Contrato `RelatorioMensagem` definido e documentado.
- [ ] `POST /notas/{id}/relatorio` publica a mensagem de forma assíncrona (RNF-011); NF não emitida → 409.
- [ ] Publicação verificada por teste (conector in-memory); build/test verde.
- [ ] `traceparent` viaja nos headers (via OTel) — documentado (validação ponta a ponta em T-403).
- [ ] `PLAN.md` 100% ✅ e `state.md` atualizado.

## Riscos

| Risco | Mitigação |
|---|---|
| Teste depender de RabbitMQ real | Conector **in-memory** em `%test`. |
| Propagação de `traceparent` não automática | OTel + messaging injeta no PRODUCER; validação fim-a-fim fica em T-403. |

## Referências

- [`../../architecture/report-message.md`](../../architecture/report-message.md) — o contrato
- ADR-0004 · [`../../telemetry/CONTRACT.md`](../../telemetry/CONTRACT.md) (§2) · [`../../PRD.md`](../../PRD.md) (RF-021, RNF-011)
- [`./PLAN.md`](./PLAN.md)
