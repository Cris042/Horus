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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Verifica o endpoint "explique este trace" (T-604) com o {@link ContextAssembler} mockado;
 * o {@code TraceExplainer} real usa o modo stub do {@code AnthropicLlmEngine} (sem chave).
 */
@QuarkusTest
class HorusExplainResourceTest {

    @InjectMock
    ContextAssembler assembler;

    @Test
    void explainTrace_wiresContextToExplainer() {
        when(assembler.assembleForTrace(eq("abc123"), anyInt()))
                .thenReturn(new PromptContext("# Trace abc123\n", 4, false, List.of("trace")));

        given().when().get("/horus/ai/explain/trace/abc123")
                .then().statusCode(200)
                .body("traceId", equalTo("abc123"))
                .body("live", is(false))                       // stub
                .body("modelId", equalTo("claude-sonnet-5")); // camada BALANCED
    }
}
