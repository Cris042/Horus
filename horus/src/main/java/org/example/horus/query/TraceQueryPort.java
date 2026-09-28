package org.example.horus.query;

import java.util.List;
import java.util.Optional;

/** Porta de consulta de traces (implementada sobre Jaeger — T-501, RF-H-014). */
public interface TraceQueryPort {

    /** Busca um trace pelo seu {@code traceId}; vazio se não encontrado. */
    Optional<QueryModel.TraceResult> findTrace(String traceId);

    /**
     * Busca traces numa janela temporal (T-1001), do mais recente para o mais antigo,
     * até {@code search.limit()}.
     */
    List<QueryModel.TraceSummary> searchTraces(QueryModel.TraceSearch search);

    /** Serviços que já reportaram traces ao backend (T-1001). */
    List<String> listServices();
}
