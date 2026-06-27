package org.example.payment.domain;

/** Sentido de uma movimentação financeira (RF-012). */
public enum TipoMovimentacao {
    /** Crédito — aumenta o saldo. */
    ENTRADA,
    /** Débito — reduz o saldo. */
    SAIDA
}
