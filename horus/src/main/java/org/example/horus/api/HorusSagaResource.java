package org.example.horus.api;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.example.horus.lifecycle.SagaVisualizationModel.SagaFlow;
import org.example.horus.lifecycle.SagaVisualizationService;

import java.util.Locale;
import java.util.regex.Pattern;

/** API da visualização de SAGA de um trace (T-507, RF-H-016, ADR-0013). */
@Path("/horus/lifecycle/saga")
@Produces(MediaType.APPLICATION_JSON)
public class HorusSagaResource {

    private static final Pattern TRACE_ID = Pattern.compile("^[0-9a-fA-F]{32}$");

    private final SagaVisualizationService saga;

    public HorusSagaResource(SagaVisualizationService saga) {
        this.saga = saga;
    }

    @GET
    @Path("/{traceId}")
    public SagaFlow byTrace(@PathParam("traceId") String rawTraceId) {
        String traceId = normalizeTraceId(rawTraceId);
        return saga.findByTraceId(traceId)
                .orElseThrow(() -> new NotFoundException("trace nao encontrado: " + traceId));
    }

    private static String normalizeTraceId(String rawTraceId) {
        if (rawTraceId == null || !TRACE_ID.matcher(rawTraceId).matches()) {
            throw new BadRequestException("traceId invalido: use 32 caracteres hexadecimais");
        }
        return rawTraceId.toLowerCase(Locale.ROOT);
    }
}
