package org.example.invoice.api;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

/**
 * Teste de fumaça do scaffold: a app sobe (Flyway migra o {@code invoice_db} efêmero
 * via Dev Services) e responde no endpoint de identificação e no liveness do SmallRye Health.
 */
@QuarkusTest
class InvoiceInfoResourceTest {

    @Test
    void infoEndpointReturnsService() {
        given()
                .when().get("/invoice/info")
                .then()
                .statusCode(200)
                .body("service", is("invoice-service"))
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
