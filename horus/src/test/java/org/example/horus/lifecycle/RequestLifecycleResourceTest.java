package org.example.horus.lifecycle;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import org.example.horus.query.QueryModel.SpanRef;
import org.example.horus.query.QueryModel.TraceResult;
import org.example.horus.query.TraceQueryPort;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Testes da API de lifecycle de request (T-503, RF-H-001/RF-H-011). */
@QuarkusTest
class RequestLifecycleResourceTest {

    private static final String TRACE_ID = "4bf92f3577b34da6a3ce929d0e0e4736";

    @InjectMock
    TraceQueryPort traces;

    @Test
    void lifecycleReturnsWaterfallOrderedByStartTime() {
        when(traces.findTrace(TRACE_ID)).thenReturn(Optional.of(new TraceResult(TRACE_ID, 5, List.of(
                new SpanRef("s3", "SELECT consulta", "prontuario-service", 700, 1_000_900, "s2", "client"),
                new SpanRef("s1", "POST /prontuarios/{id}", "load-balancer", 4_000, 1_000_000, null, "server"),
                new SpanRef("s5", "processar_relatorio", "report-worker", 600, 1_003_100, "s4", "consumer"),
                new SpanRef("s2", "POST /consultas", "prontuario-service", 2_000, 1_000_500, "s1", "server"),
                new SpanRef("s4", "relatorios publish", "invoice-service", 300, 1_002_700, "s2", "producer")))));

        given().when().get("/horus/lifecycle/requests/" + TRACE_ID)
                .then().statusCode(200)
                .body("traceId", equalTo(TRACE_ID))
                .body("spanCount", is(5))
                .body("durationMicros", is(4_000))
                .body("messagingInvolved", is(true))
                .body("workerInvolved", is(true))
                .body("services[0]", equalTo("load-balancer"))
                .body("services[1]", equalTo("prontuario-service"))
                .body("services[2]", equalTo("invoice-service"))
                .body("services[3]", equalTo("report-worker"))
                .body("spans[0].spanId", equalTo("s1"))
                .body("spans[0].offsetMicros", is(0))
                .body("spans[0].depth", is(0))
                .body("spans[0].category", equalTo("http"))
                .body("spans[2].spanId", equalTo("s3"))
                .body("spans[2].offsetMicros", is(900))
                .body("spans[2].depth", is(2))
                .body("spans[2].category", equalTo("database"))
                .body("spans[4].spanId", equalTo("s5"))
                .body("spans[4].offsetMicros", is(3_100))
                .body("spans[4].depth", is(3))
                .body("spans[4].category", equalTo("worker"));
    }

    @Test
    void missingTraceReturns404() {
        when(traces.findTrace(TRACE_ID)).thenReturn(Optional.empty());

        given().when().get("/horus/lifecycle/requests/" + TRACE_ID)
                .then().statusCode(404);
    }

    @Test
    void invalidTraceIdReturns400AndDoesNotQueryBackend() {
        given().when().get("/horus/lifecycle/requests/not-a-trace")
                .then().statusCode(400);

        verify(traces, never()).findTrace("not-a-trace");
    }
}
