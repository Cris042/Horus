package org.example.horus.ai.context;

import org.example.horus.query.QueryModel.LogLine;
import org.example.horus.query.QueryModel.SpanRef;
import org.example.horus.query.QueryModel.TraceResult;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

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
}
