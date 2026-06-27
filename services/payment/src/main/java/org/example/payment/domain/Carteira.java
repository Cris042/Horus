package org.example.payment.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * Carteira/saldo de um titular (scaffold T-101).
 *
 * <p>Entidade mínima que prova a fiação Hibernate Panache ↔ {@code payment_db}.
 * Movimentações, aprovação/rejeição/estorno e histórico entram em T-103.
 */
@Entity
@Table(name = "carteira")
public class Carteira extends PanacheEntity {

    @Column(name = "titular_id", nullable = false, length = 64)
    public String titularId;

    @Column(name = "saldo", nullable = false, precision = 18, scale = 2)
    public BigDecimal saldo = BigDecimal.ZERO;
}
