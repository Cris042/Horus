# Plano de Execução — T-104: Invoice Service (domínio)

> ⚠️ **Regra R2 ([WORKFLOW](../../WORKFLOW.md)).** Sempre que **criar ou modificar** um arquivo desta tabela, atualize **na mesma hora** o seu `Status` e `Atualizado`, e registre em *Registro de alterações*.

| Campo | Valor |
|---|---|
| **Task** | `T-104` |
| **Branch** | `task/T-104-invoice-domain` |
| **PRD** | [`./PRD.md`](./PRD.md) |
| **Progresso** | `13/13` arquivos |

## Arquivos a criar/modificar

| # | Arquivo | Ação | Propósito | Status | Atualizado |
|---|---|---|---|---|---|
| 1 | `services/invoice/pom.xml` | Modificar | + `quarkus-hibernate-validator` | ✅ Concluído | 2026-06-27 |
| 2 | `.../domain/StatusNota.java` | Criar | Enum de estado da NF | ✅ Concluído | 2026-06-27 |
| 3 | `.../domain/NotaFiscal.java` | Modificar | Entidade de domínio (número, status, falha, tentativas…) | ✅ Concluído | 2026-06-27 |
| 4 | `.../db/migration/V1__init.sql` | Modificar | Schema completo + `nota_fiscal_seq` (sequência) | ✅ Concluído | 2026-06-27 |
| 5 | `.../api/dto/EmitirNotaRequest.java` | Criar | DTO (RF-016) + knob `simularFalha` | ✅ Concluído | 2026-06-27 |
| 6 | `.../api/dto/NotaFiscalResponse.java` | Criar | DTO de saída | ✅ Concluído | 2026-06-27 |
| 7 | `.../service/ConflitoNotaException.java` | Criar | Exceção de conflito | ✅ Concluído | 2026-06-27 |
| 8 | `.../api/ConflitoNotaMapper.java` | Criar | Mapper → HTTP 409 | ✅ Concluído | 2026-06-27 |
| 9 | `.../service/NotaFiscalService.java` | Criar | Emitir/listar/reprocessar | ✅ Concluído | 2026-06-27 |
| 10 | `.../api/NotaFiscalResource.java` | Criar | REST de notas fiscais | ✅ Concluído | 2026-06-27 |
| 11 | `.../test/.../InvoiceFlowTest.java` | Criar | Teste de fluxo `@QuarkusTest` | ✅ Concluído | 2026-06-27 |
| 12 | `docs/tasks/T-104/PRD.md` | Criar | PRD desta task | ✅ Concluído | 2026-06-27 |
| 13 | `docs/tasks/T-104/PLAN.md` | Criar | Este plano | ✅ Concluído | 2026-06-27 |

**Legenda:** ⬜ Pendente · 🟡 Em progresso · ✅ Concluído

## Passos de implementação

1. ✅ Branch `task/T-104-invoice-domain` a partir de `main` (pós-merge de T-103).
2. ✅ Modelo: `NotaFiscal`+`StatusNota`; reescrita da migração `V1` (sequência + schema completo).
3. ✅ Serviço: emissão simulada (sucesso/falha), listagem com filtro, reprocessamento.
4. ✅ REST + DTOs + Bean Validation + mapper 409.
5. ✅ Teste de fluxo (RF-016..020 + 409 + 400).
6. ✅ Build de produção verde (`-DskipTests`); testes Dev Services no CI.
7. ✅ Atualizar `state.md`; abrir o PR [#10](https://github.com/mclovin137/Horus/pull/10); nº preenchido aqui e no PRD.

## Verificação / testes

- [x] **Build de produção verde** (`./mvnw -DskipTests -pl services/invoice package`) sob JDK 25 + Quarkus 3.37.
- [ ] **`InvoiceFlowTest`** (RF-016..020 + 409/400) roda no **CI** (Dev Services PostgreSQL).
- [x] Endpoints/regras alinhados a RF-016..020; persistência só em `invoice_db` (RF-027).

## Registro de alterações

| Data | Arquivo | Mudança |
|---|---|---|
| 2026-06-27 | `services/invoice/**` | Domínio do Invoice: `NotaFiscal`+enum, reescrita `V1` (sequência + schema), serviço, REST+DTOs+validação, mapper 409, teste de fluxo. |
| 2026-06-27 | `services/invoice/pom.xml` | + `quarkus-hibernate-validator`. |
| 2026-06-27 | `docs/tasks/T-104/PRD.md`, `PLAN.md` | PRD e plano de execução. |
