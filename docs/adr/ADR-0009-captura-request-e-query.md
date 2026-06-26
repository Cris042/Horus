# ADR-0009 — Captura do ciclo de vida de request e query

- **Status:** Aceito
- **Data:** 2026-06-26
- **Origem:** Novo
- **Requisitos:** RF-H-001, RF-H-002, RF-H-003, RF-H-004, RF-H-011, RNF-H-001, RNF-H-002

## Contexto

O objetivo central inclui ver **o fluxo de vida completo de cada request e de cada query executada**, além de **todo log de erro**. O tracing padrão do documento-fonte captura spans de serviço, mas não garante visão de **query** (SQL) nem a correlação com **logs**.

## Decisão

Capturar e correlacionar três sinais por unidade de trabalho:

### Request (ponta a ponta)
- Span raiz na borda (Load Balancer / FastAPI) propagado por todos os serviços até a resposta.
- Visualização em **waterfall** no painel Horus (RF-H-011), incluindo o salto HTTP→AMQP→worker.

### Query (cada execução de SQL)
- Instrumentar o acesso a dados de cada serviço (em Quarkus: instrumentação OTel de **JDBC/Hibernate**) para emitir um **span por query**, filho do span da request.
- Cada span de query registra: statement (com **parâmetros mascarados**), duração, banco/serviço de origem e, quando disponível, indicadores de plano/linhas.
- **Nunca** registrar valores sensíveis crus (RNF-H-002): sanitização **antes** de persistir.

### Logs de erro
- Toda exceção/erro é emitido como log estruturado contendo `trace_id` e `span_id`, permitindo saltar do erro para o trace e vice-versa (RF-H-003/004).

A correlação usa o **`trace_id`** como chave única que liga request ↔ queries ↔ logs ↔ mensagem RabbitMQ ↔ processamento do worker.

## Consequências

**Positivas**
- Visão real de ciclo de vida: do clique à query e ao log de erro, em um só fio.
- Base estruturada e sanitizada para a IA raciocinar (ADR-0011).

**Negativas**
- Instrumentação de query adiciona overhead — controlado por amostragem e orçamento (RNF-H-001).
- Exige disciplina de sanitização em todos os componentes (risco de vazamento de PII se ignorado).
