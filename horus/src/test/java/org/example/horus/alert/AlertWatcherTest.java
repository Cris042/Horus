package org.example.horus.alert;

import org.example.horus.ai.anomaly.AnomalyDetector;
import org.example.horus.ai.anomaly.AnomalyModel.Anomaly;
import org.example.horus.ai.anomaly.AnomalyModel.AnomalyReport;
import org.example.horus.ai.anomaly.AnomalyModel.Comparison;
import org.example.horus.alert.AlertModel.AlertRequest;
import org.example.horus.query.QueryModel.TraceSummary;
import org.example.horus.query.TraceQueryPort;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Disparo automático + deduplicação (T-1008), sem backends. */
class AlertWatcherTest {

    private static final Instant T0 = Instant.parse("2026-09-28T12:00:00Z");

    private final AnomalyDetector detector = mock(AnomalyDetector.class);
    private final TraceQueryPort traces = mock(TraceQueryPort.class);
    private final AlertService alerts = mock(AlertService.class);

    private static AlertWatchConfig config(int errorTraces, Map<String, AlertWatchConfig.Rule> rules) {
        return new AlertWatchConfig() {
            public String interval() { return "1m"; }
            public String lookback() { return "15m"; }
            public int errorTraces() { return errorTraces; }
            public Duration dedupWindow() { return Duration.ofMinutes(30); }
            public Map<String, Rule> rules() { return rules; }
        };
    }

    private static AlertWatchConfig.Rule rule(double threshold) {
        return new AlertWatchConfig.Rule() {
            public String promql() { return "rate(errors[5m])"; }
            public Comparison comparison() { return Comparison.GT; }
            public double threshold() { return threshold; }
            public String severity() { return "critical"; }
        };
    }

    private static TraceSummary errorTrace(String id) {
        return new TraceSummary(id, "saga-orchestrator", "POST /sagas/pagar-e-emitir", 0, 10, 5,
                List.of("saga-orchestrator", "invoice-service"), 1, null);
    }

    @Test
    void ruleBreach_raisesAlert_thenDedupsWithinWindow() {
        when(detector.detect(any())).thenReturn(new AnomalyReport(1, 1,
                List.of(new Anomaly("erros", "critical", Comparison.GT, 0.05, 0.2, Map.of("service", "payment")))));
        AlertWatcher watcher = new AlertWatcher(config(0, Map.of("erros", rule(0.05))), detector, traces, alerts);

        assertEquals(1, watcher.tick(T0));
        assertEquals(0, watcher.tick(T0.plus(Duration.ofMinutes(10))));   // dentro da janela
        assertEquals(1, watcher.tick(T0.plus(Duration.ofMinutes(31))));   // janela expirou

        ArgumentCaptor<AlertRequest> captor = ArgumentCaptor.forClass(AlertRequest.class);
        verify(alerts, times(2)).raise(captor.capture());
        assertEquals("Anomalia: erros", captor.getValue().title());
        assertEquals("critical", captor.getValue().severity());
        assertTrue(captor.getValue().details().contains("0.2"));
    }

    @Test
    void errorTraces_aboveThreshold_alertWithLatestTraceId() {
        when(traces.searchTraces(any())).thenReturn(List.of(errorTrace("t-new"), errorTrace("t-old")));
        AlertWatcher watcher = new AlertWatcher(config(2, Map.of()), detector, traces, alerts);

        assertEquals(1, watcher.tick(T0));

        ArgumentCaptor<AlertRequest> captor = ArgumentCaptor.forClass(AlertRequest.class);
        verify(alerts).raise(captor.capture());
        assertEquals("t-new", captor.getValue().traceId());
        assertTrue(captor.getValue().details().contains("invoice-service"));
        verify(detector, never()).detect(any());   // sem regras configuradas
    }

    @Test
    void errorTraces_belowThreshold_noAlert() {
        when(traces.searchTraces(any())).thenReturn(List.of(errorTrace("t1")));
        assertEquals(0, new AlertWatcher(config(2, Map.of()), detector, traces, alerts).tick(T0));
        verify(alerts, never()).raise(any());
    }

    @Test
    void backendFailure_isContained() {
        when(detector.detect(any())).thenThrow(new RuntimeException("prometheus fora"));
        when(traces.searchTraces(any())).thenReturn(List.of(errorTrace("t1"), errorTrace("t2")));
        AlertWatcher watcher = new AlertWatcher(config(2, Map.of("erros", rule(0.05))), detector, traces, alerts);

        assertEquals(1, watcher.tick(T0));   // a checagem de traces segue mesmo com o Prometheus fora
    }
}
