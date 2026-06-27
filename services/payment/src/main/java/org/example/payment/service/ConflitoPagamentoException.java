package org.example.payment.service;

/** Operação inválida no estado atual (ex.: aprovar sem saldo, estornar não-aprovado). */
public class ConflitoPagamentoException extends RuntimeException {
    public ConflitoPagamentoException(String message) {
        super(message);
    }
}
