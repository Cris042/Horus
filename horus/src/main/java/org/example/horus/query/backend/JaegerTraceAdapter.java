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
                    span.path("duration").asLong(0)));
        }
        return Optional.of(new TraceResult(trace.path("traceID").asText(traceId), spans.size(), spans));
    }
}
