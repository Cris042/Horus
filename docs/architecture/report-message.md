# Contrato da mensagem de relatório (T-301)

| Campo | Valor |
|---|---|
| **Requisitos** | RF-021 (publicar solicitação), RF-022 (consumir no worker), RNF-011 (desacoplamento assíncrono) |
| **ADRs** | ADR-0004 (RabbitMQ restrito a relatórios), ADR-0007/0009 (tracing) |
| **Status** | Aceito (T-301) |

## Visão geral

Os serviços publicam **solicitações de relatório** no RabbitMQ; o **worker Rust** (T-303) consome, gera o relatório e envia o e-mail. A mensageria existe **apenas para relatórios** (ADR-0004) — não é barramento de eventos de domínio nem da SAGA.

```
invoice-service ──(AMQP: exchange "relatorios")──▶ RabbitMQ ──▶ worker Rust (T-303)
```

## Corpo da mensagem (JSON)

| Campo | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `id` | string (UUID) | sim | Id único da solicitação — **idempotência** no consumidor |
| `tipo` | string | sim | Tipo do relatório (ex.: `NOTA_FISCAL`) |
| `notaId` | number | sim | Id da nota fiscal de referência |
| `valor` | number | sim | Valor da NF (parâmetro do relatório) |
| `numero` | string | sim | Número da NF emitida |
| `solicitadoEm` | string (ISO-8601, UTC) | sim | Instante da solicitação |

> Parâmetros adicionais por tipo podem ser acrescentados no futuro; consumidores devem **ignorar campos desconhecidos**.

## Contexto de tracing (headers — RF-029, crítico)

O **`traceparent`/`tracestate`** (W3C Trace Context) viaja nos **headers da mensagem AMQP**, **não** no corpo. É injetado automaticamente pelo OpenTelemetry no `quarkus-messaging-rabbitmq` (span `PRODUCER`). O worker (T-403) **extrai** esses headers e abre o span de processamento como filho — preservando o `trace_id` na fronteira **HTTP→AMQP** (a que mais quebra correlação; ver [`../telemetry/CONTRACT.md`](../telemetry/CONTRACT.md) §2).

## Tópico / roteamento

- **Exchange:** `relatorios`. **Routing key (atual):** `nota-fiscal`.
- **Conexão (dev):** RabbitMQ do compose (`localhost:5672`, `horus/horus`).

## Produtor (T-301/T-302)

- `invoice-service`: `POST /notas/{id}/relatorio` (NF **emitida**) → publica `RelatorioMensagem` no canal `relatorios` de forma **assíncrona** (não bloqueia a request — RNF-011).
- **Disparo por fluxo (T-302):** ao concluir a SAGA `pagar→emitir NF` (T-107), o **`saga-orchestrator`** chama `POST /notas/{id}/relatorio` no invoice — passo final **best-effort** (falha não compensa a SAGA já concluída). Assim o fluxo fica `pagar → emitir → solicitar relatório`, todo no mesmo `trace_id`.

## Fora do escopo (próximas tasks)

- **Consumo + geração de relatório + e-mail** no worker Rust → **T-303** (RF-022..024).
- **Propagação/validação HTTP→AMQP** ponta a ponta no worker → **T-403** (RF-029, RF-H-004).

## Referências

- ADR-0004 · [`../telemetry/CONTRACT.md`](../telemetry/CONTRACT.md) (§2 propagação AMQP) · [`../PRD.md`](../PRD.md) (RF-021/022, RNF-011)
