package org.example.horus.alert;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import org.example.horus.ai.anomaly.AnomalyModel.Comparison;

import java.time.Duration;
import java.util.Map;

/**
 * Configuração do disparo <b>automático</b> de alertas (T-1008, RF-H-008/013).
 *
 * <pre>
 * horus.alert.watch.interval=1m            # "off" desliga (padrão)
 * horus.alert.watch.lookback=15m           # janela da checagem de traces com erro
 * horus.alert.watch.error-traces=5         # alerta se houver >= N traces com erro (0 = desliga)
 * horus.alert.watch.dedup-window=30m       # não repete o mesmo alerta dentro da janela
 * horus.alert.watch.rules.latencia-p95.promql=histogram_quantile(0.95, ...)
 * horus.alert.watch.rules.latencia-p95.threshold=0.5
 * horus.alert.watch.rules.latencia-p95.comparison=GT
 * horus.alert.watch.rules.latencia-p95.severity=critical
 * </pre>
 */
@ConfigMapping(prefix = "horus.alert.watch")
public interface AlertWatchConfig {

    /** Intervalo do disparo agendado ({@code off} desliga). */
    @WithDefault("off")
    String interval();

    @WithDefault("15m")
    String lookback();

    /** Mínimo de traces com erro na janela para alertar; {@code 0} desliga essa checagem. */
    @WithDefault("0")
    int errorTraces();

    @WithDefault("30m")
    Duration dedupWindow();

    /** Regras de limiar sobre métricas, por nome. */
    Map<String, Rule> rules();

    interface Rule {
        String promql();

        @WithDefault("GT")
        Comparison comparison();

        double threshold();

        @WithDefault("warning")
        String severity();
    }
}
