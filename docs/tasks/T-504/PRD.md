# PRD — T-504: API do ciclo de vida da query

| Campo | Valor |
|---|---|
| **Task** | `T-504` |
| **Fase do roadmap** | `Fase 5 — Horus core` |
| **Status** | `Em progresso` |
| **Branch** | `task/T-504-query-lifecycle-api` |
| **PR** | `—` |
| **Depende de** | `T-502`, `T-503` |
| **Requisitos atendidos** | `RF-H-002` |
| **ADRs relacionados** | `ADR-0007`, `ADR-0009`, `ADR-0010` |
| **Data** | `2026-06-28` |

## Objetivo

Expor uma API do Horus que destaque, dentro de um `trace_id`, o ciclo de vida de cada query SQL
como spans próprios: statement sanitizado, banco/serviço de origem, duração e posição temporal
relativa à request. Esta fatia prepara tanto a inspeção direta de queries quanto a waterfall UI.

## Escopo (o que entra)

- Enriquecimento de `SpanRef` com metadados de query vindos do Jaeger: `db.query.text`,
  `db.namespace`, `db.system.name` e `db.operation.name`.
- Serviço determinístico que filtra apenas spans SQL/client e devolve uma visão `QueryLifecycle`.
- Endpoint `GET /horus/lifecycle/queries/{traceId}` com 200, 400 para `traceId` inválido e 404 se o trace não existir.
- Ordenação temporal por `startTimeMicros` com `offsetMicros` relativo ao início da request.
- Testes `@QuarkusTest` com `TraceQueryPort` mockado cobrindo query única, múltiplas queries, 404 e validação.

## Fora do escopo

- Waterfall completa da request → `T-503`.
- Agregação de logs de erro → `T-505`.
- Plano de execução, linhas retornadas ou análise avançada de query.
- Métricas agregadas de query lenta e detecção de anomalias → `T-606`.

## Premissas e dependências

- As queries já são spans filhos da request por instrumentação OTel/JDBC/Hibernate (ADR-0009).
- `db.query.text` já chega parametrizado/sanitizado pelo contrato de telemetria; a API não deve remontar SQL cru.
- O trace vem do Jaeger via `TraceQueryPort`; esta fatia não consulta Loki nem Prometheus.
- A identificação de query deve depender primeiro de metadados `db.*` e só usar `operation` como fallback.

## Critérios de aceite

- [ ] API retorna apenas spans de query do trace, ordenados por `startTimeMicros`.
- [ ] Cada item expõe `spanId`, `parentSpanId`, `serviceName`, `databaseName`, `databaseSystem`, `operationName`, `statement`, `durationMicros` e `offsetMicros`.
- [ ] API preserva somente statements sanitizados/parametrizados; não introduz valores crus.
- [ ] API retorna 400 para `traceId` fora do formato W3C/OTel e 404 para trace inexistente.
- [ ] Adapter Jaeger lê os atributos `db.*` quando disponíveis.
- [ ] `./mvnw -pl horus test` verde.

## Riscos

| Risco | Mitigação |
|---|---|
| Alguns exporters usarem chaves diferentes de `db.*` | Suportar `db.system.name` / `db.system` e `db.operation.name` / inferência por `operationName`. |
| Queries sem `db.query.text` perderem utilidade visual | Manter `operationName` como fallback explícito e não mascarar ausência como sucesso. |
| Vazamento de PII se algum backend trouxer SQL cru | Não enriquecer nem interpolar parâmetros; apenas repassar o valor recebido do trace e manter o contrato explícito no PRD. |

## Referências

- [`../../PRD.md`](../../PRD.md) — RF-H-002 / RNF-H-002
- [`../../ROADMAP.md`](../../ROADMAP.md) — T-504
- [`../../telemetry/CONTRACT.md`](../../telemetry/CONTRACT.md) §3.3 / §6
- [`../../adr/ADR-0009-captura-request-e-query.md`](../../adr/ADR-0009-captura-request-e-query.md)
- [`../T-503/PRD.md`](../T-503/PRD.md)
- [`./PLAN.md`](./PLAN.md)
