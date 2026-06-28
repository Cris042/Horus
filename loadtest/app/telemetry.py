"""Configuração de telemetria OpenTelemetry — T-402, RF-029.

Inicializa o `TracerProvider` com o recurso canônico do contrato (T-005):
``service.name=loadtest-api`` / ``service.namespace=medrec``. A propagação é **W3C
Trace Context** (padrão do OTel) — é daqui que o contexto de trace nasce na borda e é
injetado nas chamadas HTTP rumo ao Load Balancer e aos serviços.

O endpoint OTLP vem das variáveis padrão do OTel (`OTEL_EXPORTER_OTLP_ENDPOINT`); sem
elas, o exportador usa o default local e simplesmente não entrega (não bloqueia a app).
"""

from __future__ import annotations

from opentelemetry import trace
from opentelemetry.sdk.resources import Resource
from opentelemetry.sdk.trace import TracerProvider
from opentelemetry.sdk.trace.export import (
    BatchSpanProcessor,
    SimpleSpanProcessor,
    SpanExporter,
)

# Valores canônicos do contrato de telemetria (docs/telemetry/CONTRACT.md §1).
SERVICE_NAME = "loadtest-api"
SERVICE_NAMESPACE = "medrec"

_configured = False


def build_tracer_provider(
    *,
    service_name: str = SERVICE_NAME,
    exporter: SpanExporter | None = None,
) -> TracerProvider:
    """Cria um `TracerProvider` com o recurso canônico (sem tocar no estado global).

    Se ``exporter`` for dado (ex.: em memória, nos testes), usa um processador síncrono;
    caso contrário, exporta via OTLP/HTTP em lote (configurado por env do OTel).
    """
    resource = Resource.create(
        {"service.name": service_name, "service.namespace": SERVICE_NAMESPACE}
    )
    provider = TracerProvider(resource=resource)
    if exporter is not None:
        provider.add_span_processor(SimpleSpanProcessor(exporter))
    else:
        from opentelemetry.exporter.otlp.proto.http.trace_exporter import OTLPSpanExporter

        provider.add_span_processor(BatchSpanProcessor(OTLPSpanExporter()))
    return provider


def configure_telemetry(
    *,
    service_name: str = SERVICE_NAME,
    set_global: bool = True,
    exporter: SpanExporter | None = None,
) -> TracerProvider:
    """Constrói o provider e (por padrão) o registra como global, de forma idempotente."""
    global _configured
    provider = build_tracer_provider(service_name=service_name, exporter=exporter)
    if set_global and not _configured:
        trace.set_tracer_provider(provider)
        _configured = True
    return provider
