# loadtest/ — API de carga (FastAPI + Locust)

Gerador e **controlador** de teste de carga do sistema observado. Dispara tráfego concorrente e realista contra o Load Balancer para produzir a telemetria que o Horus observa.

- **Stack:** Python 3.13, FastAPI (controle), Locust (geração de carga).
- **Requisitos:** RF-001..004, RNF-016.

## Responsabilidades

- **API de controle:** `POST /load-tests`, `GET /load-tests/{id}`, `POST /load-tests/{id}/stop` (RF-001..003). ✅ T-202.
- **Cenários Locust:** usuários virtuais e requisições concorrentes (RF-004). ✅ T-203.

## Desenvolvimento

```bash
cd loadtest
uv venv --python 3.13 .venv
uv pip install -e ".[dev]" --python .venv
.venv/bin/python -m pytest -q              # testes
.venv/bin/uvicorn app.main:app --reload    # subir a API (http://localhost:8000/docs)
```

### Geração de carga (Locust — T-203)

Os cenários vivem em [`app/locustfile.py`](app/locustfile.py): usuários virtuais por domínio
(Prontuário, Payment, Invoice) + SAGA, encadeando chamadas dependentes contra o **Load
Balancer**. O controlador (T-202) dispara a carga quando o runner Locust está selecionado:

```bash
# Via controlador: a API passa a usar o runner Locust real
HORUS_LOADTEST_RUNNER=locust .venv/bin/uvicorn app.main:app

# Ou diretamente (headless):
.venv/bin/locust -f app/locustfile.py --headless -u 50 -r 5 --run-time 60s -H http://load-balancer
```

Padrão (sem a env var): runner **no-op** — a API de controle é testável sem rede/Locust.

## Observabilidade (OTel — T-402, RF-029) ✅

Instrumentada com OpenTelemetry: **inicia o contexto de tracing** e o propaga ponta a ponta
(**W3C Trace Context**) a partir da borda. Recurso canônico do contrato (T-005):
`service.name=loadtest-api`, `service.namespace=medrec`.

- **Locust** (borda): no `events.init`, instrumenta o cliente `requests` → injeta
  `traceparent` em cada chamada ao Load Balancer. **Ligado por padrão**; desligue com
  `HORUS_OTEL_ENABLED=false`.
- **API FastAPI**: instrumentada quando `HORUS_OTEL_ENABLED=true` (padrão **desligado** p/
  testes/dev sem Collector).
- Endpoint OTLP via env padrão do OTel: `OTEL_EXPORTER_OTLP_ENDPOINT` (ex.: o Collector do compose).

```bash
HORUS_OTEL_ENABLED=true OTEL_EXPORTER_OTLP_ENDPOINT=http://otel-collector:4318 \
  .venv/bin/uvicorn app.main:app
```

## Tasks

`T-202` (API de controle) ✅ · `T-203` (cenários) ✅ · `T-402` (OTel + propagação) ✅.
