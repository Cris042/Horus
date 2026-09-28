# Plano de Execução — T-1003: Tiers de modelo reais + IA ao vivo

> ⚠️ **Regra R2 ([WORKFLOW](../../WORKFLOW.md)).** Sempre que **criar ou modificar** um arquivo desta tabela, atualize **na mesma hora** o seu `Status` e `Atualizado`, e registre a mudança em *Registro de alterações*. Arquivo não previsto que precise ser tocado deve ser **adicionado** à tabela.

| Campo | Valor |
|---|---|
| **Task** | `T-1003` |
| **Branch** | `claude/beautiful-cray-sfblsr` |
| **PRD** | [`./PRD.md`](./PRD.md) |
| **Progresso** | `22/22` arquivos |

## Arquivos a criar/modificar

| # | Arquivo | Ação | Propósito | Status | Atualizado |
|---|---|---|---|---|---|
| 1 | `pom.xml` | Modificar | `anthropic-java.version` no lugar de `quarkus-langchain4j.version` | ✅ Concluído | 2026-09-28 |
| 2 | `horus/pom.xml` | Modificar | Dependência `com.anthropic:anthropic-java` | ✅ Concluído | 2026-09-28 |
| 3 | `horus/src/main/java/org/example/horus/ai/AnthropicLlmEngine.java` | Criar | Motor único (live/stub em runtime) sobre o SDK oficial | ✅ Concluído | 2026-09-28 |
| 4 | `horus/src/main/java/org/example/horus/ai/LangChain4jLlmEngine.java` | Remover | Substituído | ✅ Concluído | 2026-09-28 |
| 5 | `horus/src/main/java/org/example/horus/ai/StubLlmEngine.java` | Remover | Modo stub incorporado ao motor | ✅ Concluído | 2026-09-28 |
| 6 | `horus/src/main/java/org/example/horus/ai/ModelTier.java` | Modificar | Modelos atuais + `effort` por camada | ✅ Concluído | 2026-09-28 |
| 7 | `horus/src/main/resources/application.properties` | Modificar | Remove config LangChain4j | ✅ Concluído | 2026-09-28 |
| 8 | `horus/src/main/java/org/example/horus/ai/anomaly/ErrorClusterer.java` | Modificar | Javadoc (nome do motor) | ✅ Concluído | 2026-09-28 |
| 9 | `horus/src/main/java/org/example/horus/alert/AlertService.java` | Modificar | Javadoc (nome do motor) | ✅ Concluído | 2026-09-28 |
| 10 | `horus/src/test/java/org/example/horus/ai/AnthropicLlmEngineTest.java` | Criar | Requisição por camada, sem amostragem, stub/live | ✅ Concluído | 2026-09-28 |
| 11 | `horus/src/test/java/org/example/horus/**` | Modificar | IDs de modelo atualizados nos testes existentes | ✅ Concluído | 2026-09-28 |
| 12 | `scripts/ai-smoke.sh` | Criar | Smoke real por camada | ✅ Concluído | 2026-09-28 |
| 13 | `.github/workflows/ci.yml` | Modificar | Job `ai-live` (gated pela secret) | ✅ Concluído | 2026-09-28 |
| 14 | `deploy/docker-compose.yml` | Modificar | `HORUS_AI_ENABLED`; chave vazia permitida | ✅ Concluído | 2026-09-28 |
| 15 | `deploy/k8s/config.yaml` | Modificar | `HORUS_AI_ENABLED` | ✅ Concluído | 2026-09-28 |
| 16 | `deploy/k8s/secrets.example.yaml` | Modificar | Comentário atualizado | ✅ Concluído | 2026-09-28 |
| 17 | `deploy/README.md` | Modificar | Como ligar a IA real | ✅ Concluído | 2026-09-28 |
| 18 | `docs/adr/ADR-0011-camada-ia-claude.md` | Modificar | Adendo T-1003 + IDs atuais | ✅ Concluído | 2026-09-28 |
| 19 | `lib.md` | Modificar | SDK e IDs de modelo | ✅ Concluído | 2026-09-28 |
| 20 | `docs/tasks/T-1003/PRD.md` | Criar | PRD | ✅ Concluído | 2026-09-28 |
| 21 | `docs/tasks/T-1003/PLAN.md` | Criar | Este plano | ✅ Concluído | 2026-09-28 |
| 22 | `state.md` | Modificar | R1 | ✅ Concluído | 2026-09-28 |

**Legenda:** ⬜ Pendente · 🟡 Em progresso · ✅ Concluído

## Passos de implementação

1. Confirmar (bytecode da extensão) que o LangChain4j envia `temperature`/`top_k` sempre.
2. Trocar para `anthropic-java` atrás da porta `LlmEngine`; seleção live/stub em runtime.
3. Tiers com modelos atuais + `effort`; `fallbacks` no DEEP; 503 em falha.
4. Testes sem rede; validação do jar empacotado contra a API real; smoke + job de CI.

## Verificação / testes

- [x] `./mvnw -pl horus test` (JDK 25) → **98 testes, 0 falhas**.
- [x] Jar empacotado com `HORUS_AI_ENABLED=true` + chave inválida: `mode=live`, API real devolve 401,
  Horus responde 503 (não 500).
- [x] e2e (T-1002) verde com o novo motor em modo stub.
- [ ] `scripts/ai-smoke.sh` com chave real (job `ai-live`) — depende da secret.

## Registro de alterações

| Data | Arquivo | Mudança |
|---|---|---|
| `2026-09-28` | (todos acima) | SDK oficial + tiers reais (T-1003) |
