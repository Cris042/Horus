package org.example.prontuario.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

/**
 * Consulta clínica vinculada a um {@link Prontuario} (RF-008..010).
 *
 * <p>Registrada {@code EM_ANDAMENTO}, pode ser atualizada (RF-009) e então finalizada
 * (RF-010), após o que se torna imutável.
 */
@Entity
@Table(name = "consulta")
public class Consulta extends PanacheEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "prontuario_id", nullable = false)
    public Prontuario prontuario;

    @Column(name = "descricao", nullable = false, length = 2000)
    public String descricao;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    public StatusConsulta status = StatusConsulta.EM_ANDAMENTO;

    @Column(name = "criado_em", nullable = false)
    public OffsetDateTime criadoEm = OffsetDateTime.now();

    @Column(name = "atualizado_em")
    public OffsetDateTime atualizadoEm;

    @Column(name = "finalizado_em")
    public OffsetDateTime finalizadoEm;

    public boolean finalizada() {
        return status == StatusConsulta.FINALIZADA;
    }
}
