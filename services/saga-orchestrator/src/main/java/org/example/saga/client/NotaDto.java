package org.example.saga.client;

/** Subconjunto da resposta do invoice-service necessário à SAGA. */
public record NotaDto(Long id, String status, String numero) {
}
