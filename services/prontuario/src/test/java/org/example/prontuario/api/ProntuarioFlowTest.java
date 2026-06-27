package org.example.prontuario.api;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Fluxo de domínio do Prontuário (RF-006..010): criar prontuário, registrar consulta,
 * atualizar, finalizar e garantir a imutabilidade pós-finalização. Usa o banco do Dev
 * Services (PostgreSQL/Testcontainers) — roda no CI.
 */
@QuarkusTest
class ProntuarioFlowTest {

    @Test
    void cicloDeVidaProntuarioEConsulta() {
        // RF-006: criar prontuário
        Integer prontuarioId = given()
                .contentType("application/json")
                .body("{\"pacienteId\":\"pac-123\"}")
                .when().post("/prontuarios")
                .then()
                .statusCode(201)
                .body("pacienteId", is("pac-123"))
                .body("id", notNullValue())
                .extract().path("id");

        // RF-007: consultar prontuário
        given()
                .when().get("/prontuarios/{id}", prontuarioId)
                .then()
                .statusCode(200)
                .body("id", is(prontuarioId));

        // RF-008: registrar consulta
        Integer consultaId = given()
                .contentType("application/json")
                .body("{\"descricao\":\"queixa inicial\"}")
                .when().post("/prontuarios/{id}/consultas", prontuarioId)
                .then()
                .statusCode(201)
                .body("status", is("EM_ANDAMENTO"))
                .body("prontuarioId", is(prontuarioId))
                .extract().path("id");

        // RF-009: atualizar consulta
        given()
                .contentType("application/json")
                .body("{\"descricao\":\"evolução do quadro\"}")
                .when().put("/consultas/{id}", consultaId)
                .then()
                .statusCode(200)
                .body("descricao", is("evolução do quadro"))
                .body("atualizadoEm", notNullValue());

        // RF-010: finalizar consulta
        given()
                .when().post("/consultas/{id}/finalizar", consultaId)
                .then()
                .statusCode(200)
                .body("status", is("FINALIZADA"))
                .body("finalizadoEm", notNullValue());

        // RF-010: consulta finalizada é imutável -> 409
        given()
                .contentType("application/json")
                .body("{\"descricao\":\"tentativa após finalizar\"}")
                .when().put("/consultas/{id}", consultaId)
                .then()
                .statusCode(409);
    }

    @Test
    void prontuarioInexistenteRetorna404() {
        given()
                .when().get("/prontuarios/{id}", 999999)
                .then()
                .statusCode(404);
    }

    @Test
    void pacienteIdEmBrancoRetorna400() {
        given()
                .contentType("application/json")
                .body("{\"pacienteId\":\"\"}")
                .when().post("/prontuarios")
                .then()
                .statusCode(400);
    }
}
