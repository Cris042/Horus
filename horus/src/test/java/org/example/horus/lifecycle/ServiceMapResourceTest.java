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

/** Testes da API de mapa de serviços/dependências (T-506, RF-H-015). */
@QuarkusTest
class ServiceMapResourceTest {

    private static final String TRACE_ID = "4bf92f3577b34da6a3ce929d0e0e4736";

    @InjectMock
    TraceQueryPort traces;

    @Test
    void buildsServiceGraphFromSpans() {
        when(traces.findTrace(TRACE_ID)).thenReturn(Optional.of(new TraceResult(TRACE_ID, 5, List.of(
                new SpanRef("s1", "POST /prontuarios", "load-balancer", 5_000, 100, null, "server"),
                new SpanRef("s2", "POST /consultas", "prontuario-service", 4_000, 110, "s1", "server"),
                new SpanRef("s3", "select consulta", "prontuario-service", 500, 120, "s2", "client"),
                new SpanRef("s4", "POST /pagamentos", "payment-service", 1_000, 130, "s2", "client"),
                new SpanRef("s5", "POST /pagamentos/estorno", "payment-service", 800, 140, "s2", "client")))));

        given().when().get("/horus/lifecycle/service-map/" + TRACE_ID)
                .then().statusCode(200)
                .body("traceId", equalTo(TRACE_ID))
                .body("spanCount", is(5))
                .body("nodeCount", is(3))
                .body("edgeCount", is(2))
                .body("nodes[0].serviceName", equalTo("load-balancer"))
                .body("nodes[0].spanCount", is(1))
                .body("nodes[0].entryPoint", is(true))
                .body("nodes[1].serviceName", equalTo("prontuario-service"))
                .body("nodes[1].spanCount", is(2))
                .body("nodes[1].entryPoint", is(false))
                .body("edges[0].from", equalTo("load-balancer"))
                .body("edges[0].to", equalTo("prontuario-service"))
                .body("edges[0].callCount", is(1))
                .body("edges[1].from", equalTo("prontuario-service"))
                .body("edges[1].to", equalTo("payment-service"))
                .body("edges[1].callCount", is(2));
    }

    @Test
    void missingTraceReturns404() {
        when(traces.findTrace(TRACE_ID)).thenReturn(Optional.empty());

        given().when().get("/horus/lifecycle/service-map/" + TRACE_ID)
                .then().statusCode(404);
    }

    @Test
    void invalidTraceIdReturns400AndDoesNotQueryBackend() {
        given().when().get("/horus/lifecycle/service-map/not-a-trace")
                .then().statusCode(400);

        verify(traces, never()).findTrace("not-a-trace");
    }
}
