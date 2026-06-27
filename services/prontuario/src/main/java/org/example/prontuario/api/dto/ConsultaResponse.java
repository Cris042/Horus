package org.example.prontuario.api.dto;

import org.example.prontuario.domain.Consulta;

import java.time.OffsetDateTime;

/** Representação de uma consulta (RF-008..010). */
public record ConsultaResponse(
        Long id,
        Long prontuarioId,
        String descricao,
        String status,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm,
        OffsetDateTime finalizadoEm) {

    public static ConsultaResponse from(Consulta c) {
        return new ConsultaResponse(
                c.id, c.prontuario.id, c.descricao, c.status.name(),
                c.criadoEm, c.atualizadoEm, c.finalizadoEm);
    }
}
