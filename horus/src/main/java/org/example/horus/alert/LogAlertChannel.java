package org.example.horus.alert;

import jakarta.enterprise.context.ApplicationScoped;
import org.example.horus.alert.AlertModel.Alert;
import org.example.horus.alert.AlertModel.ChannelResult;
import org.jboss.logging.Logger;

/**
 * Canal de alerta padrão: registra o alerta no log (T-703, RF-H-013).
 *
 * <p>Sempre habilitado — é o destino seguro que garante que todo alerta deixa rastro
 * mesmo sem webhook/e-mail configurados (espelha o stub de e-mail do worker Rust).
 */
@ApplicationScoped
public class LogAlertChannel implements AlertChannel {

    private static final Logger LOG = Logger.getLogger(LogAlertChannel.class);

    @Override
    public String name() {
        return "log";
    }

    @Override
    public boolean enabled() {
        return true;
    }

    @Override
    public ChannelResult dispatch(Alert alert) {
        LOG.warnf("[ALERTA %s] %s (trace=%s)%n%s",
                alert.severity(), alert.title(), alert.traceId(), alert.summary());
        return new ChannelResult(name(), true, "registrado no log");
    }
}
