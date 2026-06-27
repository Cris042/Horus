package org.example.prontuario.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

/**
 * Prontuário de um paciente (scaffold T-101).
 *
 * <p>Entidade mínima que prova a fiação Hibernate Panache ↔ {@code prontuario_db}.
 * O modelo de domínio real (consultas, histórico clínico) entra em T-102.
 */
@Entity
@Table(name = "prontuario")
public class Prontuario extends PanacheEntity {

    @Column(name = "paciente_id", nullable = false, length = 64)
    public String pacienteId;

    @Column(name = "criado_em", nullable = false)
    public OffsetDateTime criadoEm = OffsetDateTime.now();
}
