package org.example.saga.api.dto;

import org.example.saga.domain.Saga;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/** Estado de uma SAGA (RF-H-016: passos, resultado e compensação). */
public record SagaResponse(
        Long id,
        String fluxo,
        Long carteiraId,
        BigDecimal valor,
        Long pagamentoId,
        Long notaId,
        String status,
        String motivoFalha,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm) {

    public static SagaResponse from(Saga s) {
        return new SagaResponse(
                s.id, s.fluxo, s.carteiraId, s.valor, s.pagamentoId, s.notaId,
                s.status.name(), s.motivoFalha, s.criadoEm, s.atualizadoEm);
    }
}
