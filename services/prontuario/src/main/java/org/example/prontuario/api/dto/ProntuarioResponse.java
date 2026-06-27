package org.example.prontuario.api.dto;

import org.example.prontuario.domain.Prontuario;

import java.time.OffsetDateTime;

/** Representação de um prontuário (RF-006/007). */
public record ProntuarioResponse(Long id, String pacienteId, OffsetDateTime criadoEm) {

    public static ProntuarioResponse from(Prontuario p) {
        return new ProntuarioResponse(p.id, p.pacienteId, p.criadoEm);
    }
}
