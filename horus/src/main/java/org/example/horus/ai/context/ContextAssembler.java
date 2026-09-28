package org.example.horus.ai.context;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.example.horus.query.LogQueryPort;
import org.example.horus.query.MetricQueryPort;
import org.example.horus.query.QueryModel.LogLine;
import org.example.horus.query.QueryModel.MetricSample;
import org.example.horus.query.QueryModel.SlowQuery;
import org.example.horus.query.QueryModel.SpanRef;
import org.example.horus.query.QueryModel.TraceResult;
import org.example.horus.query.QueryModel.TraceSummary;
import org.example.horus.query.TimeWindow;
import org.example.horus.query.TraceQueryPort;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Montador de contexto da IA (T-602, RNF-H-003/006).
 *
 * <p>Transforma telemetria (trace + logs + métricas, vinda das portas do T-501) num
 * **prompt compacto, sanitizado e dentro de um orçamento de tokens**. É o insumo dos
 * agentes da Fase 6 (Summarizer, Trace Explainer, RCA): eles decidem *o que perguntar*,
 * este componente decide *o que cabe e é seguro mandar*.
 *
 * <p>Ordem de prioridade ao caber no orçamento: trace (estrutura) → logs (erros) →
 * métricas. Cada seção é truncada por linha quando o orçamento aperta.
 */
@ApplicationScoped
public class ContextAssembler {

    private final TraceQueryPort traces;
    private final LogQueryPort logs;
    private final MetricQueryPort metrics;
    private final TokenBudget budget;

    public ContextAssembler(TraceQueryPort traces, LogQueryPort logs, MetricQueryPort metrics,
                            @ConfigProperty(name = "horus.ai.context.max-tokens",
                                    defaultValue = "4000") int maxTokens) {
        this.traces = traces;
        this.logs = logs;
        this.metrics = metrics;
        this.budget = new TokenBudget(maxTokens);
    }

    /** Monta o contexto de um {@code traceId}: trace + logs correlacionados. */
    public PromptContext assembleForTrace(String traceId, int logLimit) {
        Optional<TraceResult> trace = traces.findTrace(traceId);
        List<LogLine> traceLogs = logs.findByTraceId(traceId, logLimit);
        return assemble(trace.orElse(null), traceLogs, List.of());
    }

    /**
     * Monta o contexto de um <b>incidente</b>: os <b>três</b> sinais correlacionados — trace +
     * logs + métricas (PromQL) — base da RCA (T-605, RF-H-007).
     */
    public PromptContext assembleForIncident(String traceId, String promQl, int logLimit) {
        Optional<TraceResult> trace = traces.findTrace(traceId);
        List<LogLine> traceLogs = logs.findByTraceId(traceId, logLimit);
        List<MetricSample> samples = promQl == null || promQl.isBlank()
                ? List.of() : metrics.instantQuery(promQl);
        return assemble(trace.orElse(null), traceLogs, samples);
    }

    /**
     * Monta o contexto a partir de sinais já obtidos (usável pelo modelo de correlação,
     * T-502, e direto em testes). Qualquer argumento pode ser nulo/vazio.
     */
    public PromptContext assemble(TraceResult trace, List<LogLine> logLines,
                                  List<MetricSample> metricSamples) {
        StringBuilder sb = new StringBuilder();
        List<String> included = new ArrayList<>();
        int used = 0;
        boolean truncated = false;

        // --- Trace
        if (trace != null) {
            String header = "# Trace " + trace.traceId() + " (" + trace.spanCount() + " spans)\n";
            if (budget.fits(used, header)) {
                sb.append(header);
                used += TokenBudget.estimateTokens(header);
                included.add("trace");
                for (SpanRef s : trace.spans()) {
                    String line = "- " + s.serviceName() + " " + s.operation()
                            + " (" + s.durationMicros() + "µs)\n";
                    if (!budget.fits(used, line)) {
                        truncated = true;
                        break;
                    }
                    sb.append(line);
                    used += TokenBudget.estimateTokens(line);
                }
            } else {
                truncated = true;
            }
        }

        // --- Logs
        if (logLines != null && !logLines.isEmpty()) {
            String header = "# Logs (" + logLines.size() + ")\n";
            if (budget.fits(used, header)) {
                sb.append(header);
                used += TokenBudget.estimateTokens(header);
                included.add("logs");
                for (LogLine l : logLines) {
                    String line = "[" + l.timestampNanos() + "] " + l.line() + "\n";
                    if (!budget.fits(used, line)) {
                        truncated = true;
                        break;
                    }
                    sb.append(line);
                    used += TokenBudget.estimateTokens(line);
                }
            } else {
                truncated = true;
            }
        }

        // --- Métricas
        if (metricSamples != null && !metricSamples.isEmpty()) {
            String header = "# Métricas (" + metricSamples.size() + ")\n";
            if (budget.fits(used, header)) {
                sb.append(header);
                used += TokenBudget.estimateTokens(header);
                included.add("metrics");
                for (MetricSample m : metricSamples) {
                    String line = m.labels() + " = " + m.value() + "\n";
                    if (!budget.fits(used, line)) {
                        truncated = true;
                        break;
                    }
                    sb.append(line);
                    used += TokenBudget.estimateTokens(line);
                }
            } else {
                truncated = true;
            }
        }

        // Guarda final de PII na fronteira do prompt (RNF-H-006).
        String text = PromptSanitizer.sanitize(sb.toString());
        return new PromptContext(text, TokenBudget.estimateTokens(text), truncated, included);
    }

    /**
     * Monta o contexto de uma <b>janela temporal</b> (T-1001, RF-H-005/010) — sem {@code traceId}:
     * visão agregada dos traces da janela (volume, erros, mais lentos, queries SQL mais lentas),
     * traces com erro, logs de erro e métricas. Sinais que falharam na coleta são citados em
     * {@code unavailable} para a IA não confundir "sem dados" com "sem problemas".
     *
     * <p>Ordem de prioridade: visão geral → traces com erro → queries lentas → traces lentos →
     * logs de erro → métricas.
     */
    public PromptContext assembleForWindow(TimeWindow window, List<TraceSummary> recent,
                                           List<TraceSummary> errorTraces, List<LogLine> errorLogs,
                                           List<MetricSample> metricSamples, List<String> unavailable) {
        List<TraceSummary> all = recent == null ? List.of() : recent;
        List<TraceSummary> errs = errorTraces == null ? List.of() : errorTraces;
        Sections out = new Sections(budget);

        List<String> overview = new ArrayList<>();
        overview.add("Início: " + Instant.ofEpochMilli(window.startMicros() / 1_000)
                + " · Fim: " + Instant.ofEpochMilli(window.endMicros() / 1_000)
                + " · Duração: " + window.span());
        overview.add("Traces amostrados: " + all.size() + " · com erro: " + errs.size());
        if (!all.isEmpty()) {
            long[] durations = all.stream().mapToLong(TraceSummary::durationMicros).sorted().toArray();
            overview.add("Duração dos traces (µs): p50=" + percentile(durations, 50)
                    + " p95=" + percentile(durations, 95) + " máx=" + durations[durations.length - 1]);
            overview.add("Serviços: " + String.join(", ", all.stream()
                    .flatMap(t -> t.services().stream()).distinct().sorted().toList()));
        }
        if (unavailable != null && !unavailable.isEmpty()) {
            overview.add("Sinais indisponíveis nesta coleta: " + String.join(", ", unavailable));
        }
        out.section("window", "# Janela", overview);

        out.section("errorTraces", "# Traces com erro (" + errs.size() + ")",
                errs.stream().map(ContextAssembler::describe).toList());

        List<String> slowQueries = java.util.stream.Stream.concat(all.stream(), errs.stream())
                .map(TraceSummary::slowestQuery).filter(Objects::nonNull).distinct()
                .sorted(Comparator.comparingLong(SlowQuery::durationMicros).reversed())
                .limit(10)
                .map(q -> "- " + q.serviceName() + " " + nullToDash(q.dbNamespace()) + " "
                        + nullToDash(q.dbOperationName()) + " (" + q.durationMicros() + "µs): "
                        + nullToDash(q.dbQueryText()))
                .toList();
        out.section("slowQueries", "# Queries SQL mais lentas", slowQueries);

        out.section("slowTraces", "# Traces mais lentos", all.stream()
                .sorted(Comparator.comparingLong(TraceSummary::durationMicros).reversed())
                .limit(10).map(ContextAssembler::describe).toList());

        out.section("logs", "# Logs de erro (" + (errorLogs == null ? 0 : errorLogs.size()) + ")",
                errorLogs == null ? List.of() : errorLogs.stream()
                        .map(l -> "[" + l.timestampNanos() + "] " + l.line()).toList());

        out.section("metrics", "# Métricas (" + (metricSamples == null ? 0 : metricSamples.size()) + ")",
                metricSamples == null ? List.of() : metricSamples.stream()
                        .map(m -> m.labels() + " = " + m.value()).toList());

        String text = PromptSanitizer.sanitize(out.text());
        return new PromptContext(text, TokenBudget.estimateTokens(text), out.truncated, out.included);
    }

    private static String describe(TraceSummary t) {
        return "- " + t.traceId() + " " + nullToDash(t.rootService()) + " " + nullToDash(t.rootOperation())
                + " (" + t.durationMicros() + "µs, " + t.spanCount() + " spans"
                + (t.errorSpanCount() > 0 ? ", " + t.errorSpanCount() + " em erro" : "") + ")";
    }

    private static long percentile(long[] sorted, int p) {
        int idx = (int) Math.ceil(p / 100.0 * sorted.length) - 1;
        return sorted[Math.max(0, Math.min(idx, sorted.length - 1))];
    }

    private static String nullToDash(String s) {
        return s == null || s.isBlank() ? "-" : s;
    }

    /** Acumula seções respeitando o orçamento de tokens (truncamento por linha). */
    private static final class Sections {
        private final TokenBudget budget;
        private final StringBuilder sb = new StringBuilder();
        private final List<String> included = new ArrayList<>();
        private int used;
        private boolean truncated;

        private Sections(TokenBudget budget) {
            this.budget = budget;
        }

        /** Adiciona a seção se houver linhas; seções vazias são omitidas. */
        void section(String signal, String header, List<String> lines) {
            if (lines.isEmpty()) {
                return;
            }
            String h = header + "\n";
            if (!budget.fits(used, h)) {
                truncated = true;
                return;
            }
            sb.append(h);
            used += TokenBudget.estimateTokens(h);
            included.add(signal);
            for (String line : lines) {
                String l = line + "\n";
                if (!budget.fits(used, l)) {
                    truncated = true;
                    return;
                }
                sb.append(l);
                used += TokenBudget.estimateTokens(l);
            }
        }

        String text() {
            return sb.toString();
        }
    }
}
