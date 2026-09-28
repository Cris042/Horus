package org.example.horus.ai;

import com.anthropic.models.beta.messages.MessageCreateParams;
import org.example.horus.ai.LlmEngine.LlmRequest;
import org.example.horus.ai.LlmEngine.LlmResponse;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Montagem das requisições por camada e seleção de modo em runtime (T-1003) — sem rede.
 */
class AnthropicLlmEngineTest {

    private static final LlmRequest DEEP = new LlmRequest("Você é o Horus.", "Qual a causa raiz?", ModelTier.DEEP);

    @Test
    void eachTierRoutesToItsOwnModel() {
        for (ModelTier tier : ModelTier.values()) {
            MessageCreateParams params = AnthropicLlmEngine.toParams(new LlmRequest(null, "oi", tier));
            assertEquals(tier.modelId(), params.model().asString());
            assertEquals(AnthropicLlmEngine.MAX_TOKENS, params.maxTokens());
        }
    }

    @Test
    void systemPromptIsSentSeparately_notMergedIntoUserMessage() {
        MessageCreateParams params = AnthropicLlmEngine.toParams(DEEP);
        assertTrue(params.system().isPresent());
        assertEquals(1, params.messages().size());
        assertFalse(params.messages().get(0).content().toString().contains("Você é o Horus"));
    }

    @Test
    void effort_onlyWhereTheModelAcceptsIt() {
        assertTrue(AnthropicLlmEngine.toParams(new LlmRequest(null, "x", ModelTier.FAST)).outputConfig().isEmpty(),
                "Haiku 4.5 não aceita effort");
        assertEquals("medium", AnthropicLlmEngine.toParams(new LlmRequest(null, "x", ModelTier.BALANCED))
                .outputConfig().orElseThrow().effort().orElseThrow().asString());
        assertEquals("high", AnthropicLlmEngine.toParams(DEEP)
                .outputConfig().orElseThrow().effort().orElseThrow().asString());
    }

    @Test
    void deepTier_enablesServerSideFallbacks() {
        MessageCreateParams deep = AnthropicLlmEngine.toParams(DEEP);
        assertTrue(deep.fallbacks().isPresent());
        assertTrue(deep.betas().orElseThrow().stream().anyMatch(b -> b.asString().startsWith("server-side-fallback")));
        assertTrue(AnthropicLlmEngine.toParams(new LlmRequest(null, "x", ModelTier.FAST)).fallbacks().isEmpty());
    }

    @Test
    void noSamplingParameters_areEverSent() {
        // Modelos atuais rejeitam temperature/top_p/top_k com 400 — o motor antigo (LangChain4j) os enviava.
        MessageCreateParams params = AnthropicLlmEngine.toParams(DEEP);
        assertTrue(params.temperature().isEmpty());
        assertTrue(params.topP().isEmpty());
        assertTrue(params.topK().isEmpty());
    }

    @Test
    void stubMode_whenDisabledOrKeyMissingOrPlaceholder() {
        assertFalse(new AnthropicLlmEngine(false, Optional.of("sk-ant-real")).isLive());
        assertFalse(new AnthropicLlmEngine(true, Optional.empty()).isLive());
        assertFalse(new AnthropicLlmEngine(true, Optional.of("  ")).isLive());
        assertFalse(new AnthropicLlmEngine(true, Optional.of("dummy-key")).isLive());

        LlmResponse r = new AnthropicLlmEngine(false, Optional.empty()).complete(DEEP);
        assertFalse(r.live());
        assertEquals("claude-opus-5", r.modelId());
    }

    @Test
    void liveMode_whenEnabledWithKey() {
        AnthropicLlmEngine engine = new AnthropicLlmEngine(true, Optional.of("sk-ant-test"));
        try {
            assertTrue(engine.isLive());
        } finally {
            engine.close();
        }
    }
}
