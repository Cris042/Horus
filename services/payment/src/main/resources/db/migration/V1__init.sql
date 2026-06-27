-- Payment Service — migração inicial (scaffold T-101; sequência alinhada ao Panache em T-103).
-- PanacheEntity gera o id via sequência `<tabela>_seq` (PooledLo, allocationSize 50).

CREATE SEQUENCE carteira_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE carteira (
    id         BIGINT         PRIMARY KEY,
    titular_id VARCHAR(64)    NOT NULL,
    saldo      NUMERIC(18, 2) NOT NULL DEFAULT 0
);
