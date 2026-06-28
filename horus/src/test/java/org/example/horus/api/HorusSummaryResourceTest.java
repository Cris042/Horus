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
 * Verifica o endpoint de resumo sob demanda (T-603). O {@link ContextAssembler} é mockado
 * (sem backends); o {@code StateSummarizer} real usa o {@code StubLlmEngine} default (sem
 * chave) — confirma o wiring assembler→summarizer→resposta.
 */
@QuarkusTest
class HorusSummaryResourceTest {

    @InjectMock
    ContextAssembler assembler;

    @Test
    void summarizeTrace_wiresContextToSummarizer() {
        when(assembler.assembleForTrace(eq("abc123"), anyInt()))
                .thenReturn(new PromptContext("# Trace abc123\n", 4, false, List.of("trace")));

        given().when().get("/horus/ai/summary/trace/abc123")
                .then().statusCode(200)
                .body("live", is(false))                 // stub (sem ANTHROPIC_API_KEY)
                .body("modelId", equalTo("claude-haiku-4-5"))  // camada FAST
                .body("signals[0]", equalTo("trace"));
    }
}
