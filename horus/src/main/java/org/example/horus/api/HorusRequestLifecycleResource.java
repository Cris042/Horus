package org.example.horus.api;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.example.horus.lifecycle.RequestLifecycleModel.RequestLifecycle;
import org.example.horus.lifecycle.RequestLifecycleService;

import java.util.Locale;
import java.util.regex.Pattern;

/** API do ciclo de vida de request em formato de waterfall (T-503). */
@Path("/horus/lifecycle/requests")
@Produces(MediaType.APPLICATION_JSON)
public class HorusRequestLifecycleResource {

    private static final Pattern TRACE_ID = Pattern.compile("^[0-9a-fA-F]{32}$");

    private final RequestLifecycleService lifecycle;

    public HorusRequestLifecycleResource(RequestLifecycleService lifecycle) {
        this.lifecycle = lifecycle;
    }

    @GET
    @Path("/{traceId}")
    public RequestLifecycle byTrace(@PathParam("traceId") String rawTraceId) {
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
