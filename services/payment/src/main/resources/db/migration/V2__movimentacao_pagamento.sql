-- Payment Service — movimentações e pagamentos (T-103, RF-012..015).
-- Ids via sequência `<tabela>_seq` (Panache / PooledLo, INCREMENT 50), como em V1.

CREATE SEQUENCE movimentacao_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE pagamento_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE movimentacao (
    id          BIGINT         PRIMARY KEY,
    carteira_id BIGINT         NOT NULL REFERENCES carteira (id),
    tipo        VARCHAR(8)     NOT NULL,
    valor       NUMERIC(18, 2) NOT NULL,
    saldo_apos  NUMERIC(18, 2) NOT NULL,
    descricao   VARCHAR(255),
    criado_em   TIMESTAMPTZ    NOT NULL DEFAULT now()
);

CREATE TABLE pagamento (
    id            BIGINT         PRIMARY KEY,
    carteira_id   BIGINT         NOT NULL REFERENCES carteira (id),
    valor         NUMERIC(18, 2) NOT NULL,
    status        VARCHAR(16)    NOT NULL DEFAULT 'PENDENTE',
    criado_em     TIMESTAMPTZ    NOT NULL DEFAULT now(),
    processado_em TIMESTAMPTZ
);

CREATE INDEX idx_movimentacao_carteira ON movimentacao (carteira_id);
CREATE INDEX idx_pagamento_carteira ON pagamento (carteira_id);
