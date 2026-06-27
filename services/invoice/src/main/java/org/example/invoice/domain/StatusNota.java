package org.example.invoice.domain;

/** Ciclo de vida de uma nota fiscal simulada (RF-017/018). */
public enum StatusNota {
    /** Recebida, ainda não emitida. */
    PENDENTE,
    /** Emitida com sucesso — possui número. */
    EMITIDA,
    /** Falha na emissão — reprocessável (RF-020). */
    FALHA
}
