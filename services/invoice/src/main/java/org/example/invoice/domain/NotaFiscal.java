package org.example.invoice.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * Nota fiscal simulada (scaffold T-101).
 *
 * <p>Entidade mínima que prova a fiação Hibernate Panache ↔ {@code invoice_db}.
 * Emissão, reprocessamento e listagem entram em T-104.
 */
@Entity
@Table(name = "nota_fiscal")
public class NotaFiscal extends PanacheEntity {

    @Column(name = "valor", nullable = false, precision = 18, scale = 2)
    public BigDecimal valor;

    @Column(name = "status", nullable = false, length = 32)
    public String status = "PENDENTE";
}
