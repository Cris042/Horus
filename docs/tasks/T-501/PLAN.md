# Plano de Execução — T-501: Serviço Horus — adaptadores de consulta aos backends

> ⚠️ **Regra R2 ([WORKFLOW](../../WORKFLOW.md)).** Sempre que **criar ou modificar** um arquivo desta tabela, atualize **na mesma hora** o seu `Status` e `Atualizado`, e registre a mudança em *Registro de alterações*.

| Campo | Valor |
|---|---|
| **Task** | `T-501` |
| **Branch** | `task/T-501-horus-query-adapters` |
| **PRD** | [`./PRD.md`](./PRD.md) |
| **Progresso** | `13/13` arquivos |

## Arquivos a criar/modificar

| # | Arquivo | Ação | Propósito | Status | Atualizado |
|---|---|---|---|---|---|
| 1 | `horus/pom.xml` | Modificar | + `quarkus-rest-client-jackson`, `quarkus-junit5-mockito` | ✅ Concluído | 2026-06-27 |
| 2 | `horus/.../query/QueryModel.java` | Criar | DTOs neutros (TraceResult/SpanRef/LogLine/MetricSample) | ✅ Concluído | 2026-06-27 |
| 3 | `horus/.../query/TraceQueryPort.java` | Criar | Porta de traces | ✅ Concluído | 2026-06-27 |
| 4 | `horus/.../query/LogQueryPort.java` | Criar | Porta de logs | ✅ Concluído | 2026-06-27 |
| 5 | `horus/.../query/MetricQueryPort.java` | Criar | Porta de métricas | ✅ Concluído | 2026-06-27 |
| 6 | `horus/.../query/backend/JaegerClient.java` | Criar | REST client Jaeger | ✅ Concluído | 2026-06-27 |
| 7 | `horus/.../query/backend/JaegerTraceAdapter.java` | Criar | Adapter TraceQueryPort | ✅ Concluído | 2026-06-27 |
| 8 | `horus/.../query/backend/LokiClient.java` | Criar | REST client Loki | ✅ Concluído | 2026-06-27 |
| 9 | `horus/.../query/backend/LokiLogAdapter.java` | Criar | Adapter LogQueryPort | ✅ Concluído | 2026-06-27 |
| 10 | `horus/.../query/backend/PrometheusClient.java` | Criar | REST client Prometheus | ✅ Concluído | 2026-06-27 |
| 11 | `horus/.../query/backend/PrometheusMetricAdapter.java` | Criar | Adapter MetricQueryPort | ✅ Concluído | 2026-06-27 |
| 12 | `horus/.../api/HorusQueryResource.java` | Criar | API REST de consulta | ✅ Concluído | 2026-06-27 |
| 13 | `horus/src/main/resources/application.properties` | Modificar | URLs dos backends + template LogQL | ✅ Concluído | 2026-06-27 |
| 14 | `horus/.../api/HorusQueryResourceTest.java` | Criar | Teste com portas mockadas | ✅ Concluído | 2026-06-27 |

**Legenda:** ⬜ Pendente · 🟡 Em progresso · ✅ Concluído

## Passos de implementação

1. pom: adicionar REST client + mockito.
2. Modelar DTOs e três portas.
3. Implementar clients REST (`JsonNode`) e adapters de cada backend.
4. Expor API REST fina sobre as portas.
5. Configurar URLs e template LogQL.
6. Testar com `@InjectMock` nas portas.

## Verificação / testes

- [x] `./mvnw -pl horus test` → BUILD SUCCESS (6 testes, 0 falhas).

## Registro de alterações

| Data | Arquivo | Mudança |
|---|---|---|
| `2026-06-27` | (todos acima) | Criação da camada de consulta do Horus (T-501) |
