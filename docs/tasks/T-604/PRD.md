# PRD — T-604: Agente Trace Explainer (explica um trace em linguagem natural)

| Campo | Valor |
|---|---|
| **Task** | `T-604` |
| **Fase do roadmap** | `Fase 6 — IA (resumo, RCA, anomalias)` |
| **Status** | `Em revisão (PR)` |
| **Branch** | `task/T-604-trace-explainer` |
| **PR** | `#<n>` (preencher ao abrir) |
| **Depende de** | `T-602` (e T-601/T-501) |
| **Requisitos atendidos** | `RF-H-006` |
| **ADRs relacionados** | `ADR-0011` |
| **Data** | `2026-06-28` |

## Objetivo

Segundo agente de IA: o **Trace Explainer** narra, em linguagem natural, o **caminho de uma
request** — serviços percorridos, onde o tempo foi gasto, onde falhou e o gargalo provável.
Reusa o par `ContextAssembler` (T-602) + `LlmEngine` (T-601), na camada **BALANCED** (Sonnet).

## Escopo (o que entra)

- `TraceExplainer` (agente CDI): system prompt de narração de trace + chamada ao `LlmEngine`
  na camada BALANCED. Retorna `TraceExplanation` com proveniência.
- `GET /horus/ai/explain/trace/{traceId}` — monta contexto do trace e explica.

## Fora do escopo

- RCA correlacionando 3 sinais (T-605), NL Query (T-607), painel (T-701).
- Waterfall de spans estruturado (T-503) — aqui é narrativa textual.

## Premissas e dependências

- Sem `ANTHROPIC_API_KEY`, o `StubLlmEngine` responde (placeholder) — endpoint e CI verdes.

## Critérios de aceite

- [ ] `TraceExplainer` usa a camada BALANCED e carrega o contexto do trace.
- [ ] Endpoint monta contexto do trace e retorna a explicação.
- [ ] `./mvnw -pl horus test` verde.

## Riscos

| Risco | Mitigação |
|---|---|
| Explicação imprecisa/alucinada | ADR-0011: assistência, não verdade; sempre ir ao trace cru |

## Referências

- [`../../PRD.md`](../../PRD.md) · [`./PLAN.md`](./PLAN.md)
- [`../../adr/ADR-0011-camada-ia-claude.md`](../../adr/ADR-0011-camada-ia-claude.md)
