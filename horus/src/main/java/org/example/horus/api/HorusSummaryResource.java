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
import org.example.horus.ai.context.WindowContextCollector;

/**
 * API do agente Summarizer (T-603, RF-H-005) — resumo de estado <b>sob demanda</b>.
 * O resumo agendado é feito por {@code ScheduledStateSummary}.
 */
@Path("/horus/ai/summary")
@Produces(MediaType.APPLICATION_JSON)
public class HorusSummaryResource {

    private final ContextAssembler assembler;
    private final WindowContextCollector windows;
    private final StateSummarizer summarizer;

    public HorusSummaryResource(ContextAssembler assembler, WindowContextCollector windows,
                                StateSummarizer summarizer) {
        this.assembler = assembler;
        this.windows = windows;
        this.summarizer = summarizer;
    }

    /**
     * Resume o estado da aplicação inteira numa janela temporal (T-1001, RF-H-005): volume,
     * erros, traces e queries mais lentos, logs de erro e métricas — sem precisar de um traceId.
     */
    @GET
    @Path("/state")
    public StateSummary summarizeState(@QueryParam("lookback") String lookback,
                                       @QueryParam("from") String from,
                                       @QueryParam("to") String to) {
        return summarizer.summarize(windows.collect(ApiWindows.resolve(lookback, from, to)));
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
