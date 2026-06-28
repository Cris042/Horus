package org.example.horus.correlation;

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
import static org.mockito.Mockito.when;

/**
 * Verifica o modelo de correlação (T-502) com as portas mockadas — sem backends reais.
 * Cobre a costura request ↔ logs ↔ mensageria ↔ worker e o caminho 404.
 */
@QuarkusTest
class CorrelationResourceTest {

    @InjectMock
    TraceQueryPort traces;
    @InjectMock
    LogQueryPort logs;

    @Test
    void correlatesAcrossServicesMessagingAndWorker() {
        when(traces.findTrace("t1")).thenReturn(Optional.of(new TraceResult("t1", 4, List.of(
                new SpanRef("s1", "POST /notas", "invoice-service", 5000),
                new SpanRef("s2", "relatorios publish", "invoice-service", 300),
                new SpanRef("s3", "processar_relatorio", "report-worker", 2000),
                new SpanRef("s4", "POST /pagamentos", "payment-service", 1500)))));
        when(logs.findByTraceId(eq("t1"), anyInt())).thenReturn(List.of(
                new LogLine("1", "emitida NF", Map.of("level", "info")),
                new LogLine("2", "falha simulada", Map.of("level", "error"))));

        given().when().get("/horus/correlation/trace/t1")
                .then().statusCode(200)
                .body("traceId", equalTo("t1"))
                .body("spanCount", is(4))
                .body("workerInvolved", is(true))
                .body("messagingInvolved", is(true))
                .body("errorLogCount", is(1))
                // invoice-service é o serviço de maior duração somada (5000+300) → primeiro.
                .body("services[0].serviceName", equalTo("invoice-service"))
                .body("services[0].spanCount", is(2))
                .body("totalDurationMicros", is(8800));
    }

    @Test
    void detectsErrorFromLogLineWhenNoLevelLabel() {
        when(traces.findTrace("t2")).thenReturn(Optional.of(new TraceResult("t2", 1, List.of(
                new SpanRef("s1", "GET /prontuarios", "prontuario-service", 1000)))));
        when(logs.findByTraceId(eq("t2"), anyInt())).thenReturn(List.of(
                new LogLine("1", "ERROR algo quebrou", Map.of())));

        given().when().get("/horus/correlation/trace/t2")
                .then().statusCode(200)
                .body("workerInvolved", is(false))
                .body("messagingInvolved", is(false))
                .body("errorLogCount", is(1));
    }

    @Test
    void missingTraceReturns404() {
        when(traces.findTrace("nope")).thenReturn(Optional.empty());
        given().when().get("/horus/correlation/trace/nope").then().statusCode(404);
    }
}
