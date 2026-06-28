package org.example.horus.correlation;

import jakarta.enterprise.context.ApplicationScoped;
import org.example.horus.correlation.CorrelationModel.RequestCorrelation;
import org.example.horus.correlation.CorrelationModel.ServiceInvolvement;
import org.example.horus.query.LogQueryPort;
import org.example.horus.query.QueryModel.LogLine;
import org.example.horus.query.QueryModel.SpanRef;
import org.example.horus.query.QueryModel.TraceResult;
import org.example.horus.query.TraceQueryPort;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Monta o {@link CorrelationModel.RequestCorrelation} de um trace a partir das portas de
 * consulta (T-501): traces (Jaeger) + logs (Loki). Sem I/O próprio — toda a lógica de
 * costura é determinística e testável com portas mockadas.
 */
@ApplicationScoped
public class CorrelationService {

    /** `service.name` canônico do worker (contrato T-005 §1). */
    static final String WORKER_SERVICE = "report-worker";

    private final TraceQueryPort traces;
    private final LogQueryPort logs;

    public CorrelationService(TraceQueryPort traces, LogQueryPort logs) {
        this.traces = traces;
        this.logs = logs;
    }

    /** Correlaciona um trace; vazio se o trace não existe. */
    public Optional<RequestCorrelation> correlate(String traceId, int logLimit) {
        Optional<TraceResult> trace = traces.findTrace(traceId);
        if (trace.isEmpty()) {
            return Optional.empty();
        }
        List<SpanRef> spans = trace.get().spans();
        List<LogLine> correlatedLogs = logs.findByTraceId(traceId, logLimit);

        List<ServiceInvolvement> services = involvementByService(spans);
        boolean worker = services.stream().anyMatch(s -> WORKER_SERVICE.equals(s.serviceName()));
        boolean messaging = worker || spans.stream().anyMatch(CorrelationService::isMessagingSpan);
        long total = spans.stream().mapToLong(SpanRef::durationMicros).sum();

        return Optional.of(new RequestCorrelation(
                traceId,
                trace.get().spanCount(),
                services,
                correlatedLogs,
                (int) correlatedLogs.stream().filter(CorrelationService::isError).count(),
                messaging,
                worker,
                total));
    }

    /** Agrupa spans por serviço (contagem + duração somada), do mais "pesado" ao mais leve. */
    private static List<ServiceInvolvement> involvementByService(List<SpanRef> spans) {
        Map<String, int[]> counts = new LinkedHashMap<>();
        Map<String, long[]> durations = new LinkedHashMap<>();
        for (SpanRef s : spans) {
            String name = s.serviceName() == null ? "desconhecido" : s.serviceName();
            counts.computeIfAbsent(name, k -> new int[1])[0]++;
            durations.computeIfAbsent(name, k -> new long[1])[0] += s.durationMicros();
        }
        return counts.keySet().stream()
                .map(name -> new ServiceInvolvement(name, counts.get(name)[0], durations.get(name)[0]))
                .sorted(Comparator.comparingLong(ServiceInvolvement::totalDurationMicros).reversed())
                .toList();
    }

    private static boolean isMessagingSpan(SpanRef span) {
        String op = span.operation() == null ? "" : span.operation().toLowerCase();
        return op.contains("publish") || op.contains("receive") || op.contains("relatorios");
    }

    private static boolean isError(LogLine log) {
        String level = log.labels() == null ? null : log.labels().get("level");
        if (level != null && level.equalsIgnoreCase("error")) {
            return true;
        }
        return log.line() != null && log.line().contains("ERROR");
    }
}
