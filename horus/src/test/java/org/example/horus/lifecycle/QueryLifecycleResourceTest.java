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

/** Testes da API de lifecycle de query (T-504, RF-H-002). */
@QuarkusTest
class QueryLifecycleResourceTest {

    private static final String TRACE_ID = "4bf92f3577b34da6a3ce929d0e0e4736";

    @InjectMock
    TraceQueryPort traces;

    @Test
    void returnsOnlyQuerySpansOrderedByStartTime() {
        when(traces.findTrace(TRACE_ID)).thenReturn(Optional.of(new TraceResult(TRACE_ID, 5, List.of(
                new SpanRef("s4", "relatorios publish", "invoice-service", 300, 1_003_000, "s2", "producer"),
                new SpanRef("s3", "SELECT consultas", "prontuario-service", 700, 1_000_900, "s2", "client",
                        "SELECT", "prontuario_db", "postgresql", "SELECT * FROM consultas WHERE id = ?"),
                new SpanRef("s1", "POST /prontuarios/{id}", "load-balancer", 4_000, 1_000_000, null, "server"),
                new SpanRef("s5", "UPDATE pagamentos", "payment-service", 500, 1_001_700, "s2", "client",
                        "UPDATE", "payment_db", "postgresql", null),
                new SpanRef("s2", "POST /consultas", "prontuario-service", 2_000, 1_000_500, "s1", "server")))));

        given().when().get("/horus/lifecycle/queries/" + TRACE_ID)
                .then().statusCode(200)
                .body("traceId", equalTo(TRACE_ID))
                .body("queryCount", is(2))
                .body("queries[0].spanId", equalTo("s3"))
                .body("queries[0].offsetMicros", is(900))
                .body("queries[0].databaseName", equalTo("prontuario_db"))
                .body("queries[0].databaseSystem", equalTo("postgresql"))
                .body("queries[0].operationName", equalTo("SELECT"))
                .body("queries[0].statement", equalTo("SELECT * FROM consultas WHERE id = ?"))
                .body("queries[1].spanId", equalTo("s5"))
                .body("queries[1].offsetMicros", is(1_700))
                .body("queries[1].databaseName", equalTo("payment_db"))
                .body("queries[1].operationName", equalTo("UPDATE"))
                .body("queries[1].statement", equalTo("UPDATE pagamentos"));
    }

    @Test
    void missingTraceReturns404() {
        when(traces.findTrace(TRACE_ID)).thenReturn(Optional.empty());

        given().when().get("/horus/lifecycle/queries/" + TRACE_ID)
                .then().statusCode(404);
    }

    @Test
    void invalidTraceIdReturns400AndDoesNotQueryBackend() {
        given().when().get("/horus/lifecycle/queries/not-a-trace")
                .then().statusCode(400);

        verify(traces, never()).findTrace("not-a-trace");
    }

    @Test
    void traceWithoutQuerySpansReturnsEmptyList() {
        when(traces.findTrace(TRACE_ID)).thenReturn(Optional.of(new TraceResult(TRACE_ID, 2, List.of(
                new SpanRef("s1", "POST /prontuarios/{id}", "load-balancer", 4_000, 1_000_000, null, "server"),
                new SpanRef("s2", "POST /consultas", "prontuario-service", 2_000, 1_000_500, "s1", "server")))));

        given().when().get("/horus/lifecycle/queries/" + TRACE_ID)
                .then().statusCode(200)
                .body("queryCount", is(0))
                .body("queries.size()", is(0));
    }
}
