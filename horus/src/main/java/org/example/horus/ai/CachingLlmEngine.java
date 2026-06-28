package org.example.horus.ai;

import jakarta.annotation.Priority;
import jakarta.decorator.Decorator;
import jakarta.decorator.Delegate;
import jakarta.inject.Inject;

import java.util.Optional;

/**
 * Decorator de cache sobre a porta {@link LlmEngine} (T-608, RNF-H-003/004).
 *
 * <p>CDI decorator (auto-habilitado no ArC): envolve transparentemente o engine ativo
 * (stub ou LangChain4j) e serve respostas idênticas a partir do {@link LlmResponseCache},
 * sem que nenhum chamador precise saber do cache. Reduz custo e latência (ADR-0011).
 */
@Decorator
@Priority(jakarta.interceptor.Interceptor.Priority.APPLICATION)
public abstract class CachingLlmEngine implements LlmEngine {

    @Inject
    @Delegate
    LlmEngine delegate;

    @Inject
    LlmResponseCache cache;

    @Override
    public LlmResponse complete(LlmRequest request) {
        String key = cache.keyFor(request);
        Optional<LlmResponse> cached = cache.get(key);
        if (cached.isPresent()) {
            return cached.get();
        }
        LlmResponse fresh = delegate.complete(request);
        cache.put(key, fresh);
        return fresh;
    }
}
