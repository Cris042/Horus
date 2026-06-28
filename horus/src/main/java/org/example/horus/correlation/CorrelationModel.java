package org.example.horus.correlation;

import org.example.horus.query.QueryModel.LogLine;

import java.util.List;

/**
 * Modelo de **correlação por `trace_id`** (T-502, RF-H-001/002/004).
 *
 * <p>Costura, num único ciclo de vida, os sinais que hoje vivem separados nos backends:
 * a request (spans, possivelmente atravessando vários serviços), as queries/SQL e os logs
 * correlacionados, além da fronteira de mensageria (publicação → worker Rust). É a base
 * sobre a qual as APIs de ciclo de vida (T-503/504/505) e a visualização de SAGA (T-507)
 * são construídas.
 */
public final class CorrelationModel {

    private CorrelationModel() {
    }

    /** Participação de um serviço no trace (quantos spans e quanto tempo somado). */
    public record ServiceInvolvement(String serviceName, int spanCount, long totalDurationMicros) {
    }

    /** Visão correlacionada de uma request por {@code traceId}. */
    public record RequestCorrelation(
            String traceId,
            int spanCount,
            List<ServiceInvolvement> services,
            List<LogLine> logs,
            int errorLogCount,
            boolean messagingInvolved,
            boolean workerInvolved,
            long totalDurationMicros) {
    }
}
