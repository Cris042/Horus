package org.example.horus.api;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.core.MediaType;
import org.example.horus.ai.agent.StateSummarizer;
import org.example.horus.ai.agent.StateSummarizer.StateSummary;
import org.example.horus.ai.context.ContextAssembler;
import org.example.horus.ai.context.PromptContext;

/**
 * API do agente Summarizer (T-603, RF-H-005) — resumo de estado <b>sob demanda</b>.
 * O resumo agendado é feito por {@code ScheduledStateSummary}.
 */
@Path("/horus/ai/summary")
@Produces(MediaType.APPLICATION_JSON)
public class HorusSummaryResource {

    private final ContextAssembler assembler;
    private final StateSummarizer summarizer;

    public HorusSummaryResource(ContextAssembler assembler, StateSummarizer summarizer) {
        this.assembler = assembler;
        this.summarizer = summarizer;
    }

    /** Resume o estado correlacionado a um {@code traceId} (trace + logs). */
    @GET
    @Path("/trace/{traceId}")
    public StateSummary summarizeTrace(@PathParam("traceId") String traceId,
                                       @QueryParam("logLimit") @DefaultValue("100") int logLimit) {
        PromptContext context = assembler.assembleForTrace(traceId, logLimit);
        return summarizer.summarize(context);
    }
}
