package org.example.horus.query.backend;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.example.horus.query.MetricQueryPort;
import org.example.horus.query.QueryModel.MetricSample;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Adapter {@link MetricQueryPort} sobre o Prometheus (T-501). */
@ApplicationScoped
public class PrometheusMetricAdapter implements MetricQueryPort {

    private final PrometheusClient client;

    public PrometheusMetricAdapter(@RestClient PrometheusClient client) {
        this.client = client;
    }

    @Override
    public List<MetricSample> instantQuery(String promQl) {
        JsonNode root = client.instantQuery(promQl);
        List<MetricSample> out = new ArrayList<>();
        for (JsonNode item : root.path("data").path("result")) {
            Map<String, String> labels = new LinkedHashMap<>();
            item.path("metric").fields()
                    .forEachRemaining(e -> labels.put(e.getKey(), e.getValue().asText()));
            JsonNode value = item.path("value"); // [ <ts>, "<val>" ]
            if (value.isArray() && value.size() >= 2) {
                double ts = value.get(0).asDouble(0);
                double v = parseDouble(value.get(1).asText("0"));
                out.add(new MetricSample(labels, v, ts));
            }
        }
        return out;
    }

    private static double parseDouble(String s) {
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return Double.NaN; // Prometheus pode retornar "NaN"/"+Inf"
        }
    }
}
