# PRD — T-605: Agente Root-Cause Analyst (RCA correlacionando 3 sinais)

| Campo | Valor |
|---|---|
| **Task** | `T-605` |
| **Fase do roadmap** | `Fase 6 — IA (resumo, RCA, anomalias)` |
| **Status** | `Em revisão (PR)` |
| **Branch** | `task/T-605-rca-agent` |
| **PR** | `#<n>` (preencher ao abrir) |
| **Depende de** | `T-602` (e T-601/T-501) |
| **Requisitos atendidos** | `RF-H-007` |
| **ADRs relacionados** | `ADR-0011` |
| **Data** | `2026-06-28` |

## Objetivo

Terceiro agente de IA: o **Root-Cause Analyst** faz a **RCA** de um incidente correlacionando
os **três** sinais (trace + logs + métricas) e propõe **causa provável** + **próximos passos**.
Usa a camada **DEEP** (Opus — raciocínio mais forte, ADR-0011).

## Escopo (o que entra)

- `ContextAssembler.assembleForIncident(traceId, promQl, logLimit)` — monta os 3 sinais.
- `RootCauseAnalyst` (agente CDI): system prompt de RCA (causa provável + passos) na camada DEEP.
- `GET /horus/ai/rca/trace/{traceId}?promql=...` — RCA do incidente.

## Fora do escopo

- NL Query (T-607), cache/amostragem/seleção (T-608), painel (T-701).
- Detecção/clusterização de anomalias (T-606) — aqui a RCA é acionada por `traceId`.

## Premissas e dependências

- Sem `ANTHROPIC_API_KEY`, o `StubLlmEngine` responde (placeholder) — endpoint e CI verdes.

## Critérios de aceite

- [ ] Assembler monta os 3 sinais; `RootCauseAnalyst` usa a camada DEEP.
- [ ] Endpoint correlaciona trace+logs+métricas e retorna a RCA.
- [ ] `./mvnw -pl horus test` verde.

## Riscos

| Risco | Mitigação |
|---|---|
| RCA alucinada | ADR-0011: hipótese assistiva, não veredito; sempre ir ao sinal cru |

## Referências

- [`../../PRD.md`](../../PRD.md) · [`./PLAN.md`](./PLAN.md)
- [`../../adr/ADR-0011-camada-ia-claude.md`](../../adr/ADR-0011-camada-ia-claude.md)
