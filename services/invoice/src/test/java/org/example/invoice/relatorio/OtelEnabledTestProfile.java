package org.example.invoice.relatorio;

import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.Map;

/**
 * Reativa o SDK OTel (desligado por padrão em {@code %test}) só para
 * {@link RelatorioPublicacaoTest}, para poder verificar a injeção do {@code traceparent} nos
 * headers AMQP (T-405) sem depender de um Collector real — o exportador de traces fica
 * {@code none} (sem I/O de rede durante o teste).
 */
public class OtelEnabledTestProfile implements QuarkusTestProfile {
    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of(
                "quarkus.otel.sdk.disabled", "false",
                "quarkus.otel.traces.exporter", "none");
    }
}
