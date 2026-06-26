# ADR-0004 — RabbitMQ restrito a relatórios

- **Status:** Aceito
- **Data:** 2026-06-26
- **Origem:** Herdado do documento-fonte (DA-005)
- **Requisitos:** RF-021, RF-022, RNF-011

## Contexto

O estudo quer exercitar processamento **assíncrono** sem transformar a mensageria no barramento principal do domínio. Usar RabbitMQ para toda regra de negócio aumentaria a complexidade e dificultaria o raciocínio sobre os fluxos síncronos.

## Decisão

O **RabbitMQ é usado exclusivamente** para o fluxo de **solicitação de relatório**: um microsserviço **publica** um pedido de relatório e o worker **consome**. Todo o restante da comunicação de negócio permanece **síncrono por HTTP**.

O contrato conceitual da mensagem inclui: identificador da solicitação, tipo do relatório, parâmetros necessários e **contexto de tracing** nos headers.

## Consequências

**Positivas**
- Desacoplamento do trabalho pesado (relatório/e-mail) do caminho síncrono (RNF-011).
- Escopo de mensageria pequeno e fácil de observar.

**Negativas**
- A correlação de trace precisa atravessar a fronteira HTTP→AMQP (risco de perda de contexto; mitigado por RF-029 e reforçado pelo Horus em ADR-0009).
