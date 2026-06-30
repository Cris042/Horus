package org.example.horus.api;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.example.horus.lifecycle.ServiceMapModel.ServiceMap;
import org.example.horus.lifecycle.ServiceMapService;

import java.util.Locale;
import java.util.regex.Pattern;

/** API do mapa de serviços/dependências de um trace (T-506, RF-H-015). */
@Path("/horus/lifecycle/service-map")
@Produces(MediaType.APPLICATION_JSON)
public class HorusServiceMapResource {

    private static final Pattern TRACE_ID = Pattern.compile("^[0-9a-fA-F]{32}$");

    private final ServiceMapService serviceMap;

    public HorusServiceMapResource(ServiceMapService serviceMap) {
        this.serviceMap = serviceMap;
    }

    @GET
    @Path("/{traceId}")
    public ServiceMap byTrace(@PathParam("traceId") String rawTraceId) {
        String traceId = normalizeTraceId(rawTraceId);
        return serviceMap.findByTraceId(traceId)
                .orElseThrow(() -> new NotFoundException("trace nao encontrado: " + traceId));
    }

    private static String normalizeTraceId(String rawTraceId) {
        if (rawTraceId == null || !TRACE_ID.matcher(rawTraceId).matches()) {
            throw new BadRequestException("traceId invalido: use 32 caracteres hexadecimais");
        }
        return rawTraceId.toLowerCase(Locale.ROOT);
    }
}
