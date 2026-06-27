package org.example.saga.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Inicia a SAGA pagar→emitir NF.
 *
 * <p>{@code simularFalhaNota} (knob de simulação) força a falha do passo de emissão,
 * exercitando a compensação do pagamento (RF-H-016).
 */
public record PagarEEmitirRequest(
        @NotNull Long carteiraId,
        @NotNull @DecimalMin("0.01") BigDecimal valor,
        boolean simularFalhaNota) {
}
