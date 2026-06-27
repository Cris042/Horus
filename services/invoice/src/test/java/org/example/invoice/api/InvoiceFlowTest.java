package org.example.invoice.api;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

/**
 * Fluxo de domínio do Invoice (RF-016..020): emitir NF simulada (sucesso e falha),
 * consultar/listar e reprocessar. Usa o banco do Dev Services (PostgreSQL) — roda no CI.
 */
@QuarkusTest
class InvoiceFlowTest {

    @Test
    void emissaoComSucesso() {
        // RF-016/017/018: receber e emitir -> EMITIDA com número
        Integer id = given()
                .contentType("application/json")
                .body("{\"valor\":250.00,\"referencia\":\"ped-1\"}")
                .when().post("/notas")
                .then()
                .statusCode(201)
                .body("status", is("EMITIDA"))
                .body("numero", notNullValue())
                .body("tentativas", is(1))
                .extract().path("id");

        // RF-019: consultar
        given()
                .when().get("/notas/{id}", id)
                .then()
                .statusCode(200)
                .body("status", is("EMITIDA"));
    }

    @Test
    void falhaEReprocessamento() {
        // RF-018: emissão com falha simulada -> FALHA sem número
        Integer id = given()
                .contentType("application/json")
                .body("{\"valor\":99.90,\"simularFalha\":true}")
                .when().post("/notas")
                .then()
                .statusCode(201)
                .body("status", is("FALHA"))
                .body("numero", nullValue())
                .body("motivoFalha", notNullValue())
                .extract().path("id");

        // RF-020: reprocessar -> EMITIDA, segunda tentativa
        given()
                .when().post("/notas/{id}/reprocessar", id)
                .then()
                .statusCode(200)
                .body("status", is("EMITIDA"))
                .body("numero", notNullValue())
                .body("tentativas", is(2));

        // RF-020: reprocessar uma já emitida -> 409
        given()
                .when().post("/notas/{id}/reprocessar", id)
                .then()
                .statusCode(409);
    }

    @Test
    void listarFiltrandoPorStatus() {
        given()
                .contentType("application/json")
                .body("{\"valor\":10.00,\"simularFalha\":true}")
                .when().post("/notas")
                .then().statusCode(201);

        // RF-019: listar só as FALHA retorna ao menos uma
        given()
                .when().get("/notas?status=FALHA")
                .then()
                .statusCode(200)
                .body("findAll { it.status == 'FALHA' }.size() > 0", is(true));
    }

    @Test
    void valorInvalidoRetorna400() {
        given()
                .contentType("application/json")
                .body("{\"valor\":0}")
                .when().post("/notas")
                .then()
                .statusCode(400);
    }
}
