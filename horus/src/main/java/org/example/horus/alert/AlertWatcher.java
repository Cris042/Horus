package org.example.horus.alert;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import org.example.horus.ai.anomaly.AnomalyDetector;
import org.example.horus.ai.anomaly.AnomalyModel.Anomaly;
import org.example.horus.ai.anomaly.AnomalyModel.AnomalyRule;
import org.example.horus.alert.AlertModel.AlertRequest;
import org.example.horus.query.QueryModel.TraceSearch;
import org.example.horus.query.QueryModel.TraceSummary;
import org.example.horus.query.TimeWindow;
import org.example.horus.query.TraceQueryPort;
import org.jboss.logging.Logger;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Disparo <b>automático</b> de alertas (T-1008, RF-H-008/013). A cada {@code interval}:
 * <ol>
 *   <li>avalia as regras de limiar ({@link AnomalyDetector}) — cada série violada vira um alerta;</li>
 *   <li>conta os traces com erro na janela ({@code lookback}, via busca da T-1001) — acima de
 *       {@code error-traces} vira um alerta com o {@code traceId} mais recente (ponto de partida da RCA).</li>
 * </ol>
 * O mesmo alerta (regra + rótulos) não se repete dentro de {@code dedup-window}. Best-effort:
 * uma falha de backend é registrada e o ciclo segue — nunca derruba o Horus.
 */
@ApplicationScoped
public class AlertWatcher {

    private static final Logger LOG = Logger.getLogger(AlertWatcher.class);

    private final AlertWatchConfig config;
    private final AnomalyDetector detector;
    private final TraceQueryPort traces;
    private final AlertService alerts;
    private final Map<String, Instant> lastSent = new ConcurrentHashMap<>();

    public AlertWatcher(AlertWatchConfig config, AnomalyDetector detector, TraceQueryPort traces,
                        AlertService alerts) {
        this.config = config;
        this.detector = detector;
        this.traces = traces;
        this.alerts = alerts;
    }

    @Scheduled(every = "{horus.alert.watch.interval:off}", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    void scheduled() {
        tick(Instant.now());
    }

    /** Um ciclo de checagem; devolve quantos alertas foram disparados (visível para teste). */
    int tick(Instant now) {
        int raised = 0;
        try {
            raised += checkRules(now);
        } catch (RuntimeException e) {
            LOG.warnf("Checagem de regras de alerta falhou: %s", e.getMessage());
        }
        try {
            raised += checkErrorTraces(now);
        } catch (RuntimeException e) {
            LOG.warnf("Checagem de traces com erro falhou: %s", e.getMessage());
        }
        return raised;
    }

    private int checkRules(Instant now) {
        if (config.rules().isEmpty()) {
            return 0;
        }
        List<AnomalyRule> rules = config.rules().entrySet().stream()
                .map(e -> new AnomalyRule(e.getKey(), e.getValue().promql(), e.getValue().comparison(),
                        e.getValue().threshold(), e.getValue().severity()))
                .toList();
        int raised = 0;
        for (Anomaly a : detector.detect(rules).anomalies()) {
            String key = "rule:" + a.rule() + ":" + new TreeMap<>(a.labels());
            if (shouldSend(key, now)) {
                alerts.raise(new AlertRequest("Anomalia: " + a.rule(), a.severity(), null,
                        "Regra '" + a.rule() + "': valor " + a.value() + " " + (a.comparison() == null ? "GT" : a.comparison())
                                + " limiar " + a.threshold() + " — série " + a.labels()));
                raised++;
            }
        }
        return raised;
    }

    private int checkErrorTraces(Instant now) {
        int threshold = config.errorTraces();
        if (threshold <= 0) {
            return 0;
        }
        TimeWindow window = TimeWindow.last(TimeWindow.parseLookback(config.lookback()), now);
        List<TraceSummary> errors = traces.searchTraces(new TraceSearch(null, null, window, 0, true, 200));
        if (errors.size() < threshold || !shouldSend("error-traces", now)) {
            return 0;
        }
        TraceSummary latest = errors.get(0);
        String services = String.join(", ", errors.stream().flatMap(t -> t.services().stream()).distinct().sorted().toList());
        alerts.raise(new AlertRequest("Traces com erro: " + errors.size() + " em " + config.lookback(), "critical",
                latest.traceId(),
                errors.size() + " traces com erro na janela (limiar " + threshold + "). Serviços envolvidos: " + services
                        + ". Mais recente: " + latest.rootService() + " " + latest.rootOperation()
                        + " (" + latest.errorSpanCount() + " spans em erro)."));
        return 1;
    }

    /** Deduplicação: {@code true} (e registra) se o alerta não foi enviado dentro da janela. */
    private boolean shouldSend(String key, Instant now) {
        Instant previous = lastSent.get(key);
        if (previous != null && previous.plus(config.dedupWindow()).isAfter(now)) {
            return false;
        }
        lastSent.put(key, now);
        return true;
    }
}
