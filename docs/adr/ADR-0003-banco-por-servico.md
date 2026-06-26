# ADR-0003 — Um banco PostgreSQL por serviço, isolado

- **Status:** Aceito
- **Data:** 2026-06-26
- **Origem:** Herdado do documento-fonte (DA-004)
- **Requisitos:** RF-025, RF-026, RF-027, RF-028, RNF-002, RNF-003, RNF-015

## Contexto

Para validar isolamento de dados em microsserviços, cada serviço deve ser **dono** dos seus dados. Compartilhar tabelas acoplaria os serviços e invalidaria o estudo.

## Decisão

Cada microsserviço possui um banco **PostgreSQL exclusivo**: `prontuario_db`, `payment_db` e `invoice_db`. Regras:

- Nenhum serviço acessa diretamente as tabelas internas de outro; a interação é por **API HTTP**.
- Credenciais **segregadas** por serviço.
- Cada serviço versiona e aplica suas **migrações** de forma independente (sem migração central).
- Mesmo quando hospedados na mesma infraestrutura física, os bancos permanecem **logicamente isolados** (banco/schema/credenciais).

## Consequências

**Positivas**
- Acoplamento de dados eliminado; serviços evoluem o schema isoladamente.
- Cada query carrega a identidade do seu banco — útil para a captura de ciclo de vida de query do Horus (ADR-0009).

**Negativas**
- Sem joins entre domínios; consultas cruzadas exigem chamadas de API.
- Risco de acoplamento acidental se o isolamento lógico não for respeitado (acompanhar na revisão).
