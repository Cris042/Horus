package org.example.payment.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import org.example.payment.domain.Carteira;
import org.example.payment.domain.Pagamento;
import org.example.payment.domain.StatusPagamento;
import org.example.payment.domain.TipoMovimentacao;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/** Casos de uso de pagamento: criar, aprovar/rejeitar (RF-013) e estornar (RF-014). */
@ApplicationScoped
public class PagamentoService {

    @Inject
    CarteiraService carteiras;

    @Transactional
    public Pagamento criar(Long carteiraId, BigDecimal valor) {
        Carteira c = carteiras.buscar(carteiraId);
        Pagamento p = new Pagamento();
        p.carteira = c;
        p.valor = valor;
        p.persist();
        return p;
    }

    /** Aprova um pagamento pendente, debitando a carteira (RF-013). 409 se sem saldo. */
    @Transactional
    public Pagamento aprovar(Long pagamentoId) {
        Pagamento p = buscar(pagamentoId);
        exigirPendente(p, "aprovado");
        carteiras.aplicar(p.carteira, TipoMovimentacao.SAIDA, p.valor, "Pagamento " + p.id);
        p.status = StatusPagamento.APROVADO;
        p.processadoEm = OffsetDateTime.now();
        return p;
    }

    /** Rejeita um pagamento pendente, sem afetar o saldo (RF-013). */
    @Transactional
    public Pagamento rejeitar(Long pagamentoId) {
        Pagamento p = buscar(pagamentoId);
        exigirPendente(p, "rejeitado");
        p.status = StatusPagamento.REJEITADO;
        p.processadoEm = OffsetDateTime.now();
        return p;
    }

    /** Estorna um pagamento aprovado, devolvendo o valor à carteira (RF-014). */
    @Transactional
    public Pagamento estornar(Long pagamentoId) {
        Pagamento p = buscar(pagamentoId);
        if (!p.aprovado()) {
            throw new ConflitoPagamentoException(
                    "Pagamento " + pagamentoId + " não está aprovado; estado atual: " + p.status);
        }
        carteiras.aplicar(p.carteira, TipoMovimentacao.ENTRADA, p.valor, "Estorno do pagamento " + p.id);
        p.status = StatusPagamento.ESTORNADO;
        p.processadoEm = OffsetDateTime.now();
        return p;
    }

    public Pagamento buscar(Long pagamentoId) {
        Pagamento p = Pagamento.findById(pagamentoId);
        if (p == null) {
            throw new NotFoundException("Pagamento " + pagamentoId + " não encontrado");
        }
        return p;
    }

    private void exigirPendente(Pagamento p, String acao) {
        if (!p.pendente()) {
            throw new ConflitoPagamentoException(
                    "Pagamento " + p.id + " não pode ser " + acao + "; estado atual: " + p.status);
        }
    }
}
