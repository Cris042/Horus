package org.example.horus.ai.context;

import org.example.horus.query.LogQueryPort;
import org.example.horus.query.MetricQueryPort;
import org.example.horus.query.QueryModel.TraceSearch;
import org.example.horus.query.QueryModel.TraceSummary;
import org.example.horus.query.TimeWindow;
import org.example.horus.query.TraceQueryPort;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Coleta best-effort dos sinais de uma janela (T-1001). */
class WindowContextCollectorTest {

    private static final TimeWindow WINDOW = TimeWindow.last(Duration.ofMinutes(15), Instant.parse("2026-09-28T12:00:00Z"));

    private final TraceQueryPort traces = mock(TraceQueryPort.class);
    private final LogQueryPort logs = mock(LogQueryPort.class);
    private final MetricQueryPort metrics = mock(MetricQueryPort.class);

    private WindowContextCollector collector() {
        WindowContextCollector c = new WindowContextCollector(traces, logs, metrics,
                new ContextAssembler(traces, logs, metrics, 4000));
        c.errorLogQl = "{x=\"y\"}";
        c.promQl = "up";
        c.traceLimit = 25;
        c.logLimit = 10;
        return c;
    }

    @Test
    void collect_queriesRecentAndErrorTracesInWindow() {
        when(traces.searchTraces(any())).thenReturn(List.of(new TraceSummary("t1", "svc", "op", 0, 10, 1,
                List.of("svc"), 0, null)));

        PromptContext ctx = collector().collect(WINDOW);

        ArgumentCaptor<TraceSearch> captor = ArgumentCaptor.forClass(TraceSearch.class);
        verify(traces, times(2)).searchTraces(captor.capture());
        assertFalse(captor.getAllValues().get(0).onlyErrors());
        assertTrue(captor.getAllValues().get(1).onlyErrors());
        assertEquals(WINDOW, captor.getAllValues().get(0).window());
        assertEquals(25, captor.getAllValues().get(0).limit());
        verify(logs).findInWindow(eq("{x=\"y\"}"), eq(WINDOW), anyInt());
        verify(metrics).instantQuery("up");
        assertTrue(ctx.includedSignals().contains("slowTraces"));
    }

    @Test
    void collect_backendFailure_isReportedNotThrown() {
        when(traces.searchTraces(any())).thenReturn(List.of());
        when(logs.findInWindow(any(), any(), anyInt())).thenThrow(new RuntimeException("loki fora"));

        PromptContext ctx = collector().collect(WINDOW);

        assertTrue(ctx.text().contains("Sinais indisponíveis nesta coleta: logs"));
    }
}
