"""Testes da instrumentação OpenTelemetry — T-402, RF-029.

Verificam, de forma offline (exportador em memória, sem Collector), que o recurso é o
canônico do contrato, que a app FastAPI emite span de servidor quando instrumentada e
que a propagação é **W3C Trace Context** (injeta `traceparent`).
"""

from __future__ import annotations

import pytest
from fastapi.testclient import TestClient
from opentelemetry import trace
from opentelemetry.sdk.trace.export.in_memory_span_exporter import InMemorySpanExporter

from app import telemetry
from app.main import app


def test_resource_uses_canonical_service_name() -> None:
    exporter = InMemorySpanExporter()
    provider = telemetry.build_tracer_provider(exporter=exporter)
    attrs = provider.resource.attributes
    assert attrs["service.name"] == "loadtest-api"
    assert attrs["service.namespace"] == "medrec"


def test_fastapi_instrumentation_emits_server_span() -> None:
    from opentelemetry.instrumentation.fastapi import FastAPIInstrumentor

    exporter = InMemorySpanExporter()
    provider = telemetry.build_tracer_provider(exporter=exporter)
    FastAPIInstrumentor.instrument_app(app, tracer_provider=provider)
    # Outras suítes (test_api) já podem ter construído e cacheado o middleware_stack via
    # TestClient; força o Starlette a reconstruí-lo para incluir o middleware OTel. Em
    # produção a instrumentação ocorre no import, antes de qualquer request — sem isto.
    app.middleware_stack = None
    try:
        TestClient(app).get("/health")
        spans = exporter.get_finished_spans()
        assert spans, "nenhum span emitido pela app instrumentada"
        assert any("/health" in (s.name or "") for s in spans)
        assert spans[0].resource.attributes["service.name"] == "loadtest-api"
    finally:
        FastAPIInstrumentor.uninstrument_app(app)
        app.middleware_stack = None


def test_w3c_traceparent_is_injected() -> None:
    exporter = InMemorySpanExporter()
    provider = telemetry.build_tracer_provider(exporter=exporter)
    tracer = provider.get_tracer("test")

    from opentelemetry.propagate import inject

    carrier: dict[str, str] = {}
    with tracer.start_as_current_span("edge"):
        inject(carrier)
    assert "traceparent" in carrier, "propagação W3C deve injetar traceparent"
