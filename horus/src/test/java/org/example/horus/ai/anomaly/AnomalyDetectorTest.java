package org.example.horus.ai.anomaly;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import org.example.horus.query.MetricQueryPort;
import org.example.horus.query.QueryModel.MetricSample;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/** Testes do Anomaly Detector baseado em regras (T-606, RF-H-008). */
@QuarkusTest
class AnomalyDetectorTest {

    @InjectMock
    MetricQueryPort metrics;

    @Test
    void flagsSeriesThatBreachThresholdRankedByDeviation() {
        when(metrics.instantQuery(anyString())).thenReturn(List.of(
                new MetricSample(Map.of("service", "payment-service"), 0.80, 1.0),
                new MetricSample(Map.of("service", "invoice-service"), 0.95, 1.0),
                new MetricSample(Map.of("service", "prontuario-service"), 0.20, 1.0)));

        String body = """
                [{"name":"high-error-rate","promQl":"sum(rate(http_errors[5m]))","comparison":"GT","threshold":0.5,"severity":"critical"}]
                """;

        given().contentType("application/json").body(body)
                .when().post("/horus/ai/anomalies")
                .then().statusCode(200)
                .body("evaluatedRules", is(1))
                .body("anomalyCount", is(2))
                .body("anomalies[0].value", is(0.95f))
                .body("anomalies[0].rule", equalTo("high-error-rate"))
                .body("anomalies[0].severity", equalTo("critical"))
                .body("anomalies[0].labels.service", equalTo("invoice-service"))
                .body("anomalies[1].value", is(0.80f));
    }

    @Test
    void emptyRulesReturns400() {
        given().contentType("application/json").body("[]")
                .when().post("/horus/ai/anomalies")
                .then().statusCode(400);
    }
}
