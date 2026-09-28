package org.example.horus.alert;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.MockMailbox;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import org.example.horus.alert.AlertModel.AlertRequest;
import org.example.horus.alert.AlertModel.AlertResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Canal de e-mail real (T-1008) — mailer em modo mock no teste, múltiplos destinatários. */
@QuarkusTest
@TestProfile(EmailAlertChannelTest.EmailOn.class)
class EmailAlertChannelTest {

    public static class EmailOn implements QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of("horus.alert.email.to", "sre@medrec.local, oncall@medrec.local");
        }
    }

    @Inject
    AlertService alerts;

    @Inject
    MockMailbox mailbox;

    @BeforeEach
    void clear() {
        mailbox.clear();
    }

    @Test
    void alert_isMailedToEveryRecipient_withTraceLink() {
        AlertResult result = alerts.raise(new AlertRequest("Traces com erro: 3", "critical", "abc123", "3 traces"));

        assertTrue(result.channels().stream().anyMatch(c -> c.channel().equals("email") && c.dispatched()));
        List<Mail> sre = mailbox.getMailsSentTo("sre@medrec.local");
        assertEquals(1, sre.size());
        assertEquals(1, mailbox.getMailsSentTo("oncall@medrec.local").size());
        assertTrue(sre.get(0).getSubject().contains("CRITICAL"));
        assertTrue(sre.get(0).getText().contains("horus-waterfall.html?trace=abc123"));
    }
}
