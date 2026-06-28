# Plano de Execução — T-701: Painel Horus (saúde + resumo de IA + busca)

> ⚠️ **Regra R2 ([WORKFLOW](../../WORKFLOW.md)).** Sempre que **criar ou modificar** um arquivo desta tabela, atualize **na mesma hora** o seu `Status` e `Atualizado`, e registre a mudança em *Registro de alterações*. Arquivo não previsto que precise ser tocado deve ser **adicionado** à tabela.

| Campo | Valor |
|---|---|
| **Task** | `T-701` |
| **Branch** | `task/T-701-horus-panel` |
| **PRD** | [`./PRD.md`](./PRD.md) |
| **Progresso** | `3/3` arquivos |

## Arquivos a criar/modificar

| # | Arquivo | Ação | Propósito | Status | Atualizado |
|---|---|---|---|---|---|
| 1 | `horus/src/main/java/org/example/horus/panel/HorusPanelResource.java` | Criar | API de agregação `GET /horus/panel/overview` (saúde + capacidades) | ✅ Concluído | 2026-06-28 |
| 2 | `horus/src/main/resources/META-INF/resources/horus-panel.html` | Criar | Página estática do painel (saúde, ask, busca, capacidades) | ✅ Concluído | 2026-06-28 |
| 3 | `horus/src/test/java/org/example/horus/panel/HorusPanelResourceTest.java` | Criar | Teste da API de overview e do serviço da página estática | ✅ Concluído | 2026-06-28 |

**Legenda:** ⬜ Pendente · 🟡 Em progresso · ✅ Concluído

## Passos de implementação

1. `HorusPanelResource` injeta `LlmEngine` + `LlmResponseCache` e as URLs de backend por config; compõe `Overview(service, ai, backends, endpoints)`.
2. Página `horus-panel.html` consome `/horus/panel/overview` e chama `/horus/ai/*` diretamente do navegador (ask/explain/rca).
3. Teste `@QuarkusTest` valida o JSON do overview (modo stub) e o serviço da página estática.

## Verificação / testes

- [x] `mvn -pl horus test` verde (26/26, 2 novos em `HorusPanelResourceTest`).

## Registro de alterações

| Data | Arquivo | Mudança |
|---|---|---|
| `2026-06-28` | `HorusPanelResource.java` | API de overview (saúde + capacidades) criada |
| `2026-06-28` | `horus-panel.html` | Página estática do painel criada |
| `2026-06-28` | `HorusPanelResourceTest.java` | Testes de overview + serviço da página |
