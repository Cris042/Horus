# Plano de Execução — T-107: SAGA (orquestração)

> ⚠️ **Regra R2 ([WORKFLOW](../../WORKFLOW.md)).** Sempre que **criar ou modificar** um arquivo desta tabela, atualize **na mesma hora** o seu `Status` e `Atualizado`, e registre em *Registro de alterações*.

| Campo | Valor |
|---|---|
| **Task** | `T-107` |
| **Branch** | `task/T-107-saga-orchestrator` |
| **PRD** | [`./PRD.md`](./PRD.md) |
| **Progresso** | `16/16` arquivos |

## Arquivos a criar/modificar

| # | Arquivo | Ação | Propósito | Status | Atualizado |
|---|---|---|---|---|---|
| 1 | `pom.xml` (parent) | Modificar | Registrar módulo `services/saga-orchestrator` | ✅ Concluído | 2026-06-27 |
| 2 | `services/saga-orchestrator/pom.xml` | Criar | Módulo Quarkus (REST + REST Client + Panache + OTel) | ✅ Concluído | 2026-06-27 |
| 3 | `.../resources/application.properties` | Criar | Config: saga_db, clients REST, OTel, perfis | ✅ Concluído | 2026-06-27 |
| 4 | `.../resources/db/migration/V1__saga.sql` | Criar | Sequência + tabela `saga` | ✅ Concluído | 2026-06-27 |
| 5 | `.../domain/StatusSaga.java` | Criar | Enum de estado da SAGA | ✅ Concluído | 2026-06-27 |
| 6 | `.../domain/Saga.java` | Criar | Entidade Panache (estado persistido) | ✅ Concluído | 2026-06-27 |
| 7 | `.../client/PagamentoDto.java` | Criar | DTO de resposta do payment | ✅ Concluído | 2026-06-27 |
| 8 | `.../client/NotaDto.java` | Criar | DTO de resposta do invoice | ✅ Concluído | 2026-06-27 |
| 9 | `.../client/PaymentClient.java` | Criar | REST Client payment (criar/aprovar/estornar) | ✅ Concluído | 2026-06-27 |
| 10 | `.../client/InvoiceClient.java` | Criar | REST Client invoice (emitir) | ✅ Concluído | 2026-06-27 |
| 11 | `.../service/PassoSagaException.java` | Criar | Falha de passo → compensação | ✅ Concluído | 2026-06-27 |
| 12 | `.../service/SagaService.java` | Criar | Orquestração + compensação + persistência | ✅ Concluído | 2026-06-27 |
| 13 | `.../api/dto/PagarEEmitirRequest.java`, `SagaResponse.java` | Criar | DTOs da API | ✅ Concluído | 2026-06-27 |
| 14 | `.../api/SagaResource.java` | Criar | REST `POST /sagas/pagar-e-emitir`, `GET /sagas/{id}` | ✅ Concluído | 2026-06-27 |
| 15 | `.../test/.../SagaFlowTest.java` | Criar | Teste `@QuarkusTest` (feliz + compensação, clients mockados) | ✅ Concluído | 2026-06-27 |
| 16 | `deploy/docker-compose.yml` | Modificar | + `postgres-saga` (`saga_db`, 5435) | ✅ Concluído | 2026-06-27 |
| 17 | `services/saga-orchestrator/README.md` | Criar | Doc do módulo | ✅ Concluído | 2026-06-27 |
| 18 | `docs/tasks/T-107/PRD.md`, `PLAN.md` | Criar | PRD e plano | ✅ Concluído | 2026-06-27 |

**Legenda:** ⬜ Pendente · 🟡 Em progresso · ✅ Concluído

## Passos de implementação

1. ✅ Branch `task/T-107-saga-orchestrator` a partir de `main` (pós-merge de T-105).
2. ✅ Módulo orquestrador: estado `Saga` (Panache/Flyway), clients REST, orquestração + compensação.
3. ✅ API REST + OTel/logs JSON; `postgres-saga` no compose.
4. ✅ Teste `@QuarkusTest` com clients mockados (caminho feliz + compensação).
5. ✅ Validação local: `compose config` ok; build de produção + test-compile verdes.
6. ✅ Atualizar `state.md`; abrir o PR [#13](https://github.com/mclovin137/Horus/pull/13); nº preenchido aqui e no PRD.

## Verificação / testes

- [x] `compose config` válido; **build de produção** do módulo verde; **test-compile** verde (offline).
- [ ] **`SagaFlowTest`** (feliz → CONCLUIDA sem estorno; falha → COMPENSADA com estorno) roda no **CI** (Dev Services `saga_db`).
- [x] Estado persistido por transição; compensação idempotente; passos como chamadas HTTP instrumentadas.

## Registro de alterações

| Data | Arquivo | Mudança |
|---|---|---|
| 2026-06-27 | `services/saga-orchestrator/**` | Novo módulo: orquestrador SAGA (estado persistido + compensação), clients REST, REST, OTel, teste. |
| 2026-06-27 | `pom.xml`, `deploy/docker-compose.yml` | Registra o módulo; adiciona `postgres-saga` (`saga_db`). |
| 2026-06-27 | `docs/tasks/T-107/PRD.md`, `PLAN.md` | PRD e plano de execução. |
