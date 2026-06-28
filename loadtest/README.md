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

## Observabilidade

Instrumentada com OpenTelemetry; **inicia o contexto de tracing** que é propagado ponta a ponta a partir da borda (T-402, RF-029).

## Tasks

`T-202` (API de controle) · `T-203` (cenários) · `T-402` (OTel + propagação).
