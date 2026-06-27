package org.example.payment.api.dto;

import org.example.payment.domain.Carteira;

import java.math.BigDecimal;

/** Carteira e saldo (RF-011). */
public record CarteiraResponse(Long id, String titularId, BigDecimal saldo) {

    public static CarteiraResponse from(Carteira c) {
        return new CarteiraResponse(c.id, c.titularId, c.saldo);
    }
}
