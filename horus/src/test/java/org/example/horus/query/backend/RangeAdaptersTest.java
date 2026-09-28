package org.example.horus.query.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.horus.query.QueryModel.LogLine;
import org.example.horus.query.QueryModel.MetricSeries;
import org.example.horus.query.TimeWindow;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Consultas por janela no Loki e no Prometheus (T-1001): parâmetros enviados e mapeamento. */
class RangeAdaptersTest {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final TimeWindow WINDOW = TimeWindow.last(Duration.ofMinutes(10), Instant.parse("2026-09-28T12:00:00Z"));

    private static JsonNode read(String json) {
        try {
            return JSON.readTree(json);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    void loki_findInWindow_sendsNanosAndMapsLines() {
        long[] seen = new long[2];
        LokiClient client = new LokiClient() {
            @Override
            public JsonNode queryRange(String logQl, int limit, String direction) {
                throw new UnsupportedOperationException();
            }

            @Override
            public JsonNode queryRange(String logQl, long start, long end, int limit, String direction) {
                seen[0] = start;
                seen[1] = end;
                return read("""
                        {"data":{"result":[{"stream":{"service_name":"payment-service"},
                          "values":[["1759060000000000000","saldo insuficiente"]]}]}}""");
            }
        };
        List<LogLine> lines = new LokiLogAdapter(client).findInWindow("{x=\"y\"}", WINDOW, 10);

        assertEquals(WINDOW.startNanos(), seen[0]);
        assertEquals(WINDOW.endNanos(), seen[1]);
        assertEquals("saldo insuficiente", lines.get(0).line());
        assertEquals("payment-service", lines.get(0).labels().get("service_name"));
    }

    @Test
    void prometheus_rangeQuery_mapsMatrix() {
        double[] seen = new double[3];
        PrometheusClient client = new PrometheusClient() {
            @Override
            public JsonNode instantQuery(String promQl) {
                throw new UnsupportedOperationException();
            }

            @Override
            public JsonNode rangeQuery(String promQl, double start, double end, long step) {
                seen[0] = start;
                seen[1] = end;
                seen[2] = step;
                return read("""
                        {"data":{"resultType":"matrix","result":[{"metric":{"job":"payment"},
                          "values":[[1759060000,"1"],[1759060060,"NaN"]]}]}}""");
            }
        };
        List<MetricSeries> series = new PrometheusMetricAdapter(client).rangeQuery("up", WINDOW, 60);

        assertEquals(WINDOW.startSeconds(), seen[0]);
        assertEquals(WINDOW.endSeconds(), seen[1]);
        assertEquals(60, seen[2]);
        assertEquals("payment", series.get(0).labels().get("job"));
        assertEquals(2, series.get(0).points().size());
        assertEquals(1.0, series.get(0).points().get(0).value());
        assertTrue(Double.isNaN(series.get(0).points().get(1).value()));
    }
}
