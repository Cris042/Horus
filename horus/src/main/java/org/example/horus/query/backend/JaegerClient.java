package org.example.horus.query.backend;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * Cliente REST do Jaeger (query API). URL base em {@code quarkus.rest-client.jaeger.url}.
 * Resposta mapeada como {@link JsonNode} — o adapter extrai só o necessário (sem acoplar
 * ao schema completo do Jaeger).
 */
@RegisterRestClient(configKey = "jaeger")
@Produces(MediaType.APPLICATION_JSON)
public interface JaegerClient {

    /** {@code GET /api/traces/{traceId}} — retorna o trace e seus spans. */
    @GET
    @Path("/api/traces/{traceId}")
    JsonNode getTrace(@PathParam("traceId") String traceId);
}
