package org.example.horus.ai.agent;

import jakarta.enterprise.context.ApplicationScoped;
import org.example.horus.ai.LlmEngine;
import org.example.horus.ai.LlmEngine.LlmRequest;
import org.example.horus.ai.LlmEngine.LlmResponse;
import org.example.horus.ai.ModelTier;
import org.example.horus.ai.context.PromptContext;

/**
 * Agente <b>NL Query</b> — "pergunte ao Horus" (T-607, RF-H-010).
 *
 * <p>Responde a perguntas em linguagem natural <b>fundamentadas</b> na telemetria fornecida
 * (contexto montado pelo {@code ContextAssembler}, T-602). Esta fatia responde sobre um escopo
 * já recortado pelo chamador (trace/PromQL); o planejamento autônomo de consultas (a IA decidir
 * <em>quais</em> backends consultar via tool-calling) é a fatia seguinte.
 *
 * <p>Camada {@link ModelTier#BALANCED} (Sonnet) — equilíbrio qualidade/custo (ADR-0011).
 */
@ApplicationScoped
public class NlQueryAgent {

    private static final String SYSTEM = """
            Você é o Horus, observabilidade aumentada por IA de um sistema de prontuário médico.
            Responda à PERGUNTA do usuário usando APENAS a telemetria fornecida como contexto
            (traces, logs, métricas). Se o contexto não contiver a resposta, diga claramente que
            não há dados suficientes — não invente. Seja objetivo e cite o sinal que embasa a
            resposta. Não exponha PII. O resultado é assistência, não verdade absoluta.""";

    private final LlmEngine engine;

    public NlQueryAgent(LlmEngine engine) {
        this.engine = engine;
    }

    /** Responde a {@code question} fundamentada no {@code context}. */
    public NlAnswer answer(String question, PromptContext context) {
        String prompt = "Pergunta: " + question + "\n\nContexto"
                + (context.truncated() ? " (truncado)" : "") + ":\n" + context.text();
        LlmResponse resp = engine.complete(new LlmRequest(SYSTEM, prompt, ModelTier.BALANCED));
        return new NlAnswer(question, resp.text(), resp.modelId(), resp.live(),
                context.includedSignals());
    }

    /** Resposta + proveniência. */
    public record NlAnswer(String question, String answer, String modelId, boolean live,
                           java.util.List<String> signals) {
    }
}
