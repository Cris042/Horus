# Telemetria — pipeline de observabilidade (T-404)

Pipeline único de DEV: **um** ponto de ingestão OTLP (OTel Collector) que faz *fan-out*
para três backends especializados, com **Grafana** como lente unificada.

```
apps (HTTP/JDBC/AMQP, contrato T-005)
        │  OTLP  (gRPC :4317 / HTTP :4318)
        ▼
  OTel Collector (contrib)
        ├── traces   → Jaeger      (OTLP :4317)
        ├── logs     → Loki        (OTLP HTTP /otlp)
        └── métricas → Prometheus  (remote_write)
                                   ▲
                         Grafana ──┘ (datasources: Prometheus, Loki, Jaeger)
```

Origem do plumbing: `T-003` (Collector + backends). `T-404` endurece e valida:
`health_check` no Collector, `healthcheck`/ordenação no compose e Grafana provisionado.

## Portas (host)

| Componente | Porta | Uso |
|---|---|---|
| OTel Collector | `4317` / `4318` | ingestão OTLP gRPC / HTTP |
| OTel Collector | `13133` | extensão `health_check` (liveness) |
| Jaeger | `16686` | UI de traces (RF-031) |
| Loki | `3100` | API de logs (OTLP em `/otlp`, leitura `/ready`) |
| Prometheus | `9090` | API/UI de métricas (`/-/healthy`) |
| Grafana | `3000` | lente unificada (login anônimo em dev) |

## Arquivos

| Arquivo | Papel |
|---|---|
| `otel-collector-config.yaml` | receivers/processors/exporters + extensão `health_check` |
| `loki-config.yaml` | Loki single-binary, filesystem, OTLP habilitado |
| `prometheus.yml` | self-scrape; métricas chegam por remote_write |
| `grafana/datasources.yaml` | datasources provisionados (Prometheus/Loki/Jaeger) |
| `grafana/dashboards.yaml` | provider de dashboards (aponta para `grafana/dashboards/`) |
| `grafana/dashboards/microservices.json` | dashboard RED + JVM dos 4 serviços de domínio/SAGA |

## Verificação do pipeline

> Os apps Quarkus ainda **não** sobem no compose (containerização é `T-801`); aqui
> validamos o **plano e os backends** isoladamente.

1. **Validar o plano:**
   ```bash
   docker compose -f deploy/docker-compose.yml config >/dev/null && echo OK
   ```
2. **Subir só a telemetria:**
   ```bash
   docker compose -f deploy/docker-compose.yml up -d \
     otel-collector jaeger loki prometheus grafana
   ```
3. **Liveness:**
   - Collector: `curl -fsS http://localhost:13133/` → `200`
   - Prometheus: `curl -fsS http://localhost:9090/-/healthy`
   - Loki: `curl -fsS http://localhost:3100/ready`
   - Grafana: `curl -fsS http://localhost:3000/api/health`
4. **Datasources no Grafana:** abrir `http://localhost:3000` → *Connections → Data sources*
   deve listar **Prometheus**, **Loki** e **Jaeger** já provisionados.
5. **Dashboard:** `http://localhost:3000/d/horus-microservices` (pasta "Horus") — taxa de
   requisição/latência p95/erros por serviço (RED) + heap/threads/GC (JVM), 4 serviços de
   domínio/SAGA. Provisionado no boot, sem cliques manuais (mesmo padrão dos datasources).
6. **Smoke OTLP** (opcional, sem apps) — enviar um span de teste ao Collector e
   conferir em `http://localhost:16686` (Jaeger). A validação ponta a ponta real
   (request→query→log→mensagem→worker no mesmo `trace_id`) é o escopo de **T-405**.

## Notas

- Jaeger 2.x e o Collector são imagens *distroless* (sem `wget`): o `healthcheck` do
  compose cobre Loki/Prometheus/Grafana; a ordem de boot do Collector/Grafana usa
  `depends_on: condition: service_healthy` nesses, e `service_started` no Jaeger.
- O datasource Loki traz `derivedFields` que extrai `trace_id` dos logs e linka ao
  Jaeger — base da correlação log→trace explorada em `T-405`.
- Redação/sanitização de PII na borda do Collector é `T-406`.
- **`quarkus.otel.metrics.enabled` é `false` por padrão** na extensão OTel do Quarkus —
  sem essa flag (agora ligada em `application.properties` dos 4 serviços de domínio/SAGA),
  nenhuma métrica (`http.server.request.duration`, JVM) chega ao Prometheus, mesmo com
  traces/logs funcionando normalmente. É `BUILD_AND_RUN_TIME_FIXED` (precisa rebuild, não
  dá pra ligar só com `-D`/env em runtime).
