package org.example.horus.api;

import jakarta.ws.rs.BadRequestException;
import org.example.horus.query.TimeWindow;

import java.time.Instant;

/** Resolve os parâmetros de janela temporal das APIs (T-1001); entrada inválida → 400. */
final class ApiWindows {

    private ApiWindows() {
    }

    static TimeWindow resolve(String lookback, String from, String to) {
        try {
            return TimeWindow.resolve(lookback, from, to, Instant.now());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(e.getMessage());
        }
    }
}
