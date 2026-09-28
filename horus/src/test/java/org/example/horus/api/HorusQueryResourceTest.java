package org.example.horus.api;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import org.example.horus.query.LogQueryPort;
import org.example.horus.query.MetricQueryPort;
import org.example.horus.query.QueryModel.LogLine;
import org.example.horus.query.QueryModel.MetricPoint;
import org.example.horus.query.QueryModel.MetricSample;
import org.example.horus.query.QueryModel.MetricSeries;
import org.example.horus.query.QueryModel.SpanRef;
import org.example.horus.query.QueryModel.TraceResult;
import org.example.horus.query.TraceQueryPort;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Verifica a camada REST de consulta (T-501) com as portas mockadas — sem depender
 * de Jaeger/Loki/Prometheus reais. Garante o wiring resource→porta e o mapeamento JSON.
 */
@QuarkusTest
class HorusQueryResourceTest {

    @InjectMock
    TraceQueryPort traces;
    @InjectMock
    LogQueryPort logs;
    @InjectMock
    MetricQueryPort metrics;

    @Test
    void trace_found_returnsSpans() {
        when(traces.findTrace("abc123")).thenReturn(Optional.of(
                new TraceResult("abc123", 1,
                        List.of(new SpanRef("s1", "GET /prontuarios/{id}", "prontuario-service", 4200)))));

        given().when().get("/horus/query/traces/abc123")
                .then().statusCode(200)
                .body("traceId", equalTo("abc123"))
                .body("spanCount", is(1))
                .body("spans[0].serviceName", equalTo("prontuario-service"))
                .body("spans[0].durationMicros", is(4200));
    }

    @Test
    void trace_missing_returns404() {
        when(traces.findTrace("nope")).thenReturn(Optional.empty());

        given().when().get("/horus/query/traces/nope")
                .then().statusCode(404);
    }

    @Test
    void logs_byTrace_returnsLines() {
        when(logs.findByTraceId(eq("abc123"), anyInt())).thenReturn(
                List.of(new LogLine("1700000000000000000", "consulta registrada",
                        Map.of("service_name", "prontuario-service"))));

        given().when().get("/horus/query/logs?traceId=abc123")
                .then().statusCode(200)
                .body("[0].line", equalTo("consulta registrada"))
                .body("[0].labels.service_name", equalTo("prontuario-service"));
    }

    @Test
    void metrics_instantQuery_returnsSamples() {
        when(metrics.instantQuery("up")).thenReturn(
                List.of(new MetricSample(Map.of("job", "prometheus"), 1.0, 1700000000.0)));

        given().when().get("/horus/query/metrics?query=up")
                .then().statusCode(200)
                .body("[0].value", is(1.0f))
                .body("[0].labels.job", equalTo("prometheus"));
    }

    @Test
    void logsRange_queriesWindow() {
        when(logs.findInWindow(eq("{service_name=\"payment-service\"}"), any(), eq(50))).thenReturn(
                List.of(new LogLine("1700000000000000000", "saldo insuficiente", Map.of())));

        given().queryParam("logql", "{service_name=\"payment-service\"}")
                .queryParam("lookback", "30m").queryParam("limit", 50)
                .when().get("/horus/query/logs/range")
                .then().statusCode(200)
                .body("[0].line", equalTo("saldo insuficiente"));
    }

    @Test
    void logsRange_withoutLogql_returns400() {
        given().when().get("/horus/query/logs/range?lookback=1h").then().statusCode(400);
    }

    @Test
    void metricsRange_defaultStep_isSixtyPointsPerWindow() {
        when(metrics.rangeQuery(eq("up"), any(), eq(60L))).thenReturn(
                List.of(new MetricSeries(Map.of("job", "payment"), List.of(new MetricPoint(1700000000.0, 1.0)))));

        given().when().get("/horus/query/metrics/range?query=up&lookback=1h")
                .then().statusCode(200)
                .body("[0].labels.job", equalTo("payment"))
                .body("[0].points[0].value", is(1.0f));
    }

    @Test
    void metricsRange_invalidWindow_returns400() {
        when(metrics.rangeQuery(any(), any(), anyLong())).thenReturn(List.of());
        given().when().get("/horus/query/metrics/range?query=up&lookback=abc").then().statusCode(400);
    }
}
