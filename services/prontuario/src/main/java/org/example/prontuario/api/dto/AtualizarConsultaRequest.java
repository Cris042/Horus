package org.example.prontuario.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Dados para atualizar uma consulta em andamento (RF-009). */
public record AtualizarConsultaRequest(
        @NotBlank @Size(max = 2000) String descricao) {
}
