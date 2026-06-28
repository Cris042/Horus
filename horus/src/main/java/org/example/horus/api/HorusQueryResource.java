package org.example.horus.api;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.example.horus.query.LogQueryPort;
import org.example.horus.query.MetricQueryPort;
import org.example.horus.query.QueryModel.LogLine;
import org.example.horus.query.QueryModel.MetricSample;
import org.example.horus.query.QueryModel.TraceResult;
import org.example.horus.query.TraceQueryPort;

import java.util.List;

/**
 * API de leitura do Horus sobre os backends de telemetria (T-501, RF-H-014).
 *
 * <p>Camada fina sobre as portas de consulta — traces (Jaeger), logs (Loki) e métricas
 * (Prometheus). A correlação por {@code trace_id} (request ↔ query ↔ log ↔ mensagem ↔
 * worker) que costura estes sinais num só ciclo de vida é construída em T-502.
 */
@Path("/horus/query")
@Produces(MediaType.APPLICATION_JSON)
public class HorusQueryResource {

    private final TraceQueryPort traces;
    private final LogQueryPort logs;
    private final MetricQueryPort metrics;

    public HorusQueryResource(TraceQueryPort traces, LogQueryPort logs, MetricQueryPort metrics) {
        this.traces = traces;
        this.logs = logs;
        this.metrics = metrics;
    }

    /** Trace completo (spans) por {@code traceId}. 404 se inexistente. */
    @GET
    @Path("/traces/{traceId}")
    public TraceResult trace(@PathParam("traceId") String traceId) {
        return traces.findTrace(traceId)
                .orElseThrow(() -> new NotFoundException("trace não encontrado: " + traceId));
    }

    /** Logs correlacionados a um {@code traceId}. */
    @GET
    @Path("/logs")
    public List<LogLine> logsByTrace(@QueryParam("traceId") String traceId,
                                     @QueryParam("limit") @DefaultValue("100") int limit) {
        return logs.findByTraceId(traceId, limit);
    }

    /** Avaliação PromQL instantânea. */
    @GET
    @Path("/metrics")
    public List<MetricSample> metrics(@QueryParam("query") String promQl) {
        return metrics.instantQuery(promQl);
    }
}
