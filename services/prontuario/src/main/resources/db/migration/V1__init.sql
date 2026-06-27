-- Prontuário Service — migração inicial (scaffold T-101; sequência alinhada ao Panache em T-102).
-- PanacheEntity gera o id via sequência `<tabela>_seq` (otimizador PooledLo, allocationSize 50),
-- então o schema cria a sequência correspondente (INCREMENT 50) em vez de coluna IDENTITY.

CREATE SEQUENCE prontuario_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE prontuario (
    id          BIGINT       PRIMARY KEY,
    paciente_id VARCHAR(64)  NOT NULL,
    criado_em   TIMESTAMPTZ  NOT NULL DEFAULT now()
);
