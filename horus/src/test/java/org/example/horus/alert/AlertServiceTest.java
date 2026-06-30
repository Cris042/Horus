package org.example.horus.alert;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

/**
 * Testes do mecanismo de alerta (T-703, RF-H-013). O resumo usa o {@code StubLlmEngine}
 * default (sem chave → {@code live=false}); webhook/e-mail ficam desabilitados (config
 * vazia), de modo que apenas o canal {@code log} dispara.
 */
@QuarkusTest
class AlertServiceTest {

    @Test
    void raise_summarizesViaAiAndFansOutToEnabledChannels() {
        String body = """
                {"title":"Erro crítico no pagamento","severity":"critical",
                 "traceId":"4bf92f3577b34da6a3ce929d0e0e4736",
                 "details":"5 timeouts ao chamar payment-service em 1 min"}
                """;

        given().contentType("application/json").body(body)
                .when().post("/horus/alerts")
                .then().statusCode(200)
                .body("alert.severity", equalTo("CRITICAL"))
                .body("alert.title", equalTo("Erro crítico no pagamento"))
                .body("alert.live", is(false))
                .body("alert.modelId", equalTo("claude-haiku-4-5"))
                .body("dispatchedCount", greaterThanOrEqualTo(1))
                .body("channels.channel", hasItem("log"))
                .body("channels.find { it.channel == 'log' }.dispatched", is(true));
    }

    @Test
    void unknownSeverityDefaultsToWarning() {
        given().contentType("application/json")
                .body("{\"title\":\"algo\",\"severity\":\"banana\"}")
                .when().post("/horus/alerts")
                .then().statusCode(200)
                .body("alert.severity", equalTo("WARNING"));
    }

    @Test
    void emptyRequestReturns400() {
        given().contentType("application/json").body("{}")
                .when().post("/horus/alerts")
                .then().statusCode(400);
    }

    @Test
    void channels_listsLogAsEnabled() {
        given().when().get("/horus/alerts/channels")
                .then().statusCode(200)
                .body("find { it.channel == 'log' }.dispatched", is(true))
                .body("find { it.channel == 'webhook' }.dispatched", is(false));
    }
}
