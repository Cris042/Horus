package org.example.horus.api;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.example.horus.ai.LlmEngine;
import org.example.horus.ai.LlmEngine.LlmRequest;
import org.example.horus.ai.LlmEngine.LlmResponse;
import org.example.horus.ai.ModelTier;

/**
 * API da camada de IA do Horus (ADR-0011). Camada fina sobre a porta {@link LlmEngine};
 * os agentes de produto (Summarizer, RCA, "pergunte ao Horus" — Fase 6) constroem sobre ela.
 */
@Path("/horus/ai")
@Produces(MediaType.APPLICATION_JSON)
public class HorusAiResource {

    private final LlmEngine engine;

    public HorusAiResource(LlmEngine engine) {
        this.engine = engine;
    }

    /** Estado da IA: se há um provedor real configurado. */
    @GET
    @Path("/health")
    public AiHealth health() {
        return new AiHealth(engine.isLive() ? "live" : "stub", engine.isLive());
    }

    /** Completação genérica (base dos casos de uso de IA). */
    @POST
    @Path("/complete")
    @Consumes(MediaType.APPLICATION_JSON)
    public LlmResponse complete(CompleteRequest body) {
        ModelTier tier = body.tier() == null ? ModelTier.BALANCED : body.tier();
        return engine.complete(new LlmRequest(body.system(), body.prompt(), tier));
    }

    /** Corpo do POST /complete. */
    public record CompleteRequest(String system, String prompt, ModelTier tier) {
    }

    /** Status da camada de IA. */
    public record AiHealth(String mode, boolean live) {
    }
}
