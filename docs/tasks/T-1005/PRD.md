# PRD — T-1005: Retenção configurável por tipo de sinal

| Campo | Valor |
|---|---|
| **Task** | `T-1005` |
| **Fase do roadmap** | `Fase 10 — Produto utilizável` |
| **Status** | `Entregue` |
| **Branch** | `claude/beautiful-cray-sfblsr` |
| **PR** | — |
| **Depende de** | `T-404` |
| **Requisitos atendidos** | `RNF-H-005` |
| **ADRs relacionados** | `ADR-0010` |
| **Data** | `2026-09-28` |

## Objetivo

RNF-H-005 ("retenção por tipo de telemetria configurável") nunca teve task. O Jaeger guardava
traces só em memória (perdidos no restart) e Loki/Prometheus usavam padrões embutidos.

## Escopo (o que entra)

- **Traces:** Jaeger v2 com config própria (`deploy/telemetry/jaeger-config.yaml`) — Badger em
  disco (volume `jaeger-data`) com TTL `JAEGER_RETENTION` (168h); init de uma linha ajusta o dono
  do volume (Jaeger roda como uid 10001).
- **Logs:** Loki com compactor (`retention_enabled`) e `retention_period: ${LOKI_RETENTION:-168h}`
  (`-config.expand-env=true`).
- **Métricas:** Prometheus `--storage.tsdb.retention.time=${PROMETHEUS_RETENTION:-15d}`.
- Documentação em `deploy/telemetry/README.md`.

## Fora do escopo

- Retenção no Kubernetes → junto do overlay de infraestrutura (`T-1009`).
- Retenção diferenciada por serviço/tenant.

## Critérios de aceite

- [x] Os três backends sobem com a nova config (`docker compose up --wait`).
- [x] Valores vêm das variáveis: com `LOKI_RETENTION=72h PROMETHEUS_RETENTION=3d` o Loki reporta
  `retention_period: 3d` e o Prometheus `storage.tsdb.retention.time=3d`.
- [x] Jaeger persiste traces em disco: trace enviado via OTLP continua consultável após restart
  do contêiner (antes: perdido).

## Riscos

| Risco | Mitigação |
|---|---|
| Badger cresce em disco | TTL + volume dedicado; `make clean` apaga |
| Volume com dono root | Init `jaeger-volume-init` (mesma imagem, sem pull extra) |

## Referências

- [`../../../deploy/telemetry/README.md`](../../../deploy/telemetry/README.md) · [`./PLAN.md`](./PLAN.md)
