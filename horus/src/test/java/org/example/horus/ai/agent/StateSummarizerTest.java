package org.example.horus.ai.agent;

import org.example.horus.ai.LlmEngine;
import org.example.horus.ai.ModelTier;
import org.example.horus.ai.context.PromptContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testa o agente Summarizer (T-603) com um {@link LlmEngine} falso — sem CDI nem backends.
 * Garante: usa a camada FAST, passa o contexto no prompt e propaga a proveniência.
 */
class StateSummarizerTest {

    /** Engine falso que captura o pedido e devolve um texto fixo. */
    static class FakeEngine implements LlmEngine {
        LlmRequest captured;

        @Override
        public LlmResponse complete(LlmRequest request) {
            this.captured = request;
            return new LlmResponse("Sistema saudável; sem erros relevantes.", "claude-haiku-4-5", true);
        }

        @Override
        public boolean isLive() {
            return true;
        }
    }

    @Test
    void summarize_usesFastTier_andCarriesContext() {
        FakeEngine engine = new FakeEngine();
        var ctx = new PromptContext("# Trace abc\n- svc op (10µs)\n", 12, false, List.of("trace"));

        var summary = new StateSummarizer(engine).summarize(ctx);

        assertEquals(ModelTier.FAST, engine.captured.tier());
        assertTrue(engine.captured.prompt().contains("# Trace abc"));
        assertEquals("claude-haiku-4-5", summary.modelId());
        assertTrue(summary.live());
        assertTrue(summary.signals().contains("trace"));
        assertFalse(summary.contextTruncated());
    }
}
