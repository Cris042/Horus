-- SAGA Orchestrator — estado persistido da SAGA (T-107, ADR-0013).
-- Id via sequência `saga_seq` (Panache / PooledLo, INCREMENT 50; não IDENTITY).

CREATE SEQUENCE saga_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE saga (
    id            BIGINT         PRIMARY KEY,
    fluxo         VARCHAR(32)    NOT NULL,
    carteira_id   BIGINT         NOT NULL,
    valor         NUMERIC(18, 2) NOT NULL,
    pagamento_id  BIGINT,
    nota_id       BIGINT,
    status        VARCHAR(24)    NOT NULL DEFAULT 'INICIADA',
    motivo_falha  VARCHAR(255),
    criado_em     TIMESTAMPTZ    NOT NULL DEFAULT now(),
    atualizado_em TIMESTAMPTZ
);

CREATE INDEX idx_saga_status ON saga (status);
