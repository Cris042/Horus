package org.example.invoice.service;

/** Operação inválida no estado atual da nota (ex.: reprocessar uma que não falhou). */
public class ConflitoNotaException extends RuntimeException {
    public ConflitoNotaException(String message) {
        super(message);
    }
}
