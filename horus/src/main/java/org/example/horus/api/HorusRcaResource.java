package org.example.horus.api;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.example.horus.ai.agent.RootCauseAnalyst;
import org.example.horus.ai.agent.RootCauseAnalyst.RootCauseAnalysis;
import org.example.horus.ai.context.ContextAssembler;
import org.example.horus.ai.context.PromptContext;

/**
 * API do agente Root-Cause Analyst (T-605, RF-H-007) — RCA de um incidente.
 */
@Path("/horus/ai/rca")
@Produces(MediaType.APPLICATION_JSON)
public class HorusRcaResource {

    private final ContextAssembler assembler;
    private final RootCauseAnalyst analyst;

    public HorusRcaResource(ContextAssembler assembler, RootCauseAnalyst analyst) {
        this.assembler = assembler;
        this.analyst = analyst;
    }

    /** RCA do incidente correlacionado a um {@code traceId} (trace + logs + métricas/PromQL). */
    @GET
    @Path("/trace/{traceId}")
    public RootCauseAnalysis analyzeTrace(@PathParam("traceId") String traceId,
                                          @QueryParam("promql") String promQl,
                                          @QueryParam("logLimit") @DefaultValue("100") int logLimit) {
        PromptContext context = assembler.assembleForIncident(traceId, promQl, logLimit);
        return analyst.analyze(traceId, context);
    }
}
