package org.example.horus.ai.job;

import java.time.Instant;

/** Estado de uma análise assíncrona (T-1006). Mutável só pelo {@link AiJobService}. */
public final class AiJob {

    /** Ciclo de vida: {@code PENDING → RUNNING → SUCCEEDED | FAILED}. */
    public enum Status { PENDING, RUNNING, SUCCEEDED, FAILED }

    private final String id;
    private final String kind;
    private final Instant createdAt = Instant.now();
    private volatile Status status = Status.PENDING;
    private volatile Instant startedAt;
    private volatile Instant finishedAt;
    private volatile Object result;
    private volatile String error;

    AiJob(String id, String kind) {
        this.id = id;
        this.kind = kind;
    }

    void start() {
        startedAt = Instant.now();
        status = Status.RUNNING;
    }

    void succeed(Object value) {
        result = value;
        finishedAt = Instant.now();
        status = Status.SUCCEEDED;
    }

    void fail(String message) {
        error = message;
        finishedAt = Instant.now();
        status = Status.FAILED;
    }

    public String id() { return id; }
    public String kind() { return kind; }
    public Instant createdAt() { return createdAt; }
    public Status status() { return status; }
    public Instant startedAt() { return startedAt; }
    public Instant finishedAt() { return finishedAt; }
    public Object result() { return result; }
    public String error() { return error; }

    /** Visão serializável do job. */
    public View view() {
        return new View(id, kind, status, createdAt, startedAt, finishedAt, result, error);
    }

    public record View(String id, String kind, Status status, Instant createdAt, Instant startedAt,
                       Instant finishedAt, Object result, String error) {
    }
}
