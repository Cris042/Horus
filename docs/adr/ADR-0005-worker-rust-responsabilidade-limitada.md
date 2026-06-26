# ADR-0005 — Worker em Rust com responsabilidade limitada

- **Status:** Aceito
- **Data:** 2026-06-26
- **Origem:** Herdado do documento-fonte (DA-006)
- **Requisitos:** RF-022, RF-023, RF-024, RNF-012, RNF-013

## Contexto

O processamento assíncrono de relatórios precisa de um consumidor dedicado. Há o risco de esse componente crescer e virar um "serviço centralizador" que coordena regra de negócio — o que o estudo quer evitar.

## Decisão

Implementar um **Report & Email Worker em Rust** com **exatamente duas responsabilidades**: (1) **gerar o relatório** solicitado e (2) **enviar o relatório por e-mail**. O worker:

- **Não** coordena regra de negócio, pagamentos, notas ou compensações.
- **Não** acessa diretamente os bancos de domínio; opera sobre o conteúdo da mensagem/informações disponíveis para o caso de estudo.
- **Continua o trace** durante a geração e o envio (propagação de contexto a partir dos headers da mensagem).

## Consequências

**Positivas**
- Componente pequeno, previsível e fácil de escalar isoladamente.
- Falha do worker é rastreável sem comprometer os serviços de domínio (RNF-013).

**Negativas**
- Acrescenta uma terceira linguagem ao conjunto (esforço de instrumentação coberto por ADR-0007/0010).
