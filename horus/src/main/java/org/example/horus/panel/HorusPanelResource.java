package org.example.horus.panel;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.example.horus.ai.LlmEngine;
import org.example.horus.ai.LlmResponseCache;
import org.example.horus.ai.LlmResponseCache.CacheStats;

import java.util.Map;

/**
 * API de agregação do painel Horus (T-701, RF-H-012).
 *
 * <p>Compõe num só payload o estado de **saúde** (motor de IA, cache, backends configurados)
 * e os **pontos de entrada** das capacidades (resumo de IA, explicação, RCA, "pergunte ao
 * Horus", consultas). É o que a página do painel consome. O ciclo de vida em waterfall
 * (T-702) e a busca avançada (sobre T-503..505) entram nas fatias seguintes.
 */
@Path("/horus/panel")
@Produces(MediaType.APPLICATION_JSON)
public class HorusPanelResource {

    private final LlmEngine engine;
    private final LlmResponseCache cache;

    @ConfigProperty(name = "quarkus.rest-client.jaeger.url", defaultValue = "")
    String jaegerUrl;
    @ConfigProperty(name = "quarkus.rest-client.loki.url", defaultValue = "")
    String lokiUrl;
    @ConfigProperty(name = "quarkus.rest-client.prometheus.url", defaultValue = "")
    String prometheusUrl;

    public HorusPanelResource(LlmEngine engine, LlmResponseCache cache) {
        this.engine = engine;
        this.cache = cache;
    }

    /** Visão geral do painel: saúde + capacidades. */
    @GET
    @Path("/overview")
    public Overview overview() {
        var ai = new AiStatus(engine.isLive() ? "live" : "stub", engine.isLive(), cache.stats());
        var backends = new Backends(jaegerUrl, lokiUrl, prometheusUrl);
        var endpoints = Map.of(
                "summaryTrace", "GET /horus/ai/summary/trace/{traceId}",
                "explainTrace", "GET /horus/ai/explain/trace/{traceId}",
                "rcaTrace", "GET /horus/ai/rca/trace/{traceId}",
                "ask", "POST /horus/ai/ask",
                "queryTrace", "GET /horus/query/traces/{traceId}",
                "queryLogs", "GET /horus/query/logs?traceId=",
                "queryMetrics", "GET /horus/query/metrics?query=");
        return new Overview("horus", ai, backends, endpoints);
    }

    /** Payload do painel. */
    public record Overview(String service, AiStatus ai, Backends backends,
                           Map<String, String> endpoints) {
    }

    public record AiStatus(String mode, boolean live, CacheStats cache) {
    }

    public record Backends(String jaeger, String loki, String prometheus) {
    }
}
