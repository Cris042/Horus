package org.example.horus.lifecycle;

import jakarta.enterprise.context.ApplicationScoped;
import org.example.horus.lifecycle.QueryLifecycleModel.QueryLifecycle;
import org.example.horus.lifecycle.QueryLifecycleModel.QuerySpan;
import org.example.horus.query.QueryModel.SpanRef;
import org.example.horus.query.TraceQueryPort;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/** Projeta os spans SQL de um trace em uma visão de ciclo de vida de queries. */
@ApplicationScoped
public class QueryLifecycleService {

    private final TraceQueryPort traces;

    public QueryLifecycleService(TraceQueryPort traces) {
        this.traces = traces;
    }

    public Optional<QueryLifecycle> findByTraceId(String traceId) {
        return traces.findTrace(traceId).map(trace -> {
            long requestStartMicros = trace.spans().stream()
                    .mapToLong(SpanRef::startTimeMicros)
                    .filter(value -> value > 0)
                    .min()
                    .orElse(0L);

            List<QuerySpan> queries = trace.spans().stream()
                    .filter(QueryLifecycleService::isQuerySpan)
                    .sorted(Comparator
                            .comparingLong((SpanRef span) -> span.startTimeMicros() > 0 ? span.startTimeMicros() : Long.MAX_VALUE)
                            .thenComparing(SpanRef::spanId, Comparator.nullsLast(String::compareTo)))
                    .map(span -> new QuerySpan(
                            span.spanId(),
                            span.parentSpanId(),
                            span.serviceName(),
                            span.dbNamespace(),
                            span.dbSystemName(),
                            operationName(span),
                            statement(span),
                            span.startTimeMicros(),
                            requestStartMicros > 0 && span.startTimeMicros() > 0
                                    ? span.startTimeMicros() - requestStartMicros
                                    : 0L,
                            span.durationMicros()))
                    .toList();

            return new QueryLifecycle(trace.traceId(), queries.size(), queries);
        });
    }

    private static boolean isQuerySpan(SpanRef span) {
        if (notBlank(span.dbSystemName()) || notBlank(span.dbNamespace())
                || notBlank(span.dbQueryText()) || notBlank(span.dbOperationName())) {
            return true;
        }

        String operation = lower(span.operation());
        if (!"client".equals(lower(span.kind()))) {
            return false;
        }
        return operation.startsWith("select ")
                || operation.startsWith("insert ")
                || operation.startsWith("update ")
                || operation.startsWith("delete ")
                || operation.startsWith("with ");
    }

    private static String operationName(SpanRef span) {
        if (notBlank(span.dbOperationName())) {
            return span.dbOperationName();
        }
        String operation = span.operation();
        if (operation == null || operation.isBlank()) {
            return null;
        }
        int whitespace = operation.indexOf(' ');
        return whitespace > 0 ? operation.substring(0, whitespace) : operation;
    }

    private static String statement(SpanRef span) {
        if (notBlank(span.dbQueryText())) {
            return span.dbQueryText();
        }
        return span.operation();
    }

    private static String lower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    private static boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }
}
