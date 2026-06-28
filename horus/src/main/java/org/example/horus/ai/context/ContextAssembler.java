package org.example.horus.ai.context;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.example.horus.query.LogQueryPort;
import org.example.horus.query.MetricQueryPort;
import org.example.horus.query.QueryModel.LogLine;
import org.example.horus.query.QueryModel.MetricSample;
import org.example.horus.query.QueryModel.SpanRef;
import org.example.horus.query.QueryModel.TraceResult;
import org.example.horus.query.TraceQueryPort;

import java.util.ArrayList;
import java.util.List;
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
}
