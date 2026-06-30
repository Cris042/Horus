package org.example.horus.alert;

import org.example.horus.alert.AlertModel.Alert;
import org.example.horus.alert.AlertModel.ChannelResult;

/**
 * Porta de **canal de alerta** (T-703, RF-H-013).
 *
 * <p>Cada implementação (log, webhook, e-mail) decide se está habilitada (config) e
 * como entregar o alerta. O {@link AlertService} faz fan-out a todos os canais
 * habilitados. Implementações são best-effort: nunca devem lançar.
 */
public interface AlertChannel {

    /** Nome curto e estável do canal (ex.: {@code log}, {@code webhook}, {@code email}). */
    String name();

    /** {@code true} se o canal está configurado/ativo. */
    boolean enabled();

    /** Entrega o alerta; retorna o resultado (sucesso/falha + detalhe). */
    ChannelResult dispatch(Alert alert);
}
