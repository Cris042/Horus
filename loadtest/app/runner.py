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

from .models import LoadTest, LoadTestStatus

# Caminho do locustfile deste pacote.
LOCUSTFILE = Path(__file__).with_name("locustfile.py")

# Cenário → classe de usuário do locustfile (T-203). "default" roda todas.
SCENARIO_USERS = {
    "prontuario": "ProntuarioUser",
    "payment": "PaymentUser",
    "invoice": "InvoiceUser",
    "saga": "SagaUser",
}


def build_command(test: LoadTest, *, locustfile: Path = LOCUSTFILE) -> list[str]:
    """Monta o comando ``locust --headless`` para o teste (sem executá-lo).

    Honra ``config.scenario``: 'default' executa o locustfile inteiro; um domínio
    seleciona a classe de usuário correspondente (argumento posicional do Locust).
    Isolado para ser testável de forma determinística e offline.
    """
    cfg = test.config
    cmd = [
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
    scenario = (cfg.scenario or "default").lower()
    if scenario != "default":
        try:
            cmd.append(SCENARIO_USERS[scenario])  # seleciona só esse grupo de usuários
        except KeyError:
            raise ValueError(
                f"cenário desconhecido: {cfg.scenario!r} "
                f"(use um de {sorted(SCENARIO_USERS)} ou 'default')"
            )
    return cmd


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
        # Acompanha o término natural (fim do --run-time ou erro) para refletir o
        # estado no descritor — senão o teste ficaria 'running' para sempre.
        threading.Thread(target=self._watch, args=(test, proc), daemon=True).start()

    def _watch(self, test: LoadTest, proc: subprocess.Popen) -> None:
        proc.wait()
        with self._lock:
            # Só conclui se ainda for este processo registrado; um stop() concorrente
            # remove o registro e já marcou STOPPED — não sobrescrever.
            if self._procs.get(test.id) is not proc:
                return
            self._procs.pop(test.id, None)
            if test.status is LoadTestStatus.RUNNING:
                test.status = (
                    LoadTestStatus.COMPLETED if proc.returncode == 0 else LoadTestStatus.FAILED
                )
                test.stopped_at = LoadTest.now()

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
