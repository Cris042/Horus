package org.example.horus.ai.agent;

import jakarta.enterprise.context.ApplicationScoped;
import org.example.horus.ai.LlmEngine;
import org.example.horus.ai.LlmEngine.LlmRequest;
import org.example.horus.ai.LlmEngine.LlmResponse;
import org.example.horus.ai.ModelTier;
import org.example.horus.ai.context.PromptContext;

/**
 * Agente <b>Root-Cause Analyst</b> (T-605, RF-H-007): diante de um incidente, correlaciona
 * os <b>três</b> sinais (trace + logs + métricas) e propõe <b>causa provável</b> e
 * <b>próximos passos</b>.
 *
 * <p>Mesmo padrão dos demais agentes (contexto do T-602 + `LlmEngine` do T-601), mas na
 * camada {@link ModelTier#DEEP} (Opus) — RCA pede o raciocínio mais forte (ADR-0011).
 */
@ApplicationScoped
public class RootCauseAnalyst {

    private static final String SYSTEM = """
            Você é o Horus, observabilidade aumentada por IA de um sistema de prontuário médico.
            Faça a ANÁLISE DE CAUSA RAIZ (RCA) do incidente descrito pela telemetria a seguir,
            correlacionando trace, logs e métricas. Responda em português, de forma estruturada e
            objetiva: (1) CAUSA PROVÁVEL (uma hipótese principal, com a evidência que a sustenta);
            (2) PRÓXIMOS PASSOS (ações de verificação/mitigação, em ordem). Use apenas o que está
            no contexto — não invente sinais nem PII. Trate como hipótese assistiva, não veredito.""";

    private final LlmEngine engine;

    public RootCauseAnalyst(LlmEngine engine) {
        this.engine = engine;
    }

    /** RCA a partir de um contexto de incidente já montado (3 sinais). */
    public RootCauseAnalysis analyze(String traceId, PromptContext context) {
        String prompt = "Incidente (trace " + traceId + ")"
                + (context.truncated() ? " — contexto truncado" : "")
                + ", sinais: " + context.includedSignals() + "\n" + context.text();
        LlmResponse resp = engine.complete(new LlmRequest(SYSTEM, prompt, ModelTier.DEEP, "rca"));
        return new RootCauseAnalysis(traceId, resp.text(), resp.modelId(), resp.live(),
                context.includedSignals(), context.truncated());
    }

    /** Resultado da RCA + proveniência. */
    public record RootCauseAnalysis(String traceId, String analysis, String modelId, boolean live,
                                    java.util.List<String> signals, boolean contextTruncated) {
    }
}
