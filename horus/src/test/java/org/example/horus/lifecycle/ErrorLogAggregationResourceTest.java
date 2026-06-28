package org.example.horus.lifecycle;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import org.example.horus.query.LogQueryPort;
import org.example.horus.query.QueryModel.LogLine;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Testes da API de agregação de logs de erro (T-505, RF-H-003). */
@QuarkusTest
class ErrorLogAggregationResourceTest {

    private static final String TRACE_ID = "4bf92f3577b34da6a3ce929d0e0e4736";

    @InjectMock
    TraceQueryPort traces;
    @InjectMock
    LogQueryPort logs;

    @Test
    void aggregatesErrorsByServiceAndFingerprint() {
        when(traces.findTrace(TRACE_ID)).thenReturn(Optional.of(new TraceResult(TRACE_ID, 2, List.of(
                new SpanRef("s1", "POST /prontuarios/{id}", "load-balancer", 4_000),
                new SpanRef("s2", "POST /consultas", "prontuario-service", 2_000)))));
        when(logs.findByTraceId(eq(TRACE_ID), anyInt())).thenReturn(List.of(
                new LogLine("300", "Timeout while calling payment 123", Map.of("level", "error", "service_name", "invoice-service")),
                new LogLine("200", "Timeout while calling payment 456", Map.of("level", "error", "service_name", "invoice-service")),
                new LogLine("150", "ERROR database unavailable\njava.lang.RuntimeException: boom", Map.of("service_name", "payment-service")),
                new LogLine("100", "consulta registrada", Map.of("level", "info", "service_name", "prontuario-service"))));

        given().when().get("/horus/lifecycle/errors/" + TRACE_ID)
                .then().statusCode(200)
                .body("traceId", equalTo(TRACE_ID))
                .body("errorCount", is(3))
                .body("groupCount", is(2))
                .body("groups[0].serviceName", equalTo("invoice-service"))
                .body("groups[0].count", is(2))
                .body("groups[0].level", equalTo("error"))
                .body("groups[0].sample", equalTo("Timeout while calling payment 123"))
                .body("groups[0].hasStacktrace", is(false))
                .body("groups[1].serviceName", equalTo("payment-service"))
                .body("groups[1].count", is(1))
                .body("groups[1].hasStacktrace", is(true));
    }

    @Test
    void missingTraceReturns404() {
        when(traces.findTrace(TRACE_ID)).thenReturn(Optional.empty());

        given().when().get("/horus/lifecycle/errors/" + TRACE_ID)
                .then().statusCode(404);
    }

    @Test
    void invalidTraceIdReturns400AndDoesNotQueryBackends() {
        given().when().get("/horus/lifecycle/errors/not-a-trace")
                .then().statusCode(400);

        verify(traces, never()).findTrace("not-a-trace");
        verify(logs, never()).findByTraceId(eq("not-a-trace"), anyInt());
    }
}
