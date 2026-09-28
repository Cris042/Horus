package org.example.horus.query;

import java.util.List;

/** Porta de consulta de logs (implementada sobre Loki — T-501, RF-H-003/014). */
public interface LogQueryPort {

    /** Logs correlacionados a um {@code traceId}, do mais recente para o mais antigo, até {@code limit}. */
    List<QueryModel.LogLine> findByTraceId(String traceId, int limit);

    /**
     * Avalia uma query LogQL numa janela temporal (T-1001), do mais recente para o mais
     * antigo, até {@code limit}.
     */
    List<QueryModel.LogLine> findInWindow(String logQl, TimeWindow window, int limit);
}
