package org.example.horus.query.backend;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.example.horus.query.QueryModel.SpanRef;
import org.example.horus.query.QueryModel.TraceResult;
import org.example.horus.query.TraceQueryPort;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Adapter {@link TraceQueryPort} sobre o Jaeger (T-501). */
@ApplicationScoped
public class JaegerTraceAdapter implements TraceQueryPort {

    private final JaegerClient client;

    public JaegerTraceAdapter(@RestClient JaegerClient client) {
        this.client = client;
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
        JsonNode trace = data.get(0);

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
                    tagValue(span, "db.query.text")));
        }
        return Optional.of(new TraceResult(trace.path("traceID").asText(traceId), spans.size(), spans));
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

    private static String firstNonBlank(String left, String right) {
        if (left != null && !left.isBlank()) {
            return left;
        }
        return (right == null || right.isBlank()) ? null : right;
    }
}
