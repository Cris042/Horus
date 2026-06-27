package org.example.payment.api.dto;

import org.example.payment.domain.Pagamento;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/** Pagamento e seu estado (RF-013/014). */
public record PagamentoResponse(
        Long id,
        Long carteiraId,
        BigDecimal valor,
        String status,
        OffsetDateTime criadoEm,
        OffsetDateTime processadoEm) {

    public static PagamentoResponse from(Pagamento p) {
        return new PagamentoResponse(
                p.id, p.carteira.id, p.valor, p.status.name(), p.criadoEm, p.processadoEm);
    }
}
