"""Controlador (in-memory) do ciclo de vida dos testes de carga — T-202.

Gerencia descritores de teste e transições de estado. A geração real de carga
(Locust) é plugada em T-203 por meio de um *runner*; aqui o runner padrão é um
no-op, de modo que a API de controle (RF-001..003) já é testável ponta a ponta.
"""

from __future__ import annotations

import threading
import uuid
from typing import Protocol

from .models import LoadTest, LoadTestRequest, LoadTestStatus


class LoadRunner(Protocol):
    """Porta para a execução real da carga (implementada com Locust em T-203)."""

    def start(self, test: LoadTest) -> None: ...

    def stop(self, test: LoadTest) -> None: ...


class NoopRunner:
    """Runner padrão: não gera carga (placeholder até T-203)."""

    def start(self, test: LoadTest) -> None:  # noqa: D102
        return None

    def stop(self, test: LoadTest) -> None:  # noqa: D102
        return None


class LoadTestNotFound(Exception):
    """Teste inexistente."""


class InvalidStateTransition(Exception):
    """Transição de estado não permitida (ex.: parar um teste já encerrado)."""


class LoadTestManager:
    """Registro thread-safe de testes de carga e suas transições de estado."""

    def __init__(self, runner: LoadRunner | None = None) -> None:
        self._runner: LoadRunner = runner or NoopRunner()
        self._tests: dict[str, LoadTest] = {}
        self._lock = threading.Lock()

    def create(self, request: LoadTestRequest) -> LoadTest:
        """Inicia um teste de carga (RF-001)."""
        now = LoadTest.now()
        test = LoadTest(
            id=uuid.uuid4().hex,
            status=LoadTestStatus.RUNNING,
            config=request,
            created_at=now,
            started_at=now,
        )
        with self._lock:
            self._tests[test.id] = test
        self._runner.start(test)
        return test

    def get(self, test_id: str) -> LoadTest:
        """Consulta um teste (RF-002); lança se não existir."""
        with self._lock:
            test = self._tests.get(test_id)
        if test is None:
            raise LoadTestNotFound(test_id)
        return test

    def list(self) -> list[LoadTest]:
        with self._lock:
            return sorted(self._tests.values(), key=lambda t: t.created_at, reverse=True)

    def stop(self, test_id: str) -> LoadTest:
        """Interrompe um teste em execução (RF-003)."""
        with self._lock:
            test = self._tests.get(test_id)
            if test is None:
                raise LoadTestNotFound(test_id)
            if test.status is not LoadTestStatus.RUNNING:
                raise InvalidStateTransition(
                    f"teste {test_id} está {test.status.value}, não pode ser interrompido"
                )
            test.status = LoadTestStatus.STOPPED
            test.stopped_at = LoadTest.now()
        self._runner.stop(test)
        return test
