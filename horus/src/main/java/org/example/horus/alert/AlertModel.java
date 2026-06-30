package org.example.horus.alert;

import java.util.List;
import java.util.Locale;

/**
 * Modelos de **alerta** (T-703, RF-H-013).
 *
 * <p>Um {@link AlertRequest} (título, severidade, contexto — tipicamente vindo do
 * Anomaly Detector / Error Clusterer da T-606) é enriquecido com um resumo de IA e
 * disparado para os canais habilitados, produzindo um {@link AlertResult}.
 */
public final class AlertModel {

    private AlertModel() {
    }

    /** Severidade do alerta. */
    public enum AlertSeverity {
        INFO, WARNING, CRITICAL;

        /** Tolerante a maiúsc./minúsc. e nulo (default {@code WARNING}). */
        public static AlertSeverity from(String value) {
            if (value == null || value.isBlank()) {
                return WARNING;
            }
            try {
                return AlertSeverity.valueOf(value.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                return WARNING;
            }
        }
    }

    /** Gatilho de alerta recebido pela API. */
    public record AlertRequest(String title, String severity, String traceId, String details) {
    }

    /** Alerta montado: título, severidade, resumo de IA e proveniência. */
    public record Alert(
            String title,
            AlertSeverity severity,
            String traceId,
            String summary,
            String modelId,
            boolean live) {
    }

    /** Resultado do envio por um canal. */
    public record ChannelResult(String channel, boolean dispatched, String detail) {
    }

    /** Resultado completo: o alerta montado + o que cada canal fez. */
    public record AlertResult(Alert alert, int dispatchedCount, List<ChannelResult> channels) {
    }
}
