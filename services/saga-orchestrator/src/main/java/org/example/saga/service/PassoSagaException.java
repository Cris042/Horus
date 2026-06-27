package org.example.saga.service;

/** Falha de um passo da SAGA — dispara a compensação dos passos anteriores. */
public class PassoSagaException extends RuntimeException {
    public PassoSagaException(String message) {
        super(message);
    }
}
