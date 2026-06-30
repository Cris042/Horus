# PRD — T-505: Agregação de logs de erro correlacionados

| Campo | Valor |
|---|---|
| **Task** | `T-505` |
| **Fase do roadmap** | `Fase 5 — Horus core` |
| **Status** | `Em progresso` |
| **Branch** | `task/T-505-error-log-aggregation` |
| **PR** | `—` |
| **Depende de** | `T-502` |
| **Requisitos atendidos** | `RF-H-003` |
| **ADRs relacionados** | `ADR-0007`, `ADR-0009`, `ADR-0010` |
| **Data** | `2026-06-28` |

## Objetivo

Expor uma API do Horus que agregue os logs de erro correlacionados a um `trace_id`, reduzindo
ruído operacional ao agrupar linhas equivalentes por serviço e fingerprint textual. Esta fatia
entrega a visão estruturada de erro que alimenta o painel e prepara `T-606`.

## Escopo (o que entra)

- Serviço determinístico que lê `LogLine` correlacionadas e mantém apenas eventos de erro.
- Fingerprint textual estável por serviço, baseado na primeira linha normalizada da mensagem.
- Endpoint `GET /horus/lifecycle/errors/{traceId}` com 200, 400 para `traceId` inválido e 404 se o trace não existir.
- Payload com resumo agregado: total de logs de erro, grupos por serviço/fingerprint, severidade, amostra e flag de stack trace.
- Testes `@QuarkusTest` com portas mockadas cobrindo agrupamento, fallback por conteúdo da linha, trace inexistente e validação.

## Fora do escopo

- Query lifecycle (`T-504`) e waterfall completa (`T-503`).
- Clusterização avançada multi-trace / IA (`T-606`).
- Correlação com métricas ou classificação automática de causa raiz.
- Persistência própria de fingerprints.

## Premissas e dependências

- Loki já retorna logs correlacionados por `trace_id` via `LogQueryPort`.
- O contrato de telemetria mantém `trace_id`/`span_id` e campos de erro padronizados quando disponíveis.
- Alguns produtores podem não preencher `level=error`; nesses casos a detecção usa o conteúdo da linha.
- A API não deve expor PII nova nem reconstituir stack traces além do que já foi logado.

## Critérios de aceite

- [ ] API retorna apenas logs de erro do trace.
- [ ] API agrupa erros equivalentes por serviço + fingerprint textual normalizada.
- [ ] Cada grupo expõe `serviceName`, `level`, `count`, `fingerprint`, `sample`, `hasStacktrace` e `latestTimestampNanos`.
- [ ] API retorna 400 para `traceId` fora do formato W3C/OTel e 404 para trace inexistente.
- [ ] A detecção de erro funciona tanto por label `level=error` quanto por conteúdo da linha.
- [ ] `./mvnw -pl horus test` verde.

## Riscos

| Risco | Mitigação |
|---|---|
| Formato de linha variar muito entre componentes | Fingerprint conservador baseado na primeira linha normalizada; refinável em `T-606`. |
| Logs sem label de serviço perderem contexto | Fallback para `service_name`, `service.name` e valor `unknown`. |
| Agrupar demais mensagens parecidas | Normalização limitada a números/UUIDs/hex longos, mantendo semântica principal do texto. |

## Referências

- [`../../PRD.md`](../../PRD.md) — RF-H-003 / RF-H-004
- [`../../ROADMAP.md`](../../ROADMAP.md) — T-505
- [`../../telemetry/CONTRACT.md`](../../telemetry/CONTRACT.md) §4
- [`../../adr/ADR-0009-captura-request-e-query.md`](../../adr/ADR-0009-captura-request-e-query.md)
- [`./PLAN.md`](./PLAN.md)
