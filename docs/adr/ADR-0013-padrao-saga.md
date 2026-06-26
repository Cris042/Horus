# ADR-0013 — Adotar o padrão SAGA desde o início (orquestração)

- **Status:** Aceito — **adotado**
- **Data:** 2026-06-26
- **Origem:** Novo (decisão arquitetural)
- **Substitui:** a decisão anterior de *não* usar SAGA (antigo ADR-0006, removido)
- **Relacionado:** ADR-0003 (banco por serviço), ADR-0004 (RabbitMQ restrito), ADR-0008/0009 (observabilidade Horus)
- **Requisitos:** RNF-019 (revisado), RF-H-016 (visualização de SAGA)

## Contexto

Com **um banco por serviço** (ADR-0003) e comunicação síncrona por HTTP, não existe transação ACID entre microsserviços. Fluxos que tocam mais de um domínio (ex.: **pagar → emitir nota fiscal**) precisam de uma estratégia de consistência.

O **foco principal da aplicação é observabilidade.** Uma SAGA — sequência de transações locais, cada uma com sua **compensação** — é justamente o tipo de **fluxo distribuído rico** que o Horus deve enxergar: múltiplos passos entre serviços, caminhos de sucesso, falha e **compensação**, todos correlacionados em um único trace. Adotar SAGA **desde o início** dá à plataforma material real e valioso para rastrear, resumir e diagnosticar com IA.

> Esta decisão **reverte a decisão anterior de não usar SAGA** (antigo ADR-0006, removido). Aquela versão (herdada do documento-fonte) optara por não usar SAGA visando simplicidade; aqui priorizamos o **objetivo de observabilidade** sobre essa simplificação.

## Decisão

**Adotar o padrão SAGA como mecanismo de consistência entre serviços, desde o início do projeto**, com estilo de **orquestração**:

1. **Orquestração (não coreografia).** Um orquestrador conduz os passos da SAGA via **HTTP** e dispara as **compensações** em caso de falha. Escolhemos orquestração porque o fluxo fica **centralizado e altamente rastreável** — ideal para a observabilidade — e porque **preserva o ADR-0004** (o RabbitMQ continua restrito a relatórios; a SAGA não usa o broker como barramento de eventos).
2. **Implementação:** **MicroProfile LRA (Long Running Actions)** via a extensão `quarkus-narayana-lra`, com um **LRA Coordinator**. Alternativa: orquestrador próprio com estado de SAGA persistido em PostgreSQL.
3. **Regras obrigatórias por passo:**
   - **Idempotência** em cada passo e em cada compensação.
   - Toda ação que altera estado em um serviço tem uma **compensação** correspondente.
   - O estado da SAGA é **persistido** (auditável e recuperável).
4. **Observabilidade de primeira classe:** cada passo e cada compensação é um **span OTel** correlacionado pelo mesmo `trace_id` (ADR-0009). O Horus **visualiza a SAGA inteira** (passos, compensações, falhas) — ver RF-H-016.

### Escopo de "rollback"

- **SAGA com compensações: SIM** (rollback *lógico*).
- **Rollback distribuído ACID / two-phase commit (2PC): NÃO** — continua fora de escopo (não escala e não é necessário).

## Consequências

**Positivas**
- Fornece ao Horus fluxos distribuídos reais (passos + compensações) para rastrear/resumir — atende ao foco do produto.
- Consistência entre serviços sem 2PC; cada serviço mantém autonomia (ADR-0003).
- Mantém o RabbitMQ restrito a relatórios (ADR-0004 intacto).

**Negativas**
- Mais complexidade: exige idempotência, correção das compensações e tratamento de falhas parciais.
- O **coordenador LRA** vira componente de infraestrutura (precisa de disponibilidade); mitigado por ser observável e reiniciável.
- Aumenta o escopo de implementação face ao documento-fonte (assumido conscientemente, dado o foco em observabilidade).
