package org.example.horus.alert;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.example.horus.alert.AlertModel.Alert;
import org.example.horus.alert.AlertModel.ChannelResult;
import org.jboss.logging.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Canal de alerta por <b>e-mail</b> (T-703, RF-H-013; envio real via {@code quarkus-mailer} em
 * T-1008).
 *
 * <p>Habilitado quando {@code horus.alert.email.to} está definido (lista separada por vírgula).
 * O servidor SMTP vem de {@code quarkus.mailer.*} ({@code QUARKUS_MAILER_HOST}, {@code _PORT},
 * {@code _USERNAME}, {@code _PASSWORD}, {@code _TLS}); em dev/test o mailer é mock (não envia).
 * Best-effort: falha de SMTP vira {@code dispatched=false}, nunca exceção.
 */
@ApplicationScoped
public class EmailAlertChannel implements AlertChannel {

    private static final Logger LOG = Logger.getLogger(EmailAlertChannel.class);

    private final Mailer mailer;

    @ConfigProperty(name = "horus.alert.email.to")
    Optional<String> recipients;

    public EmailAlertChannel(Mailer mailer) {
        this.mailer = mailer;
    }

    @Override
    public String name() {
        return "email";
    }

    @Override
    public boolean enabled() {
        return !to().isEmpty();
    }

    @Override
    public ChannelResult dispatch(Alert alert) {
        List<String> to = to();
        String subject = "[Horus " + alert.severity() + "] " + alert.title();
        StringBuilder body = new StringBuilder()
                .append(alert.summary()).append("\n\n")
                .append("Severidade: ").append(alert.severity()).append('\n');
        if (alert.traceId() != null && !alert.traceId().isBlank()) {
            body.append("Trace: ").append(alert.traceId())
                    .append(" (painel → /horus-waterfall.html?trace=").append(alert.traceId()).append(")\n");
        }
        body.append("Resumo gerado por IA (").append(alert.modelId()).append(alert.live() ? "" : ", modo stub")
                .append(") — trate como assistência, não verdade absoluta.\n");
        try {
            Mail mail = Mail.withText(to.get(0), subject, body.toString());
            to.stream().skip(1).forEach(mail::addTo);
            mailer.send(mail);
            return new ChannelResult(name(), true, "enviado para " + String.join(", ", to));
        } catch (RuntimeException e) {
            LOG.warnf("Falha ao enviar alerta por e-mail para %s: %s", to, e.getMessage());
            return new ChannelResult(name(), false, "falha SMTP: " + e.getMessage());
        }
    }

    private List<String> to() {
        return recipients.map(r -> Arrays.stream(r.split(",")).map(String::trim).filter(s -> !s.isBlank()).toList())
                .orElse(List.of());
    }
}
