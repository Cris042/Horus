package org.example.horus.ai.context;

import org.example.horus.query.QueryModel.LogLine;
import org.example.horus.query.QueryModel.MetricSample;
import org.example.horus.query.QueryModel.SlowQuery;
import org.example.horus.query.QueryModel.TraceSummary;
import org.example.horus.query.TimeWindow;

import java.time.Duration;
import java.time.Instant;
import org.example.horus.query.QueryModel.SpanRef;
import org.example.horus.query.QueryModel.TraceResult;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testa a lógica de montagem (orçamento + sanitização) do {@link ContextAssembler}
 * via o overload {@code assemble(...)}, sem backends nem CDI.
 */
class ContextAssemblerTest {

    private static ContextAssembler assembler(int maxTokens) {
        return new ContextAssembler(null, null, null, maxTokens);
    }

    private static TraceResult trace() {
        return new TraceResult("abc123", 2, List.of(
                new SpanRef("s1", "GET /prontuarios/{id}", "prontuario-service", 4200),
                new SpanRef("s2", "SELECT prontuario", "prontuario-service", 1200)));
    }

    @Test
    void assembles_traceAndLogs_withinBudget() {
        var ctx = assembler(4000).assemble(trace(),
                List.of(new LogLine("1700000000000000000", "consulta registrada", Map.of())),
                List.of());

        assertTrue(ctx.text().contains("# Trace abc123"));
        assertTrue(ctx.text().contains("prontuario-service"));
        assertTrue(ctx.text().contains("# Logs"));
        assertTrue(ctx.includedSignals().contains("trace"));
        assertTrue(ctx.includedSignals().contains("logs"));
        assertFalse(ctx.truncated());
        assertTrue(ctx.estimatedTokens() <= 4000);
    }

    @Test
    void truncates_whenBudgetTooSmall() {
        // Orçamento minúsculo: cabe o header do trace, mas não todos os spans.
        var ctx = assembler(8).assemble(trace(), List.of(), List.of());
        assertTrue(ctx.truncated());
        assertTrue(ctx.estimatedTokens() <= 8);
    }

    @Test
    void sanitizes_piiAtPromptBoundary() {
        var ctx = assembler(4000).assemble(null,
                List.of(new LogLine("1", "login email=joao@hospital.com cpf=123.456.789-00", Map.of())),
                List.of());

        assertFalse(ctx.text().contains("joao@hospital.com"));
        assertFalse(ctx.text().contains("123.456.789-00"));
        assertTrue(ctx.text().contains("***@***"));
    }

    private static final TimeWindow WINDOW = TimeWindow.last(Duration.ofHours(1), Instant.parse("2026-09-28T12:00:00Z"));

    private static TraceSummary summary(String id, long durationMicros, int errors, SlowQuery q) {
        return new TraceSummary(id, "prontuario-service", "GET /prontuarios/{id}", 0, durationMicros, 2,
                List.of("prontuario-service"), errors, q);
    }

    @Test
    void window_includesOverviewErrorsSlowQueriesAndUnavailable() {
        SlowQuery slow = new SlowQuery("payment-service", "payment_db", "SELECT",
                "select * from carteira where email = 'ana@example.com'", 90_000);
        var ctx = assembler(4000).assembleForWindow(WINDOW,
                List.of(summary("t1", 10_000, 0, null), summary("t2", 200_000, 0, slow)),
                List.of(summary("t3", 50_000, 2, null)),
                List.of(new LogLine("1", "falha ao debitar", Map.of())),
                List.of(new MetricSample(Map.of("job", "payment"), 1.0, 0)),
                List.of("metrics"));

        assertEquals(List.of("window", "errorTraces", "slowQueries", "slowTraces", "logs", "metrics"),
                ctx.includedSignals());
        assertTrue(ctx.text().contains("Traces amostrados: 2 · com erro: 1"));
        assertTrue(ctx.text().contains("p95=200000"));
        assertTrue(ctx.text().contains("t3"));
        assertTrue(ctx.text().contains("payment_db SELECT (90000µs)"));
        assertTrue(ctx.text().contains("Sinais indisponíveis nesta coleta: metrics"));
        assertFalse(ctx.text().contains("ana@example.com"), "PII deve ser sanitizada");
        assertFalse(ctx.truncated());
    }

    @Test
    void window_emptySignals_onlyOverview() {
        var ctx = assembler(4000).assembleForWindow(WINDOW, List.of(), List.of(), List.of(), List.of(), List.of());
        assertEquals(List.of("window"), ctx.includedSignals());
        assertTrue(ctx.text().contains("Traces amostrados: 0"));
    }

    @Test
    void window_tightBudget_truncates() {
        var many = java.util.stream.IntStream.range(0, 200)
                .mapToObj(i -> summary("trace-" + i, i, 1, null)).toList();
        var ctx = assembler(80).assembleForWindow(WINDOW, many, many, List.of(), List.of(), List.of());
        assertTrue(ctx.truncated());
        assertTrue(ctx.estimatedTokens() <= 90);
    }
}
