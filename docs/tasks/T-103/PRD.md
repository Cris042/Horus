# PRD — T-103: Payment Service (domínio)

| Campo | Valor |
|---|---|
| **Task** | `T-103` |
| **Fase do roadmap** | Fase 1 — Microsserviços de domínio |
| **Status** | `Em revisão (PR)` |
| **Branch** | `task/T-103-payment-domain` |
| **PR** | [#9](https://github.com/mclovin137/Horus/pull/9) |
| **Depende de** | `T-101` (scaffold) |
| **Requisitos atendidos** | RF-011, RF-012, RF-013, RF-014, RF-015, RF-026 |
| **ADRs relacionados** | ADR-0002 (banco por serviço), ADR-0003 (Quarkus), ADR-0005 (Flyway) |
| **Data** | 2026-06-27 |

## Objetivo

Implementar o **domínio do Payment Service** sobre o scaffold de T-101: carteira/saldo (RF-011), movimentações de entrada/saída (RF-012), aprovar/rejeitar pagamento (RF-013), estornar (RF-014) e histórico financeiro (RF-015), persistindo **somente em `payment_db`** (RF-026).

## Escopo (o que entra)

- **Entidades Panache:** `Movimentacao` (entrada/saída + `saldoApos`), `Pagamento` (`PENDENTE`→`APROVADO`/`REJEITADO`→`ESTORNADO`), reuso de `Carteira`; enums `TipoMovimentacao`, `StatusPagamento`.
- **Migração Flyway `V2`** (`movimentacao`, `pagamento` + sequências) e **correção da `V1`** (sequência `carteira_seq` no lugar de IDENTITY — alinhamento ao id do Panache, lição de T-102).
- **Serviços** transacionais: `CarteiraService` (criar/buscar/movimentar/histórico, **bloqueia saldo negativo**), `PagamentoService` (criar/aprovar/rejeitar/estornar com transições válidas).
- **REST** (JSON + Bean Validation):
  - `POST /carteiras`, `GET /carteiras/{id}` (RF-011);
  - `POST/GET /carteiras/{id}/movimentacoes` (RF-012/015);
  - `POST /carteiras/{id}/pagamentos`, `GET /pagamentos/{id}`, `POST /pagamentos/{id}/aprovar|rejeitar|estornar` (RF-013/014).
- **Erros:** 404 (não encontrado), 409 (saldo insuficiente / transição inválida), 400 (validação).
- **Teste de fluxo `@QuarkusTest`** (ciclo completo + saldo insuficiente + 404).

## Fora do escopo

- **Prontuário/Invoice** (T-102/T-104). **SAGA** cross-service (T-107).
- **Migrações consolidadas/isolamento** (T-105/T-106), **OTel/PII** (T-401/T-406), **RBAC**.

## Premissas e dependências

- Scaffold T-101 (módulo `payment-service`); testes via Dev Services (CI com Docker).
- Entidades Panache usam id por **sequência** — migrações criam `*_seq` (não IDENTITY).

## Critérios de aceite

- [ ] Consultar carteira/saldo (RF-011); registrar entradas/saídas (RF-012); histórico (RF-015).
- [ ] Aprovar/rejeitar pagamento (RF-013) e estornar aprovado (RF-014); transições inválidas → 409.
- [ ] Aprovação sem saldo suficiente → 409; saldo nunca negativo.
- [ ] Persistência só em `payment_db` (RF-026); schema versionado por Flyway.
- [ ] Build verde; `PLAN.md` 100% ✅ e `state.md` atualizado.

## Riscos

| Risco | Mitigação |
|---|---|
| Concorrência em débito/crédito da carteira | Operações transacionais; bloqueio otimista/locks ficam para hardening futuro (fora do escopo do domínio inicial). |
| Precisão monetária | `BigDecimal` + `NUMERIC(18,2)`. |

## Referências

- [`../../PRD.md`](../../PRD.md), [`../../ROADMAP.md`](../../ROADMAP.md) — RF-011..015, RF-026
- ADR-0002, ADR-0003, ADR-0005
- [`./PLAN.md`](./PLAN.md)
