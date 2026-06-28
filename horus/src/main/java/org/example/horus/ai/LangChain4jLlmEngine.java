package org.example.horus.ai;

import dev.langchain4j.model.chat.ChatModel;
import io.quarkus.arc.properties.IfBuildProperty;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Adapter real do {@link LlmEngine} sobre Quarkus LangChain4j (Anthropic) — ADR-0011.
 *
 * <p>Ativo apenas quando {@code horus.ai.enabled=true}; do contrário vale o
 * {@link StubLlmEngine}. Mantém o LangChain4j confinado a esta classe — o resto do
 * Horus enxerga só a porta {@link LlmEngine}.
 *
 * <p>Esta fatia usa o <em>chat model</em> default configurado pela extensão
 * ({@code quarkus.langchain4j.anthropic.chat-model.model-name}). O roteamento por
 * {@link ModelTier} com modelos nomeados (Haiku/Sonnet/Opus simultâneos) é a fatia seguinte.
 */
@ApplicationScoped
@IfBuildProperty(name = "horus.ai.enabled", stringValue = "true")
public class LangChain4jLlmEngine implements LlmEngine {

    private final ChatModel chatModel;

    @ConfigProperty(name = "quarkus.langchain4j.anthropic.chat-model.model-name",
            defaultValue = "claude-opus-4-8")
    String configuredModelId;

    public LangChain4jLlmEngine(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public LlmResponse complete(LlmRequest request) {
        String prompt = request.system() == null || request.system().isBlank()
                ? request.prompt()
                : request.system() + "\n\n" + request.prompt();
        String answer = chatModel.chat(prompt);
        return new LlmResponse(answer, configuredModelId, true);
    }

    @Override
    public boolean isLive() {
        return true;
    }
}
