package org.example.horus.ai.context;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.example.horus.query.LogQueryPort;
import org.example.horus.query.MetricQueryPort;
import org.example.horus.query.QueryModel.LogLine;
import org.example.horus.query.QueryModel.MetricSample;
import org.example.horus.query.QueryModel.TraceSearch;
import org.example.horus.query.QueryModel.TraceSummary;
import org.example.horus.query.TimeWindow;
import org.example.horus.query.TraceQueryPort;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Coleta os sinais de uma janela temporal nos três backends e monta o contexto da IA
 * (T-1001, RF-H-005/010). Cada sinal é <b>best-effort</b>: a falha de um backend não derruba o
 * resumo — o sinal vai para {@code unavailable} no contexto.
 */
@ApplicationScoped
public class WindowContextCollector {

    private static final Logger LOG = Logger.getLogger(WindowContextCollector.class);

    private final TraceQueryPort traces;
    private final LogQueryPort logs;
    private final MetricQueryPort metrics;
    private final ContextAssembler assembler;

    /** LogQL dos logs de erro da janela. */
    @ConfigProperty(name = "horus.query.loki.error-logql",
            defaultValue = "{service_namespace=\"medrec\"} | severity_text=~\"(?i)(error|fatal)\"")
    String errorLogQl;

    /** PromQL de saúde incluída no resumo de estado. */
    @ConfigProperty(name = "horus.ai.summary.promql", defaultValue = "up")
    String promQl;

    @ConfigProperty(name = "horus.ai.window.trace-limit", defaultValue = "50")
    int traceLimit;

    @ConfigProperty(name = "horus.ai.window.log-limit", defaultValue = "50")
    int logLimit;

    public WindowContextCollector(TraceQueryPort traces, LogQueryPort logs, MetricQueryPort metrics,
                                  ContextAssembler assembler) {
        this.traces = traces;
        this.logs = logs;
        this.metrics = metrics;
        this.assembler = assembler;
    }

    /** Contexto da janela para todos os serviços. */
    public PromptContext collect(TimeWindow window) {
        List<String> unavailable = new ArrayList<>();
        List<TraceSummary> recent = fetch("traces", unavailable,
                () -> traces.searchTraces(new TraceSearch(null, null, window, 0, false, traceLimit)));
        List<TraceSummary> errors = fetch("errorTraces", unavailable,
                () -> traces.searchTraces(new TraceSearch(null, null, window, 0, true, traceLimit)));
        List<LogLine> errorLogs = fetch("logs", unavailable,
                () -> logs.findInWindow(errorLogQl, window, logLimit));
        List<MetricSample> samples = promQl == null || promQl.isBlank() ? List.of()
                : fetch("metrics", unavailable, () -> metrics.instantQuery(promQl));
        return assembler.assembleForWindow(window, recent, errors, errorLogs, samples, unavailable);
    }

    private static <T> List<T> fetch(String signal, List<String> unavailable, Supplier<List<T>> call) {
        try {
            List<T> result = call.get();
            return result == null ? List.of() : result;
        } catch (RuntimeException e) {
            LOG.warnf("Sinal '%s' indisponível na coleta da janela: %s", signal, e.getMessage());
            unavailable.add(signal);
            return List.of();
        }
    }
}
