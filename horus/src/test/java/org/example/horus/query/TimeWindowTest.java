package org.example.horus.query;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Resolução e validação da janela temporal (T-1001). */
class TimeWindowTest {

    private static final Instant NOW = Instant.parse("2026-09-28T12:00:00Z");
    private static final long NOW_MICROS = NOW.toEpochMilli() * 1_000;

    @Test
    void nothingGiven_defaultsToLastHour() {
        TimeWindow w = TimeWindow.resolve(null, null, null, NOW);
        assertEquals(NOW_MICROS, w.endMicros());
        assertEquals(Duration.ofHours(1), w.span());
    }

    @Test
    void lookback_units() {
        assertEquals(Duration.ofSeconds(30), TimeWindow.parseLookback("30s"));
        assertEquals(Duration.ofMinutes(15), TimeWindow.parseLookback("15m"));
        assertEquals(Duration.ofHours(6), TimeWindow.parseLookback("6h"));
        assertEquals(Duration.ofDays(2), TimeWindow.parseLookback("2d"));
    }

    @Test
    void fromTo_isoAndEpochMillis_takePrecedenceOverLookback() {
        TimeWindow w = TimeWindow.resolve("5m", "2026-09-28T10:00:00Z",
                String.valueOf(Instant.parse("2026-09-28T11:00:00Z").toEpochMilli()), NOW);
        assertEquals(Instant.parse("2026-09-28T10:00:00Z").toEpochMilli() * 1_000, w.startMicros());
        assertEquals(Duration.ofHours(1), w.span());
        assertEquals(w.startMicros() * 1_000, w.startNanos());
    }

    @Test
    void invalidInputs_rejected() {
        assertThrows(IllegalArgumentException.class, () -> TimeWindow.parseLookback("1w"));
        assertThrows(IllegalArgumentException.class, () -> TimeWindow.parseLookback("0m"));
        assertThrows(IllegalArgumentException.class, () -> TimeWindow.resolve(null, "ontem", null, NOW));
        // fim antes do início
        assertThrows(IllegalArgumentException.class,
                () -> TimeWindow.resolve(null, "2026-09-28T13:00:00Z", null, NOW));
        // acima do máximo (30d)
        assertThrows(IllegalArgumentException.class, () -> TimeWindow.resolve("31d", null, null, NOW));
    }
}
