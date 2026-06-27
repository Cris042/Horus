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
 * Pagamento contra uma {@link Carteira} (RF-013/014).
 *
 * <p>Nasce {@code PENDENTE}; ao ser aprovado debita a carteira (gera uma {@code SAIDA}),
 * ao ser rejeitado não a afeta, e ao ser estornado devolve o valor (gera uma {@code ENTRADA}).
 */
@Entity
@Table(name = "pagamento")
public class Pagamento extends PanacheEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "carteira_id", nullable = false)
    public Carteira carteira;

    @Column(name = "valor", nullable = false, precision = 18, scale = 2)
    public BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    public StatusPagamento status = StatusPagamento.PENDENTE;

    @Column(name = "criado_em", nullable = false)
    public OffsetDateTime criadoEm = OffsetDateTime.now();

    @Column(name = "processado_em")
    public OffsetDateTime processadoEm;

    public boolean pendente() {
        return status == StatusPagamento.PENDENTE;
    }

    public boolean aprovado() {
        return status == StatusPagamento.APROVADO;
    }
}
