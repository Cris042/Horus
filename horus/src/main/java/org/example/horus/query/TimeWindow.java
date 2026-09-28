package org.example.horus.query;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Janela temporal de consulta (T-1001, RF-H-005/010/012) — {@code [start, end)} em
 * microssegundos desde a época, a unidade nativa do Jaeger. Loki (ns) e Prometheus (s)
 * derivam daqui.
 *
 * <p>Construída a partir de um {@code lookback} relativo ({@code 30s}, {@code 15m},
 * {@code 1h}, {@code 2d}) ou de {@code from}/{@code to} absolutos (ISO-8601 ou epoch
 * em milissegundos). Entradas inválidas lançam {@link IllegalArgumentException}.
 */
public record TimeWindow(long startMicros, long endMicros) {

    /** Lookback padrão quando nada é informado. */
    public static final Duration DEFAULT_LOOKBACK = Duration.ofHours(1);

    /** Maior janela aceita — protege os backends de varreduras enormes. */
    public static final Duration MAX_SPAN = Duration.ofDays(30);

    private static final Pattern LOOKBACK = Pattern.compile("^(\\d+)([smhd])$");

    public TimeWindow {
        if (endMicros <= startMicros) {
            throw new IllegalArgumentException("janela inválida: fim deve ser após o início");
        }
        if (endMicros - startMicros > MAX_SPAN.toNanos() / 1_000) {
            throw new IllegalArgumentException("janela maior que o máximo permitido (" + MAX_SPAN.toDays() + "d)");
        }
    }

    /** Janela dos últimos {@code lookback} até {@code now}. */
    public static TimeWindow last(Duration lookback, Instant now) {
        long end = toMicros(now);
        return new TimeWindow(end - lookback.toNanos() / 1_000, end);
    }

    /**
     * Resolve a janela a partir dos parâmetros de API. {@code from}/{@code to} têm precedência
     * sobre {@code lookback}; {@code to} ausente = agora; nada informado = última 1h.
     */
    public static TimeWindow resolve(String lookback, String from, String to, Instant now) {
        Instant end = isBlank(to) ? now : parseInstant(to, "to");
        if (!isBlank(from)) {
            long start = toMicros(parseInstant(from, "from"));
            return new TimeWindow(start, toMicros(end));
        }
        Duration span = isBlank(lookback) ? DEFAULT_LOOKBACK : parseLookback(lookback);
        return last(span, end);
    }

    /** {@code 30s|15m|1h|2d} → {@link Duration}. */
    public static Duration parseLookback(String lookback) {
        Matcher m = LOOKBACK.matcher(lookback.trim());
        if (!m.matches()) {
            throw new IllegalArgumentException("lookback inválido: '" + lookback + "' (use ex.: 15m, 1h, 2d)");
        }
        long n = Long.parseLong(m.group(1));
        if (n <= 0) {
            throw new IllegalArgumentException("lookback deve ser positivo");
        }
        return switch (m.group(2)) {
            case "s" -> Duration.ofSeconds(n);
            case "m" -> Duration.ofMinutes(n);
            case "h" -> Duration.ofHours(n);
            default -> Duration.ofDays(n);
        };
    }

    public long startNanos() {
        return startMicros * 1_000;
    }

    public long endNanos() {
        return endMicros * 1_000;
    }

    public double startSeconds() {
        return startMicros / 1_000_000.0;
    }

    public double endSeconds() {
        return endMicros / 1_000_000.0;
    }

    /** Duração da janela. */
    public Duration span() {
        return Duration.ofNanos((endMicros - startMicros) * 1_000);
    }

    private static Instant parseInstant(String value, String name) {
        String v = value.trim();
        try {
            if (v.chars().allMatch(Character::isDigit)) {
                return Instant.ofEpochMilli(Long.parseLong(v));
            }
            return Instant.parse(v);
        } catch (DateTimeParseException | NumberFormatException e) {
            throw new IllegalArgumentException(name + " inválido: '" + value
                    + "' (use ISO-8601 ou epoch em ms)");
        }
    }

    private static long toMicros(Instant instant) {
        return instant.getEpochSecond() * 1_000_000 + instant.getNano() / 1_000;
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
