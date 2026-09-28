package org.example.horus.api;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.example.horus.ai.agent.RootCauseAnalyst;
import org.example.horus.ai.agent.StateSummarizer;
import org.example.horus.ai.context.ContextAssembler;
import org.example.horus.ai.context.WindowContextCollector;
import org.example.horus.ai.job.AiJob;
import org.example.horus.ai.job.AiJobService;
import org.example.horus.query.TimeWindow;

import java.net.URI;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

/**
 * Análises de IA assíncronas (T-1006, RNF-H-004). {@code POST} enfileira e responde
 * {@code 202 Accepted} com {@code Location: /horus/ai/jobs/{id}}; {@code GET} devolve o estado e,
 * quando concluído, o mesmo resultado do endpoint síncrono equivalente.
 */
@Path("/horus/ai/jobs")
@Produces(MediaType.APPLICATION_JSON)
public class HorusAiJobResource {

    private final AiJobService jobs;
    private final ContextAssembler assembler;
    private final RootCauseAnalyst analyst;
    private final WindowContextCollector windows;
    private final StateSummarizer summarizer;

    public HorusAiJobResource(AiJobService jobs, ContextAssembler assembler, RootCauseAnalyst analyst,
                              WindowContextCollector windows, StateSummarizer summarizer) {
        this.jobs = jobs;
        this.assembler = assembler;
        this.analyst = analyst;
        this.windows = windows;
        this.summarizer = summarizer;
    }

    /** RCA (camada DEEP) de um trace, em background. */
    @POST
    @Path("/rca")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response rca(RcaJobRequest body) {
        if (body == null || body.traceId() == null || body.traceId().isBlank()) {
            throw new BadRequestException("traceId obrigatório");
        }
        int logLimit = body.logLimit() == null ? 100 : body.logLimit();
        return accept("rca", () -> analyst.analyze(body.traceId(),
                assembler.assembleForIncident(body.traceId(), body.promql(), logLimit)));
    }

    /** Resumo de estado de uma janela, em background. */
    @POST
    @Path("/state-summary")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response stateSummary(WindowJobRequest body) {
        WindowJobRequest b = body == null ? new WindowJobRequest(null, null, null) : body;
        TimeWindow window = ApiWindows.resolve(b.lookback(), b.from(), b.to());
        return accept("state-summary", () -> summarizer.summarize(windows.collect(window)));
    }

    @GET
    @Path("/{id}")
    public AiJob.View get(@PathParam("id") String id) {
        return jobs.find(id).map(AiJob::view)
                .orElseThrow(() -> new NotFoundException("job não encontrado (ou expirado): " + id));
    }

    @GET
    public List<AiJob.View> recent(@QueryParam("limit") @DefaultValue("20") int limit) {
        return jobs.recent(Math.max(1, Math.min(limit, 200))).stream().map(AiJob::view).toList();
    }

    private Response accept(String kind, java.util.function.Supplier<Object> work) {
        final AiJob job;
        try {
            job = jobs.submit(kind, work);
        } catch (RejectedExecutionException e) {
            return Response.status(429).entity(new Rejected("fila de análises cheia — tente novamente")).build();
        }
        return Response.accepted(job.view()).location(URI.create("/horus/ai/jobs/" + job.id())).build();
    }

    public record RcaJobRequest(String traceId, String promql, Integer logLimit) {
    }

    public record WindowJobRequest(String lookback, String from, String to) {
    }

    public record Rejected(String message) {
    }
}
