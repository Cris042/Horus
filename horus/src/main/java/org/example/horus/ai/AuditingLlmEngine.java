package org.example.horus.ai;

import jakarta.annotation.Priority;
import jakarta.decorator.Decorator;
import jakarta.decorator.Delegate;
import jakarta.inject.Inject;
import org.example.horus.ai.context.PromptSanitizer;
import org.example.horus.ai.context.TokenBudget;
import org.jboss.logging.Logger;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;

/**
 * Fronteira <b>obrigatória</b> do LLM (T-1004, RNF-H-002/006): todo pedido — de qualquer agente,
 * atual ou futuro — é sanitizado pelo {@link PromptSanitizer} e registrado na
 * {@link LlmAuditTrail} antes de seguir.
 *
 * <p>Fecha o risco aceito na auditoria T-904 ("um agente novo pode esquecer o sanitizer"): como
 * decorator CDI da porta {@link LlmEngine}, não depende do chamador. Prioridade menor que a do
 * {@link CachingLlmEngine} ⇒ é o decorator <em>externo</em>: sanitiza antes do cache (a chave do
 * cache nunca contém PII) e audita inclusive os acertos de cache.
 */
@Decorator
@Priority(jakarta.interceptor.Interceptor.Priority.APPLICATION - 10)
public class AuditingLlmEngine implements LlmEngine {

    private static final Logger AUDIT = Logger.getLogger("horus.ai.audit");

    @Inject
    @Delegate
    LlmEngine delegate;

    @Inject
    LlmAuditTrail trail;

    @Override
    public LlmResponse complete(LlmRequest request) {
        PromptSanitizer.Sanitized system = PromptSanitizer.sanitizeCounting(request.system());
        PromptSanitizer.Sanitized prompt = PromptSanitizer.sanitizeCounting(request.prompt());
        LlmRequest safe = new LlmRequest(system.text(), prompt.text(), request.tier(), request.purpose());

        String payload = (safe.system() == null ? "" : safe.system()) + "\n" + safe.prompt();
        int redactions = system.redactions() + prompt.redactions();
        long start = System.nanoTime();
        LlmResponse response = null;
        try {
            response = delegate.complete(safe);
            return response;
        } finally {
            long latency = (System.nanoTime() - start) / 1_000_000;
            var record = new LlmAuditTrail.AuditRecord(Instant.now(), safe.purpose(), safe.tier(),
                    safe.tier().modelId(), response == null ? null : response.modelId(),
                    response != null && response.live(), payload.length(), TokenBudget.estimateTokens(payload),
                    sha256(payload), redactions, latency, response == null ? "error" : "ok");
            trail.record(record);
            AUDIT.infof("llm_request purpose=%s tier=%s model=%s live=%s chars=%d tokens~%d sha256=%s redactions=%d latency_ms=%d outcome=%s",
                    record.purpose(), record.tier(), record.answeredModel() == null ? record.requestedModel() : record.answeredModel(),
                    record.live(), record.promptChars(), record.estimatedTokens(), record.promptSha256(),
                    record.redactions(), record.latencyMillis(), record.outcome());
        }
    }

    @Override
    public boolean isLive() {
        return delegate.isLive();
    }

    static String sha256(String text) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(text.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
