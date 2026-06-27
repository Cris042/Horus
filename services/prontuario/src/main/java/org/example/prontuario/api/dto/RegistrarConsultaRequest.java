package org.example.prontuario.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Dados para registrar uma consulta (RF-008). */
public record RegistrarConsultaRequest(
        @NotBlank @Size(max = 2000) String descricao) {
}
