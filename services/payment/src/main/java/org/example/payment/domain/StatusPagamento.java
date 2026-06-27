package org.example.payment.domain;

/** Ciclo de vida de um pagamento (RF-013/014). */
public enum StatusPagamento {
    /** Criado, aguardando processamento. */
    PENDENTE,
    /** Aprovado — debitou a carteira. */
    APROVADO,
    /** Rejeitado — não afetou o saldo. */
    REJEITADO,
    /** Estornado — pagamento aprovado revertido (RF-014). */
    ESTORNADO
}
