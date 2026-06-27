package org.example.payment.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/** Dados para abrir uma carteira (RF-011). Saldo inicial opcional (≥ 0). */
public record CriarCarteiraRequest(
        @NotBlank @Size(max = 64) String titularId,
        @DecimalMin("0.00") BigDecimal saldoInicial) {

    public BigDecimal saldoInicialOuZero() {
        // Escala 2 fixa para representação monetária consistente (JSON sempre "0.00").
        return saldoInicial == null ? BigDecimal.ZERO.setScale(2) : saldoInicial;
    }
}
