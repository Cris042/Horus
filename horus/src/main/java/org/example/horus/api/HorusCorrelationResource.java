package org.example.horus.api;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.example.horus.correlation.CorrelationModel.RequestCorrelation;
import org.example.horus.correlation.CorrelationService;

/**
 * API do modelo de correlação por {@code trace_id} (T-502, RF-H-001/002/004).
 *
 * <p>Devolve, num só payload, a costura request ↔ queries ↔ logs ↔ mensageria ↔ worker
 * de um trace — base para as APIs de ciclo de vida (T-503/504/505) e a visualização (T-7xx).
 */
@Path("/horus/correlation")
@Produces(MediaType.APPLICATION_JSON)
public class HorusCorrelationResource {

    private final CorrelationService correlation;

    public HorusCorrelationResource(CorrelationService correlation) {
        this.correlation = correlation;
    }

    /** Correlação completa de um trace. 404 se o trace não existir. */
    @GET
    @Path("/trace/{traceId}")
    public RequestCorrelation byTrace(@PathParam("traceId") String traceId,
                                      @QueryParam("logLimit") @DefaultValue("100") int logLimit) {
        return correlation.correlate(traceId, logLimit)
                .orElseThrow(() -> new NotFoundException("trace não encontrado: " + traceId));
    }
}
