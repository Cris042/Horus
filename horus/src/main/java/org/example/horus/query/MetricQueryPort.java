package org.example.horus.query;

import java.util.List;

/** Porta de consulta de métricas (implementada sobre Prometheus — T-501, RF-H-014). */
public interface MetricQueryPort {

    /** Avalia uma query PromQL instantânea e retorna as amostras do vetor resultante. */
    List<QueryModel.MetricSample> instantQuery(String promQl);
}
