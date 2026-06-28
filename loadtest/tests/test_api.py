"""Testes da API de controle de teste de carga — T-202, RF-001..003."""

from __future__ import annotations

import pytest
from fastapi.testclient import TestClient

from app.main import LoadTestManager, app
import app.main as main


@pytest.fixture(autouse=True)
def fresh_manager() -> None:
    """Isola o estado entre testes (manager em memória)."""
    main.manager = LoadTestManager()
    yield


@pytest.fixture
def client() -> TestClient:
    return TestClient(app)


def test_health(client: TestClient) -> None:
    resp = client.get("/health")
    assert resp.status_code == 200
    assert resp.json() == {"status": "ok"}


def test_start_load_test_returns_running(client: TestClient) -> None:
    resp = client.post("/load-tests", json={"users": 50, "spawn_rate": 5, "duration_seconds": 30})
    assert resp.status_code == 201
    body = resp.json()
    assert body["status"] == "running"
    assert body["config"]["users"] == 50
    assert body["id"]
    assert body["started_at"] is not None


def test_start_uses_defaults(client: TestClient) -> None:
    resp = client.post("/load-tests", json={})
    assert resp.status_code == 201
    cfg = resp.json()["config"]
    assert cfg["users"] == 10
    assert cfg["scenario"] == "default"


def test_start_rejects_invalid_params(client: TestClient) -> None:
    resp = client.post("/load-tests", json={"users": 0})
    assert resp.status_code == 422


def test_get_load_test(client: TestClient) -> None:
    test_id = client.post("/load-tests", json={}).json()["id"]
    resp = client.get(f"/load-tests/{test_id}")
    assert resp.status_code == 200
    assert resp.json()["id"] == test_id


def test_get_unknown_is_404(client: TestClient) -> None:
    assert client.get("/load-tests/does-not-exist").status_code == 404


def test_stop_running_test(client: TestClient) -> None:
    test_id = client.post("/load-tests", json={}).json()["id"]
    resp = client.post(f"/load-tests/{test_id}/stop")
    assert resp.status_code == 200
    body = resp.json()
    assert body["status"] == "stopped"
    assert body["stopped_at"] is not None


def test_stop_twice_is_conflict(client: TestClient) -> None:
    test_id = client.post("/load-tests", json={}).json()["id"]
    client.post(f"/load-tests/{test_id}/stop")
    resp = client.post(f"/load-tests/{test_id}/stop")
    assert resp.status_code == 409


def test_stop_unknown_is_404(client: TestClient) -> None:
    assert client.post("/load-tests/nope/stop").status_code == 404


def test_list_load_tests(client: TestClient) -> None:
    client.post("/load-tests", json={})
    client.post("/load-tests", json={})
    resp = client.get("/load-tests")
    assert resp.status_code == 200
    assert len(resp.json()) == 2
