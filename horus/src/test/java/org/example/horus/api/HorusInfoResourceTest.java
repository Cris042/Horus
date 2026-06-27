package org.example.horus.api;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

/**
 * Teste de fumaça do bootstrap: confirma que a aplicação sobe e responde no endpoint
 * de identificação e no liveness do SmallRye Health.
 */
@QuarkusTest
class HorusInfoResourceTest {

    @Test
    void infoEndpointReturnsHorus() {
        given()
                .when().get("/horus/info")
                .then()
                .statusCode(200)
                .body("name", is("Horus"))
                .body("status", is("ok"));
    }

    @Test
    void livenessIsUp() {
        given()
                .when().get("/q/health/live")
                .then()
                .statusCode(200);
    }
}
