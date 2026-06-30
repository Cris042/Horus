package org.example.horus.lifecycle;

import jakarta.enterprise.context.ApplicationScoped;
import org.example.horus.lifecycle.SagaVisualizationModel.SagaFlow;
import org.example.horus.lifecycle.SagaVisualizationModel.SagaStep;
import org.example.horus.query.QueryModel.SpanRef;
import org.example.horus.query.QueryModel.TraceResult;
import org.example.horus.query.TraceQueryPort;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Projeta um trace na linha do tempo de uma SAGA (T-507, RF-H-016, ADR-0013).
 *
 * <p>Filtra os spans cuja operação segue o contrato {@code saga.{flow}.{step}}
 * (T-005), deriva {@code flow}/{@code step}/compensação do nome e ordena por tempo
 * de início. O desfecho é {@code compensated} quando há compensação, {@code completed}
 * quando há passos sem compensação e {@code none} quando o trace não tem SAGA.
 */
@ApplicationScoped
public class SagaVisualizationService {

    static final String SAGA_PREFIX = "saga.";
    static final String COMPENSATE_SUFFIX = ".compensate";

    private final TraceQueryPort traces;

    public SagaVisualizationService(TraceQueryPort traces) {
        this.traces = traces;
    }

    public Optional<SagaFlow> findByTraceId(String traceId) {
        return traces.findTrace(traceId).map(SagaVisualizationService::toSagaFlow);
    }

    private static SagaFlow toSagaFlow(TraceResult trace) {
        List<SpanRef> sagaSpans = trace.spans().stream()
                .filter(span -> isSagaOperation(span.operation()))
                .sorted(Comparator
                        .comparingLong((SpanRef span) -> span.startTimeMicros() > 0 ? span.startTimeMicros() : Long.MAX_VALUE)
                        .thenComparing(SpanRef::spanId, Comparator.nullsLast(String::compareTo)))
                .toList();

        if (sagaSpans.isEmpty()) {
            return new SagaFlow(trace.traceId(), null, "none", 0, 0, 0L, List.of());
        }

        long minStart = sagaSpans.stream()
                .mapToLong(SpanRef::startTimeMicros)
                .filter(value -> value > 0)
                .min()
                .orElse(0L);
        long maxEnd = sagaSpans.stream()
                .filter(span -> span.startTimeMicros() > 0)
                .mapToLong(span -> span.startTimeMicros() + Math.max(span.durationMicros(), 0L))
                .max()
                .orElse(0L);

        List<SagaStep> steps = new ArrayList<>(sagaSpans.size());
        String flow = null;
        int stepCount = 0;
        int compensationCount = 0;

        for (SpanRef span : sagaSpans) {
            ParsedOperation parsed = parse(span.operation());
            if (flow == null) {
                flow = parsed.flow();
            }
            if (parsed.compensation()) {
                compensationCount++;
            } else {
                stepCount++;
            }
            steps.add(new SagaStep(
                    parsed.flow(),
                    parsed.step(),
                    parsed.compensation(),
                    span.operation(),
                    span.serviceName(),
                    minStart > 0 && span.startTimeMicros() > 0 ? span.startTimeMicros() - minStart : 0L,
                    span.durationMicros()));
        }

        long totalDurationMicros = minStart > 0 && maxEnd >= minStart
                ? maxEnd - minStart
                : sagaSpans.stream().mapToLong(SpanRef::durationMicros).max().orElse(0L);

        String outcome = compensationCount > 0 ? "compensated" : "completed";

        return new SagaFlow(trace.traceId(), flow, outcome, stepCount, compensationCount, totalDurationMicros, steps);
    }

    private static boolean isSagaOperation(String operation) {
        return operation != null && operation.startsWith(SAGA_PREFIX);
    }

    /**
     * Quebra {@code saga.{flow}.{step}} (ou {@code ....compensate}) em flow/step.
     * Steps com múltiplos segmentos são re-unidos por ponto, exceto o sufixo de compensação.
     */
    private static ParsedOperation parse(String operation) {
        String body = operation.substring(SAGA_PREFIX.length());
        boolean compensation = body.endsWith(COMPENSATE_SUFFIX);
        if (compensation) {
            body = body.substring(0, body.length() - COMPENSATE_SUFFIX.length());
        }
        int dot = body.indexOf('.');
        String flow = dot >= 0 ? body.substring(0, dot) : body;
        String step = dot >= 0 ? body.substring(dot + 1) : "";
        return new ParsedOperation(flow, step, compensation);
    }

    private record ParsedOperation(String flow, String step, boolean compensation) {
    }
}
