package org.example.horus.panel;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/** Verifica a API de agregação do painel (T-701) e que a página estática é servida. */
@QuarkusTest
class HorusPanelResourceTest {

    @Test
    void overview_reportsHealthAndEndpoints() {
        given().when().get("/horus/panel/overview")
                .then().statusCode(200)
                .body("service", equalTo("horus"))
                .body("ai.mode", equalTo("stub"))          // sem chave
                .body("ai.live", is(false))
                .body("ai.cache.enabled", is(true))
                .body("backends.jaeger", notNullValue())
                .body("endpoints.ask", containsString("/horus/ai/ask"));
    }

    @Test
    void staticPanelPage_isServed() {
        given().when().get("/horus-panel.html")
                .then().statusCode(200)
                .body(containsString("Horus"));
    }
}
