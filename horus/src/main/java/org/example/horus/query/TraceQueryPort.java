package org.example.horus.query;

import java.util.Optional;

/** Porta de consulta de traces (implementada sobre Jaeger — T-501, RF-H-014). */
public interface TraceQueryPort {

    /** Busca um trace pelo seu {@code traceId}; vazio se não encontrado. */
    Optional<QueryModel.TraceResult> findTrace(String traceId);
}
