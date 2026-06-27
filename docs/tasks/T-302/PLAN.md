# Plano de Execução — T-302: Publicação de solicitação de relatório pelos serviços

> ⚠️ **Regra R2 ([WORKFLOW](../../WORKFLOW.md)).** Sempre que **criar ou modificar** um arquivo desta tabela, atualize **na mesma hora** o seu `Status` e `Atualizado`, e registre em *Registro de alterações*.

| Campo | Valor |
|---|---|
| **Task** | `T-302` |
| **Branch** | `task/T-302-saga-report-publication` |
| **PRD** | [`./PRD.md`](./PRD.md) |
| **Progresso** | `5/5` arquivos |

## Arquivos a criar/modificar

| # | Arquivo | Ação | Propósito | Status | Atualizado |
|---|---|---|---|---|---|
| 1 | `.../saga/client/InvoiceClient.java` | Modificar | + `solicitarRelatorio(notaId)` | ✅ Concluído | 2026-06-27 |
| 2 | `.../saga/service/SagaService.java` | Modificar | Passo final best-effort: solicitar relatório | ✅ Concluído | 2026-06-27 |
| 3 | `.../test/.../SagaFlowTest.java` | Modificar | Verifica solicitação no sucesso e ausência na compensação | ✅ Concluído | 2026-06-27 |
| 4 | `docs/architecture/report-message.md` | Modificar | Produtores por fluxo (SAGA dispara) | ✅ Concluído | 2026-06-27 |
| 5 | `docs/tasks/T-302/PRD.md`, `PLAN.md` | Criar | PRD e plano | ✅ Concluído | 2026-06-27 |

**Legenda:** ⬜ Pendente · 🟡 Em progresso · ✅ Concluído

## Passos de implementação

1. ✅ Branch `task/T-302-saga-report-publication` a partir de `main` (pós-merge de T-301).
2. ✅ `InvoiceClient.solicitarRelatorio`; chamada best-effort no fim da SAGA.
3. ✅ Atualizar o teste da SAGA (sucesso solicita; compensação não).
4. ✅ Doc dos produtores por fluxo.
5. ✅ Validação local: build de produção + test-compile verdes.
6. ⬜ Atualizar `state.md`; abrir o PR `T-302: …`; preencher o nº do PR aqui e no PRD.

## Verificação / testes

- [x] **Build de produção** + **test-compile** do orquestrador verdes (offline).
- [ ] **`SagaFlowTest`** (sucesso → solicita relatório; compensação → não) roda no **CI** (Dev Services `saga_db`, clients mockados).
- [x] Solicitação é best-effort (não compensa a SAGA concluída).

## Registro de alterações

| Data | Arquivo | Mudança |
|---|---|---|
| 2026-06-27 | `services/saga-orchestrator/**` | SAGA solicita relatório da NF após conclusão (best-effort); teste atualizado. |
| 2026-06-27 | `docs/architecture/report-message.md` | Produtores por fluxo (SAGA dispara o relatório). |
| 2026-06-27 | `docs/tasks/T-302/PRD.md`, `PLAN.md` | PRD e plano de execução. |
