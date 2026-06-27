package org.example.prontuario.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Dados para criar um prontuário (RF-006). */
public record CriarProntuarioRequest(
        @NotBlank @Size(max = 64) String pacienteId) {
}
