package org.example.payment.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import org.example.payment.domain.Carteira;
import org.example.payment.domain.Movimentacao;
import org.example.payment.domain.TipoMovimentacao;

import java.math.BigDecimal;
import java.util.List;

/** Casos de uso de carteira/saldo e movimentações (RF-011/012/015). Persiste só em {@code payment_db} (RF-026). */
@ApplicationScoped
public class CarteiraService {

    @Transactional
    public Carteira criar(String titularId, BigDecimal saldoInicial) {
        Carteira c = new Carteira();
        c.titularId = titularId;
        c.saldo = saldoInicial;
        c.persist();
        return c;
    }

    public Carteira buscar(Long id) {
        Carteira c = Carteira.findById(id);
        if (c == null) {
            throw new NotFoundException("Carteira " + id + " não encontrada");
        }
        return c;
    }

    /**
     * Aplica uma movimentação à carteira, atualizando o saldo e registrando o extrato (RF-012/015).
     * Saídas não podem deixar o saldo negativo.
     */
    @Transactional
    public Movimentacao movimentar(Long carteiraId, TipoMovimentacao tipo, BigDecimal valor, String descricao) {
        Carteira c = buscar(carteiraId);
        return aplicar(c, tipo, valor, descricao);
    }

    /** Aplica a movimentação a uma carteira já carregada (reuso interno por {@code PagamentoService}). */
    @Transactional
    public Movimentacao aplicar(Carteira c, TipoMovimentacao tipo, BigDecimal valor, String descricao) {
        BigDecimal novoSaldo = tipo == TipoMovimentacao.ENTRADA
                ? c.saldo.add(valor)
                : c.saldo.subtract(valor);
        if (novoSaldo.signum() < 0) {
            throw new ConflitoPagamentoException("Saldo insuficiente na carteira " + c.id);
        }
        c.saldo = novoSaldo;

        Movimentacao m = new Movimentacao();
        m.carteira = c;
        m.tipo = tipo;
        m.valor = valor;
        m.saldoApos = novoSaldo;
        m.descricao = descricao;
        m.persist();
        return m;
    }

    public List<Movimentacao> historico(Long carteiraId) {
        buscar(carteiraId); // 404 se não existir
        return Movimentacao.list("carteira.id = ?1 order by id", carteiraId);
    }
}
