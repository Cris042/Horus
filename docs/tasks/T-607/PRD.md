# PRD — T-607: Agente NL Query ("pergunte ao Horus")

| Campo | Valor |
|---|---|
| **Task** | `T-607` |
| **Fase do roadmap** | `Fase 6 — IA (resumo, RCA, anomalias)` |
| **Status** | `Em revisão (PR)` |
| **Branch** | `task/T-607-nl-query-agent` |
| **PR** | `#<n>` (preencher ao abrir) |
| **Depende de** | `T-602` (e T-601/T-501) |
| **Requisitos atendidos** | `RF-H-010` |
| **ADRs relacionados** | `ADR-0011` |
| **Data** | `2026-06-28` |

## Objetivo

Agente **NL Query** — "pergunte ao Horus": responde a perguntas em linguagem natural
**fundamentadas** na telemetria. Reusa `ContextAssembler` (T-602) + `LlmEngine` (T-601),
camada **BALANCED** (Sonnet).

## Escopo (o que entra)

- `NlQueryAgent.answer(question, context)` — responde fundamentado no contexto, recusando-se a
  inventar quando faltam dados.
- `POST /horus/ai/ask` `{question, traceId?, promql?, logLimit?}` — recorta o escopo (incidente)
  e responde; valida `question` (400 se ausente).

## Fora do escopo

- **Planejamento autônomo de consultas** (a IA decidir *quais* backends consultar via
  tool-calling do LangChain4j) — fatia seguinte; aqui o escopo é recortado pelo chamador.
- Detecção de anomalias (T-606), cache/seleção (T-608), painel (T-701).

## Premissas e dependências

- Sem `ANTHROPIC_API_KEY`, o `StubLlmEngine` responde (placeholder) — endpoint e CI verdes.

## Critérios de aceite

- [ ] `NlQueryAgent` usa a camada BALANCED, carrega pergunta + contexto.
- [ ] Endpoint recorta o escopo por trace/PromQL e responde; valida `question`.
- [ ] `./mvnw -pl horus test` verde.

## Riscos

| Risco | Mitigação |
|---|---|
| Resposta inventada sem dados | System prompt instrui a recusar; sempre ir ao sinal cru |

## Referências

- [`../../PRD.md`](../../PRD.md) · [`./PLAN.md`](./PLAN.md)
- [`../../adr/ADR-0011-camada-ia-claude.md`](../../adr/ADR-0011-camada-ia-claude.md)
