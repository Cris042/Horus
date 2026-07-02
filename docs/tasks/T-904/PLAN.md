# Plano de Execução — T-904: Auditoria de privacidade

> ⚠️ **Regra R2 ([WORKFLOW](../../WORKFLOW.md)).** Sempre que **criar ou modificar** um arquivo desta tabela, atualize **na mesma hora** o seu `Status` e `Atualizado`, e registre a mudança em *Registro de alterações*. Arquivo não previsto que precise ser tocado deve ser **adicionado** à tabela.

| Campo | Valor |
|---|---|
| **Task** | `T-904` |
| **Branch** | `task/T-904-privacy-audit` |
| **PRD** | [`./PRD.md`](./PRD.md) |
| **Progresso** | `4/4` arquivos |

## Arquivos a criar/modificar

| # | Arquivo | Ação | Propósito | Status | Atualizado |
|---|---|---|---|---|---|
| 1 | `docs/tasks/T-904/PRD.md` | Criar | Metodologia + evidência da auditoria + gap encontrado/corrigido | ✅ Concluído | 2026-07-02 |
| 2 | `docs/tasks/T-904/PLAN.md` | Criar | Plano de execução desta task | ✅ Concluído | 2026-07-02 |
| 3 | `horus/src/main/java/org/example/horus/ai/anomaly/ErrorClusterer.java` | Modificar | Passa o prompt do rótulo por `PromptSanitizer` (gap encontrado) | ✅ Concluído | 2026-07-02 |
| 4 | `horus/src/test/java/org/example/horus/ai/anomaly/ErrorClustererTest.java` | Modificar | Regressão: prompt real capturado via mock não contém PII crua | ✅ Concluído | 2026-07-02 |

> `state.md` atualizado ao final.

## Passos de implementação

1. Camada 1 (origem): `POST /prontuarios` com valor PII-like; inspecionar `db.statement` real
   no Jaeger + `grep` no Loki.
2. Camada 2 (borda do Collector): montar e enviar span/log sintéticos direto ao OTLP do
   Collector com chaves proibidas + padrões de PII; inspecionar o que chegou ao Jaeger/Loki.
3. Camada 3 (fronteira do prompt): `grep` por todo ponto de chamada de `LlmEngine.complete`;
   para cada um, confirmar se o texto passa por `PromptSanitizer` (direto ou via
   `ContextAssembler`).
4. **Gap encontrado**: `ErrorClusterer.label()` não sanitizava. Corrigir.
5. Adicionar teste de regressão isolado (sem CDI, mocks diretos) capturando o `LlmRequest`
   real via `ArgumentCaptor` e afirmando ausência de PII crua.
6. `./mvnw -pl horus -am test` verde.

## Verificação / testes

- [x] `db.statement` do INSERT real parametrizado (sem PII), span+log sintéticos confirmam
      redação real no Jaeger/Loki via OTLP direto ao Collector.
- [x] `./mvnw -pl horus -am test` → 64/64 (inclui o novo teste de regressão).
- [x] Todos os pontos de chamada de `LlmEngine.complete` no módulo `horus` auditados
      (`NlQueryAgent`, `RootCauseAnalyst`, `TraceExplainer`, `StateSummarizer`,
      `ErrorClusterer`, `AlertService`).

## Registro de alterações

| Data | Arquivo | Mudança |
|---|---|---|
| `2026-07-02` | `docs/tasks/T-904/*` | PRD (metodologia + evidência + gap) e plano criados |
| `2026-07-02` | `horus/.../ErrorClusterer.java` | Prompt do rótulo passa por `PromptSanitizer` antes de `engine.complete` |
| `2026-07-02` | `horus/.../ErrorClustererTest.java` | Novo teste: `LlmRequest` real capturado via mock não contém PII crua |
