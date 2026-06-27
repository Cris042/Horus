package org.example.invoice.relatorio;

import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.reactive.messaging.memory.InMemoryConnector;
import io.smallrye.reactive.messaging.memory.InMemorySink;
import jakarta.enterprise.inject.Any;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Publicação da solicitação de relatório (T-301, RF-021): emite uma NF, pede o relatório e
 * verifica, pelo conector in-memory, que a mensagem foi publicada com o contrato esperado.
 * Reprocessar/pedir relatório de NF não emitida → 409.
 */
@QuarkusTest
class RelatorioPublicacaoTest {

    @Inject
    @Any
    InMemoryConnector connector;

    @Test
    void emitirEPublicarRelatorio() {
        InMemorySink<RelatorioMensagem> sink = connector.sink("relatorios");
        sink.clear();

        Integer notaId = given()
                .contentType("application/json")
                .body("{\"valor\":120.00,\"referencia\":\"ped-rel\"}")
                .when().post("/notas")
                .then()
                .statusCode(201)
                .body("status", is("EMITIDA"))
                .extract().path("id");

        given()
                .when().post("/notas/{id}/relatorio", notaId)
                .then()
                .statusCode(202)
                .body("tipo", is("NOTA_FISCAL"))
                .body("notaId", is(notaId));

        assertEquals(1, sink.received().size(), "deve publicar exatamente uma mensagem de relatório");
        RelatorioMensagem msg = sink.received().get(0).getPayload();
        assertNotNull(msg.id());
        assertEquals("NOTA_FISCAL", msg.tipo());
        assertEquals(notaId.longValue(), msg.notaId());
    }

    @Test
    void relatorioDeNotaNaoEmitidaRetorna409() {
        InMemorySink<RelatorioMensagem> sink = connector.sink("relatorios");
        sink.clear();

        Integer notaId = given()
                .contentType("application/json")
                .body("{\"valor\":10.00,\"simularFalha\":true}")
                .when().post("/notas")
                .then()
                .statusCode(201)
                .body("status", is("FALHA"))
                .extract().path("id");

        given()
                .when().post("/notas/{id}/relatorio", notaId)
                .then()
                .statusCode(409);

        assertEquals(0, sink.received().size(), "nenhuma mensagem deve ser publicada em falha");
    }
}
