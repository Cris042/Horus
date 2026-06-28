package org.example.horus.api;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.example.horus.ai.LlmResponseCache;
import org.example.horus.ai.LlmResponseCache.CacheStats;

/**
 * Observabilidade do cache de IA (T-608) — expõe hits/misses/tamanho.
 */
@Path("/horus/ai/cache")
@Produces(MediaType.APPLICATION_JSON)
public class HorusCacheResource {

    private final LlmResponseCache cache;

    public HorusCacheResource(LlmResponseCache cache) {
        this.cache = cache;
    }

    @GET
    @Path("/stats")
    public CacheStats stats() {
        return cache.stats();
    }
}
