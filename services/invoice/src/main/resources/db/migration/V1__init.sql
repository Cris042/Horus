-- Invoice Service — schema da nota fiscal simulada (scaffold T-101 + domínio T-104).
-- Id via sequência `nota_fiscal_seq` (Panache / PooledLo, INCREMENT 50; não IDENTITY).

CREATE SEQUENCE nota_fiscal_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE nota_fiscal (
    id            BIGINT         PRIMARY KEY,
    valor         NUMERIC(18, 2) NOT NULL,
    referencia    VARCHAR(64),
    numero        VARCHAR(32),
    status        VARCHAR(16)    NOT NULL DEFAULT 'PENDENTE',
    motivo_falha  VARCHAR(255),
    tentativas    INT            NOT NULL DEFAULT 0,
    criado_em     TIMESTAMPTZ    NOT NULL DEFAULT now(),
    atualizado_em TIMESTAMPTZ
);

CREATE INDEX idx_nota_fiscal_status ON nota_fiscal (status);
