package org.example.prontuario.domain;

/** Ciclo de vida de uma consulta (RF-008..010). */
public enum StatusConsulta {
    /** Consulta registrada e ainda editável. */
    EM_ANDAMENTO,
    /** Consulta finalizada — imutável (RF-010). */
    FINALIZADA
}
