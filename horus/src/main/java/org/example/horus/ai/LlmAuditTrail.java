package org.example.horus.ai;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Trilha do que foi enviado ao LLM (T-1004, RNF-H-006 — "trilha do que foi enviado").
 *
 * <p>Guarda os últimos N registros em memória (consulta rápida pelo painel/API) — a trilha
 * <b>durável</b> é o log estruturado da categoria {@code horus.ai.audit}, que vai ao Loki pelo
 * pipeline OTel como qualquer outro log. Nenhum registro contém o texto do prompt: só hash,
 * tamanho, camada, modelo e quantas redações de PII foram aplicadas.
 */
@ApplicationScoped
public class LlmAuditTrail {

    private final int maxEntries;
    private final Deque<AuditRecord> records = new ArrayDeque<>();
    private final AtomicLong total = new AtomicLong();
    private final AtomicLong redactions = new AtomicLong();

    public LlmAuditTrail(@ConfigProperty(name = "horus.ai.audit.max-entries", defaultValue = "500") int maxEntries) {
        this.maxEntries = Math.max(1, maxEntries);
    }

    void record(AuditRecord record) {
        total.incrementAndGet();
        redactions.addAndGet(record.redactions());
        synchronized (records) {
            records.addFirst(record);
            while (records.size() > maxEntries) {
                records.removeLast();
            }
        }
    }

    /** Registros mais recentes primeiro, até {@code limit}. */
    public List<AuditRecord> recent(int limit) {
        synchronized (records) {
            List<AuditRecord> out = new ArrayList<>(Math.min(limit, records.size()));
            for (AuditRecord r : records) {
                if (out.size() >= limit) {
                    break;
                }
                out.add(r);
            }
            return out;
        }
    }

    /** Totais desde o início do processo. */
    public AuditStats stats() {
        return new AuditStats(total.get(), redactions.get(), maxEntries);
    }

    /**
     * Um envio ao LLM. {@code promptSha256} identifica o conteúdo (já sanitizado) sem expô-lo;
     * {@code outcome} = {@code ok} | {@code error}.
     */
    public record AuditRecord(Instant at, String purpose, ModelTier tier, String requestedModel,
                              String answeredModel, boolean live, int promptChars, int estimatedTokens,
                              String promptSha256, int redactions, long latencyMillis, String outcome) {
    }

    /** Totais da trilha. */
    public record AuditStats(long totalRequests, long totalRedactions, int retainedMax) {
    }
}
