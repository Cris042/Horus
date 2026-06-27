package org.example.payment.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/** Criar um pagamento contra a carteira (RF-013). */
public record CriarPagamentoRequest(
        @NotNull @DecimalMin(value = "0.01") BigDecimal valor) {
}
