package org.example.horus.ai;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.errors.AnthropicException;
import com.anthropic.models.beta.AnthropicBeta;
import com.anthropic.models.beta.messages.BetaFallbacksParam;
import com.anthropic.models.beta.messages.BetaMessage;
import com.anthropic.models.beta.messages.BetaOutputConfig;
import com.anthropic.models.beta.messages.BetaStopReason;
import com.anthropic.models.beta.messages.MessageCreateParams;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ServiceUnavailableException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Motor de IA do Horus sobre o <b>SDK oficial da Anthropic para Java</b> (ADR-0011, adendo T-1003).
 *
 * <p>Decide <b>em runtime</b> entre dois modos — o único bean {@link LlmEngine}, para que o
 * {@link CachingLlmEngine} (decorator) continue valendo nos dois:
 * <ul>
 *   <li><b>live</b>: {@code horus.ai.enabled=true} <em>e</em> {@code ANTHROPIC_API_KEY} presente —
 *       cada {@link LlmRequest} vai ao modelo da sua {@link ModelTier} (Haiku/Sonnet/Opus), com
 *       {@code system} separado do prompt e o {@code effort} da camada;</li>
 *   <li><b>stub</b> (padrão): placeholder claro, sem rede — app e CI rodam sem chave.</li>
 * </ul>
 *
 * <p>Antes (T-601) o adapter usava {@code quarkus-langchain4j-anthropic} 1.1.0, que envia sempre
 * {@code temperature}/{@code top_k} (rejeitados com 400 pelos modelos atuais), ignorava a camada
 * (um único modelo) e só era selecionável por flag de <em>build</em>.
 *
 * <p>Na camada {@link ModelTier#DEEP} (Claude Opus 5) liga os <em>fallbacks</em> do servidor
 * ({@code fallbacks: "default"}): se o classificador de segurança recusar, a API reencaminha a
 * requisição a outro modelo em vez de só parar.
 */
@ApplicationScoped
public class AnthropicLlmEngine implements LlmEngine {

    private static final Logger LOG = Logger.getLogger(AnthropicLlmEngine.class);

    /** Limite de saída por resposta (não-streaming: mantém a chamada abaixo dos timeouts HTTP). */
    static final long MAX_TOKENS = 16_000L;

    private final AnthropicClient client;

    public AnthropicLlmEngine(@ConfigProperty(name = "horus.ai.enabled", defaultValue = "false") boolean enabled,
                              @ConfigProperty(name = "ANTHROPIC_API_KEY") Optional<String> apiKey) {
        Optional<String> key = apiKey.map(String::trim).filter(k -> !k.isEmpty() && !"dummy-key".equals(k));
        if (enabled && key.isPresent()) {
            this.client = AnthropicOkHttpClient.builder().apiKey(key.get()).build();
            LOG.info("IA do Horus: modo live (SDK Anthropic)");
        } else {
            this.client = null;
            if (enabled) {
                LOG.warn("horus.ai.enabled=true, mas ANTHROPIC_API_KEY ausente — IA em modo stub");
            }
        }
    }

    @Override
    public LlmResponse complete(LlmRequest request) {
        if (client == null) {
            return stub(request);
        }
        BetaMessage message;
        try {
            message = client.beta().messages().create(toParams(request));
        } catch (AnthropicException e) {
            // Falha da IA nunca vira 500 genérico: o chamador recebe 503 (RNF-H-008 — a IA é
            // opcional e sua indisponibilidade é um estado esperado, não um bug).
            LOG.warnf("Chamada à API Anthropic falhou (%s): %s", request.tier(), e.getMessage());
            throw new ServiceUnavailableException("IA indisponível: " + e.getClass().getSimpleName());
        }
        String modelId = message.model().asString();
        if (message.stopReason().filter(BetaStopReason.REFUSAL::equals).isPresent()) {
            return new LlmResponse("A IA recusou esta solicitação (refusal).", modelId, true);
        }
        String text = message.content().stream()
                .flatMap(block -> block.text().stream())
                .map(t -> t.text())
                .collect(Collectors.joining());
        return new LlmResponse(text, modelId, true);
    }

    @Override
    public boolean isLive() {
        return client != null;
    }

    /** Monta a requisição de uma {@link LlmRequest} (visível para teste — sem rede). */
    static MessageCreateParams toParams(LlmRequest request) {
        ModelTier tier = request.tier();
        MessageCreateParams.Builder params = MessageCreateParams.builder()
                .model(tier.modelId())
                .maxTokens(MAX_TOKENS)
                .addUserMessage(request.prompt());
        if (request.system() != null && !request.system().isBlank()) {
            params.system(request.system());
        }
        if (tier.effort() != null) {
            params.outputConfig(BetaOutputConfig.builder()
                    .effort(BetaOutputConfig.Effort.of(tier.effort()))
                    .build());
        }
        if (tier == ModelTier.DEEP) {
            params.addBeta(AnthropicBeta.SERVER_SIDE_FALLBACK_2026_07_01)
                    .fallbacks(BetaFallbacksParam.ofDefault());
        }
        return params.build();
    }

    /** Placeholder sem chave: deixa claro que a IA não está configurada. */
    static LlmResponse stub(LlmRequest request) {
        String msg = "IA não configurada (defina ANTHROPIC_API_KEY e horus.ai.enabled=true). "
                + "Pedido recebido para a camada " + request.tier() + " ("
                + request.tier().modelId() + ").";
        return new LlmResponse(msg, request.tier().modelId(), false);
    }

    @PreDestroy
    void close() {
        if (client != null) {
            client.close();
        }
    }
}
