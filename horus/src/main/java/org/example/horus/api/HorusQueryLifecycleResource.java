package org.example.horus.api;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.example.horus.lifecycle.QueryLifecycleModel.QueryLifecycle;
import org.example.horus.lifecycle.QueryLifecycleService;

import java.util.Locale;
import java.util.regex.Pattern;

/** API do ciclo de vida das queries correlacionadas a um trace (T-504). */
@Path("/horus/lifecycle/queries")
@Produces(MediaType.APPLICATION_JSON)
public class HorusQueryLifecycleResource {

    private static final Pattern TRACE_ID = Pattern.compile("^[0-9a-fA-F]{32}$");

    private final QueryLifecycleService lifecycle;

    public HorusQueryLifecycleResource(QueryLifecycleService lifecycle) {
        this.lifecycle = lifecycle;
    }

    @GET
    @Path("/{traceId}")
    public QueryLifecycle byTrace(@PathParam("traceId") String rawTraceId) {
        String traceId = normalizeTraceId(rawTraceId);
        return lifecycle.findByTraceId(traceId)
                .orElseThrow(() -> new NotFoundException("trace nao encontrado: " + traceId));
    }

    private static String normalizeTraceId(String rawTraceId) {
        if (rawTraceId == null || !TRACE_ID.matcher(rawTraceId).matches()) {
            throw new BadRequestException("traceId invalido: use 32 caracteres hexadecimais");
        }
        return rawTraceId.toLowerCase(Locale.ROOT);
    }
}
