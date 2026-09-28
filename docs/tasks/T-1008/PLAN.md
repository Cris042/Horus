# Plano de Execução — T-1008: Alertas automáticos + e-mail real

> ⚠️ **Regra R2 ([WORKFLOW](../../WORKFLOW.md)).** Sempre que **criar ou modificar** um arquivo desta tabela, atualize **na mesma hora** o seu `Status` e `Atualizado`, e registre a mudança em *Registro de alterações*. Arquivo não previsto que precise ser tocado deve ser **adicionado** à tabela.

| Campo | Valor |
|---|---|
| **Task** | `T-1008` |
| **Branch** | `claude/beautiful-cray-sfblsr` |
| **PRD** | [`./PRD.md`](./PRD.md) |
| **Progresso** | `10/10` arquivos |

## Arquivos a criar/modificar

| # | Arquivo | Ação | Propósito | Status | Atualizado |
|---|---|---|---|---|---|
| 1 | `horus/pom.xml` | Modificar | `quarkus-mailer` | ✅ Concluído | 2026-09-28 |
| 2 | `horus/src/main/java/org/example/horus/alert/AlertWatcher.java` | Criar | Disparo agendado + dedup | ✅ Concluído | 2026-09-28 |
| 3 | `horus/src/main/java/org/example/horus/alert/AlertWatchConfig.java` | Criar | Config mapping das regras | ✅ Concluído | 2026-09-28 |
| 4 | `horus/src/main/java/org/example/horus/alert/EmailAlertChannel.java` | Modificar | Envio real via mailer | ✅ Concluído | 2026-09-28 |
| 5 | `horus/src/main/resources/application.properties` | Modificar | `horus.alert.watch.*`, remetente | ✅ Concluído | 2026-09-28 |
| 6 | `horus/src/test/java/org/example/horus/alert/AlertWatcherTest.java` | Criar | Regras, traces com erro, dedup, falha contida | ✅ Concluído | 2026-09-28 |
| 7 | `horus/src/test/java/org/example/horus/alert/EmailAlertChannelTest.java` | Criar | MockMailbox | ✅ Concluído | 2026-09-28 |
| 8 | `docs/tasks/T-1008/PRD.md` | Criar | PRD | ✅ Concluído | 2026-09-28 |
| 9 | `docs/tasks/T-1008/PLAN.md` | Criar | Este plano | ✅ Concluído | 2026-09-28 |
| 10 | `state.md` | Modificar | R1 | ✅ Concluído | 2026-09-28 |

**Legenda:** ⬜ Pendente · 🟡 Em progresso · ✅ Concluído

## Verificação / testes

- [x] `./mvnw -pl horus test` (JDK 25) → **123 testes, 0 falhas**.

## Registro de alterações

| Data | Arquivo | Mudança |
|---|---|---|
| `2026-09-28` | (todos acima) | Alertas automáticos + e-mail real (T-1008) |
