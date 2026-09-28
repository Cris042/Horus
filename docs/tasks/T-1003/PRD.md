# PRD — T-1003: Tiers de modelo reais + IA ao vivo

| Campo | Valor |
|---|---|
| **Task** | `T-1003` |
| **Fase do roadmap** | `Fase 10 — Produto utilizável` |
| **Status** | `Entregue` |
| **Branch** | `claude/beautiful-cray-sfblsr` |
| **PR** | — |
| **Depende de** | `T-601`, `T-608` |
| **Requisitos atendidos** | `RNF-H-003`, `RNF-H-008`, ADR-0011 |
| **ADRs relacionados** | `ADR-0011` (adendo) |
| **Data** | `2026-09-28` |

## Objetivo

Fazer a IA real do Horus funcionar de fato, com **um modelo por tarefa** (Haiku / Sonnet / Opus)
como a ADR-0011 prevê. A análise mostrou que o `ModelTier` era só rótulo e — pior — que o adapter
LangChain4j da T-601 **não conseguiria falar com os modelos atuais**.

## Achados (motivo da troca de biblioteca)

- `quarkus-langchain4j-anthropic` 1.1.0 envia sempre `temperature` e `top_k=40` (não opcional);
  Opus 4.7+/Opus 5/Sonnet 5 rejeitam parâmetros de amostragem com **400** — a IA real, com o
  `claude-opus-4-8` configurado, falharia em toda chamada.
- Um único modelo global (`chat-model.model-name`): a seleção por tarefa não acontecia.
- `system` concatenado ao prompt; `max_tokens` 1024.
- Motor real selecionável só por flag de **build**: `horus.ai.enabled=true` em runtime (compose/K8s)
  não surtia efeito.

## Escopo (o que entra)

- Adapter trocado para o **SDK oficial `com.anthropic:anthropic-java` 2.65.0** atrás da mesma porta
  `LlmEngine` (nenhum chamador muda). `AnthropicLlmEngine` único bean: *live* ou *stub* decidido em
  runtime (`horus.ai.enabled` + `ANTHROPIC_API_KEY`); o decorator de cache (T-608) segue valendo.
- `ModelTier`: `FAST` → `claude-haiku-4-5` (sem `effort`), `BALANCED` → `claude-sonnet-5`
  (`effort: medium`), `DEEP` → `claude-opus-5` (`effort: high` + `fallbacks: "default"`, beta
  `server-side-fallback-2026-07-01`, para recusas do classificador).
- `system` separado; `max_tokens` 16000; nenhum parâmetro de amostragem; `refusal` tratado;
  falha da API → **503** (IA opcional, RNF-H-008).
- `scripts/ai-smoke.sh` + job `ai-live` no CI (só com a secret; fora de PRs).
- Compose/K8s: `HORUS_AI_ENABLED` exposto; chave vazia deixa de ser problema.
- Adendo na ADR-0011 e `lib.md` atualizados.

## Fora do escopo

- Tool-calling / planejamento autônomo do NL Query.
- Streaming das respostas (respostas curtas; `max_tokens` 16000 sem streaming).
- Auditoria do que é enviado ao LLM → `T-1004`.

## Critérios de aceite

- [x] Cada camada vai ao seu modelo (teste de montagem da requisição por tier).
- [x] Nenhum `temperature`/`top_p`/`top_k` enviado; `effort` só onde o modelo aceita.
- [x] `system` enviado separado do prompt.
- [x] Modo live ativado em **runtime** (verificado com o jar empacotado: `/horus/ai/health` →
  `live`; chamada real à API com chave inválida → 401 da Anthropic → **503** no Horus).
- [x] Sem chave → modo stub; app e CI verdes (98 testes).
- [ ] Smoke com chave real — job `ai-live` roda quando a secret `ANTHROPIC_API_KEY` existir
  (pendência do usuário, já registrada em `state.md`).

## Riscos

| Risco | Mitigação |
|---|---|
| `fallbacks` pode rotear a outro modelo | `modelId` da resposta vem da API (`message.model()`), não do tier |
| Custo do job `ai-live` | 3 respostas curtas; só fora de PR |

## Referências

- [`../../adr/ADR-0011-camada-ia-claude.md`](../../adr/ADR-0011-camada-ia-claude.md) (adendo) · [`./PLAN.md`](./PLAN.md)
