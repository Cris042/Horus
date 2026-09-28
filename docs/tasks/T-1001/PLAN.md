# Plano de Execução — T-1001: Busca e janela temporal

> ⚠️ **Regra R2 ([WORKFLOW](../../WORKFLOW.md)).** Sempre que **criar ou modificar** um arquivo desta tabela, atualize **na mesma hora** o seu `Status` e `Atualizado`, e registre a mudança em *Registro de alterações*. Arquivo não previsto que precise ser tocado deve ser **adicionado** à tabela.

| Campo | Valor |
|---|---|
| **Task** | `T-1001` |
| **Branch** | `claude/beautiful-cray-sfblsr` |
| **PRD** | [`./PRD.md`](./PRD.md) |
| **Progresso** | `34/34` arquivos |

## Arquivos a criar/modificar

| # | Arquivo | Ação | Propósito | Status | Atualizado |
|---|---|---|---|---|---|
| 1 | `horus/src/main/java/org/example/horus/query/TimeWindow.java` | Criar | Janela temporal (lookback/from/to, limites, conversões µs/ns/s) | ✅ Concluído | 2026-09-28 |
| 2 | `horus/src/main/java/org/example/horus/query/QueryModel.java` | Modificar | `TraceSearch`, `TraceSummary`, `SlowQuery`, `MetricSeries`, `MetricPoint` | ✅ Concluído | 2026-09-28 |
| 3 | `horus/src/main/java/org/example/horus/query/TraceQueryPort.java` | Modificar | `searchTraces` + `listServices` | ✅ Concluído | 2026-09-28 |
| 4 | `horus/src/main/java/org/example/horus/query/LogQueryPort.java` | Modificar | `findInWindow` | ✅ Concluído | 2026-09-28 |
| 5 | `horus/src/main/java/org/example/horus/query/MetricQueryPort.java` | Modificar | `rangeQuery` | ✅ Concluído | 2026-09-28 |
| 6 | `horus/src/main/java/org/example/horus/query/backend/JaegerClient.java` | Modificar | `GET /api/traces` (busca) + `/api/services` | ✅ Concluído | 2026-09-28 |
| 7 | `horus/src/main/java/org/example/horus/query/backend/JaegerTraceAdapter.java` | Modificar | Busca multi-serviço, dedupe, `TraceSummary` (raiz, erros, query lenta) | ✅ Concluído | 2026-09-28 |
| 8 | `horus/src/main/java/org/example/horus/query/backend/LokiClient.java` | Modificar | `query_range` com start/end | ✅ Concluído | 2026-09-28 |
| 9 | `horus/src/main/java/org/example/horus/query/backend/LokiLogAdapter.java` | Modificar | `findInWindow` | ✅ Concluído | 2026-09-28 |
| 10 | `horus/src/main/java/org/example/horus/query/backend/PrometheusClient.java` | Modificar | `/api/v1/query_range` | ✅ Concluído | 2026-09-28 |
| 11 | `horus/src/main/java/org/example/horus/query/backend/PrometheusMetricAdapter.java` | Modificar | `rangeQuery` (matrix) | ✅ Concluído | 2026-09-28 |
| 12 | `horus/src/main/java/org/example/horus/api/ApiWindows.java` | Criar | Parâmetros de janela → `TimeWindow` (400 se inválido) | ✅ Concluído | 2026-09-28 |
| 13 | `horus/src/main/java/org/example/horus/api/HorusTraceSearchResource.java` | Criar | `GET /horus/traces` + `/services` | ✅ Concluído | 2026-09-28 |
| 14 | `horus/src/main/java/org/example/horus/api/HorusQueryResource.java` | Modificar | `/logs/range` e `/metrics/range` | ✅ Concluído | 2026-09-28 |
| 15 | `horus/src/main/java/org/example/horus/api/HorusSummaryResource.java` | Modificar | `GET /horus/ai/summary/state` | ✅ Concluído | 2026-09-28 |
| 16 | `horus/src/main/java/org/example/horus/api/HorusAskResource.java` | Modificar | Sem traceId → contexto da janela | ✅ Concluído | 2026-09-28 |
| 17 | `horus/src/main/java/org/example/horus/ai/context/ContextAssembler.java` | Modificar | `assembleForWindow` (visão geral, erros, queries/traces lentos, logs, métricas) | ✅ Concluído | 2026-09-28 |
| 18 | `horus/src/main/java/org/example/horus/ai/context/WindowContextCollector.java` | Criar | Coleta best-effort dos sinais da janela | ✅ Concluído | 2026-09-28 |
| 19 | `horus/src/main/java/org/example/horus/ai/agent/ScheduledStateSummary.java` | Modificar | Resumo agendado sobre a janela (`horus.ai.summary.lookback`) | ✅ Concluído | 2026-09-28 |
| 20 | `horus/src/main/java/org/example/horus/panel/HorusPanelResource.java` | Modificar | Novos endpoints no overview | ✅ Concluído | 2026-09-28 |
| 21 | `horus/src/main/resources/META-INF/resources/horus-panel.html` | Modificar | Card Traces + Resumir estado + ask por janela | ✅ Concluído | 2026-09-28 |
| 22 | `horus/src/main/resources/application.properties` | Modificar | `error-logql`, `excluded-services`, `summary.lookback`, limites da janela | ✅ Concluído | 2026-09-28 |
| 23 | `horus/src/test/java/org/example/horus/query/TimeWindowTest.java` | Criar | Resolução/validação da janela | ✅ Concluído | 2026-09-28 |
| 24 | `horus/src/test/java/org/example/horus/query/backend/JaegerTraceAdapterTest.java` | Criar | Mapeamento da busca Jaeger (sem rede) | ✅ Concluído | 2026-09-28 |
| 25 | `horus/src/test/java/org/example/horus/query/backend/RangeAdaptersTest.java` | Criar | Loki/Prometheus por janela | ✅ Concluído | 2026-09-28 |
| 26 | `horus/src/test/java/org/example/horus/api/HorusTraceSearchResourceTest.java` | Criar | API de busca | ✅ Concluído | 2026-09-28 |
| 27 | `horus/src/test/java/org/example/horus/api/HorusQueryResourceTest.java` | Modificar | Rotas de range | ✅ Concluído | 2026-09-28 |
| 28 | `horus/src/test/java/org/example/horus/api/HorusSummaryResourceTest.java` | Modificar | Resumo de estado | ✅ Concluído | 2026-09-28 |
| 29 | `horus/src/test/java/org/example/horus/api/HorusAskResourceTest.java` | Modificar | Ask por janela | ✅ Concluído | 2026-09-28 |
| 30 | `horus/src/test/java/org/example/horus/ai/context/ContextAssemblerTest.java` | Modificar | `assembleForWindow` | ✅ Concluído | 2026-09-28 |
| 31 | `horus/src/test/java/org/example/horus/ai/context/WindowContextCollectorTest.java` | Criar | Coleta best-effort | ✅ Concluído | 2026-09-28 |
| 32 | `docs/tasks/T-1001/PRD.md` | Criar | PRD da task | ✅ Concluído | 2026-09-28 |
| 33 | `docs/tasks/T-1001/PLAN.md` | Criar | Este plano | ✅ Concluído | 2026-09-28 |
| 34 | `state.md` | Modificar | R1 — registro da entrega | ✅ Concluído | 2026-09-28 |

**Legenda:** ⬜ Pendente · 🟡 Em progresso · ✅ Concluído

## Passos de implementação

1. `TimeWindow` + novos tipos em `QueryModel`.
2. Estender as 3 portas e os 3 adapters/clients (Jaeger busca + serviços, Loki e Prometheus por janela).
3. APIs: `/horus/traces`, `/horus/query/{logs,metrics}/range`, `/horus/ai/summary/state`; ask sem traceId → janela.
4. `ContextAssembler.assembleForWindow` + `WindowContextCollector` (best-effort); resumo agendado usa a janela.
5. Painel: card Traces + Resumir estado.
6. Testes unitários (adapters sem rede, janela, assembler) e `@QuarkusTest` (APIs).

## Verificação / testes

- [x] `./mvnw -B -o -pl horus -Dmaven.compiler.release=21 test` → **91 testes, 0 falhas** (eram 64).
  JDK 25 indisponível no ambiente da sessão; CI (JDK 25) é a validação de referência.
- [ ] Validação contra Jaeger/Loki/Prometheus reais — coberta pelo e2e automatizado de `T-1002`.

## Registro de alterações

| Data | Arquivo | Mudança |
|---|---|---|
| `2026-09-28` | (todos acima) | Busca e janela temporal (T-1001) |
