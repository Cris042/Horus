# PRD — T-602: Montador de contexto da IA (telemetria sanitizada → prompt com orçamento de tokens)

| Campo | Valor |
|---|---|
| **Task** | `T-602` |
| **Fase do roadmap** | `Fase 6 — IA (resumo, RCA, anomalias)` |
| **Status** | `Entregue` |
| **Branch** | `task/T-602-context-assembler` |
| **PR** | `#22` |
| **Depende de** | `T-501`, `T-406` (T-502 enriquece depois) |
| **Requisitos atendidos** | `RNF-H-003`, `RNF-H-006` |
| **ADRs relacionados** | `ADR-0011`, `ADR-0009` |
| **Data** | `2026-06-28` |

## Objetivo

Construir o **montador de contexto** da IA — o gargalo de valor do Horus (ROADMAP). Transforma
telemetria (trace + logs + métricas, das portas do T-501) num **prompt compacto, sanitizado e
dentro de um orçamento de tokens**. Os agentes da Fase 6 (Summarizer, Trace Explainer, RCA)
decidem *o que perguntar*; este componente decide *o que cabe e é seguro mandar*.

## Escopo (o que entra)

- `TokenBudget` — estimativa heurística (~4 chars/token) + truncamento por orçamento (RNF-H-003).
- `PromptSanitizer` — guarda final de PII na fronteira do prompt (e-mail/CPF/cartão; RNF-H-006).
- `ContextAssembler` — `assembleForTrace(traceId)` (via portas) e `assemble(trace, logs, metrics)`
  (sinais já obtidos; usável pelo modelo de correlação T-502 e em testes). Prioridade
  trace → logs → métricas; trunca por linha quando o orçamento aperta; marca `truncated`.
- `PromptContext` — texto + tokens estimados + `truncated` + sinais incluídos (proveniência).
- Config `horus.ai.context.max-tokens` (default 4000).

## Fora do escopo

- O **modelo de correlação** (T-502) que escolhe *quais* sinais juntar por `trace_id` —
  aqui o assembler aceita sinais já correlacionados.
- Os agentes que consomem o contexto (T-603 Summarizer, T-604 Explainer, T-605 RCA).
- Cache de resumos / amostragem / seleção de modelo por tarefa (T-608).
- Contagem de tokens exata via API Anthropic (exige chave) — heurística é suficiente p/ bound.

## Premissas e dependências

- Portas de consulta do `T-501` entregam os sinais; PII já reduzida na origem (T-401) e na
  borda (T-406) — o `PromptSanitizer` é a **3ª** rede, não a garantia primária.

## Critérios de aceite

- [ ] Monta contexto de trace + logs + métricas respeitando o orçamento (trunca, sinaliza).
- [ ] PII (e-mail/CPF/cartão) mascarada no texto final.
- [ ] `./mvnw -pl horus test` verde.

## Riscos

| Risco | Mitigação |
|---|---|
| Heurística de tokens imprecisa | Conservadora (~4 chars/tok); refinar com `count_tokens` quando houver chave |
| PII escapar | Defesa em profundidade (origem T-401 + borda T-406 + esta) |

## Referências

- [`../../PRD.md`](../../PRD.md) · [`./PLAN.md`](./PLAN.md)
- [`../../adr/ADR-0011-camada-ia-claude.md`](../../adr/ADR-0011-camada-ia-claude.md)
- [`../../telemetry/CONTRACT.md`](../../telemetry/CONTRACT.md) §6 (PII)
