package org.example.horus.alert;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.example.horus.alert.AlertModel.Alert;
import org.example.horus.alert.AlertModel.ChannelResult;
import org.jboss.logging.Logger;

import java.util.Optional;

/**
 * Canal de alerta por **e-mail** (T-703, RF-H-013) — stub gated por config.
 *
 * <p>Habilitado quando {@code horus.alert.email.to} está definido. Nesta fatia o envio é
 * um stub que registra a intenção (mesma abordagem do {@code EmailSender} stub do worker);
 * a integração SMTP real ({@code quarkus-mailer}) fica para fatia seguinte.
 */
@ApplicationScoped
public class EmailAlertChannel implements AlertChannel {

    private static final Logger LOG = Logger.getLogger(EmailAlertChannel.class);

    @ConfigProperty(name = "horus.alert.email.to")
    Optional<String> recipient;

    @Override
    public String name() {
        return "email";
    }

    @Override
    public boolean enabled() {
        return recipient.filter(to -> !to.isBlank()).isPresent();
    }

    @Override
    public ChannelResult dispatch(Alert alert) {
        String to = recipient.orElseThrow();
        LOG.infof("[ALERTA→email %s] %s :: %s — %s",
                to, alert.severity(), alert.title(), alert.summary());
        return new ChannelResult(name(), true, "stub: enviaria para " + to);
    }
}
