package org.example.horus.api;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.example.horus.ai.agent.TraceExplainer;
import org.example.horus.ai.agent.TraceExplainer.TraceExplanation;
import org.example.horus.ai.context.ContextAssembler;
import org.example.horus.ai.context.PromptContext;

/**
 * API do agente Trace Explainer (T-604, RF-H-006) — "explique este trace".
 */
@Path("/horus/ai/explain")
@Produces(MediaType.APPLICATION_JSON)
public class HorusExplainResource {

    private final ContextAssembler assembler;
    private final TraceExplainer explainer;

    public HorusExplainResource(ContextAssembler assembler, TraceExplainer explainer) {
        this.assembler = assembler;
        this.explainer = explainer;
    }

    /** Explica o caminho da request correlacionada a um {@code traceId}. */
    @GET
    @Path("/trace/{traceId}")
    public TraceExplanation explainTrace(@PathParam("traceId") String traceId,
                                         @QueryParam("logLimit") @DefaultValue("100") int logLimit) {
        PromptContext context = assembler.assembleForTrace(traceId, logLimit);
        return explainer.explain(traceId, context);
    }
}
