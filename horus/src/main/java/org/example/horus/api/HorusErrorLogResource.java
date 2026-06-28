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
import org.example.horus.lifecycle.ErrorLogAggregationModel.ErrorLogAggregation;
import org.example.horus.lifecycle.ErrorLogAggregationService;

import java.util.Locale;
import java.util.regex.Pattern;

/** API de agregação de logs de erro correlacionados a um trace (T-505). */
@Path("/horus/lifecycle/errors")
@Produces(MediaType.APPLICATION_JSON)
public class HorusErrorLogResource {

    private static final Pattern TRACE_ID = Pattern.compile("^[0-9a-fA-F]{32}$");

    private final ErrorLogAggregationService errors;

    public HorusErrorLogResource(ErrorLogAggregationService errors) {
        this.errors = errors;
    }

    @GET
    @Path("/{traceId}")
    public ErrorLogAggregation byTrace(@PathParam("traceId") String rawTraceId,
                                       @QueryParam("limit") @DefaultValue("100") int limit) {
        String traceId = normalizeTraceId(rawTraceId);
        return errors.findByTraceId(traceId, limit)
                .orElseThrow(() -> new NotFoundException("trace nao encontrado: " + traceId));
    }

    private static String normalizeTraceId(String rawTraceId) {
        if (rawTraceId == null || !TRACE_ID.matcher(rawTraceId).matches()) {
            throw new BadRequestException("traceId invalido: use 32 caracteres hexadecimais");
        }
        return rawTraceId.toLowerCase(Locale.ROOT);
    }
}
