# Plano de Execução — T-702: Visualização do fluxo de vida (waterfall)

> ⚠️ **Regra R2 ([WORKFLOW](../../WORKFLOW.md)).** Sempre que **criar ou modificar** um arquivo desta tabela, atualize **na mesma hora** o seu `Status` e `Atualizado`, e registre a mudança em *Registro de alterações*. Arquivo não previsto que precise ser tocado deve ser **adicionado** à tabela.

| Campo | Valor |
|---|---|
| **Task** | `T-702` |
| **Branch** | `task/T-702-waterfall-ui` |
| **PRD** | [`./PRD.md`](./PRD.md) |
| **Progresso** | `6/6` arquivos |

## Arquivos a criar/modificar

| # | Arquivo | Ação | Propósito | Status | Atualizado |
|---|---|---|---|---|---|
| 1 | `docs/tasks/T-702/PRD.md` | Criar | PRD da task | ✅ Concluído | 2026-06-30 |
| 2 | `docs/tasks/T-702/PLAN.md` | Criar | Plano arquivo por arquivo | ✅ Concluído | 2026-06-30 |
| 3 | `horus/src/main/resources/META-INF/resources/horus-waterfall.html` | Criar | Página waterfall (spans + queries + logs) | ✅ Concluído | 2026-06-30 |
| 4 | `horus/src/main/java/org/example/horus/panel/HorusPanelResource.java` | Modificar | Expor endpoints de ciclo de vida no overview | ✅ Concluído | 2026-06-30 |
| 5 | `horus/src/main/resources/META-INF/resources/horus-panel.html` | Modificar | Link para o waterfall | ✅ Concluído | 2026-06-30 |
| 6 | `horus/src/test/java/org/example/horus/panel/HorusPanelResourceTest.java` | Modificar | Servir página waterfall + endpoint de ciclo de vida | ✅ Concluído | 2026-06-30 |
| 7 | `state.md` | Modificar | Alinhar estado ao progresso de T-702 | ✅ Concluído | 2026-06-30 |

**Legenda:** ⬜ Pendente · 🟡 Em progresso · ✅ Concluído

## Passos de implementação

1. Estender o `overview` do painel com os endpoints de ciclo de vida + teste.
2. Criar a página `horus-waterfall.html` consumindo T-503/504/505.
3. Linkar o waterfall a partir do painel.
4. Atualizar este plano e, ao final, o `state.md`; rodar `./mvnw -pl horus test`.

## Verificação / testes

- [x] `./mvnw -pl horus test` verde.

## Registro de alterações

| Data | Arquivo | Mudança |
|---|---|---|
| `2026-06-30` | `docs/tasks/T-702/PRD.md`, `docs/tasks/T-702/PLAN.md` | PRD e plano de execução criados |
| `2026-06-30` | `horus-waterfall.html` | Página waterfall (spans + queries + logs de erro) |
| `2026-06-30` | `panel/HorusPanelResource.java` | Endpoints de ciclo de vida no overview |
| `2026-06-30` | `horus-panel.html` | Link para a página waterfall |
| `2026-06-30` | `HorusPanelResourceTest.java` | Verifica página waterfall + endpoint de ciclo de vida |
| `2026-06-30` | `state.md` | Estado do projeto alinhado ao progresso de T-702 |
