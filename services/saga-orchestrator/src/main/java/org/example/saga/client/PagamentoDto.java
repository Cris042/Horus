package org.example.saga.client;

/** Subconjunto da resposta do payment-service necessário à SAGA. */
public record PagamentoDto(Long id, String status) {
}
