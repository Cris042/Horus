package org.example.horus.query.backend;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * Cliente REST do Loki (query API). URL base em {@code quarkus.rest-client.loki.url}.
 */
@RegisterRestClient(configKey = "loki")
@Produces(MediaType.APPLICATION_JSON)
public interface LokiClient {

    /** {@code GET /loki/api/v1/query_range} — avalia uma query LogQL num intervalo. */
    @GET
    @Path("/loki/api/v1/query_range")
    JsonNode queryRange(@QueryParam("query") String logQl,
                        @QueryParam("limit") int limit,
                        @QueryParam("direction") String direction);

    /** {@code query_range} restrito a {@code [start, end]} em nanossegundos (T-1001). */
    @GET
    @Path("/loki/api/v1/query_range")
    JsonNode queryRange(@QueryParam("query") String logQl,
                        @QueryParam("start") long startNanos,
                        @QueryParam("end") long endNanos,
                        @QueryParam("limit") int limit,
                        @QueryParam("direction") String direction);
}
