package org.example.horus.api;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.example.horus.ai.agent.NlQueryAgent;
import org.example.horus.ai.agent.NlQueryAgent.NlAnswer;
import org.example.horus.ai.context.ContextAssembler;
import org.example.horus.ai.context.PromptContext;
import org.example.horus.ai.context.WindowContextCollector;


/**
 * API do agente NL Query (T-607, RF-H-010) — "pergunte ao Horus".
 *
 * <p>O escopo da telemetria é recortado pelo chamador (trace + PromQL opcional); o agente
 * responde fundamentado nesse contexto. Planejamento autônomo de consultas é fatia seguinte.
 */
@Path("/horus/ai/ask")
@Produces(MediaType.APPLICATION_JSON)
public class HorusAskResource {

    private final ContextAssembler assembler;
    private final WindowContextCollector windows;
    private final NlQueryAgent agent;

    public HorusAskResource(ContextAssembler assembler, WindowContextCollector windows, NlQueryAgent agent) {
        this.assembler = assembler;
        this.windows = windows;
        this.agent = agent;
    }

    /** Responde a uma pergunta fundamentada no escopo informado (trace/PromQL). */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public NlAnswer ask(AskRequest body) {
        if (body == null || body.question() == null || body.question().isBlank()) {
            throw new BadRequestException("question obrigatória");
        }
        int logLimit = body.logLimit() == null ? 100 : body.logLimit();
        // Com traceId: escopo do incidente. Sem traceId: janela temporal (T-1001) — permite
        // perguntas como "quais as queries mais lentas na última hora?".
        PromptContext context = body.traceId() == null || body.traceId().isBlank()
                ? windows.collect(ApiWindows.resolve(body.lookback(), body.from(), body.to()))
                : assembler.assembleForIncident(body.traceId(), body.promql(), logLimit);
        return agent.answer(body.question(), context);
    }

    /**
     * Corpo do POST: pergunta + escopo opcional — trace/PromQL, ou janela temporal
     * ({@code lookback} relativo ou {@code from}/{@code to}; padrão última 1h) quando não há traceId.
     */
    public record AskRequest(String question, String traceId, String promql, Integer logLimit,
                             String lookback, String from, String to) {
    }
}
