package org.example.horus.query;

import java.util.List;
import java.util.Map;

/**
 * DTOs mínimos da camada de consulta do Horus (T-501, RF-H-014).
 *
 * <p>Representações neutras de backend — o Horus consome Jaeger (traces), Loki (logs)
 * e Prometheus (métricas) por trás de portas e expõe estes tipos, sem vazar o schema
 * de cada backend. A correlação por {@code trace_id} (request ↔ query ↔ log ↔ mensagem)
 * é construída sobre estes blocos em T-502.
 */
public final class QueryModel {

    private QueryModel() {
    }

    /** Resultado de uma consulta de trace por {@code traceId}. */
    public record TraceResult(String traceId, int spanCount, List<SpanRef> spans) {
    }

    /**
     * Referência enxuta a um span (sem atributos crus — PII fica fora, ver CONTRACT §6).
     *
     * <p>Os campos de waterfall ({@code startTimeMicros}, {@code parentSpanId}, {@code kind})
     * são opcionais para manter compatibilidade com testes/fatias anteriores que só precisam
     * de identificação, operação, serviço e duração.
     */
    public record SpanRef(
            String spanId,
            String operation,
            String serviceName,
            long durationMicros,
            long startTimeMicros,
            String parentSpanId,
            String kind,
            String dbOperationName,
            String dbNamespace,
            String dbSystemName,
            String dbQueryText) {

        public SpanRef(String spanId, String operation, String serviceName, long durationMicros) {
            this(spanId, operation, serviceName, durationMicros, 0L, null, null, null, null, null, null);
        }

        public SpanRef(String spanId, String operation, String serviceName, long durationMicros,
                       long startTimeMicros, String parentSpanId, String kind) {
            this(spanId, operation, serviceName, durationMicros, startTimeMicros, parentSpanId, kind,
                    null, null, null, null);
        }
    }

    /** Linha de log correlacionada (rótulos de stream + linha). */
    public record LogLine(String timestampNanos, String line, Map<String, String> labels) {
    }

    /** Amostra de métrica instantânea (vector) do Prometheus. */
    public record MetricSample(Map<String, String> labels, double value, double timestampSeconds) {
    }
}
