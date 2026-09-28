package org.example.horus.query.backend;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.example.horus.query.QueryModel.SlowQuery;
import org.example.horus.query.QueryModel.SpanRef;
import org.example.horus.query.QueryModel.TraceResult;
import org.example.horus.query.QueryModel.TraceSearch;
import org.example.horus.query.QueryModel.TraceSummary;
import org.example.horus.query.TraceQueryPort;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** Adapter {@link TraceQueryPort} sobre o Jaeger (T-501; busca por janela em T-1001). */
@ApplicationScoped
public class JaegerTraceAdapter implements TraceQueryPort {

    /** Filtro de tags do Jaeger para spans em erro (OTel exporta status ERROR como {@code error=true}). */
    static final String ERROR_TAGS = "{\"error\":\"true\"}";

    private final JaegerClient client;
    private final Set<String> excludedServices;

    public JaegerTraceAdapter(@RestClient JaegerClient client,
                              @ConfigProperty(name = "horus.query.jaeger.excluded-services",
                                      defaultValue = "jaeger,jaeger-all-in-one,jaeger-query")
                              List<String> excludedServices) {
        this.client = client;
        this.excludedServices = Set.copyOf(excludedServices);
    }

    @Override
    public Optional<TraceResult> findTrace(String traceId) {
        final JsonNode root;
        try {
            root = client.getTrace(traceId);
        } catch (WebApplicationException e) {
            // 404/erro do Jaeger => trace inexistente para o chamador
            if (e.getResponse() != null && e.getResponse().getStatus() == 404) {
                return Optional.empty();
            }
            throw e;
        }
        JsonNode data = root.path("data");
        if (!data.isArray() || data.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(toTraceResult(data.get(0), traceId));
    }

    @Override
    public List<TraceSummary> searchTraces(TraceSearch search) {
        int limit = Math.max(1, search.limit());
        List<String> services = isBlank(search.service()) ? listServices() : List.of(search.service());
        String minDuration = search.minDurationMicros() > 0 ? search.minDurationMicros() + "us" : null;
        String tags = search.onlyErrors() ? ERROR_TAGS : null;
        String operation = isBlank(search.operation()) ? null : search.operation();

        // O Jaeger exige um serviço por busca: sem filtro, varre todos e mescla por traceId.
        Map<String, TraceSummary> byId = new LinkedHashMap<>();
        for (String service : services) {
            JsonNode root = client.searchTraces(service, operation, search.window().startMicros(),
                    search.window().endMicros(), minDuration, limit, tags);
            for (JsonNode trace : root.path("data")) {
                TraceSummary summary = summarize(trace);
                if (summary.spanCount() == 0 || (search.onlyErrors() && summary.errorSpanCount() == 0)) {
                    continue;
                }
                byId.putIfAbsent(summary.traceId(), summary);
            }
        }
        return byId.values().stream()
                .sorted(Comparator.comparingLong(TraceSummary::startTimeMicros).reversed())
                .limit(limit)
                .toList();
    }

    @Override
    public List<String> listServices() {
        List<String> out = new ArrayList<>();
        for (JsonNode s : client.services().path("data")) {
            String name = s.asText("");
            if (!name.isBlank() && !excludedServices.contains(name)) {
                out.add(name);
            }
        }
        out.sort(Comparator.naturalOrder());
        return out;
    }

    private static TraceResult toTraceResult(JsonNode trace, String fallbackTraceId) {
        // processID -> serviceName
        JsonNode processes = trace.path("processes");
        List<SpanRef> spans = new ArrayList<>();
        for (JsonNode span : trace.path("spans")) {
            String processId = span.path("processID").asText("");
            String serviceName = processes.path(processId).path("serviceName").asText("unknown");
            spans.add(new SpanRef(
                    span.path("spanID").asText(""),
                    span.path("operationName").asText(""),
                    serviceName,
                    span.path("duration").asLong(0),
                    span.path("startTime").asLong(0),
                    parentSpanId(span),
                    spanKind(span),
                    tagValue(span, "db.operation.name"),
                    firstNonBlank(tagValue(span, "db.namespace"), tagValue(span, "db.name")),
                    firstNonBlank(tagValue(span, "db.system.name"), tagValue(span, "db.system")),
                    tagValue(span, "db.query.text"),
                    isError(span)));
        }
        return new TraceResult(trace.path("traceID").asText(fallbackTraceId), spans.size(), spans);
    }

    /**
     * Resume um trace do Jaeger: raiz (span sem pai, ou o mais antigo), duração de ponta a
     * ponta, serviços envolvidos, spans em erro e a query SQL mais lenta.
     */
    private static TraceSummary summarize(JsonNode trace) {
        TraceResult result = toTraceResult(trace, "");
        List<SpanRef> spans = result.spans();
        int errors = (int) spans.stream().filter(SpanRef::error).count();
        if (spans.isEmpty()) {
            return new TraceSummary(result.traceId(), null, null, 0, 0, 0, List.of(), errors, null);
        }
        Set<String> ids = new LinkedHashSet<>();
        spans.forEach(sp -> ids.add(sp.spanId()));
        SpanRef root = spans.stream()
                .filter(sp -> sp.parentSpanId() == null || !ids.contains(sp.parentSpanId()))
                .min(Comparator.comparingLong(SpanRef::startTimeMicros))
                .orElse(spans.get(0));
        long start = spans.stream().mapToLong(SpanRef::startTimeMicros).min().orElse(0);
        long end = spans.stream().mapToLong(sp -> sp.startTimeMicros() + sp.durationMicros()).max().orElse(start);
        Set<String> services = new LinkedHashSet<>();
        spans.forEach(sp -> services.add(sp.serviceName()));
        SlowQuery slowest = spans.stream()
                .filter(sp -> sp.dbSystemName() != null || sp.dbQueryText() != null)
                .max(Comparator.comparingLong(SpanRef::durationMicros))
                .map(sp -> new SlowQuery(sp.serviceName(), sp.dbNamespace(), sp.dbOperationName(),
                        sp.dbQueryText(), sp.durationMicros()))
                .orElse(null);
        return new TraceSummary(result.traceId(), root.serviceName(), root.operation(), start,
                end - start, spans.size(), List.copyOf(services), errors, slowest);
    }

    /** Span em erro: {@code error=true} (convenção OpenTracing/Jaeger) ou {@code otel.status_code=ERROR}. */
    private static boolean isError(JsonNode span) {
        return "true".equals(tagValue(span, "error")) || "error".equals(tagValue(span, "otel.status_code"));
    }

    private static String parentSpanId(JsonNode span) {
        JsonNode refs = span.path("references");
        if (!refs.isArray()) {
            return null;
        }
        for (JsonNode ref : refs) {
            if ("CHILD_OF".equalsIgnoreCase(ref.path("refType").asText())) {
                String spanId = ref.path("spanID").asText("");
                return spanId.isBlank() ? null : spanId;
            }
        }
        return null;
    }

    private static String spanKind(JsonNode span) {
        return tagValue(span, "span.kind");
    }

    private static String tagValue(JsonNode span, String key) {
        JsonNode tags = span.path("tags");
        if (!tags.isArray()) {
            return null;
        }
        for (JsonNode tag : tags) {
            if (!key.equals(tag.path("key").asText())) {
                continue;
            }
            String kind = tag.path("value").asText("");
            return kind.isBlank() ? null : kind.toLowerCase();
        }
        return null;
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static String firstNonBlank(String left, String right) {
        if (left != null && !left.isBlank()) {
            return left;
        }
        return (right == null || right.isBlank()) ? null : right;
    }
}
