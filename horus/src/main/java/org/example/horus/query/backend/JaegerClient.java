package org.example.horus.query.backend;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
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

    /**
     * {@code GET /api/traces} — busca traces de um serviço numa janela (T-1001). Tempos em
     * microssegundos; {@code minDuration} no formato de duração do Jaeger (ex.: {@code 1500us});
     * {@code tags} é um objeto JSON (ex.: {@code {"error":"true"}}). Parâmetros nulos são omitidos.
     */
    @GET
    @Path("/api/traces")
    JsonNode searchTraces(@QueryParam("service") String service,
                          @QueryParam("operation") String operation,
                          @QueryParam("start") long startMicros,
                          @QueryParam("end") long endMicros,
                          @QueryParam("minDuration") String minDuration,
                          @QueryParam("limit") int limit,
                          @QueryParam("tags") String tags);

    /** {@code GET /api/services} — serviços conhecidos pelo Jaeger. */
    @GET
    @Path("/api/services")
    JsonNode services();
}
