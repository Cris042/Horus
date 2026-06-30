package org.example.horus.lifecycle;

import java.util.List;

/**
 * Modelo da **visualização de SAGA** de um trace (T-507, RF-H-016, ADR-0013).
 *
 * <p>Reconstrói, a partir dos spans do trace, os passos da SAGA, as compensações e
 * o desfecho. Os spans de SAGA seguem o contrato de telemetria (T-005): operação
 * {@code saga.{flow}.{step}} para um passo e {@code saga.{flow}.{step}.compensate}
 * para uma compensação.
 */
public final class SagaVisualizationModel {

    private SagaVisualizationModel() {
    }

    /** Um passo da SAGA (ou sua compensação) na linha do tempo. */
    public record SagaStep(
            String flow,
            String step,
            boolean compensation,
            String operation,
            String serviceName,
            long offsetMicros,
            long durationMicros) {
    }

    /** Visão correlacionada de uma SAGA por {@code traceId}. */
    public record SagaFlow(
            String traceId,
            String flow,
            String outcome,
            int stepCount,
            int compensationCount,
            long totalDurationMicros,
            List<SagaStep> steps) {
    }
}
