package org.example.horus.ai;

import org.example.horus.ai.LlmEngine.LlmRequest;
import org.example.horus.ai.LlmEngine.LlmResponse;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Fronteira obrigatória do LLM (T-1004): sanitização + trilha, sem depender do chamador. */
class AuditingLlmEngineTest {

    /** Motor falso que registra exatamente o que recebeu. */
    private static final class CapturingEngine implements LlmEngine {
        final List<LlmRequest> received = new ArrayList<>();
        boolean fail;

        @Override
        public LlmResponse complete(LlmRequest request) {
            received.add(request);
            if (fail) {
                throw new IllegalStateException("api fora");
            }
            return new LlmResponse("ok", "claude-haiku-4-5", true);
        }

        @Override
        public boolean isLive() {
            return true;
        }
    }

    private static AuditingLlmEngine engine(CapturingEngine delegate, LlmAuditTrail trail) {
        AuditingLlmEngine e = new AuditingLlmEngine();
        e.delegate = delegate;
        e.trail = trail;
        return e;
    }

    @Test
    void piiNeverReachesTheModel_evenIfTheCallerForgotToSanitize() {
        CapturingEngine delegate = new CapturingEngine();
        LlmAuditTrail trail = new LlmAuditTrail(10);
        engine(delegate, trail).complete(new LlmRequest("sistema de ana@example.com",
                "paciente CPF 123.456.789-09 cartão 4111111111111111", ModelTier.FAST, "new-agent"));

        LlmRequest sent = delegate.received.get(0);
        assertFalse(sent.system().contains("ana@example.com"));
        assertFalse(sent.prompt().contains("123.456.789-09"));
        assertFalse(sent.prompt().contains("4111111111111111"));
        assertEquals(ModelTier.FAST, sent.tier());
        assertEquals("new-agent", sent.purpose());

        var record = trail.recent(1).get(0);
        assertEquals(3, record.redactions());
        assertEquals("new-agent", record.purpose());
        assertEquals("ok", record.outcome());
        assertTrue(record.live());
        assertEquals(64, record.promptSha256().length());
        assertEquals(3, trail.stats().totalRedactions());
    }

    @Test
    void failures_areAuditedAndPropagated() {
        CapturingEngine delegate = new CapturingEngine();
        delegate.fail = true;
        LlmAuditTrail trail = new LlmAuditTrail(10);

        assertThrows(IllegalStateException.class,
                () -> engine(delegate, trail).complete(new LlmRequest(null, "x", ModelTier.DEEP, "rca")));
        assertEquals("error", trail.recent(1).get(0).outcome());
    }

    @Test
    void trail_isBoundedAndNewestFirst() {
        LlmAuditTrail trail = new LlmAuditTrail(2);
        AuditingLlmEngine e = engine(new CapturingEngine(), trail);
        for (String p : List.of("a", "b", "c")) {
            e.complete(new LlmRequest(null, p, ModelTier.FAST, p));
        }
        assertEquals(List.of("c", "b"), trail.recent(10).stream().map(LlmAuditTrail.AuditRecord::purpose).toList());
        assertEquals(3, trail.stats().totalRequests());
    }

    @Test
    void purposeDefaultsToAdhoc() {
        assertEquals("adhoc", new LlmRequest(null, "x", ModelTier.FAST).purpose());
    }
}
