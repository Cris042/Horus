package org.example.horus.api;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasLength;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;

/**
 * A trilha de auditoria está ligada à porta real do CDI (decorator ativo) e nunca expõe o prompt.
 */
@QuarkusTest
class HorusAuditResourceTest {

    @Test
    void everyLlmCall_isAuditedWithoutThePromptText() {
        given().contentType("application/json")
                .body("{\"prompt\":\"erro para joao@example.com\",\"tier\":\"FAST\"}")
                .when().post("/horus/ai/complete")
                .then().statusCode(200);

        given().when().get("/horus/ai/audit?limit=1")
                .then().statusCode(200)
                .body("stats.totalRequests", greaterThanOrEqualTo(1))
                .body("records[0].purpose", equalTo("complete"))
                .body("records[0].tier", equalTo("FAST"))
                .body("records[0].redactions", equalTo(1))
                .body("records[0].promptSha256", hasLength(64))
                .body(not(containsString("joao@example.com")));
    }
}
