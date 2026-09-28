package org.example.horus.ai.anomaly;

import jakarta.enterprise.context.ApplicationScoped;
import org.example.horus.ai.LlmEngine;
import org.example.horus.ai.LlmEngine.LlmRequest;
import org.example.horus.ai.LlmEngine.LlmResponse;
import org.example.horus.ai.ModelTier;
import org.example.horus.ai.anomaly.ErrorClusterModel.ErrorCluster;
import org.example.horus.ai.anomaly.ErrorClusterModel.ErrorClustering;
import org.example.horus.ai.context.PromptSanitizer;
import org.example.horus.query.LogQueryPort;
import org.example.horus.query.QueryModel.LogLine;
import org.example.horus.query.TraceQueryPort;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Agente <b>Error Clusterer</b> (T-606, RF-H-009): agrupa logs de erro por fingerprint
 * normalizado <em>atravessando serviços</em> e produz um rótulo em linguagem natural via IA.
 *
 * <p>Reutiliza a mesma fonte de logs da T-505 ({@link LogQueryPort#findByTraceId}), mas
 * clusteriza por assinatura — destacando o quanto cada falha se espalha pelos serviços.
 * A rotulagem usa a camada {@link ModelTier#FAST} (alto volume / baixo custo, ADR-0011)
 * e funciona com o modo stub do {@code AnthropicLlmEngine} (sem chave): nesse caso {@code live=false}.
 */
@ApplicationScoped
public class ErrorClusterer {

    private static final Pattern UUID_PATTERN =
            Pattern.compile("\\b[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\b");
    private static final Pattern LONG_HEX_PATTERN = Pattern.compile("\\b[0-9a-f]{12,}\\b");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\b\\d+\\b");

    private static final List<String> SEVERITY_ORDER = List.of("fatal", "error", "warn", "unknown");

    private static final String SYSTEM = """
            Você é o Horus, observabilidade aumentada por IA. Receberá CLUSTERS de erro
            (assinatura, serviços afetados, contagem) de uma request. Rotule, em português
            claro e em uma frase curta, o problema dominante — priorize clusters que cruzam
            vários serviços. Não invente dados nem PII; trate como assistência, não verdade.""";

    private final TraceQueryPort traces;
    private final LogQueryPort logs;
    private final LlmEngine engine;

    public ErrorClusterer(TraceQueryPort traces, LogQueryPort logs, LlmEngine engine) {
        this.traces = traces;
        this.logs = logs;
        this.engine = engine;
    }

    public Optional<ErrorClustering> clusterByTrace(String traceId, int limit) {
        if (traces.findTrace(traceId).isEmpty()) {
            return Optional.empty();
        }

        List<LogLine> errorLogs = logs.findByTraceId(traceId, limit).stream()
                .filter(ErrorClusterer::isError)
                .toList();

        Map<String, MutableCluster> clusters = new LinkedHashMap<>();
        for (LogLine log : errorLogs) {
            String sample = firstLine(log.line());
            String fingerprint = normalizeFingerprint(sample);
            clusters.computeIfAbsent(fingerprint, fp -> new MutableCluster(fp, sample))
                    .accept(serviceName(log), level(log));
        }

        List<ErrorCluster> out = clusters.values().stream()
                .map(MutableCluster::toView)
                .sorted(Comparator.comparingInt(ErrorCluster::totalCount).reversed()
                        .thenComparing(c -> SEVERITY_ORDER.indexOf(c.severity()))
                        .thenComparing(ErrorCluster::fingerprint))
                .toList();

        int crossServiceClusters = (int) out.stream().filter(ErrorCluster::crossService).count();
        LlmResponse label = label(out);

        return Optional.of(new ErrorClustering(
                traceId, errorLogs.size(), out.size(), crossServiceClusters,
                label.text(), label.modelId(), label.live(), out));
    }

    private LlmResponse label(List<ErrorCluster> clusters) {
        if (clusters.isEmpty()) {
            return new LlmResponse("Nenhum erro correlacionado neste trace.", "none", false);
        }
        StringBuilder sb = new StringBuilder("Clusters de erro (mais frequentes primeiro):\n");
        clusters.stream().limit(5).forEach(c -> sb
                .append("- [").append(c.severity()).append("] x").append(c.totalCount())
                .append(c.crossService() ? " (cross-service " + c.serviceCount() + ")" : "")
                .append(" serviços=").append(c.affectedServices())
                .append(" :: ").append(c.sample()).append('\n'));
        // Guarda final de PII na fronteira do prompt (RNF-H-006) — mesma rede de segurança do
        // ContextAssembler (T-602); achado em T-904: este agente monta o prompt direto de
        // `sample()` (log cru da telemetria) e não passava por ela.
        String prompt = PromptSanitizer.sanitize(sb.toString());
        return engine.complete(new LlmRequest(SYSTEM, prompt, ModelTier.FAST));
    }

    private static boolean isError(LogLine log) {
        String level = level(log);
        if ("error".equals(level) || "fatal".equals(level)) {
            return true;
        }
        String line = lower(log.line());
        return line.contains("error") || line.contains("exception") || line.contains("failed");
    }

    private static String serviceName(LogLine log) {
        Map<String, String> labels = log.labels();
        if (labels == null) {
            return "unknown";
        }
        String service = firstNonBlank(labels.get("service_name"), labels.get("service.name"));
        return service == null ? "unknown" : service;
    }

    private static String level(LogLine log) {
        Map<String, String> labels = log.labels();
        if (labels != null) {
            String level = firstNonBlank(labels.get("level"), labels.get("severity_text"));
            if (level != null) {
                return level.toLowerCase(Locale.ROOT);
            }
        }
        String line = lower(log.line());
        if (line.contains("fatal")) {
            return "fatal";
        }
        if (line.contains("error") || line.contains("exception") || line.contains("failed")) {
            return "error";
        }
        return "unknown";
    }

    private static String firstLine(String line) {
        if (line == null || line.isBlank()) {
            return "";
        }
        int newline = line.indexOf('\n');
        return newline >= 0 ? line.substring(0, newline).trim() : line.trim();
    }

    private static String normalizeFingerprint(String line) {
        String normalized = lower(line);
        normalized = UUID_PATTERN.matcher(normalized).replaceAll(":uuid");
        normalized = LONG_HEX_PATTERN.matcher(normalized).replaceAll(":hex");
        normalized = NUMBER_PATTERN.matcher(normalized).replaceAll(":n");
        return normalized;
    }

    private static String lower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    private static String firstNonBlank(String left, String right) {
        if (left != null && !left.isBlank()) {
            return left;
        }
        return (right == null || right.isBlank()) ? null : right;
    }

    /** Severidade "mais alta" entre duas (fatal > error > warn > unknown). */
    private static String maxSeverity(String a, String b) {
        int ia = SEVERITY_ORDER.indexOf(a);
        int ib = SEVERITY_ORDER.indexOf(b);
        if (ia < 0) {
            ia = SEVERITY_ORDER.size();
        }
        if (ib < 0) {
            ib = SEVERITY_ORDER.size();
        }
        return ia <= ib ? a : b;
    }

    private static final class MutableCluster {
        private final String fingerprint;
        private final String sample;
        private final Set<String> services = new LinkedHashSet<>();
        private int totalCount;
        private String severity = "unknown";

        private MutableCluster(String fingerprint, String sample) {
            this.fingerprint = fingerprint;
            this.sample = sample;
        }

        private void accept(String service, String level) {
            totalCount++;
            services.add(service);
            severity = maxSeverity(severity, level);
        }

        private ErrorCluster toView() {
            List<String> svc = new ArrayList<>(services);
            return new ErrorCluster(fingerprint, sample, severity, totalCount,
                    svc.size(), svc.size() > 1, svc);
        }
    }
}
