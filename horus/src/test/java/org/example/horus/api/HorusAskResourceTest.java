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
 * Verifica o endpoint "pergunte ao Horus" (T-607). Assembler mockado; {@code NlQueryAgent}
 * real usa o {@code StubLlmEngine} default (sem chave).
 */
@QuarkusTest
class HorusAskResourceTest {

    @InjectMock
    ContextAssembler assembler;

    @Test
    void ask_withTraceScope_answersGrounded() {
        when(assembler.assembleForIncident(eq("abc123"), any(), anyInt()))
                .thenReturn(new PromptContext("# Trace abc123\n", 4, false, List.of("trace")));

        given().contentType("application/json")
                .body("{\"question\":\"Qual o gargalo?\",\"traceId\":\"abc123\"}")
                .when().post("/horus/ai/ask")
                .then().statusCode(200)
                .body("question", equalTo("Qual o gargalo?"))
                .body("live", is(false))                     // stub
                .body("modelId", equalTo("claude-sonnet-4-6")); // camada BALANCED
    }

    @Test
    void ask_withoutQuestion_returns400() {
        given().contentType("application/json").body("{}")
                .when().post("/horus/ai/ask")
                .then().statusCode(400);
    }
}
