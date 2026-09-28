package org.example.horus.ai.job;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Ciclo de vida dos jobs e contrapressão da fila (T-1006). */
class AiJobServiceTest {

    private static AiJob await(AiJob job) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (job.finishedAt() == null && System.nanoTime() < deadline) {
            Thread.sleep(10);
        }
        return job;
    }

    @Test
    void job_succeeds_withResult() throws InterruptedException {
        AiJobService service = new AiJobService(1, 5, Duration.ofHours(1));
        AiJob job = await(service.submit("rca", () -> "causa provável"));
        assertEquals(AiJob.Status.SUCCEEDED, job.status());
        assertEquals("causa provável", job.result());
        assertTrue(service.find(job.id()).isPresent());
        service.shutdown();
    }

    @Test
    void job_fails_withError() throws InterruptedException {
        AiJobService service = new AiJobService(1, 5, Duration.ofHours(1));
        AiJob job = await(service.submit("rca", () -> { throw new IllegalStateException("api fora"); }));
        assertEquals(AiJob.Status.FAILED, job.status());
        assertTrue(job.error().contains("api fora"));
        service.shutdown();
    }

    @Test
    void fullQueue_isRejected_notAccumulated() throws InterruptedException {
        AiJobService service = new AiJobService(1, 1, Duration.ofHours(1));
        CountDownLatch release = new CountDownLatch(1);
        CountDownLatch started = new CountDownLatch(1);
        service.submit("slow", () -> { started.countDown(); awaitQuietly(release); return "a"; });
        started.await(5, TimeUnit.SECONDS);
        service.submit("queued", () -> "b");             // ocupa a única vaga da fila
        assertThrows(RejectedExecutionException.class, () -> service.submit("extra", () -> "c"));
        assertEquals(2, service.recent(10).size());       // o rejeitado não fica registrado
        release.countDown();
        service.shutdown();
    }

    @Test
    void finishedJobs_expireAfterRetention() throws InterruptedException {
        AiJobService service = new AiJobService(1, 5, Duration.ZERO);
        AiJob job = await(service.submit("rca", () -> "x"));
        Thread.sleep(5);
        assertTrue(service.recent(10).isEmpty());
        assertTrue(service.find(job.id()).isEmpty());
        service.shutdown();
    }

    private static void awaitQuietly(CountDownLatch latch) {
        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
