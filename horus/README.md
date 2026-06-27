# horus/ — Plataforma Horus (observabilidade com IA)

**O produto principal.** Correlaciona toda a telemetria do sistema observado e usa **IA (Claude)** para resumir, explicar e diagnosticar em linguagem natural. É o "olho que tudo vê".

- **Stack:** Quarkus (Java 25) + Quarkus LangChain4j (Anthropic/Claude).
- **Requisitos:** `RF-H-*`, `RNF-H-*`.

> **Destino do bootstrap em T-002:** o `pom.xml`/`src/` que hoje vivem na raiz do repositório migram para cá quando o scaffold IntelliJ for substituído pelo bootstrap Quarkus.

## Responsabilidades (por fase)

- **Core (Fase 5):** receptor/adaptadores OTLP → Jaeger/Tempo, Loki, Prometheus; correlação por `trace_id`; ciclo de vida de **request** e **query**; agregação de logs de erro; mapa de serviços; visualização de **SAGA**.
- **IA (Fase 6):** Summarizer, Trace Explainer, Root-Cause Analyst, Anomaly Detector e NL Query — sobre contexto **sanitizado** e com orçamento de tokens.
- **Painel e alertas (Fase 7):** dashboard próprio, waterfall do fluxo de vida, alertas com resumo de IA, RBAC.

## Não-intrusividade

Derrubar o Horus ou a camada de IA **não afeta** os serviços de domínio (RNF-H-008). É implantado **separado** do domínio (ADR-0012).

## Tasks

`T-002` (bootstrap) · `T-501`..`T-507` (core) · `T-601`..`T-608` (IA) · `T-701`..`T-704` (painel/alertas).
