package org.example.payment.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.example.payment.domain.TipoMovimentacao;

import java.math.BigDecimal;

/** Registrar uma entrada/saída na carteira (RF-012). */
public record MovimentacaoRequest(
        @NotNull TipoMovimentacao tipo,
        @NotNull @DecimalMin(value = "0.01") BigDecimal valor,
        @Size(max = 255) String descricao) {
}
