"""Testes dos cenários Locust e do runner — T-203, RF-004.

Validam, de forma offline (sem gerar carga nem rede), que os cenários estão bem-formados
e que o ``LocustRunner`` monta o comando correto e gerencia o ciclo de vida do processo.

⚠️ Importar ``locust`` aplica ``gevent.monkey.patch_all()`` no processo, o que conflita com
o ``TestClient`` (threads) das outras suítes. Por isso a inspeção do ``locustfile`` roda num
**subprocesso isolado**; os testes do runner usam apenas stdlib (runner.py não importa locust).
"""

from __future__ import annotations

import subprocess
import sys
from datetime import datetime, timezone
from pathlib import Path

import pytest

from app import runner
from app.models import LoadTest, LoadTestRequest, LoadTestStatus

LOADTEST_ROOT = Path(__file__).resolve().parents[1]


def _test(users: int = 25, spawn: float = 5, duration: int = 30, target: str = "http://lb") -> LoadTest:
    return LoadTest(
        id="t1",
        status=LoadTestStatus.RUNNING,
        config=LoadTestRequest(
            users=users, spawn_rate=spawn, duration_seconds=duration, target_url=target
        ),
        created_at=datetime.now(timezone.utc),
    )


def test_locustfile_defines_domain_and_saga_users() -> None:
    """Importa o locustfile num subprocesso e confere usuários + tasks (sem poluir o processo)."""
    script = (
        "import json; from locust import HttpUser; from app import locustfile as L\n"
        "classes = {n: c for n, c in vars(L).items() "
        "if isinstance(c, type) and issubclass(c, HttpUser) and c is not HttpUser}\n"
        "print(json.dumps({n: {'tasks': len(c.tasks), 'weight': c.weight} "
        "for n, c in classes.items()}))"
    )
    out = subprocess.run(
        [sys.executable, "-c", script],
        cwd=LOADTEST_ROOT, capture_output=True, text=True, timeout=120,
    )
    assert out.returncode == 0, out.stderr
    import json

    info = json.loads(out.stdout.strip().splitlines()[-1])
    assert set(info) == {"ProntuarioUser", "PaymentUser", "InvoiceUser", "SagaUser"}
    for name, meta in info.items():
        assert meta["tasks"] >= 1, f"{name} sem tasks"
        assert meta["weight"] >= 1


def test_build_command_has_expected_flags() -> None:
    cmd = runner.build_command(_test(users=42, spawn=7, duration=90, target="http://load-balancer"))
    assert "--headless" in cmd
    assert cmd[cmd.index("-u") + 1] == "42"
    assert cmd[cmd.index("-r") + 1] == "7.0"
    assert cmd[cmd.index("--run-time") + 1] == "90s"
    assert cmd[cmd.index("-H") + 1] == "http://load-balancer"
    assert cmd[cmd.index("-f") + 1].endswith("locustfile.py")


def test_runner_available_detects_locust() -> None:
    # locust está nas deps de dev → o módulo deve ser detectável (sem importá-lo aqui).
    assert runner.LocustRunner.available() is True


def test_runner_start_and_stop_manage_process(monkeypatch: pytest.MonkeyPatch) -> None:
    started: dict[str, object] = {}

    class FakeProc:
        def __init__(self, cmd):
            started["cmd"] = cmd
            self._alive = True
            self.signals: list[int] = []

        def poll(self):
            return None if self._alive else 0

        def send_signal(self, sig):
            self.signals.append(sig)
            self._alive = False

        def wait(self, timeout=None):
            return 0

        def kill(self):
            self._alive = False

    monkeypatch.setattr(runner.subprocess, "Popen", lambda cmd: FakeProc(cmd))

    r = runner.LocustRunner()
    test = _test()
    r.start(test)
    assert started["cmd"][0:3] == [runner.sys.executable, "-m", "locust"]
    # stop deve sinalizar o processo e removê-lo do registro (stop idempotente).
    r.stop(test)
    r.stop(test)
