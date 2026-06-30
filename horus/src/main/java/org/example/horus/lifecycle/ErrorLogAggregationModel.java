package org.example.horus.lifecycle;

import java.util.List;

/** Modelos da API de agregação de logs de erro correlacionados (T-505, RF-H-003). */
public final class ErrorLogAggregationModel {

    private ErrorLogAggregationModel() {
    }

    public record ErrorLogAggregation(
            String traceId,
            int errorCount,
            int groupCount,
            List<ErrorGroup> groups) {
    }

    public record ErrorGroup(
            String serviceName,
            String level,
            String fingerprint,
            int count,
            String sample,
            boolean hasStacktrace,
            String latestTimestampNanos) {
    }
}
