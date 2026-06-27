package org.example.invoice.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Solicitação de emissão de NF simulada (RF-016).
 *
 * <p>{@code simularFalha} é um botão de simulação (não persistido): quando {@code true},
 * a emissão é registrada como falha — útil para exercitar o reprocessamento (RF-020).
 */
public record EmitirNotaRequest(
        @NotNull @DecimalMin(value = "0.01") BigDecimal valor,
        @Size(max = 64) String referencia,
        boolean simularFalha) {
}
