# PRD — T-1001: Busca e janela temporal

| Campo | Valor |
|---|---|
| **Task** | `T-1001` |
| **Fase do roadmap** | `Fase 10 — Produto utilizável` |
| **Status** | `Entregue` |
| **Branch** | `claude/beautiful-cray-sfblsr` (branch designada da sessão; ver Premissas) |
| **PR** | — |
| **Depende de** | `T-501`, `T-701` |
| **Requisitos atendidos** | `RF-H-005`, `RF-H-010`, `RF-H-012` |
| **ADRs relacionados** | `ADR-0008`, `ADR-0010` |
| **Data** | `2026-09-28` |

## Objetivo

Até aqui o Horus só operava sobre um `traceId` conhecido de antemão: as portas expunham
apenas `findTrace(traceId)`, `findByTraceId` e `instantQuery`. Isso impedia o resumo do
**estado da aplicação** (RF-H-005 — o agendado só via a métrica `up`), perguntas como
"quais as queries mais lentas na última hora?" (RF-H-010) e um painel que liste o que está
acontecendo (RF-H-012). Esta task adiciona **busca por janela temporal** nos três sinais e a
usa no resumo de estado, no "pergunte ao Horus" e no painel.

## Escopo (o que entra)

- `TimeWindow`: janela `[start, end)` a partir de `lookback` (`30s|15m|1h|2d`) ou
  `from`/`to` (ISO-8601 ou epoch ms); padrão 1h; máximo 30d; entrada inválida → 400.
- Portas: `TraceQueryPort.searchTraces(TraceSearch)` + `listServices()`,
  `LogQueryPort.findInWindow(logQl, window, limit)`,
  `MetricQueryPort.rangeQuery(promQl, window, step)`.
- Adapters: Jaeger `GET /api/traces` (serviço/operação/janela/`minDuration`/`tags={"error":"true"}`)
  e `/api/services` — sem serviço, varre todos (exceto os internos do Jaeger) e deduplica;
  cada trace vira um `TraceSummary` (raiz, duração ponta a ponta, serviços, spans em erro,
  query SQL mais lenta). Loki `query_range` com `start`/`end`; Prometheus `query_range`.
- API: `GET /horus/traces` (+ `/services`), `GET /horus/query/logs/range`,
  `GET /horus/query/metrics/range`, `GET /horus/ai/summary/state`.
- IA: `ContextAssembler.assembleForWindow` (visão geral com p50/p95, traces com erro, queries
  SQL mais lentas, traces mais lentos, logs de erro, métricas — dentro do orçamento e
  sanitizado) + `WindowContextCollector` (coleta **best-effort**: backend que falha vira
  "sinal indisponível" no contexto, não erro). `POST /horus/ai/ask` sem `traceId` passa a usar
  a janela (`lookback`/`from`/`to`); o resumo agendado usa `horus.ai.summary.lookback` (15m).
- Painel: card **Traces** (janela, serviço, só com erro, duração mínima; link para o waterfall)
  e botão **Resumir estado**.

## Fora do escopo

- Tool-calling (a IA escolher sozinha quais consultas fazer) — continua fora; a janela dá à
  IA um recorte agregado suficiente para as perguntas comuns.
- Agregações exatas de percentil sobre todos os traces (o p50/p95 é da **amostra** buscada,
  limitada por `horus.ai.window.trace-limit`); percentis exatos vêm das métricas.
- Alertas disparados pela janela → `T-1008`.

## Premissas e dependências

- A sessão exige desenvolvimento na branch designada `claude/beautiful-cray-sfblsr`; a regra
  "uma task = uma branch" do `WORKFLOW.md` foi substituída por **um commit por task** nessa branch.
- A validação local usou JDK 25 (Temurin, extraído da imagem `eclipse-temurin:25-jdk`) e o
  stack real do compose (e2e de `T-1002`).
- Logs de erro no Loki identificados por `severity_text` (configurável em
  `horus.query.loki.error-logql`).

## Critérios de aceite

- [x] `GET /horus/traces` busca por janela/serviço/operação/duração mínima/erro e retorna
  resumos ordenados do mais recente; parâmetros inválidos → 400.
- [x] Sem serviço, a busca cobre todos os serviços do Jaeger (exceto internos), sem duplicatas.
- [x] Logs e métricas consultáveis por janela (`/horus/query/logs/range`, `/metrics/range`).
- [x] `GET /horus/ai/summary/state` resume o estado da aplicação sem `traceId`.
- [x] "Pergunte ao Horus" sem `traceId` é fundamentado na janela (queries lentas incluídas).
- [x] Falha de um backend não derruba o resumo; o contexto informa o sinal indisponível.
- [x] Contexto da janela passa pelo `PromptSanitizer` e respeita o orçamento de tokens.
- [x] Painel lista traces da janela com link para o waterfall.
- [x] `./mvnw -pl horus test` verde (91 testes; 27 novos).

## Riscos

| Risco | Mitigação |
|---|---|
| Varredura de todos os serviços custa N chamadas ao Jaeger | Limite por serviço = `limit`; serviços internos excluídos; janela máxima 30d |
| Jaeger devolve traces "em erro" pelo filtro de tag, mas o formato da tag varia | Filtro reaplicado no Horus (`error=true` ou `otel.status_code=ERROR`) |
| LogQL/PromQL livres nas rotas de range | Somente leitura, sob RBAC `VIEW_TELEMETRY`; limite de linhas (≤ 5000) |

## Referências

- [`../../PRD.md`](../../PRD.md) — RF-H-005/010/012
- [`./PLAN.md`](./PLAN.md)
- [`../T-501/PRD.md`](../T-501/PRD.md) — portas de consulta
