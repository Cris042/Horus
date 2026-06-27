package org.example.invoice.api.dto;

import org.example.invoice.domain.NotaFiscal;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/** Representação de uma nota fiscal (RF-017..019). */
public record NotaFiscalResponse(
        Long id,
        BigDecimal valor,
        String referencia,
        String numero,
        String status,
        String motivoFalha,
        int tentativas,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm) {

    public static NotaFiscalResponse from(NotaFiscal n) {
        return new NotaFiscalResponse(
                n.id, n.valor, n.referencia, n.numero, n.status.name(),
                n.motivoFalha, n.tentativas, n.criadoEm, n.atualizadoEm);
    }
}
