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

/** Testes da API de visualização de SAGA (T-507, RF-H-016). */
@QuarkusTest
class SagaVisualizationResourceTest {

    private static final String TRACE_ID = "4bf92f3577b34da6a3ce929d0e0e4736";

    @InjectMock
    TraceQueryPort traces;

    @Test
    void buildsSagaTimelineWithCompensation() {
        when(traces.findTrace(TRACE_ID)).thenReturn(Optional.of(new TraceResult(TRACE_ID, 4, List.of(
                new SpanRef("s1", "saga.pay-then-invoice.reserve-payment", "saga-orchestrator", 1_000, 100, null, "internal"),
                new SpanRef("s2", "saga.pay-then-invoice.issue-invoice", "saga-orchestrator", 800, 200, "s1", "internal"),
                new SpanRef("s3", "saga.pay-then-invoice.reserve-payment.compensate", "saga-orchestrator", 500, 300, "s1", "internal"),
                new SpanRef("s4", "POST /pagamentos", "payment-service", 700, 150, "s1", "client")))));

        given().when().get("/horus/lifecycle/saga/" + TRACE_ID)
                .then().statusCode(200)
                .body("traceId", equalTo(TRACE_ID))
                .body("flow", equalTo("pay-then-invoice"))
                .body("outcome", equalTo("compensated"))
                .body("stepCount", is(2))
                .body("compensationCount", is(1))
                .body("steps.size()", is(3))
                .body("steps[0].step", equalTo("reserve-payment"))
                .body("steps[0].compensation", is(false))
                .body("steps[0].offsetMicros", is(0))
                .body("steps[1].step", equalTo("issue-invoice"))
                .body("steps[1].offsetMicros", is(100))
                .body("steps[2].step", equalTo("reserve-payment"))
                .body("steps[2].compensation", is(true));
    }

    private static SpanRef step(String id, String op, long start, String parent, boolean error) {
        return new SpanRef(id, op, "saga-orchestrator", 500, start, parent, "internal", null, null, null, null, error);
    }

    @Test
    void failedStep_isIdentifiedFromSpanStatus() {
        when(traces.findTrace(TRACE_ID)).thenReturn(Optional.of(new TraceResult(TRACE_ID, 3, List.of(
                step("s1", "saga.pay-then-invoice.reserve-payment", 100, null, false),
                step("s2", "saga.pay-then-invoice.issue-invoice", 200, "s1", true),
                step("s3", "saga.pay-then-invoice.reserve-payment.compensate", 300, "s1", false)))));

        given().when().get("/horus/lifecycle/saga/" + TRACE_ID)
                .then().statusCode(200)
                .body("outcome", equalTo("compensated"))
                .body("failedStep", equalTo("issue-invoice"))
                .body("recovered", is(false))
                .body("steps[1].failed", is(true))
                .body("steps[0].failed", is(false));
    }

    @Test
    void failedWithoutCompensation_isFailed_notCompleted() {
        // Antes da T-1010 isto aparecia como "completed": a falha só era inferida pela compensação.
        when(traces.findTrace(TRACE_ID)).thenReturn(Optional.of(new TraceResult(TRACE_ID, 2, List.of(
                step("s1", "saga.pay-then-invoice.reserve-payment", 100, null, false),
                step("s2", "saga.pay-then-invoice.issue-invoice", 200, "s1", true)))));

        given().when().get("/horus/lifecycle/saga/" + TRACE_ID)
                .then().statusCode(200)
                .body("outcome", equalTo("failed"))
                .body("failedStep", equalTo("issue-invoice"));
    }

    @Test
    void recoveryTrace_onlyCompensation_isMarkedRecovered() {
        when(traces.findTrace(TRACE_ID)).thenReturn(Optional.of(new TraceResult(TRACE_ID, 1, List.of(
                step("s9", "saga.pay-then-invoice.reserve-payment.compensate", 100, null, false)))));

        given().when().get("/horus/lifecycle/saga/" + TRACE_ID)
                .then().statusCode(200)
                .body("outcome", equalTo("compensated"))
                .body("recovered", is(true));
    }

    @Test
    void completedSagaWhenNoCompensation() {
        when(traces.findTrace(TRACE_ID)).thenReturn(Optional.of(new TraceResult(TRACE_ID, 2, List.of(
                new SpanRef("s1", "saga.pay-then-invoice.reserve-payment", "saga-orchestrator", 1_000, 100, null, "internal"),
                new SpanRef("s2", "saga.pay-then-invoice.issue-invoice", "saga-orchestrator", 800, 200, "s1", "internal")))));

        given().when().get("/horus/lifecycle/saga/" + TRACE_ID)
                .then().statusCode(200)
                .body("outcome", equalTo("completed"))
                .body("stepCount", is(2))
                .body("compensationCount", is(0));
    }

    @Test
    void noSagaSpansReturnsOutcomeNone() {
        when(traces.findTrace(TRACE_ID)).thenReturn(Optional.of(new TraceResult(TRACE_ID, 1, List.of(
                new SpanRef("s1", "POST /consultas", "prontuario-service", 1_000, 100, null, "server")))));

        given().when().get("/horus/lifecycle/saga/" + TRACE_ID)
                .then().statusCode(200)
                .body("outcome", equalTo("none"))
                .body("stepCount", is(0))
                .body("steps.size()", is(0));
    }

    @Test
    void missingTraceReturns404() {
        when(traces.findTrace(TRACE_ID)).thenReturn(Optional.empty());

        given().when().get("/horus/lifecycle/saga/" + TRACE_ID)
                .then().statusCode(404);
    }

    @Test
    void invalidTraceIdReturns400AndDoesNotQueryBackend() {
        given().when().get("/horus/lifecycle/saga/not-a-trace")
                .then().statusCode(400);

        verify(traces, never()).findTrace("not-a-trace");
    }
}
