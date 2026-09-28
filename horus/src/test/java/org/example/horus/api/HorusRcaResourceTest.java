package org.example.horus.api;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import org.example.horus.ai.context.ContextAssembler;
import org.example.horus.ai.context.PromptContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Verifica o endpoint de RCA (T-605) com o {@link ContextAssembler} mockado; o
 * {@code RootCauseAnalyst} real usa o modo stub do {@code AnthropicLlmEngine} (sem chave).
 */
@QuarkusTest
class HorusRcaResourceTest {

    @InjectMock
    ContextAssembler assembler;

    @Test
    void analyzeTrace_wiresIncidentContextToAnalyst() {
        when(assembler.assembleForIncident(eq("abc123"), any(), anyInt()))
                .thenReturn(new PromptContext("# Trace abc123\n# Métricas\n", 8, false,
                        List.of("trace", "metrics")));

        given().when().get("/horus/ai/rca/trace/abc123?promql=up")
                .then().statusCode(200)
                .body("traceId", equalTo("abc123"))
                .body("live", is(false))                     // stub
                .body("modelId", equalTo("claude-opus-5")); // camada DEEP
    }
}
