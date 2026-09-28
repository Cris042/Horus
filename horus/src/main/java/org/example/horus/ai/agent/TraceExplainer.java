package org.example.horus.ai.agent;

import jakarta.enterprise.context.ApplicationScoped;
import org.example.horus.ai.LlmEngine;
import org.example.horus.ai.LlmEngine.LlmRequest;
import org.example.horus.ai.LlmEngine.LlmResponse;
import org.example.horus.ai.ModelTier;
import org.example.horus.ai.context.PromptContext;

/**
 * Agente <b>Trace Explainer</b> (T-604, RF-H-006): explica, em linguagem natural, o
 * caminho de uma request — quais serviços passou, onde gastou tempo, o que falhou.
 *
 * <p>Mesmo padrão do {@link StateSummarizer}: contexto do {@code ContextAssembler} (T-602)
 * + {@link LlmEngine} (T-601). Usa a camada {@link ModelTier#BALANCED} (Sonnet) — explicação
 * de trace pede equilíbrio qualidade/custo (ADR-0011).
 */
@ApplicationScoped
public class TraceExplainer {

    private static final String SYSTEM = """
            Você é o Horus, observabilidade aumentada por IA de um sistema de prontuário médico.
            Explique, em português claro, o CAMINHO da request descrita pela telemetria a seguir:
            por quais serviços passou (na ordem), onde o tempo foi gasto e se houve erro. Aponte o
            gargalo provável. Seja objetivo (poucas frases). Use apenas o que está no contexto —
            não invente spans, serviços nem PII. O resultado é assistência, não verdade absoluta.""";

    private final LlmEngine engine;

    public TraceExplainer(LlmEngine engine) {
        this.engine = engine;
    }

    /** Narrativa de um trace a partir de um contexto já montado. */
    public TraceExplanation explain(String traceId, PromptContext context) {
        String prompt = "Trace " + traceId + (context.truncated() ? " (contexto truncado)" : "")
                + ":\n" + context.text();
        LlmResponse resp = engine.complete(new LlmRequest(SYSTEM, prompt, ModelTier.BALANCED, "trace-explain"));
        return new TraceExplanation(traceId, resp.text(), resp.modelId(), resp.live(),
                context.truncated());
    }

    /** Resultado: explicação + proveniência. */
    public record TraceExplanation(String traceId, String explanation, String modelId,
                                   boolean live, boolean contextTruncated) {
    }
}
