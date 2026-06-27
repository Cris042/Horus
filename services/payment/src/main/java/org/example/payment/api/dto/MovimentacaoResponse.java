package org.example.payment.api.dto;

import org.example.payment.domain.Movimentacao;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/** Item do histórico financeiro (RF-015). */
public record MovimentacaoResponse(
        Long id,
        Long carteiraId,
        String tipo,
        BigDecimal valor,
        BigDecimal saldoApos,
        String descricao,
        OffsetDateTime criadoEm) {

    public static MovimentacaoResponse from(Movimentacao m) {
        return new MovimentacaoResponse(
                m.id, m.carteira.id, m.tipo.name(), m.valor, m.saldoApos, m.descricao, m.criadoEm);
    }
}
