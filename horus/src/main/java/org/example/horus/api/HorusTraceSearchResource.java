package org.example.horus.api;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import org.example.horus.query.QueryModel.TraceSearch;
import org.example.horus.query.QueryModel.TraceSummary;
import org.example.horus.query.TimeWindow;
import org.example.horus.query.TraceQueryPort;
import org.example.horus.security.CallerScope;

import java.util.List;

/**
 * Busca de traces por janela temporal (T-1001, RF-H-012) — o ponto de entrada do Horus
 * quando não se conhece um {@code traceId}: traces recentes, lentos ou com erro, filtráveis
 * por serviço/operação. Cada resultado leva ao ciclo de vida completo em
 * {@code /horus/lifecycle/requests/{traceId}}.
 */
@Path("/horus/traces")
@Produces(MediaType.APPLICATION_JSON)
public class HorusTraceSearchResource {

    static final int MAX_LIMIT = 200;

    private final TraceQueryPort traces;

    public HorusTraceSearchResource(TraceQueryPort traces) {
        this.traces = traces;
    }

    /**
     * Traces na janela ({@code lookback} relativo ou {@code from}/{@code to} absolutos), do
     * mais recente para o mais antigo.
     */
    @GET
    public TraceSearchResult search(@Context ContainerRequestContext request,
                                    @QueryParam("service") String service,
                                    @QueryParam("operation") String operation,
                                    @QueryParam("lookback") String lookback,
                                    @QueryParam("from") String from,
                                    @QueryParam("to") String to,
                                    @QueryParam("minDurationMs") @DefaultValue("0") long minDurationMs,
                                    @QueryParam("error") @DefaultValue("false") boolean onlyErrors,
                                    @QueryParam("limit") @DefaultValue("20") int limit,
                                    @QueryParam("minSpans") @DefaultValue("1") int minSpans) {
        if (limit < 1 || limit > MAX_LIMIT) {
            throw new BadRequestException("limit deve estar entre 1 e " + MAX_LIMIT);
        }
        if (minDurationMs < 0) {
            throw new BadRequestException("minDurationMs não pode ser negativo");
        }
        TimeWindow window = ApiWindows.resolve(lookback, from, to);
        String scoped = CallerScope.scopedService(request, service); // DEVELOPER: só seus serviços (T-1007)
        // minSpans (T-1011): esconde "ruído de fundo" (queries de boot/Flyway viram traces de 1 span).
        // Busca mais que o limite para compensar os descartados.
        int fetch = minSpans > 1 ? Math.min(MAX_LIMIT, limit * 3) : limit;
        List<TraceSummary> found = traces.searchTraces(
                        new TraceSearch(scoped, operation, window, minDurationMs * 1_000, onlyErrors, fetch))
                .stream().filter(t -> t.spanCount() >= minSpans).limit(limit).toList();
        return new TraceSearchResult(window.startMicros(), window.endMicros(), found.size(), found);
    }

    /** Serviços com traces no backend — para popular filtros. */
    @GET
    @Path("/services")
    public List<String> services(@Context ContainerRequestContext request) {
        return CallerScope.filterServices(request, traces.listServices());
    }

    /** Resultado da busca: janela efetiva + traces encontrados. */
    public record TraceSearchResult(long startMicros, long endMicros, int count, List<TraceSummary> traces) {
    }
}
