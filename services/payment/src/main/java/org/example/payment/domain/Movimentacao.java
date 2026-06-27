package org.example.payment.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Movimentação financeira de uma {@link Carteira} (RF-012) — também o histórico (RF-015).
 *
 * <p>Cada entrada/saída registra o {@code saldoApos} para auditoria do extrato.
 */
@Entity
@Table(name = "movimentacao")
public class Movimentacao extends PanacheEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "carteira_id", nullable = false)
    public Carteira carteira;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 8)
    public TipoMovimentacao tipo;

    @Column(name = "valor", nullable = false, precision = 18, scale = 2)
    public BigDecimal valor;

    @Column(name = "saldo_apos", nullable = false, precision = 18, scale = 2)
    public BigDecimal saldoApos;

    @Column(name = "descricao", length = 255)
    public String descricao;

    @Column(name = "criado_em", nullable = false)
    public OffsetDateTime criadoEm = OffsetDateTime.now();
}
