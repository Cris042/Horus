# Plano de Execução — T-608: Cache de respostas do LLM + salvaguardas de custo/latência

> ⚠️ **Regra R2 ([WORKFLOW](../../WORKFLOW.md)).** Atualize `Status`/`Atualizado` e o registro ao criar/modificar cada arquivo.

| Campo | Valor |
|---|---|
| **Task** | `T-608` |
| **Branch** | `task/T-608-llm-cache` |
| **PRD** | [`./PRD.md`](./PRD.md) |
| **Progresso** | `6/6` arquivos |

## Arquivos a criar/modificar

| # | Arquivo | Ação | Propósito | Status | Atualizado |
|---|---|---|---|---|---|
| 1 | `horus/.../ai/LlmResponseCache.java` | Criar | mapa LRU + hit/miss + config | ✅ Concluído | 2026-06-28 |
| 2 | `horus/.../ai/CachingLlmEngine.java` | Criar | CDI decorator sobre `LlmEngine` | ✅ Concluído | 2026-06-28 |
| 3 | `horus/.../api/HorusCacheResource.java` | Criar | `GET /horus/ai/cache/stats` | ✅ Concluído | 2026-06-28 |
| 4 | `horus/src/main/resources/application.properties` | Modificar | `horus.ai.cache.*` | ✅ Concluído | 2026-06-28 |
| 5 | `horus/.../ai/LlmResponseCacheTest.java` | Criar | teste do cache (LRU/hit/miss/off) | ✅ Concluído | 2026-06-28 |
| 6 | `horus/.../api/HorusCacheResourceTest.java` | Criar | teste do decorator (hit ponta a ponta) | ✅ Concluído | 2026-06-28 |

**Legenda:** ⬜ Pendente · 🟡 Em progresso · ✅ Concluído

## Passos de implementação

1. `LlmResponseCache` (LRU `LinkedHashMap` access-order + contadores).
2. `CachingLlmEngine` decorator (`@Decorator` + `@Delegate`).
3. Endpoint de stats + config.
4. Testes (unidade do cache + @QuarkusTest provando o hit).

## Verificação / testes

- [x] `./mvnw -pl horus test` → **BUILD SUCCESS** (24 testes; 4 novos).

## Registro de alterações

| Data | Arquivo | Mudança |
|---|---|---|
| `2026-06-28` | (todos acima) | Cache de respostas do LLM (T-608) |
