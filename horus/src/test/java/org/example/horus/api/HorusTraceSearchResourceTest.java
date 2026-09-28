package org.example.horus.api;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import org.example.horus.query.QueryModel.SlowQuery;
import org.example.horus.query.QueryModel.TraceSearch;
import org.example.horus.query.QueryModel.TraceSummary;
import org.example.horus.query.TraceQueryPort;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Duration;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** API de busca de traces por janela temporal (T-1001), com a porta mockada. */
@QuarkusTest
class HorusTraceSearchResourceTest {

    @InjectMock
    TraceQueryPort traces;

    private static TraceSummary summary() {
        return new TraceSummary("abc123", "prontuario-service", "GET /prontuarios/{id}", 1_000, 9_000, 3,
                List.of("prontuario-service", "payment-service"), 1,
                new SlowQuery("prontuario-service", "prontuario_db", "SELECT", "select 1", 4_000));
    }

    @Test
    void search_passesCriteriaAndReturnsSummaries() {
        when(traces.searchTraces(any())).thenReturn(List.of(summary()));

        given().when().get("/horus/traces?service=payment-service&lookback=15m&minDurationMs=5&error=true&limit=10")
                .then().statusCode(200)
                .body("count", is(1))
                .body("traces[0].traceId", equalTo("abc123"))
                .body("traces[0].errorSpanCount", is(1))
                .body("traces[0].slowestQuery.dbNamespace", equalTo("prontuario_db"));

        ArgumentCaptor<TraceSearch> captor = ArgumentCaptor.forClass(TraceSearch.class);
        verify(traces).searchTraces(captor.capture());
        TraceSearch search = captor.getValue();
        assertEquals("payment-service", search.service());
        assertNull(search.operation());
        assertEquals(Duration.ofMinutes(15), search.window().span());
        assertEquals(5_000, search.minDurationMicros());
        assertTrue(search.onlyErrors());
        assertEquals(10, search.limit());
    }

    @Test
    void search_defaults_lastHourAllServices() {
        when(traces.searchTraces(any())).thenReturn(List.of());

        given().when().get("/horus/traces").then().statusCode(200).body("count", is(0));

        ArgumentCaptor<TraceSearch> captor = ArgumentCaptor.forClass(TraceSearch.class);
        verify(traces).searchTraces(captor.capture());
        assertEquals(Duration.ofHours(1), captor.getValue().window().span());
        assertNull(captor.getValue().service());
        assertEquals(20, captor.getValue().limit());
    }

    @Test
    void search_invalidInputs_return400() {
        given().when().get("/horus/traces?lookback=1w").then().statusCode(400);
        given().when().get("/horus/traces?limit=0").then().statusCode(400);
        given().when().get("/horus/traces?limit=1000").then().statusCode(400);
        given().when().get("/horus/traces?minDurationMs=-1").then().statusCode(400);
    }

    @Test
    void services_listed() {
        when(traces.listServices()).thenReturn(List.of("payment-service", "prontuario-service"));

        given().when().get("/horus/traces/services")
                .then().statusCode(200)
                .body("[1]", equalTo("prontuario-service"));
    }
}
