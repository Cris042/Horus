package org.example.horus.ai.agent;

import org.example.horus.ai.LlmEngine;
import org.example.horus.ai.ModelTier;
import org.example.horus.ai.context.PromptContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Testa o NL Query agent (T-607) com um {@link LlmEngine} falso — usa a camada BALANCED. */
class NlQueryAgentTest {

    static class FakeEngine implements LlmEngine {
        LlmRequest captured;

        @Override
        public LlmResponse complete(LlmRequest request) {
            this.captured = request;
            return new LlmResponse("A request mais lenta passou pelo payment.", "claude-sonnet-5", true);
        }

        @Override
        public boolean isLive() {
            return true;
        }
    }

    @Test
    void answer_usesBalancedTier_andCarriesQuestionAndContext() {
        FakeEngine engine = new FakeEngine();
        var ctx = new PromptContext("# Trace abc\n- payment POST (9000µs)\n", 14, false, List.of("trace"));

        var ans = new NlQueryAgent(engine).answer("Qual o serviço mais lento?", ctx);

        assertEquals(ModelTier.BALANCED, engine.captured.tier());
        assertTrue(engine.captured.prompt().contains("Qual o serviço mais lento?"));
        assertTrue(engine.captured.prompt().contains("payment POST"));
        assertEquals("claude-sonnet-5", ans.modelId());
        assertTrue(ans.live());
    }
}
