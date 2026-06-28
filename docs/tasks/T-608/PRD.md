# PRD — T-608: Cache de respostas + seleção de modelo por tarefa (salvaguardas de custo/latência)

| Campo | Valor |
|---|---|
| **Task** | `T-608` |
| **Fase do roadmap** | `Fase 6 — IA (resumo, RCA, anomalias)` |
| **Status** | `Em revisão (PR)` |
| **Branch** | `task/T-608-llm-cache` |
| **PR** | `#<n>` (preencher ao abrir) |
| **Depende de** | `T-603`, `T-604`, `T-605`, `T-607` |
| **Requisitos atendidos** | `RNF-H-003`, `RNF-H-004` |
| **ADRs relacionados** | `ADR-0011` |
| **Data** | `2026-06-28` |

## Objetivo

Salvaguardas de **custo** (RNF-H-003) e **latência** (RNF-H-004) da camada de IA. Esta fatia
entrega o **cache de respostas do LLM** — evita repetir chamadas idênticas — de forma
transparente (decorator sobre a porta `LlmEngine`), e consolida as outras duas salvaguardas
já realizadas: **seleção de modelo por tarefa** (`ModelTier` FAST/BALANCED/DEEP nos agentes) e
**amostragem/orçamento** de contexto (`TokenBudget`/truncamento, T-602).

## Escopo (o que entra)

- `LlmResponseCache` — mapa LRU limitado (config `max-entries`), hit/miss, on/off por config.
- `CachingLlmEngine` — **CDI decorator** sobre `LlmEngine`: serve respostas idênticas do cache
  sem que nenhum chamador (agentes/endpoints) precise saber.
- `GET /horus/ai/cache/stats` — observabilidade (enabled/size/hits/misses).
- Config `horus.ai.cache.{enabled,max-entries}`.

## Fora do escopo

- Cache distribuído/persistente (memória local basta para dev; escalar é fase posterior).
- Invalidação por tempo (TTL) — fatia seguinte se necessário.
- Amostragem de spans na ingestão (Collector) — aqui a amostragem é no orçamento de contexto.

## Premissas e dependências

- Os 3 agentes-núcleo + NL Query (T-603..607) já usam `ModelTier` por tarefa — a seleção de
  modelo já está distribuída; T-608 a documenta e adiciona o cache por cima.

## Critérios de aceite

- [ ] Decorator cacheia: dois pedidos idênticos → ≥1 hit (verificado por `/cache/stats`).
- [ ] Cache limitado (LRU) e desativável por config.
- [ ] `./mvnw -pl horus test` verde.

## Riscos

| Risco | Mitigação |
|---|---|
| Cache servir resposta velha | Chave inclui prompt completo; TTL/invalidação é fatia seguinte se preciso |
| Crescimento de memória | LRU com `max-entries` configurável (default 200) |

## Referências

- [`../../PRD.md`](../../PRD.md) · [`./PLAN.md`](./PLAN.md)
- [`../../adr/ADR-0011-camada-ia-claude.md`](../../adr/ADR-0011-camada-ia-claude.md)
