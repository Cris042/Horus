# Plano de Execução — T-301: RabbitMQ + contrato da mensagem de relatório

> ⚠️ **Regra R2 ([WORKFLOW](../../WORKFLOW.md)).** Sempre que **criar ou modificar** um arquivo desta tabela, atualize **na mesma hora** o seu `Status` e `Atualizado`, e registre em *Registro de alterações*.

| Campo | Valor |
|---|---|
| **Task** | `T-301` |
| **Branch** | `task/T-301-rabbitmq-report-contract` |
| **PRD** | [`./PRD.md`](./PRD.md) |
| **Progresso** | `9/9` arquivos |

## Arquivos a criar/modificar

| # | Arquivo | Ação | Propósito | Status | Atualizado |
|---|---|---|---|---|---|
| 1 | `services/invoice/pom.xml` | Modificar | + `quarkus-messaging-rabbitmq`; test: in-memory connector | ✅ Concluído | 2026-06-27 |
| 2 | `.../relatorio/RelatorioMensagem.java` | Criar | Contrato da mensagem de relatório | ✅ Concluído | 2026-06-27 |
| 3 | `.../relatorio/RelatorioPublisher.java` | Criar | Publica no canal `relatorios` (assíncrono) | ✅ Concluído | 2026-06-27 |
| 4 | `.../service/NotaFiscalService.java` | Modificar | `solicitarRelatorio` (exige NF emitida) | ✅ Concluído | 2026-06-27 |
| 5 | `.../api/NotaFiscalResource.java` | Modificar | `POST /notas/{id}/relatorio` (202) | ✅ Concluído | 2026-06-27 |
| 6 | `.../resources/application.properties` | Modificar | Canal `relatorios` (rabbitmq) + conexão + `%test` in-memory | ✅ Concluído | 2026-06-27 |
| 7 | `.../test/.../RelatorioPublicacaoTest.java` | Criar | Teste `@QuarkusTest` (publica + 409), conector in-memory | ✅ Concluído | 2026-06-27 |
| 8 | `docs/architecture/report-message.md` | Criar | Contrato + tracing nos headers + roteamento | ✅ Concluído | 2026-06-27 |
| 9 | `docs/tasks/T-301/PRD.md`, `PLAN.md` | Criar | PRD e plano | ✅ Concluído | 2026-06-27 |

**Legenda:** ⬜ Pendente · 🟡 Em progresso · ✅ Concluído

## Passos de implementação

1. ✅ Branch `task/T-301-rabbitmq-report-contract` a partir de `main` (pós-merge de T-106).
2. ✅ Contrato `RelatorioMensagem` + publisher + canal `relatorios` (rabbitmq); conexão por perfil.
3. ✅ Endpoint `POST /notas/{id}/relatorio` (NF emitida → 202; senão 409).
4. ✅ Teste com conector in-memory (publica 1 msg; falha não publica).
5. ✅ Documentar o contrato (corpo + tracing nos headers).
6. ✅ Validação local: build de produção + test-compile verdes.
7. ✅ Atualizar `state.md`; abrir o PR [#15](https://github.com/mclovin137/Horus/pull/15); nº preenchido aqui e no PRD.

## Verificação / testes

- [x] **Build de produção** do invoice verde; **test-compile** verde (offline).
- [ ] **`RelatorioPublicacaoTest`** (publica/contrato + 409) roda no **CI** (Dev Services `invoice_db` + conector in-memory).
- [x] Contrato definido/documentado; publicação assíncrona (RNF-011); `traceparent` via OTel nos headers (validação fim-a-fim em T-403).

## Registro de alterações

| Data | Arquivo | Mudança |
|---|---|---|
| 2026-06-27 | `services/invoice/**` | Mensageria de relatório: contrato, publisher, endpoint, config (rabbitmq + in-memory em teste), teste. |
| 2026-06-27 | `docs/architecture/report-message.md` | Contrato da mensagem + tracing nos headers (RF-021/029). |
| 2026-06-27 | `docs/tasks/T-301/PRD.md`, `PLAN.md` | PRD e plano de execução. |
