"""Modelos (Pydantic) da API de controle de teste de carga — T-202, RF-001..003."""

from __future__ import annotations

from datetime import datetime, timezone
from enum import Enum

from pydantic import BaseModel, Field, field_validator

# Cenários aceitos: "default" exercita todos os domínios; os demais selecionam um
# único grupo de usuários do locustfile (T-203). Mantido em sincronia com
# ``app.runner.SCENARIO_USERS``.
VALID_SCENARIOS = frozenset({"default", "prontuario", "payment", "invoice", "saga"})


class LoadTestStatus(str, Enum):
    """Ciclo de vida de um teste de carga."""

    PENDING = "pending"
    RUNNING = "running"
    STOPPED = "stopped"
    COMPLETED = "completed"
    FAILED = "failed"


class LoadTestRequest(BaseModel):
    """Parâmetros para iniciar um teste de carga (RF-001).

    A execução real (Locust) chega em T-203; aqui o request descreve o cenário e
    o controlador gerencia seu ciclo de vida.
    """

    target_url: str = Field(
        default="http://load-balancer",
        description="URL de entrada (Load Balancer) contra a qual gerar carga.",
    )
    users: int = Field(default=10, ge=1, le=100_000, description="Usuários virtuais.")
    spawn_rate: float = Field(
        default=1.0, gt=0, le=10_000, description="Usuários iniciados por segundo."
    )
    duration_seconds: int = Field(
        default=60, ge=1, le=86_400, description="Duração-alvo do cenário (s)."
    )
    scenario: str = Field(
        default="default",
        description="Cenário a executar: 'default' (todos) ou um domínio "
        "(prontuario|payment|invoice|saga).",
    )

    @field_validator("scenario")
    @classmethod
    def _known_scenario(cls, v: str) -> str:
        normalized = v.strip().lower()
        if normalized not in VALID_SCENARIOS:
            raise ValueError(
                f"cenário desconhecido: {v!r} (use um de {sorted(VALID_SCENARIOS)})"
            )
        return normalized


class LoadTest(BaseModel):
    """Estado/resultado de um teste de carga (RF-002)."""

    id: str
    status: LoadTestStatus
    config: LoadTestRequest
    created_at: datetime
    started_at: datetime | None = None
    stopped_at: datetime | None = None

    @staticmethod
    def now() -> datetime:
        return datetime.now(timezone.utc)
