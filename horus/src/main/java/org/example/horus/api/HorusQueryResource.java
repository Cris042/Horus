package org.example.horus.api;

import jakarta.ws.rs.BadRequestException;
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
import org.example.horus.query.QueryModel.MetricSeries;
import org.example.horus.query.TimeWindow;
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

    /** Logs de uma query LogQL numa janela temporal (T-1001). */
    @GET
    @Path("/logs/range")
    public List<LogLine> logsInWindow(@QueryParam("logql") String logQl,
                                      @QueryParam("lookback") String lookback,
                                      @QueryParam("from") String from,
                                      @QueryParam("to") String to,
                                      @QueryParam("limit") @DefaultValue("100") int limit) {
        if (logQl == null || logQl.isBlank()) {
            throw new BadRequestException("logql obrigatório");
        }
        TimeWindow window = ApiWindows.resolve(lookback, from, to);
        return logs.findInWindow(logQl, window, Math.max(1, Math.min(limit, 5_000)));
    }

    /** Avaliação PromQL sobre uma janela (T-1001); {@code step} em segundos, padrão ~60 pontos. */
    @GET
    @Path("/metrics/range")
    public List<MetricSeries> metricsInWindow(@QueryParam("query") String promQl,
                                              @QueryParam("lookback") String lookback,
                                              @QueryParam("from") String from,
                                              @QueryParam("to") String to,
                                              @QueryParam("step") Long stepSeconds) {
        if (promQl == null || promQl.isBlank()) {
            throw new BadRequestException("query obrigatória");
        }
        TimeWindow window = ApiWindows.resolve(lookback, from, to);
        long step = stepSeconds != null && stepSeconds > 0 ? stepSeconds
                : Math.max(1, window.span().toSeconds() / 60);
        return metrics.rangeQuery(promQl, window, step);
    }
}
