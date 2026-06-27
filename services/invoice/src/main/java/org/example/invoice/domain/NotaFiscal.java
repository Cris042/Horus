package org.example.invoice.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Nota fiscal simulada (RF-016..020).
 *
 * <p>Nasce {@code PENDENTE} ao receber a solicitação; a emissão simulada a leva a
 * {@code EMITIDA} (com {@code numero}) ou {@code FALHA} (com {@code motivoFalha}),
 * podendo ser reprocessada (RF-020).
 */
@Entity
@Table(name = "nota_fiscal")
public class NotaFiscal extends PanacheEntity {

    @Column(name = "valor", nullable = false, precision = 18, scale = 2)
    public BigDecimal valor;

    @Column(name = "referencia", length = 64)
    public String referencia;

    @Column(name = "numero", length = 32)
    public String numero;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    public StatusNota status = StatusNota.PENDENTE;

    @Column(name = "motivo_falha", length = 255)
    public String motivoFalha;

    @Column(name = "tentativas", nullable = false)
    public int tentativas = 0;

    @Column(name = "criado_em", nullable = false)
    public OffsetDateTime criadoEm = OffsetDateTime.now();

    @Column(name = "atualizado_em")
    public OffsetDateTime atualizadoEm;

    public boolean falha() {
        return status == StatusNota.FALHA;
    }
}
