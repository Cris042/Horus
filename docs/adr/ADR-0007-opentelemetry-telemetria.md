# ADR-0007 — OpenTelemetry como padrão de telemetria

- **Status:** Aceito
- **Data:** 2026-06-26
- **Origem:** Herdado do documento-fonte (DA-008), estendido pelo Horus
- **Requisitos:** RF-029, RF-030, RF-031, RNF-007, RNF-008, RNF-009

## Contexto

O ambiente é poliglota (Python, Java/Quarkus, Rust) e distribuído, atravessando HTTP e AMQP (RabbitMQ). É preciso um padrão **único e neutro de fornecedor** para instrumentar todos os componentes e correlacionar a operação ponta a ponta.

## Decisão

Adotar **OpenTelemetry (OTel)** como contrato único de telemetria de todos os componentes:

- **Propagação de contexto** via `traceparent`/`tracestate` em chamadas HTTP e nos **headers das mensagens RabbitMQ** (RF-029).
- **Exportação via OTLP** para um coletor central (ver ADR-0010).
- **Jaeger UI** mantido para inspeção de traces crus (RF-031).

> O documento-fonte limitava a observabilidade a traces no Jaeger. O Horus **estende** este ADR para também coletar **logs e métricas** via OTel (ver ADR-0010) e para capturar o ciclo de vida de **queries** (ver ADR-0009).

## Consequências

**Positivas**
- Instrumentação homogênea entre linguagens; correlação ponta a ponta possível.
- Base padronizada sobre a qual o Horus e sua IA operam.

**Negativas**
- A correlação depende de **não perder** o contexto nas fronteiras (especialmente HTTP→AMQP); ponto de atenção central do projeto.
