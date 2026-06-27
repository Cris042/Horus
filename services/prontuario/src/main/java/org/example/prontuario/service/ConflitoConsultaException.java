package org.example.prontuario.service;

/** Operação inválida no estado atual da consulta (ex.: editar/finalizar uma já finalizada). */
public class ConflitoConsultaException extends RuntimeException {
    public ConflitoConsultaException(String message) {
        super(message);
    }
}
