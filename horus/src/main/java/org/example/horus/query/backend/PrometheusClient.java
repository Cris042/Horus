package org.example.horus.query.backend;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * Cliente REST do Prometheus (HTTP API). URL base em {@code quarkus.rest-client.prometheus.url}.
 */
@RegisterRestClient(configKey = "prometheus")
@Produces(MediaType.APPLICATION_JSON)
public interface PrometheusClient {

    /** {@code GET /api/v1/query} — avalia uma expressão PromQL instantânea. */
    @GET
    @Path("/api/v1/query")
    JsonNode instantQuery(@QueryParam("query") String promQl);
}
