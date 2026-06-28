"""Runner Locust — executa a carga de um teste em processo Locust headless (T-203).

Implementa a porta ``LoadRunner`` (T-202): para cada teste, monta a linha de comando do
Locust a partir do ``LoadTestRequest`` e lança um processo *headless*; ``stop`` o encerra.
Mantém o controlador (``LoadTestManager``) agnóstico de como a carga é gerada.
"""

from __future__ import annotations

import shutil
import signal
import subprocess
import sys
import threading
from pathlib import Path

from .models import LoadTest

# Caminho do locustfile deste pacote.
LOCUSTFILE = Path(__file__).with_name("locustfile.py")


def build_command(test: LoadTest, *, locustfile: Path = LOCUSTFILE) -> list[str]:
    """Monta o comando ``locust --headless`` para o teste (sem executá-lo).

    Isolado para ser testável de forma determinística e offline.
    """
    cfg = test.config
    return [
        sys.executable,
        "-m",
        "locust",
        "-f",
        str(locustfile),
        "--headless",
        "-u",
        str(cfg.users),
        "-r",
        str(cfg.spawn_rate),
        "--run-time",
        f"{cfg.duration_seconds}s",
        "-H",
        cfg.target_url,
        # Encerra o processo ao fim do --run-time (não fica aguardando).
        "--exit-code-on-error",
        "0",
    ]


class LocustRunner:
    """Lança/para processos Locust por id de teste."""

    def __init__(self) -> None:
        self._procs: dict[str, subprocess.Popen] = {}
        self._lock = threading.Lock()

    @staticmethod
    def available() -> bool:
        """True se o Locust está instalável/disponível neste ambiente."""
        return shutil.which("locust") is not None or _module_present("locust")

    def start(self, test: LoadTest) -> None:
        cmd = build_command(test)
        proc = subprocess.Popen(cmd)  # noqa: S603 (comando montado internamente)
        with self._lock:
            self._procs[test.id] = proc

    def stop(self, test: LoadTest) -> None:
        with self._lock:
            proc = self._procs.pop(test.id, None)
        if proc is None or proc.poll() is not None:
            return
        # SIGINT permite ao Locust finalizar e imprimir estatísticas.
        proc.send_signal(signal.SIGINT)
        try:
            proc.wait(timeout=10)
        except subprocess.TimeoutExpired:
            proc.kill()


def _module_present(name: str) -> bool:
    import importlib.util

    return importlib.util.find_spec(name) is not None
