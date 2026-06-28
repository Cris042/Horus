package org.example.horus.lifecycle;

import java.util.List;

/** Modelos da API de ciclo de vida de query (T-504, RF-H-002). */
public final class QueryLifecycleModel {

    private QueryLifecycleModel() {
    }

    public record QueryLifecycle(
            String traceId,
            int queryCount,
            List<QuerySpan> queries) {
    }

    public record QuerySpan(
            String spanId,
            String parentSpanId,
            String serviceName,
            String databaseName,
            String databaseSystem,
            String operationName,
            String statement,
            long startTimeMicros,
            long offsetMicros,
            long durationMicros) {
    }
}
