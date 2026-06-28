# Plano de Execução — T-601: Integração Quarkus LangChain4j (Anthropic) atrás de interface desacoplada

> ⚠️ **Regra R2 ([WORKFLOW](../../WORKFLOW.md)).** Atualize `Status`/`Atualizado` e o registro ao criar/modificar cada arquivo.

| Campo | Valor |
|---|---|
| **Task** | `T-601` |
| **Branch** | `task/T-601-llm-anthropic-integration` |
| **PRD** | [`./PRD.md`](./PRD.md) |
| **Progresso** | `10/10` arquivos |

## Arquivos a criar/modificar

| # | Arquivo | Ação | Propósito | Status | Atualizado |
|---|---|---|---|---|---|
| 1 | `pom.xml` (parent) | Modificar | property `quarkus-langchain4j.version=1.1.0` | ✅ Concluído | 2026-06-27 |
| 2 | `horus/pom.xml` | Modificar | dependência `quarkus-langchain4j-anthropic` | ✅ Concluído | 2026-06-27 |
| 3 | `horus/.../ai/ModelTier.java` | Criar | camadas FAST/BALANCED/DEEP → IDs de modelo | ✅ Concluído | 2026-06-27 |
| 4 | `horus/.../ai/LlmEngine.java` | Criar | porta desacoplada + DTOs | ✅ Concluído | 2026-06-27 |
| 5 | `horus/.../ai/StubLlmEngine.java` | Criar | impl default (sem chave) | ✅ Concluído | 2026-06-27 |
| 6 | `horus/.../ai/LangChain4jLlmEngine.java` | Criar | adapter real (flag de build) | ✅ Concluído | 2026-06-27 |
| 7 | `horus/.../api/HorusAiResource.java` | Criar | API `/horus/ai/{health,complete}` | ✅ Concluído | 2026-06-27 |
| 8 | `horus/src/main/resources/application.properties` | Modificar | config da IA (modelo, chave, timeout, flag) | ✅ Concluído | 2026-06-27 |
| 9 | `horus/.../api/HorusAiResourceTest.java` | Criar | teste do modo stub | ✅ Concluído | 2026-06-27 |
| 10 | `docs/tasks/T-601/{PRD,PLAN}.md` | Criar | docs da task | ✅ Concluído | 2026-06-27 |

**Legenda:** ⬜ Pendente · 🟡 Em progresso · ✅ Concluído

## Passos de implementação

1. Fixar a versão da extensão (property no parent) e adicionar a dependência no `horus`.
2. Modelar `ModelTier`, a porta `LlmEngine` e os DTOs.
3. `StubLlmEngine` default e `LangChain4jLlmEngine` mutuamente exclusivos por
   `@IfBuildProperty(horus.ai.enabled)` (evita ambiguidade CDI).
4. Expor a API REST; configurar modelo/chave/timeout.
5. Testar o modo stub e validar o build com a extensão.

## Verificação / testes

- [x] `./mvnw -pl horus test` → **BUILD SUCCESS** (8 testes; extensão sob Quarkus 3.37 + JDK 25).

## Registro de alterações

| Data | Arquivo | Mudança |
|---|---|---|
| `2026-06-27` | (todos acima) | Camada de IA desacoplada do Horus (T-601) |
| `2026-06-27` | `Stub/LangChain4jLlmEngine` | `@LookupIfProperty`→`@IfBuildProperty` (resolve ambiguidade CDI) |
