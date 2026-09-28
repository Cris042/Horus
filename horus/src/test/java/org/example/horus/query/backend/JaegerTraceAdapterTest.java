package org.example.horus.query.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.horus.query.QueryModel.TraceSearch;
import org.example.horus.query.QueryModel.TraceSummary;
import org.example.horus.query.TimeWindow;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Mapeamento da busca de traces do Jaeger para {@link TraceSummary} (T-1001), sem rede. */
class JaegerTraceAdapterTest {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final TimeWindow WINDOW = TimeWindow.last(Duration.ofHours(1), Instant.parse("2026-09-28T12:00:00Z"));

    /** Trace de 3 spans: raiz HTTP no prontuario, SELECT lento e um span em erro. */
    private static final String TRACE_A = """
            {"traceID":"aaa","processes":{"p1":{"serviceName":"prontuario-service"},"p2":{"serviceName":"payment-service"}},
             "spans":[
               {"spanID":"r","operationName":"GET /prontuarios/{id}","processID":"p1","startTime":1000,"duration":9000,"references":[],
                "tags":[{"key":"span.kind","value":"server"}]},
               {"spanID":"q","operationName":"SELECT prontuario_db","processID":"p1","startTime":2000,"duration":4000,
                "references":[{"refType":"CHILD_OF","spanID":"r"}],
                "tags":[{"key":"db.system.name","value":"postgresql"},{"key":"db.namespace","value":"prontuario_db"},
                        {"key":"db.operation.name","value":"SELECT"},{"key":"db.query.text","value":"select * from prontuario where id=?"}]},
               {"spanID":"e","operationName":"POST /pagamentos","processID":"p2","startTime":3000,"duration":8000,
                "references":[{"refType":"CHILD_OF","spanID":"r"}],"tags":[{"key":"error","value":true}]}
             ]}""";

    private static final String TRACE_B = """
            {"traceID":"bbb","processes":{"p1":{"serviceName":"payment-service"}},
             "spans":[{"spanID":"x","operationName":"GET /carteiras","processID":"p1","startTime":5000,"duration":100,"references":[],"tags":[]}]}""";

    /** Cliente falso: devolve traces por serviço e registra as chamadas. */
    private static final class FakeJaeger implements JaegerClient {
        final List<String> searchedServices = new ArrayList<>();
        String lastTags;
        String lastMinDuration;

        @Override
        public JsonNode getTrace(String traceId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public JsonNode searchTraces(String service, String operation, long start, long end,
                                     String minDuration, int limit, String tags) {
            searchedServices.add(service);
            lastTags = tags;
            lastMinDuration = minDuration;
            String data = switch (service) {
                case "prontuario-service" -> TRACE_A;
                case "payment-service" -> TRACE_A + "," + TRACE_B; // A reaparece: deve ser deduplicado
                default -> "";
            };
            return read("{\"data\":[" + data + "]}");
        }

        @Override
        public JsonNode services() {
            return read("{\"data\":[\"prontuario-service\",\"jaeger-all-in-one\",\"payment-service\"]}");
        }
    }

    private static JsonNode read(String json) {
        try {
            return JSON.readTree(json);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static JaegerTraceAdapter adapter(FakeJaeger client) {
        return new JaegerTraceAdapter(client, List.of("jaeger-all-in-one"));
    }

    @Test
    void listServices_excludesJaegerInternals_sorted() {
        assertEquals(List.of("payment-service", "prontuario-service"), adapter(new FakeJaeger()).listServices());
    }

    @Test
    void search_allServices_mergesDedupesAndSummarizes() {
        FakeJaeger client = new FakeJaeger();
        List<TraceSummary> found = adapter(client).searchTraces(new TraceSearch(null, null, WINDOW, 0, false, 10));

        assertEquals(List.of("payment-service", "prontuario-service"), client.searchedServices);
        assertEquals(2, found.size());
        assertEquals("bbb", found.get(0).traceId()); // mais recente primeiro

        TraceSummary a = found.get(1);
        assertEquals("prontuario-service", a.rootService());
        assertEquals("GET /prontuarios/{id}", a.rootOperation());
        assertEquals(1000, a.startTimeMicros());
        assertEquals(10_000, a.durationMicros()); // 1000 → 11000 (span em erro termina por último)
        assertEquals(3, a.spanCount());
        assertEquals(1, a.errorSpanCount());
        assertEquals(List.of("prontuario-service", "payment-service"), a.services());
        assertEquals("prontuario_db", a.slowestQuery().dbNamespace());
        assertEquals(4000, a.slowestQuery().durationMicros());

        assertNull(found.get(0).slowestQuery());
        assertNull(client.lastTags);
        assertNull(client.lastMinDuration);
    }

    @Test
    void search_onlyErrors_sendsTagFilterAndDropsCleanTraces() {
        FakeJaeger client = new FakeJaeger();
        List<TraceSummary> found = adapter(client)
                .searchTraces(new TraceSearch("payment-service", null, WINDOW, 2_500, true, 10));

        assertEquals(List.of("payment-service"), client.searchedServices);
        assertEquals(JaegerTraceAdapter.ERROR_TAGS, client.lastTags);
        assertEquals("2500us", client.lastMinDuration);
        assertEquals(1, found.size());
        assertTrue(found.get(0).errorSpanCount() > 0);
    }

    @Test
    void search_respectsLimit() {
        List<TraceSummary> found = adapter(new FakeJaeger()).searchTraces(new TraceSearch(null, null, WINDOW, 0, false, 1));
        assertEquals(1, found.size());
    }
}
