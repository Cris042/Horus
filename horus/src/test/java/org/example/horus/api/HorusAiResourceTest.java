package org.example.horus.api;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * Verifica a camada de IA (T-601) no modo padrão (stub, sem ANTHROPIC_API_KEY) —
 * garante que o Horus sobe e responde sem provedor real, com a porta {@code LlmEngine}.
 */
@QuarkusTest
class HorusAiResourceTest {

    @Test
    void health_reportsStubMode_whenAiDisabled() {
        given().when().get("/horus/ai/health")
                .then().statusCode(200)
                .body("mode", equalTo("stub"))
                .body("live", is(false));
    }

    @Test
    void complete_stub_returnsPlaceholderForTier() {
        given().contentType("application/json")
                .body("{\"prompt\":\"resuma o estado\",\"tier\":\"DEEP\"}")
                .when().post("/horus/ai/complete")
                .then().statusCode(200)
                .body("live", is(false))
                .body("modelId", equalTo("claude-opus-5"));
    }
}
