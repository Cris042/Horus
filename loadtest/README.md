# loadtest/ — API de carga (FastAPI + Locust)

Gerador e **controlador** de teste de carga do sistema observado. Dispara tráfego concorrente e realista contra o Load Balancer para produzir a telemetria que o Horus observa.

- **Stack:** Python 3.13, FastAPI (controle), Locust (geração de carga).
- **Requisitos:** RF-001..004, RNF-016.

## Responsabilidades

- **API de controle:** `POST /load-tests`, `GET /load-tests/{id}`, `POST /load-tests/{id}/stop` (RF-001..003).
- **Cenários Locust:** usuários virtuais e requisições concorrentes (RF-004).

## Observabilidade

Instrumentada com OpenTelemetry; **inicia o contexto de tracing** que é propagado ponta a ponta a partir da borda (T-402, RF-029).

## Tasks

`T-202` (API de controle) · `T-203` (cenários) · `T-402` (OTel + propagação).
