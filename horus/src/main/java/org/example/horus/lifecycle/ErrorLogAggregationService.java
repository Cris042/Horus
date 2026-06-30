package org.example.horus.lifecycle;

import jakarta.enterprise.context.ApplicationScoped;
import org.example.horus.lifecycle.ErrorLogAggregationModel.ErrorGroup;
import org.example.horus.lifecycle.ErrorLogAggregationModel.ErrorLogAggregation;
import org.example.horus.query.LogQueryPort;
import org.example.horus.query.QueryModel.LogLine;
import org.example.horus.query.TraceQueryPort;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/** Agrega logs de erro correlacionados por serviço e fingerprint textual. */
@ApplicationScoped
public class ErrorLogAggregationService {

    private static final Pattern UUID_PATTERN =
            Pattern.compile("\\b[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\b");
    private static final Pattern LONG_HEX_PATTERN = Pattern.compile("\\b[0-9a-f]{12,}\\b");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\b\\d+\\b");

    private final TraceQueryPort traces;
    private final LogQueryPort logs;

    public ErrorLogAggregationService(TraceQueryPort traces, LogQueryPort logs) {
        this.traces = traces;
        this.logs = logs;
    }

    public Optional<ErrorLogAggregation> findByTraceId(String traceId, int limit) {
        if (traces.findTrace(traceId).isEmpty()) {
            return Optional.empty();
        }

        List<LogLine> errorLogs = logs.findByTraceId(traceId, limit).stream()
                .filter(ErrorLogAggregationService::isError)
                .toList();

        Map<String, MutableGroup> groups = new LinkedHashMap<>();
        for (LogLine log : errorLogs) {
            String service = serviceName(log);
            String level = level(log);
            String sample = firstLine(log.line());
            String fingerprint = normalizeFingerprint(sample);
            String key = service + "|" + level + "|" + fingerprint;
            groups.computeIfAbsent(key, ignored -> new MutableGroup(service, level, fingerprint, sample))
                    .accept(log);
        }

        List<ErrorGroup> out = groups.values().stream()
                .map(MutableGroup::toView)
                .sorted(Comparator.comparingInt(ErrorGroup::count).reversed()
                        .thenComparing(ErrorGroup::latestTimestampNanos, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(ErrorGroup::serviceName))
                .toList();

        return Optional.of(new ErrorLogAggregation(traceId, errorLogs.size(), out.size(), out));
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

    private static boolean hasStacktrace(LogLine log) {
        String line = log.line();
        if (line == null) {
            return false;
        }
        return line.contains("\n") || line.contains("\tat ") || line.contains("Caused by:");
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

    private static final class MutableGroup {
        private final String serviceName;
        private final String level;
        private final String fingerprint;
        private final String sample;
        private int count;
        private boolean hasStacktrace;
        private String latestTimestampNanos;

        private MutableGroup(String serviceName, String level, String fingerprint, String sample) {
            this.serviceName = serviceName;
            this.level = level;
            this.fingerprint = fingerprint;
            this.sample = sample;
        }

        private void accept(LogLine log) {
            count++;
            hasStacktrace = hasStacktrace || ErrorLogAggregationService.hasStacktrace(log);
            if (latestTimestampNanos == null
                    || (log.timestampNanos() != null && log.timestampNanos().compareTo(latestTimestampNanos) > 0)) {
                latestTimestampNanos = log.timestampNanos();
            }
        }

        private ErrorGroup toView() {
            return new ErrorGroup(serviceName, level, fingerprint, count, sample, hasStacktrace, latestTimestampNanos);
        }
    }
}
