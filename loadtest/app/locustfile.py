"""Cenários Locust do sistema observado — T-203, RF-004 / RNF-016.

Usuários virtuais que geram tráfego concorrente e realista contra o **Load Balancer**
(NGINX, T-201), exercitando os três domínios (Prontuário, Payment, Invoice) e a SAGA.
Cada fluxo encadeia chamadas dependentes (criar → usar → encerrar), produzindo traces
ricos para o Horus observar.

O contexto de tracing na borda (propagação OTel a partir daqui) é acoplado em T-402.

Execução (headless), normalmente disparada pelo controlador (T-202):

    locust -f app/locustfile.py --headless -u 50 -r 5 --run-time 60s -H http://load-balancer
"""

from __future__ import annotations

import os
import random
import uuid

from locust import HttpUser, between, events, task


@events.init.add_listener
def _init_telemetry(environment, **_kwargs) -> None:
    """Inicia a telemetria da borda quando o Locust sobe (T-402, RF-029).

    Instrumenta o cliente ``requests`` do Locust para **injetar W3C `traceparent`** em
    cada chamada — é aqui que o trace nasce e é propagado ponta a ponta. Habilitado por
    padrão; defina ``HORUS_OTEL_ENABLED=false`` para desligar.
    """
    if os.getenv("HORUS_OTEL_ENABLED", "true").lower() != "true":
        return
    from opentelemetry.instrumentation.requests import RequestsInstrumentor

    from app.telemetry import configure_telemetry

    configure_telemetry()
    RequestsInstrumentor().instrument()


def _ref() -> str:
    """Referência aleatória curta (sem PII) para correlacionar registros."""
    return uuid.uuid4().hex[:12]


class ProntuarioUser(HttpUser):
    """Médico/clínico: cria prontuário, registra e finaliza consulta (RF-006..010)."""

    weight = 3
    wait_time = between(0.5, 2.0)

    @task(3)
    def fluxo_consulta(self) -> None:
        paciente = f"pac-{_ref()}"
        with self.client.post(
            "/prontuarios", json={"pacienteId": paciente}, name="POST /prontuarios",
            catch_response=True,
        ) as resp:
            if resp.status_code not in (200, 201):
                resp.failure(f"criar prontuário: {resp.status_code}")
                return
            prontuario_id = resp.json().get("id")

        consulta = self.client.post(
            f"/prontuarios/{prontuario_id}/consultas",
            json={"descricao": "consulta de rotina"},
            name="POST /prontuarios/{id}/consultas",
        )
        if consulta.status_code in (200, 201):
            consulta_id = consulta.json().get("id")
            self.client.post(
                f"/consultas/{consulta_id}/finalizar", name="POST /consultas/{id}/finalizar"
            )

    @task(1)
    def listar_prontuario(self) -> None:
        self.client.get("/prontuarios", name="GET /prontuarios")


class PaymentUser(HttpUser):
    """Operador financeiro: carteira, movimentação e pagamento (RF-011..015)."""

    weight = 3
    wait_time = between(0.5, 2.0)

    @task(3)
    def fluxo_pagamento(self) -> None:
        with self.client.post(
            "/carteiras",
            json={"titularId": f"tit-{_ref()}", "saldoInicial": 1000.00},
            name="POST /carteiras", catch_response=True,
        ) as resp:
            if resp.status_code not in (200, 201):
                resp.failure(f"criar carteira: {resp.status_code}")
                return
            carteira_id = resp.json().get("id")

        self.client.post(
            f"/carteiras/{carteira_id}/movimentacoes",
            json={"tipo": "ENTRADA", "valor": 250.00, "descricao": "aporte"},
            name="POST /carteiras/{id}/movimentacoes",
        )
        pagamento = self.client.post(
            f"/carteiras/{carteira_id}/pagamentos",
            json={"valor": round(random.uniform(10, 500), 2)},
            name="POST /carteiras/{id}/pagamentos",
        )
        if pagamento.status_code in (200, 201):
            pagamento_id = pagamento.json().get("id")
            self.client.post(
                f"/pagamentos/{pagamento_id}/aprovar", name="POST /pagamentos/{id}/aprovar"
            )

    @task(1)
    def consultar_carteira(self) -> None:
        # Carteira provavelmente inexistente: exercita o caminho de leitura/404.
        self.client.get(
            f"/carteiras/{random.randint(1, 1000)}", name="GET /carteiras/{id}"
        )


class InvoiceUser(HttpUser):
    """Operador fiscal: emite NF e, às vezes, simula falha e reprocessa (RF-016..020)."""

    weight = 2
    wait_time = between(0.5, 2.0)

    @task(3)
    def emitir_nota(self) -> None:
        # ~20% das emissões pedem falha simulada. O invoice responde 201 com status
        # FALHA (não 5xx); marcamos explicitamente como falha do Locust para que o
        # sinal de erro apareça nas estatísticas e reprocessamos a nota.
        simular_falha = random.random() < 0.2
        with self.client.post(
            "/notas",
            json={
                "valor": round(random.uniform(50, 5000), 2),
                "referencia": _ref(),
                "simularFalha": simular_falha,
            },
            name="POST /notas", catch_response=True,
        ) as resp:
            if resp.status_code in (200, 201):
                body = resp.json()
                nota_id = body.get("id")
                if body.get("status") == "FALHA":
                    resp.failure("emissão simulada falhou (status=FALHA)")
                    if nota_id is not None:
                        self.client.post(
                            f"/notas/{nota_id}/reprocessar", name="POST /notas/{id}/reprocessar"
                        )
            elif resp.status_code >= 500:
                resp.failure(f"emitir nota: {resp.status_code}")

    @task(1)
    def listar_notas(self) -> None:
        self.client.get("/notas", name="GET /notas")


class SagaUser(HttpUser):
    """Fluxo ponta a ponta pagar→emitir NF via orquestrador (ADR-0013, RF-021)."""

    weight = 1
    wait_time = between(1.0, 3.0)

    def on_start(self) -> None:
        """Cria uma carteira própria com saldo para a SAGA debitar.

        Sem isto, um `carteiraId` aleatório levaria a SAGA a 404/compensação em vez
        de exercitar o caminho feliz pagar→emitir.
        """
        self.carteira_id: int | None = None
        resp = self.client.post(
            "/carteiras",
            json={"titularId": f"saga-{_ref()}", "saldoInicial": 100000.00},
            name="POST /carteiras (saga setup)",
        )
        if resp.status_code in (200, 201):
            self.carteira_id = resp.json().get("id")

    @task
    def pagar_e_emitir(self) -> None:
        if self.carteira_id is None:  # setup falhou; tenta recriar na próxima iteração
            self.on_start()
            return
        self.client.post(
            "/sagas/pagar-e-emitir",
            json={
                "carteiraId": self.carteira_id,
                "valor": round(random.uniform(20, 800), 2),
                "simularFalhaNota": random.random() < 0.15,
            },
            name="POST /sagas/pagar-e-emitir",
        )
