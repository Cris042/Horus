# horus/ — Plataforma Horus (observabilidade com IA)

**O produto principal.** Correlaciona toda a telemetria do sistema observado e usa **IA (Claude)** para resumir, explicar e diagnosticar em linguagem natural. É o "olho que tudo vê".

- **Stack:** Quarkus (Java 25) + Quarkus LangChain4j (Anthropic/Claude).
- **Requisitos:** `RF-H-*`, `RNF-H-*`.

> **Estado atual:** o bootstrap T-002 já evoluiu para uma primeira fatia funcional do Horus:
> adaptadores de consulta a Jaeger/Loki/Prometheus, modelo de correlação por `trace_id`,
> camada de IA com stub/Anthropic, agentes de resumo/explicação/RCA/NL query, cache de LLM,
> painel estático inicial e RBAC por papel. Veja `state.md` para a branch e próxima task.

## Responsabilidades (por fase)

- **Core (Fase 5):** adaptadores de consulta e correlação por `trace_id` já existem; faltam as APIs detalhadas de ciclo de vida de **request** e **query**, agregação dedicada de logs de erro, mapa de serviços e visualização de **SAGA**.
- **IA (Fase 6):** Summarizer, Trace Explainer, Root-Cause Analyst, NL Query, orçamento de tokens e cache já existem; falta Anomaly Detector + Error Clusterer (`T-606`) e uso real depende de `ANTHROPIC_API_KEY`.
- **Painel e alertas (Fase 7):** dashboard inicial e RBAC já existem; faltam waterfall visual (`T-702`) e alertas com resumo de IA (`T-703`).

## Não-intrusividade

Derrubar o Horus ou a camada de IA **não afeta** os serviços de domínio (RNF-H-008). É implantado **separado** do domínio (ADR-0012).

## Tasks

`T-002` (bootstrap) · `T-501`..`T-507` (core) · `T-601`..`T-608` (IA) · `T-701`..`T-704` (painel/alertas).
