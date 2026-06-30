# Plano de Execução — T-703: Alertas (e-mail/webhook) com resumo de IA

> ⚠️ **Regra R2 ([WORKFLOW](../../WORKFLOW.md)).** Sempre que **criar ou modificar** um arquivo desta tabela, atualize **na mesma hora** o seu `Status` e `Atualizado`, e registre a mudança em *Registro de alterações*.

| Campo | Valor |
|---|---|
| **Task** | `T-703` |
| **Branch** | `task/T-703-alerts` |
| **PRD** | [`./PRD.md`](./PRD.md) |
| **Progresso** | `11/11` arquivos |

## Arquivos a criar/modificar

| # | Arquivo | Ação | Propósito | Status | Atualizado |
|---|---|---|---|---|---|
| 1 | `docs/tasks/T-703/PRD.md` | Criar | PRD da task | ✅ Concluído | 2026-06-30 |
| 2 | `docs/tasks/T-703/PLAN.md` | Criar | Plano arquivo por arquivo | ✅ Concluído | 2026-06-30 |
| 3 | `horus/src/main/java/org/example/horus/alert/AlertModel.java` | Criar | DTOs de alerta | ✅ Concluído | 2026-06-30 |
| 4 | `horus/src/main/java/org/example/horus/alert/AlertChannel.java` | Criar | Porta de canal | ✅ Concluído | 2026-06-30 |
| 5 | `horus/src/main/java/org/example/horus/alert/LogAlertChannel.java` | Criar | Canal log (default) | ✅ Concluído | 2026-06-30 |
| 6 | `horus/src/main/java/org/example/horus/alert/WebhookAlertChannel.java` | Criar | Canal webhook (HTTP POST) | ✅ Concluído | 2026-06-30 |
| 7 | `horus/src/main/java/org/example/horus/alert/EmailAlertChannel.java` | Criar | Canal e-mail (stub gated) | ✅ Concluído | 2026-06-30 |
| 8 | `horus/src/main/java/org/example/horus/alert/AlertService.java` | Criar | Resumo IA + fan-out | ✅ Concluído | 2026-06-30 |
| 9 | `horus/src/main/java/org/example/horus/api/HorusAlertResource.java` | Criar | `POST /horus/alerts`, `GET /horus/alerts/channels` | ✅ Concluído | 2026-06-30 |
| 10 | `horus/src/test/java/org/example/horus/alert/AlertServiceTest.java` | Criar | Teste REST do fan-out | ✅ Concluído | 2026-06-30 |
| 11 | `horus/src/main/resources/application.properties` | Modificar | Config dos canais (off por padrão) | ✅ Concluído | 2026-06-30 |

> `state.md` atualizado ao final da entrega.

**Legenda:** ⬜ Pendente · 🟡 Em progresso · ✅ Concluído

## Verificação / testes

- [x] `./mvnw -pl horus test` verde.

## Registro de alterações

| Data | Arquivo | Mudança |
|---|---|---|
| `2026-06-30` | `docs/tasks/T-703/*` | PRD e plano criados |
| `2026-06-30` | `alert/*`, `api/HorusAlertResource.java`, `application.properties` | Mecanismo de alerta (IA + canais log/webhook/email) |
| `2026-06-30` | `alert/AlertServiceTest.java` | Teste do fan-out com Stub de IA |
| `2026-06-30` | `state.md` | Estado alinhado ao progresso de T-703 |
