"""Modelos (Pydantic) da API de controle de teste de carga — T-202, RF-001..003."""

from __future__ import annotations

from datetime import datetime, timezone
from enum import Enum

from pydantic import BaseModel, Field


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
        default="default", description="Nome do cenário Locust a executar (T-203)."
    )


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
