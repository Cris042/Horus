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
            String dbQueryText,
            boolean error) {

        public SpanRef(String spanId, String operation, String serviceName, long durationMicros) {
            this(spanId, operation, serviceName, durationMicros, 0L, null, null, null, null, null, null, false);
        }

        /** Sem status de erro (fatias anteriores à T-1010). */
        public SpanRef(String spanId, String operation, String serviceName, long durationMicros,
                       long startTimeMicros, String parentSpanId, String kind, String dbOperationName,
                       String dbNamespace, String dbSystemName, String dbQueryText) {
            this(spanId, operation, serviceName, durationMicros, startTimeMicros, parentSpanId, kind,
                    dbOperationName, dbNamespace, dbSystemName, dbQueryText, false);
        }

        public SpanRef(String spanId, String operation, String serviceName, long durationMicros,
                       long startTimeMicros, String parentSpanId, String kind) {
            this(spanId, operation, serviceName, durationMicros, startTimeMicros, parentSpanId, kind,
                    null, null, null, null, false);
        }
    }

    /** Linha de log correlacionada (rótulos de stream + linha). */
    public record LogLine(String timestampNanos, String line, Map<String, String> labels) {
    }

    /** Amostra de métrica instantânea (vector) do Prometheus. */
    public record MetricSample(Map<String, String> labels, double value, double timestampSeconds) {
    }

    /**
     * Critérios de busca de traces numa janela (T-1001). {@code service} nulo = todos os
     * serviços; {@code minDurationMicros} 0 = sem piso; {@code onlyErrors} filtra traces com
     * ao menos um span em erro.
     */
    public record TraceSearch(String service, String operation, TimeWindow window,
                              long minDurationMicros, boolean onlyErrors, int limit) {
    }

    /**
     * Resumo de um trace encontrado numa busca (T-1001) — o suficiente para listar e
     * priorizar sem baixar o trace inteiro de novo. {@code slowestQuery} é nulo quando o
     * trace não tem spans de banco.
     */
    public record TraceSummary(String traceId, String rootService, String rootOperation,
                               long startTimeMicros, long durationMicros, int spanCount,
                               List<String> services, int errorSpanCount, SlowQuery slowestQuery) {
    }

    /** Span de banco mais lento de um trace (statement já parametrizado pela origem, T-401). */
    public record SlowQuery(String serviceName, String dbNamespace, String dbOperationName,
                            String dbQueryText, long durationMicros) {
    }

    /** Série de uma consulta PromQL de intervalo ({@code query_range}, matrix). */
    public record MetricSeries(Map<String, String> labels, List<MetricPoint> points) {
    }

    /** Ponto de uma série temporal. */
    public record MetricPoint(double timestampSeconds, double value) {
    }
}
