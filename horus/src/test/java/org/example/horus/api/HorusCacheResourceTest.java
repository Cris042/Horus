package org.example.horus.api;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;

/**
 * Verifica que o decorator {@link org.example.horus.ai.CachingLlmEngine} realmente cacheia
 * (T-608): dois pedidos idênticos a {@code /horus/ai/complete} produzem ao menos 1 hit.
 */
@QuarkusTest
class HorusCacheResourceTest {

    @Test
    void identicalCompletions_produceCacheHit() {
        String body = "{\"prompt\":\"cache me\",\"tier\":\"FAST\"}";

        // duas chamadas idênticas — a 2ª deve vir do cache
        given().contentType("application/json").body(body)
                .when().post("/horus/ai/complete").then().statusCode(200);
        given().contentType("application/json").body(body)
                .when().post("/horus/ai/complete").then().statusCode(200);

        given().when().get("/horus/ai/cache/stats")
                .then().statusCode(200)
                .body("enabled", is(true))
                .body("hits", greaterThanOrEqualTo(1));
    }
}
