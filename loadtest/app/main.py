"""API de controle de teste de carga do Horus (FastAPI) — T-202, RF-001..003.

Endpoints:
- ``POST /load-tests``            inicia um teste de carga (RF-001)
- ``GET  /load-tests/{id}``       consulta estado/resultado (RF-002)
- ``POST /load-tests/{id}/stop``  interrompe um teste em execução (RF-003)
- ``GET  /load-tests``            lista os testes (auxiliar)
- ``GET  /health``               liveness

A instrumentação OpenTelemetry e o início do contexto de tracing na borda chegam
em T-402; os cenários Locust em T-203.
"""

from __future__ import annotations

import os

from fastapi import FastAPI, HTTPException, status

from .manager import (
    InvalidStateTransition,
    LoadRunner,
    LoadTestManager,
    LoadTestNotFound,
    NoopRunner,
)
from .models import LoadTest, LoadTestRequest


def _select_runner() -> LoadRunner:
    """Escolhe o runner por ambiente: ``locust`` gera carga real; padrão é no-op.

    O padrão no-op mantém a API testável sem dependências de rede; defina
    ``HORUS_LOADTEST_RUNNER=locust`` para gerar carga de fato (T-203).
    """
    if os.getenv("HORUS_LOADTEST_RUNNER", "noop").lower() == "locust":
        from .runner import LocustRunner

        return LocustRunner()
    return NoopRunner()


app = FastAPI(
    title="Horus — API de controle de teste de carga",
    version="0.1.0",
    summary="Dispara, consulta e interrompe testes de carga (RF-001..003).",
)

manager = LoadTestManager(_select_runner())


@app.get("/health", tags=["meta"])
def health() -> dict[str, str]:
    return {"status": "ok"}


@app.post(
    "/load-tests",
    response_model=LoadTest,
    status_code=status.HTTP_201_CREATED,
    tags=["load-tests"],
)
def start_load_test(request: LoadTestRequest) -> LoadTest:
    """Inicia um teste de carga (RF-001)."""
    return manager.create(request)


@app.get("/load-tests", response_model=list[LoadTest], tags=["load-tests"])
def list_load_tests() -> list[LoadTest]:
    return manager.list()


@app.get("/load-tests/{test_id}", response_model=LoadTest, tags=["load-tests"])
def get_load_test(test_id: str) -> LoadTest:
    """Consulta estado/resultado de um teste (RF-002)."""
    try:
        return manager.get(test_id)
    except LoadTestNotFound:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="teste não encontrado")


@app.post("/load-tests/{test_id}/stop", response_model=LoadTest, tags=["load-tests"])
def stop_load_test(test_id: str) -> LoadTest:
    """Interrompe um teste em execução (RF-003)."""
    try:
        return manager.stop(test_id)
    except LoadTestNotFound:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="teste não encontrado")
    except InvalidStateTransition as exc:
        raise HTTPException(status_code=status.HTTP_409_CONFLICT, detail=str(exc))
