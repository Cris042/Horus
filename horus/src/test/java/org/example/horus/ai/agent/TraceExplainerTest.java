package org.example.horus.ai.agent;

import org.example.horus.ai.LlmEngine;
import org.example.horus.ai.ModelTier;
import org.example.horus.ai.context.PromptContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Testa o Trace Explainer (T-604) com um {@link LlmEngine} falso — usa a camada BALANCED. */
class TraceExplainerTest {

    static class FakeEngine implements LlmEngine {
        LlmRequest captured;

        @Override
        public LlmResponse complete(LlmRequest request) {
            this.captured = request;
            return new LlmResponse("Request passou por prontuario→payment; gargalo no payment.",
                    "claude-sonnet-4-6", true);
        }

        @Override
        public boolean isLive() {
            return true;
        }
    }

    @Test
    void explain_usesBalancedTier_andCarriesTrace() {
        FakeEngine engine = new FakeEngine();
        var ctx = new PromptContext("# Trace abc\n- prontuario GET (10µs)\n", 14, false, List.of("trace"));

        var exp = new TraceExplainer(engine).explain("abc", ctx);

        assertEquals(ModelTier.BALANCED, engine.captured.tier());
        assertTrue(engine.captured.prompt().contains("Trace abc"));
        assertEquals("abc", exp.traceId());
        assertEquals("claude-sonnet-4-6", exp.modelId());
        assertTrue(exp.live());
    }
}
