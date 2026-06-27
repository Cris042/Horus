# Plano de Execução — T-103: Payment Service (domínio)

> ⚠️ **Regra R2 ([WORKFLOW](../../WORKFLOW.md)).** Sempre que **criar ou modificar** um arquivo desta tabela, atualize **na mesma hora** o seu `Status` e `Atualizado`, e registre em *Registro de alterações*.

| Campo | Valor |
|---|---|
| **Task** | `T-103` |
| **Branch** | `task/T-103-payment-domain` |
| **PRD** | [`./PRD.md`](./PRD.md) |
| **Progresso** | `20/20` arquivos |

## Arquivos a criar/modificar

| # | Arquivo | Ação | Propósito | Status | Atualizado |
|---|---|---|---|---|---|
| 1 | `services/payment/pom.xml` | Modificar | + `quarkus-hibernate-validator` | ✅ Concluído | 2026-06-27 |
| 2 | `.../domain/TipoMovimentacao.java` | Criar | Enum entrada/saída | ✅ Concluído | 2026-06-27 |
| 3 | `.../domain/StatusPagamento.java` | Criar | Enum de estado do pagamento | ✅ Concluído | 2026-06-27 |
| 4 | `.../domain/Movimentacao.java` | Criar | Entidade de movimentação/extrato | ✅ Concluído | 2026-06-27 |
| 5 | `.../domain/Pagamento.java` | Criar | Entidade de pagamento | ✅ Concluído | 2026-06-27 |
| 6 | `.../db/migration/V1__init.sql` | Modificar | `carteira_seq` (sequência no lugar de IDENTITY) | ✅ Concluído | 2026-06-27 |
| 7 | `.../db/migration/V2__movimentacao_pagamento.sql` | Criar | Tabelas + sequências + índices | ✅ Concluído | 2026-06-27 |
| 8 | `.../api/dto/CriarCarteiraRequest.java` | Criar | DTO (RF-011) | ✅ Concluído | 2026-06-27 |
| 9 | `.../api/dto/CarteiraResponse.java` | Criar | DTO de carteira/saldo | ✅ Concluído | 2026-06-27 |
| 10 | `.../api/dto/MovimentacaoRequest.java` | Criar | DTO (RF-012) | ✅ Concluído | 2026-06-27 |
| 11 | `.../api/dto/MovimentacaoResponse.java` | Criar | DTO de extrato (RF-015) | ✅ Concluído | 2026-06-27 |
| 12 | `.../api/dto/CriarPagamentoRequest.java` | Criar | DTO (RF-013) | ✅ Concluído | 2026-06-27 |
| 13 | `.../api/dto/PagamentoResponse.java` | Criar | DTO de pagamento | ✅ Concluído | 2026-06-27 |
| 14 | `.../service/ConflitoPagamentoException.java` | Criar | Exceção de conflito | ✅ Concluído | 2026-06-27 |
| 15 | `.../api/ConflitoPagamentoMapper.java` | Criar | Mapper → HTTP 409 | ✅ Concluído | 2026-06-27 |
| 16 | `.../service/CarteiraService.java` | Criar | Carteira/saldo/movimentação/histórico | ✅ Concluído | 2026-06-27 |
| 17 | `.../service/PagamentoService.java` | Criar | Criar/aprovar/rejeitar/estornar | ✅ Concluído | 2026-06-27 |
| 18 | `.../api/CarteiraResource.java` | Criar | REST de carteira + abertura de pagamento | ✅ Concluído | 2026-06-27 |
| 19 | `.../api/PagamentoResource.java` | Criar | REST de processamento de pagamento | ✅ Concluído | 2026-06-27 |
| 20 | `.../test/.../PaymentFlowTest.java` | Criar | Teste de fluxo `@QuarkusTest` | ✅ Concluído | 2026-06-27 |
| 21 | `docs/tasks/T-103/PRD.md` | Criar | PRD desta task | ✅ Concluído | 2026-06-27 |
| 22 | `docs/tasks/T-103/PLAN.md` | Criar | Este plano | ✅ Concluído | 2026-06-27 |

**Legenda:** ⬜ Pendente · 🟡 Em progresso · ✅ Concluído

## Passos de implementação

1. ✅ Branch `task/T-103-payment-domain` a partir de `main` (pós-merge de T-102).
2. ✅ Modelo: `Movimentacao`/`Pagamento` + enums; migração `V2` e correção de `V1` (sequência).
3. ✅ Serviços transacionais (saldo nunca negativo; transições válidas de pagamento).
4. ✅ REST + DTOs + Bean Validation + mapper 409.
5. ✅ Teste de fluxo (RF-011..015 + 409 sem saldo + 404).
6. ✅ Build de produção verde (`-DskipTests`); testes Dev Services no CI.
7. ⬜ Atualizar `state.md`; abrir o PR `T-103: …`; preencher o nº do PR aqui e no PRD.

## Verificação / testes

- [x] **Build de produção verde** (`./mvnw -DskipTests -pl services/payment package`) sob JDK 25 + Quarkus 3.37.
- [ ] **`PaymentFlowTest`** (ciclo RF-011..015 + 409/404) roda no **CI** (Dev Services PostgreSQL).
- [x] Endpoints/regras alinhados a RF-011..015; persistência só em `payment_db` (RF-026).

## Registro de alterações

| Data | Arquivo | Mudança |
|---|---|---|
| 2026-06-27 | `services/payment/**` | Domínio do Payment: `Movimentacao`/`Pagamento`+enums, migração `V2`, correção `V1` (sequência), serviços, REST+DTOs+validação, mapper 409, teste de fluxo. |
| 2026-06-27 | `services/payment/pom.xml` | + `quarkus-hibernate-validator`. |
| 2026-06-27 | `docs/tasks/T-103/PRD.md`, `PLAN.md` | PRD e plano de execução. |
