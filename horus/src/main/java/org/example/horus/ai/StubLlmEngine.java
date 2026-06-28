package org.example.horus.ai;

import io.quarkus.arc.properties.IfBuildProperty;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Motor de IA stub — ativo quando {@code horus.ai.enabled=false} (padrão).
 *
 * <p>Permite que o Horus suba e que a CI rode <b>sem</b> {@code ANTHROPIC_API_KEY}:
 * devolve um placeholder claro em vez de chamar a API. O adapter real
 * ({@link LangChain4jLlmEngine}) assume quando a IA é habilitada.
 */
@ApplicationScoped
@IfBuildProperty(name = "horus.ai.enabled", stringValue = "false", enableIfMissing = true)
public class StubLlmEngine implements LlmEngine {

    @Override
    public LlmResponse complete(LlmRequest request) {
        String msg = "IA não configurada (defina ANTHROPIC_API_KEY e horus.ai.enabled=true). "
                + "Pedido recebido para a camada " + request.tier() + " ("
                + request.tier().modelId() + ").";
        return new LlmResponse(msg, request.tier().modelId(), false);
    }

    @Override
    public boolean isLive() {
        return false;
    }
}
