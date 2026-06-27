package org.example.payment.api;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Fluxo de domínio do Payment (RF-011..015): carteira/saldo, movimentações, aprovar/estornar
 * pagamento e histórico. Usa o banco do Dev Services (PostgreSQL/Testcontainers) — roda no CI.
 */
@QuarkusTest
class PaymentFlowTest {

    @Test
    void cicloDeVidaCarteiraEPagamento() {
        // RF-011: abrir carteira com saldo inicial
        Integer carteiraId = given()
                .contentType("application/json")
                .body("{\"titularId\":\"tit-1\",\"saldoInicial\":100.00}")
                .when().post("/carteiras")
                .then()
                .statusCode(201)
                .body("saldo", is(100.00f))
                .extract().path("id");

        // RF-012: registrar entrada -> saldo 150
        given()
                .contentType("application/json")
                .body("{\"tipo\":\"ENTRADA\",\"valor\":50.00,\"descricao\":\"aporte\"}")
                .when().post("/carteiras/{id}/movimentacoes", carteiraId)
                .then()
                .statusCode(201)
                .body("saldoApos", is(150.00f));

        // RF-013: criar pagamento (PENDENTE) e aprovar -> debita 30 -> saldo 120
        Integer pagamentoId = given()
                .contentType("application/json")
                .body("{\"valor\":30.00}")
                .when().post("/carteiras/{id}/pagamentos", carteiraId)
                .then()
                .statusCode(201)
                .body("status", is("PENDENTE"))
                .extract().path("id");

        given()
                .when().post("/pagamentos/{id}/aprovar", pagamentoId)
                .then()
                .statusCode(200)
                .body("status", is("APROVADO"))
                .body("processadoEm", notNullValue());

        given()
                .when().get("/carteiras/{id}", carteiraId)
                .then()
                .statusCode(200)
                .body("saldo", is(120.00f));

        // RF-014: estornar -> devolve 30 -> saldo 150
        given()
                .when().post("/pagamentos/{id}/estornar", pagamentoId)
                .then()
                .statusCode(200)
                .body("status", is("ESTORNADO"));

        given()
                .when().get("/carteiras/{id}", carteiraId)
                .then()
                .statusCode(200)
                .body("saldo", is(150.00f));

        // RF-014: estornar de novo -> conflito (não está aprovado)
        given()
                .when().post("/pagamentos/{id}/estornar", pagamentoId)
                .then()
                .statusCode(409);

        // RF-015: histórico tem 3 movimentações (entrada, saída do pagamento, estorno)
        given()
                .when().get("/carteiras/{id}/movimentacoes", carteiraId)
                .then()
                .statusCode(200)
                .body("size()", is(3));
    }

    @Test
    void aprovarSemSaldoRetorna409() {
        Integer carteiraId = given()
                .contentType("application/json")
                .body("{\"titularId\":\"tit-2\"}")
                .when().post("/carteiras")
                .then()
                .statusCode(201)
                .body("saldo", is(0.00f))
                .extract().path("id");

        Integer pagamentoId = given()
                .contentType("application/json")
                .body("{\"valor\":10.00}")
                .when().post("/carteiras/{id}/pagamentos", carteiraId)
                .then()
                .statusCode(201)
                .extract().path("id");

        given()
                .when().post("/pagamentos/{id}/aprovar", pagamentoId)
                .then()
                .statusCode(409);
    }

    @Test
    void carteiraInexistenteRetorna404() {
        given()
                .when().get("/carteiras/{id}", 999999)
                .then()
                .statusCode(404);
    }
}
