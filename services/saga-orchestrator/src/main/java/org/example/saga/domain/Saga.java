package org.example.saga.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Estado persistido de uma SAGA (ADR-0013): auditável e recuperável.
 *
 * <p>Fluxo {@code pay-then-invoice}: aprova um pagamento (passo 1, compensável por estorno) e
 * emite uma nota fiscal (passo 2). Falha no passo 2 dispara a compensação do passo 1.
 */
@Entity
@Table(name = "saga")
public class Saga extends PanacheEntity {

    @Column(name = "fluxo", nullable = false, length = 32)
    public String fluxo = "pay-then-invoice";

    @Column(name = "carteira_id", nullable = false)
    public Long carteiraId;

    @Column(name = "valor", nullable = false, precision = 18, scale = 2)
    public BigDecimal valor;

    @Column(name = "pagamento_id")
    public Long pagamentoId;

    @Column(name = "nota_id")
    public Long notaId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 24)
    public StatusSaga status = StatusSaga.INICIADA;

    @Column(name = "motivo_falha", length = 255)
    public String motivoFalha;

    @Column(name = "criado_em", nullable = false)
    public OffsetDateTime criadoEm = OffsetDateTime.now();

    @Column(name = "atualizado_em")
    public OffsetDateTime atualizadoEm;
}
