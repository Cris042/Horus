package org.example.horus.query.backend;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.example.horus.query.LogQueryPort;
import org.example.horus.query.QueryModel.LogLine;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Adapter {@link LogQueryPort} sobre o Loki (T-501). */
@ApplicationScoped
public class LokiLogAdapter implements LogQueryPort {

    private final LokiClient client;

    /**
     * Template LogQL de correlação por trace_id (placeholder {@code %s}). O rótulo/seletor
     * exato depende do mapeamento OTLP→Loki, finalizado na validação ponta a ponta (T-405);
     * por isso é configurável.
     */
    @ConfigProperty(name = "horus.query.loki.logql-template",
            defaultValue = "{service_namespace=\"medrec\"} | trace_id=\"%s\"")
    String logQlTemplate;

    public LokiLogAdapter(@RestClient LokiClient client) {
        this.client = client;
    }

    @Override
    public List<LogLine> findByTraceId(String traceId, int limit) {
        String logQl = String.format(logQlTemplate, traceId);
        JsonNode root = client.queryRange(logQl, limit, "backward");

        List<LogLine> out = new ArrayList<>();
        for (JsonNode stream : root.path("data").path("result")) {
            Map<String, String> labels = new LinkedHashMap<>();
            stream.path("stream").fields()
                    .forEachRemaining(e -> labels.put(e.getKey(), e.getValue().asText()));
            for (JsonNode entry : stream.path("values")) {
                if (entry.isArray() && entry.size() >= 2) {
                    out.add(new LogLine(entry.get(0).asText(), entry.get(1).asText(), labels));
                }
            }
        }
        return out;
    }
}
