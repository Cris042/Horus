# PRD — T-503: API do ciclo de vida da request

| Campo | Valor |
|---|---|
| **Task** | `T-503` |
| **Fase do roadmap** | `Fase 5 — Horus core` |
| **Status** | `Em progresso` |
| **Branch** | `task/T-503-request-lifecycle-api` |
| **PR** | `—` |
| **Depende de** | `T-502` |
| **Requisitos atendidos** | `RF-H-001`, `RF-H-011` |
| **ADRs relacionados** | `ADR-0007`, `ADR-0008`, `ADR-0009`, `ADR-0010` |
| **Data** | `2026-06-28` |

## Objetivo

Expor uma API do Horus que devolve o ciclo de vida de uma request como uma timeline/waterfall
de spans, com ordem temporal, offset relativo, duração, profundidade e classificação operacional.
Esta é a base de API para o painel visualizar o fluxo ponta a ponta em T-702.

## Escopo (o que entra)

- Modelo `RequestLifecycle` com spans de waterfall derivados de um `trace_id`.
- Serviço determinístico que transforma `TraceResult` em timeline ordenada por início do span.
- Enriquecimento neutro de `SpanRef` com `startTimeMicros`, `parentSpanId` e `kind`, preservando o construtor antigo.
- Adapter Jaeger lendo `startTime`, referência pai (`CHILD_OF`) e `span.kind`.
- Endpoint `GET /horus/lifecycle/requests/{traceId}` com 200, 400 para `traceId` inválido e 404 se o trace não existir.
- Testes `@QuarkusTest` com porta de trace mockada.

## Fora do escopo

- Detalhamento de statement SQL, banco de origem e query sanitizada → `T-504`.
- Agregação dedicada de logs de erro correlacionados → `T-505`.
- Renderização visual do waterfall no painel → `T-702`.
- Validação com stack real ponta a ponta → `T-405`.

## Premissas e dependências

- `T-502` foi mergeada em `main` no PR #35 e fornece a base de correlação por `trace_id`.
- O trace real vem do Jaeger pela porta `TraceQueryPort`; esta task não consulta Loki nem Prometheus.
- `trace_id` segue W3C/OTel: 32 caracteres hexadecimais, normalizado para minúsculas.
- Spans sem timestamp continuam aceitos para compatibilidade, mas têm offset `0` e ficam após spans temporais.

## Critérios de aceite

- [ ] API retorna uma timeline ordenada por `startTimeMicros`, com `offsetMicros`, `durationMicros` e `depth`.
- [ ] API identifica categorias mínimas: `http`, `database`, `messaging`, `worker` e `internal`.
- [ ] API calcula duração total da request pelo intervalo `min(start)` → `max(end)` quando houver timestamps.
- [ ] API retorna 400 para `traceId` fora do formato W3C/OTel e 404 para trace inexistente.
- [ ] Adapter Jaeger preserva `startTime`, parent span e kind quando disponíveis.
- [ ] `./mvnw -pl horus test` verde.

## Riscos

| Risco | Mitigação |
|---|---|
| Alguns backends não entregarem parent/kind no mesmo formato do Jaeger | Campos opcionais no DTO neutro; fallback seguro em `null`/`0`. |
| Waterfall sem timestamp não representar offsets reais | Documentar fallback e calcular offsets reais quando `startTimeMicros` vier preenchido. |
| Categorias por heurística classificarem spans de forma imperfeita | Manter categorias simples e refinar após T-405 com dados reais. |

## Referências

- [`../../PRD.md`](../../PRD.md) — RF-H-001/RF-H-011
- [`../../ROADMAP.md`](../../ROADMAP.md) — T-503
- [`../../telemetry/CONTRACT.md`](../../telemetry/CONTRACT.md) — contrato de spans e correlação
- [`../../adr/ADR-0008-horus-observabilidade-ia.md`](../../adr/ADR-0008-horus-observabilidade-ia.md)
- [`../../adr/ADR-0009-captura-request-e-query.md`](../../adr/ADR-0009-captura-request-e-query.md)
- [`./PLAN.md`](./PLAN.md)
