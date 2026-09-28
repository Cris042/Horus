package org.example.horus.ai.job;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Execução <b>assíncrona</b> das análises pesadas de IA (T-1006, RNF-H-004: "análises pesadas
 * executadas de forma assíncrona").
 *
 * <p>Uma RCA na camada DEEP pode levar dezenas de segundos; em vez de prender a conexão HTTP,
 * o chamador recebe um {@code jobId} (202) e consulta o resultado depois. Pool limitado
 * ({@code horus.ai.jobs.workers}) com fila limitada ({@code horus.ai.jobs.queue}) — fila cheia
 * rejeita o pedido (o chamador responde 429) em vez de acumular custo e memória. Jobs concluídos
 * expiram após {@code horus.ai.jobs.retention}.
 */
@ApplicationScoped
public class AiJobService {

    private static final Logger LOG = Logger.getLogger(AiJobService.class);

    private final ThreadPoolExecutor executor;
    private final Duration retention;
    private final Map<String, AiJob> jobs = new ConcurrentHashMap<>();

    public AiJobService(@ConfigProperty(name = "horus.ai.jobs.workers", defaultValue = "2") int workers,
                        @ConfigProperty(name = "horus.ai.jobs.queue", defaultValue = "20") int queue,
                        @ConfigProperty(name = "horus.ai.jobs.retention", defaultValue = "1h") Duration retention) {
        this.executor = new ThreadPoolExecutor(workers, workers, 0, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(Math.max(1, queue)),
                Thread.ofVirtual().name("horus-ai-job-", 0).factory(),
                new ThreadPoolExecutor.AbortPolicy());
        this.retention = retention;
    }

    /**
     * Enfileira uma análise. Lança {@link RejectedExecutionException} quando a fila está cheia.
     */
    public AiJob submit(String kind, Supplier<Object> work) {
        evictExpired();
        AiJob job = new AiJob(UUID.randomUUID().toString(), kind);
        jobs.put(job.id(), job);
        try {
            executor.execute(() -> run(job, work));
        } catch (RejectedExecutionException e) {
            jobs.remove(job.id());
            throw e;
        }
        return job;
    }

    public Optional<AiJob> find(String id) {
        return Optional.ofNullable(jobs.get(id));
    }

    /** Jobs mais recentes primeiro. */
    public List<AiJob> recent(int limit) {
        evictExpired();
        return jobs.values().stream()
                .sorted(Comparator.comparing(AiJob::createdAt).reversed())
                .limit(limit)
                .toList();
    }

    private void run(AiJob job, Supplier<Object> work) {
        job.start();
        try {
            job.succeed(work.get());
        } catch (RuntimeException e) {
            LOG.warnf("Job de IA %s (%s) falhou: %s", job.id(), job.kind(), e.getMessage());
            job.fail(e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    private void evictExpired() {
        Instant cutoff = Instant.now().minus(retention);
        jobs.values().removeIf(j -> j.finishedAt() != null && j.finishedAt().isBefore(cutoff));
    }

    @PreDestroy
    void shutdown() {
        executor.shutdownNow();
    }
}
