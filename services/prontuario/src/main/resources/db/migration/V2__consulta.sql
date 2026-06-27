-- Prontuário Service — consultas vinculadas ao prontuário (T-102, RF-008..010).
-- Id via sequência `consulta_seq` (Panache / PooledLo, INCREMENT 50), como em V1.

CREATE SEQUENCE consulta_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE consulta (
    id            BIGINT        PRIMARY KEY,
    prontuario_id BIGINT        NOT NULL REFERENCES prontuario (id),
    descricao     VARCHAR(2000) NOT NULL,
    status        VARCHAR(16)   NOT NULL DEFAULT 'EM_ANDAMENTO',
    criado_em     TIMESTAMPTZ   NOT NULL DEFAULT now(),
    atualizado_em TIMESTAMPTZ,
    finalizado_em TIMESTAMPTZ
);

CREATE INDEX idx_consulta_prontuario ON consulta (prontuario_id);
