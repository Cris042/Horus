# PRD — T-603: Agente Summarizer (resumo de estado sob demanda + agendado)

| Campo | Valor |
|---|---|
| **Task** | `T-603` |
| **Fase do roadmap** | `Fase 6 — IA (resumo, RCA, anomalias)` |
| **Status** | `Em revisão (PR)` |
| **Branch** | `task/T-603-summarizer-agent` |
| **PR** | `#<n>` (preencher ao abrir) |
| **Depende de** | `T-602` (e T-601/T-501) |
| **Requisitos atendidos** | `RF-H-005` |
| **ADRs relacionados** | `ADR-0011` |
| **Data** | `2026-06-28` |

## Objetivo

Primeiro **agente de IA** do Horus: o **Summarizer** resume, em linguagem natural, o estado
do sistema observado. Combina as fatias anteriores — `ContextAssembler` (T-602) monta o
contexto sanitizado e com orçamento; `LlmEngine` (T-601) gera o resumo — entregando o
primeiro caso de uso ponta a ponta da plataforma (RF-H-005).

## Escopo (o que entra)

- `StateSummarizer` (agente CDI): system prompt de resumo + chamada ao `LlmEngine` na camada
  **FAST** (Haiku — alto volume/baixo custo, ADR-0011). Retorna `StateSummary` com proveniência.
- **Sob demanda:** `GET /horus/ai/summary/trace/{traceId}` — monta contexto do trace e resume.
- **Agendado:** `ScheduledStateSummary` (`@Scheduled`, cron via config, **`off` por padrão`**):
  consulta uma métrica de saúde, monta contexto e registra o resumo no log. Best-effort.
- Config (`horus.ai.summary.cron`, `horus.ai.summary.promql`) + extensão `quarkus-scheduler`.

## Fora do escopo

- Persistência/notificação dos resumos (T-608/T-701) e painel (T-701).
- Os demais agentes: Trace Explainer (T-604), RCA (T-605), NL Query (T-607).
- Cache/amostragem/seleção fina de modelo (T-608).

## Premissas e dependências

- Sem `ANTHROPIC_API_KEY` o `StubLlmEngine` responde (placeholder) — o agente e os endpoints
  funcionam e a CI roda verde; o resumo "real" exige a chave + `horus.ai.enabled=true`.

## Critérios de aceite

- [ ] `StateSummarizer` usa a camada FAST e carrega o contexto montado.
- [ ] Endpoint sob demanda monta contexto do trace e resume.
- [ ] Job agendado desligado por padrão; quando ligado, é best-effort (não derruba o serviço).
- [ ] `./mvnw -pl horus test` verde.

## Riscos

| Risco | Mitigação |
|---|---|
| Job agendado disparar sem backends | `cron=off` por padrão; `try/catch` best-effort |
| Resumo "alucinado" | ADR-0011: assistência, não verdade; sempre permitir ir ao trace cru |

## Referências

- [`../../PRD.md`](../../PRD.md) · [`./PLAN.md`](./PLAN.md)
- [`../../adr/ADR-0011-camada-ia-claude.md`](../../adr/ADR-0011-camada-ia-claude.md)
