package org.example.horus.ai.anomaly;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import org.example.horus.ai.LlmEngine;
import org.example.horus.ai.LlmEngine.LlmRequest;
import org.example.horus.ai.LlmEngine.LlmResponse;
import org.example.horus.query.LogQueryPort;
import org.example.horus.query.QueryModel.LogLine;
import org.example.horus.query.QueryModel.SpanRef;
import org.example.horus.query.QueryModel.TraceResult;
import org.example.horus.query.TraceQueryPort;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testes do Error Clusterer (T-606, RF-H-009). {@code TraceQueryPort}/{@code LogQueryPort}
 * mockados; o rótulo usa o {@code StubLlmEngine} default (sem chave → {@code live=false}).
 */
@QuarkusTest
class ErrorClustererTest {

    private static final String TRACE_ID = "4bf92f3577b34da6a3ce929d0e0e4736";

    @InjectMock
    TraceQueryPort traces;
    @InjectMock
    LogQueryPort logs;

    @Test
    void clustersErrorsByFingerprintAcrossServices() {
        when(traces.findTrace(TRACE_ID)).thenReturn(Optional.of(new TraceResult(TRACE_ID, 1, List.of(
                new SpanRef("s1", "POST /pagamentos", "payment-service", 1_000)))));
        when(logs.findByTraceId(eq(TRACE_ID), anyInt())).thenReturn(List.of(
                new LogLine("400", "Timeout while calling payment 1", Map.of("level", "error", "service_name", "invoice-service")),
                new LogLine("300", "Timeout while calling payment 2", Map.of("level", "error", "service_name", "payment-service")),
                new LogLine("200", "database unavailable", Map.of("level", "error", "service_name", "payment-service")),
                new LogLine("100", "consulta registrada", Map.of("level", "info", "service_name", "prontuario-service"))));

        given().when().get("/horus/ai/errors/clusters/" + TRACE_ID)
                .then().statusCode(200)
                .body("traceId", equalTo(TRACE_ID))
                .body("errorCount", is(3))
                .body("clusterCount", is(2))
                .body("crossServiceClusterCount", is(1))
                .body("live", is(false))
                .body("modelId", equalTo("claude-haiku-4-5"))
                .body("clusters[0].totalCount", is(2))
                .body("clusters[0].crossService", is(true))
                .body("clusters[0].serviceCount", is(2))
                .body("clusters[0].severity", equalTo("error"))
                .body("clusters[0].affectedServices.size()", is(2))
                .body("clusters[1].totalCount", is(1))
                .body("clusters[1].crossService", is(false));
    }

    @Test
    void missingTraceReturns404() {
        when(traces.findTrace(TRACE_ID)).thenReturn(Optional.empty());

        given().when().get("/horus/ai/errors/clusters/" + TRACE_ID)
                .then().statusCode(404);
    }

    @Test
    void invalidTraceIdReturns400AndDoesNotQueryBackends() {
        given().when().get("/horus/ai/errors/clusters/not-a-trace")
                .then().statusCode(400);

        verify(traces, never()).findTrace("not-a-trace");
        verify(logs, never()).findByTraceId(eq("not-a-trace"), anyInt());
    }

    /**
     * T-904 (auditoria de privacidade): o prompt do rótulo é montado direto de
     * {@code log.line()} (telemetria) — precisa passar pelo mesmo {@code PromptSanitizer}
     * do {@code ContextAssembler} (RNF-H-006). Instancia o agente direto (sem CDI) com um
     * {@link LlmEngine} mockado para capturar o prompt de fato enviado.
     */
    @Test
    void promptDoLabelNaoCarregaPiiCrua() {
        TraceQueryPort tracesMock = mock(TraceQueryPort.class);
        LogQueryPort logsMock = mock(LogQueryPort.class);
        LlmEngine engineMock = mock(LlmEngine.class);

        when(tracesMock.findTrace(TRACE_ID)).thenReturn(Optional.of(
                new TraceResult(TRACE_ID, 1, List.of(
                        new SpanRef("s1", "POST /pagamentos", "payment-service", 1_000)))));
        when(logsMock.findByTraceId(eq(TRACE_ID), anyInt())).thenReturn(List.of(
                new LogLine("100", "falha ao notificar paciente.real@example.com cpf 123.456.789-09",
                        Map.of("level", "error", "service_name", "payment-service"))));
        when(engineMock.complete(any(LlmRequest.class)))
                .thenReturn(new LlmResponse("resumo", "stub", false));

        ErrorClusterer clusterer = new ErrorClusterer(tracesMock, logsMock, engineMock);
        clusterer.clusterByTrace(TRACE_ID, 10);

        ArgumentCaptor<LlmRequest> captor = ArgumentCaptor.forClass(LlmRequest.class);
        verify(engineMock).complete(captor.capture());
        String prompt = captor.getValue().prompt();

        assertFalse(prompt.contains("paciente.real@example.com"), "e-mail cru vazou pro prompt");
        assertFalse(prompt.contains("123.456.789-09"), "CPF cru vazou pro prompt");
        assertTrue(prompt.contains("***@***") || prompt.contains("***"), "esperava marcador de redação no prompt");
    }
}
