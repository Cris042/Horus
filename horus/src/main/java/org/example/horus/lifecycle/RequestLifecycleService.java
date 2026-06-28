package org.example.horus.lifecycle;

import jakarta.enterprise.context.ApplicationScoped;
import org.example.horus.lifecycle.RequestLifecycleModel.RequestLifecycle;
import org.example.horus.lifecycle.RequestLifecycleModel.WaterfallSpan;
import org.example.horus.query.QueryModel.SpanRef;
import org.example.horus.query.QueryModel.TraceResult;
import org.example.horus.query.TraceQueryPort;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Projeta um trace em uma timeline/waterfall de spans. */
@ApplicationScoped
public class RequestLifecycleService {

    static final String WORKER_SERVICE = "report-worker";

    private final TraceQueryPort traces;

    public RequestLifecycleService(TraceQueryPort traces) {
        this.traces = traces;
    }

    public Optional<RequestLifecycle> findByTraceId(String traceId) {
        return traces.findTrace(traceId).map(RequestLifecycleService::toLifecycle);
    }

    private static RequestLifecycle toLifecycle(TraceResult trace) {
        List<SpanRef> ordered = trace.spans().stream()
                .sorted(Comparator
                        .comparingLong((SpanRef span) -> span.startTimeMicros() > 0 ? span.startTimeMicros() : Long.MAX_VALUE)
                        .thenComparing(Comparator.comparingLong(SpanRef::durationMicros).reversed())
                        .thenComparing(SpanRef::spanId, Comparator.nullsLast(String::compareTo)))
                .toList();

        Map<String, SpanRef> byId = ordered.stream()
                .filter(span -> span.spanId() != null && !span.spanId().isBlank())
                .collect(Collectors.toMap(SpanRef::spanId, Function.identity(), (left, right) -> left));

        long minStart = ordered.stream()
                .mapToLong(SpanRef::startTimeMicros)
                .filter(value -> value > 0)
                .min()
                .orElse(0L);
        long maxEnd = ordered.stream()
                .filter(span -> span.startTimeMicros() > 0)
                .mapToLong(span -> span.startTimeMicros() + Math.max(span.durationMicros(), 0L))
                .max()
                .orElse(0L);

        List<WaterfallSpan> waterfall = new ArrayList<>(ordered.size());
        Set<String> services = new LinkedHashSet<>();
        boolean messagingInvolved = false;
        boolean workerInvolved = false;

        for (SpanRef span : ordered) {
            services.add(span.serviceName());
            String category = categoryOf(span);
            messagingInvolved = messagingInvolved || "messaging".equals(category);
            workerInvolved = workerInvolved || WORKER_SERVICE.equals(span.serviceName());
            waterfall.add(new WaterfallSpan(
                    span.spanId(),
                    span.parentSpanId(),
                    span.operation(),
                    span.serviceName(),
                    span.kind(),
                    category,
                    span.startTimeMicros(),
                    minStart > 0 && span.startTimeMicros() > 0 ? span.startTimeMicros() - minStart : 0L,
                    span.durationMicros(),
                    depthOf(span, byId)));
        }

        long durationMicros = minStart > 0 && maxEnd >= minStart
                ? maxEnd - minStart
                : ordered.stream().mapToLong(SpanRef::durationMicros).max().orElse(0L);

        return new RequestLifecycle(
                trace.traceId(),
                trace.spanCount(),
                durationMicros,
                messagingInvolved,
                workerInvolved,
                services.stream().toList(),
                waterfall);
    }

    private static int depthOf(SpanRef span, Map<String, SpanRef> byId) {
        int depth = 0;
        String parentId = span.parentSpanId();
        while (parentId != null) {
            SpanRef parent = byId.get(parentId);
            if (parent == null) {
                break;
            }
            depth++;
            parentId = parent.parentSpanId();
            if (depth > byId.size()) {
                break;
            }
        }
        return depth;
    }

    private static String categoryOf(SpanRef span) {
        if (WORKER_SERVICE.equals(span.serviceName())) {
            return "worker";
        }
        String operation = lower(span.operation());
        String kind = lower(span.kind());

        if (operation.startsWith("select ")
                || operation.startsWith("insert ")
                || operation.startsWith("update ")
                || operation.startsWith("delete ")
                || operation.startsWith("with ")) {
            return "database";
        }
        if ("producer".equals(kind) || "consumer".equals(kind)
                || operation.contains("publish")
                || operation.contains("process")
                || operation.contains("relatorios")) {
            return "messaging";
        }
        if (operation.startsWith("get ")
                || operation.startsWith("post ")
                || operation.startsWith("put ")
                || operation.startsWith("patch ")
                || operation.startsWith("delete ")) {
            return "http";
        }
        return "internal";
    }

    private static String lower(String value) {
        return value == null ? "" : value.toLowerCase();
    }
}
