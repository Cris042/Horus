package org.example.horus.ai.agent;

import jakarta.enterprise.context.ApplicationScoped;
import org.example.horus.ai.LlmEngine;
import org.example.horus.ai.LlmEngine.LlmRequest;
import org.example.horus.ai.LlmEngine.LlmResponse;
import org.example.horus.ai.ModelTier;
import org.example.horus.ai.context.PromptContext;

/**
 * Agente <b>Summarizer</b> (T-603, RF-H-005): resume, em linguagem natural, o estado da
 * aplicação observada a partir de um contexto de telemetria já montado e sanitizado.
 *
 * <p>Combina as duas fatias anteriores: o {@link PromptContext} (montado pelo
 * {@code ContextAssembler}, T-602) vira o conteúdo, e o {@link LlmEngine} (T-601, porta
 * desacoplada) produz o resumo. Usa a camada {@link ModelTier#FAST} (Haiku) — resumo é
 * tarefa de alto volume / baixo custo (ADR-0011).
 */
@ApplicationScoped
public class StateSummarizer {

    private static final String SYSTEM = """
            Você é o Horus, observabilidade aumentada por IA de um sistema de prontuário médico.
            Resuma, em português claro e objetivo, o ESTADO do sistema a partir da telemetria
            fornecida (traces, logs, métricas). Destaque erros e latências anômalas. Seja conciso
            (no máximo um parágrafo curto). Os dados já estão sanitizados — não invente PII nem
            valores que não estejam no contexto. Trate o resultado como assistência, não verdade
            absoluta.""";

    private final LlmEngine engine;

    public StateSummarizer(LlmEngine engine) {
        this.engine = engine;
    }

    /** Resumo de estado a partir de um contexto já montado. */
    public StateSummary summarize(PromptContext context) {
        String prompt = "Telemetria (contexto" + (context.truncated() ? ", truncado" : "")
                + ", ~" + context.estimatedTokens() + " tokens):\n" + context.text();
        LlmResponse resp = engine.complete(new LlmRequest(SYSTEM, prompt, ModelTier.FAST));
        return new StateSummary(resp.text(), resp.modelId(), resp.live(),
                context.includedSignals(), context.truncated());
    }

    /** Resultado do resumo: texto + proveniência (modelo, se IA real, sinais, truncamento). */
    public record StateSummary(String summary, String modelId, boolean live,
                               java.util.List<String> signals, boolean contextTruncated) {
    }
}
