package org.example.horus.ai.agent;

import org.example.horus.ai.LlmEngine;
import org.example.horus.ai.ModelTier;
import org.example.horus.ai.context.PromptContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Testa o RCA agent (T-605) com um {@link LlmEngine} falso — usa a camada DEEP. */
class RootCauseAnalystTest {

    static class FakeEngine implements LlmEngine {
        LlmRequest captured;

        @Override
        public LlmResponse complete(LlmRequest request) {
            this.captured = request;
            return new LlmResponse("CAUSA PROVÁVEL: timeout no payment. PRÓXIMOS PASSOS: ...",
                    "claude-opus-5", true);
        }

        @Override
        public boolean isLive() {
            return true;
        }
    }

    @Test
    void analyze_usesDeepTier_andCarriesThreeSignals() {
        FakeEngine engine = new FakeEngine();
        var ctx = new PromptContext("# Trace abc\n# Logs\n# Métricas\n", 20, false,
                List.of("trace", "logs", "metrics"));

        var rca = new RootCauseAnalyst(engine).analyze("abc", ctx);

        assertEquals(ModelTier.DEEP, engine.captured.tier());
        assertTrue(engine.captured.prompt().contains("trace abc"));
        assertEquals("claude-opus-5", rca.modelId());
        assertTrue(rca.signals().containsAll(List.of("trace", "logs", "metrics")));
        assertTrue(rca.live());
    }
}
