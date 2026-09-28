package org.example.horus.ai;

import org.example.horus.ai.LlmEngine.LlmRequest;
import org.example.horus.ai.LlmEngine.LlmResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Testa a lógica do {@link LlmResponseCache} (LRU, hit/miss, desabilitado) — sem CDI. */
class LlmResponseCacheTest {

    private static LlmResponse resp(String t) {
        return new LlmResponse(t, "claude-haiku-4-5", true);
    }

    @Test
    void hitOnSameRequest_missOnNew() {
        var cache = new LlmResponseCache(true, 10, java.time.Duration.ofMinutes(10));
        var req = new LlmRequest("sys", "pergunta", ModelTier.FAST);
        String key = cache.keyFor(req);

        assertTrue(cache.get(key).isEmpty());          // miss
        cache.put(key, resp("r1"));
        assertEquals("r1", cache.get(key).orElseThrow().text());  // hit

        var stats = cache.stats();
        assertEquals(1, stats.hits());
        assertEquals(1, stats.misses());
        assertEquals(1, stats.size());
    }

    @Test
    void evictsLeastRecentlyUsed_overCapacity() {
        var cache = new LlmResponseCache(true, 2, java.time.Duration.ofMinutes(10));
        cache.put("a", resp("a"));
        cache.put("b", resp("b"));
        cache.get("a");                 // 'a' vira o mais recente
        cache.put("c", resp("c"));      // estoura: descarta 'b' (LRU)

        assertTrue(cache.get("a").isPresent());
        assertTrue(cache.get("c").isPresent());
        assertTrue(cache.get("b").isEmpty());
    }

    @Test
    void disabled_neverCaches() {
        var cache = new LlmResponseCache(false, 10, java.time.Duration.ofMinutes(10));
        cache.put("k", resp("x"));
        assertTrue(cache.get("k").isEmpty());
        assertFalse(cache.enabled());
    }

    @Test
    void expiredEntries_areNotServed() throws InterruptedException {
        var cache = new LlmResponseCache(true, 10, java.time.Duration.ofMillis(20));
        var req = new LlmEngine.LlmRequest(null, "estado?", ModelTier.FAST);
        cache.put(cache.keyFor(req), new LlmEngine.LlmResponse("antigo", "m", true));
        org.junit.jupiter.api.Assertions.assertTrue(cache.get(cache.keyFor(req)).isPresent());
        Thread.sleep(40);
        org.junit.jupiter.api.Assertions.assertTrue(cache.get(cache.keyFor(req)).isEmpty(), "TTL expirado");
        org.junit.jupiter.api.Assertions.assertEquals(0, cache.stats().size());
    }
}
