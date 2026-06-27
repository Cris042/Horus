package org.example.invoice.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import org.example.invoice.domain.NotaFiscal;
import org.example.invoice.domain.StatusNota;
import org.example.invoice.relatorio.RelatorioMensagem;
import org.example.invoice.relatorio.RelatorioPublisher;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Casos de uso da nota fiscal simulada (RF-016..020). Persiste só em {@code invoice_db} (RF-027).
 *
 * <p>A "emissão" é simulada: por padrão gera um número e marca {@code EMITIDA}; o pedido pode
 * forçar uma falha ({@code simularFalha}) para exercitar o reprocessamento.
 */
@ApplicationScoped
public class NotaFiscalService {

    @Inject
    RelatorioPublisher relatorios;

    /**
     * Solicita um relatório da NF (RF-021): publica a mensagem no RabbitMQ de forma assíncrona
     * (RNF-011). Exige nota {@code EMITIDA}.
     */
    public RelatorioMensagem solicitarRelatorio(Long notaId) {
        NotaFiscal n = buscar(notaId);
        if (n.status != StatusNota.EMITIDA) {
            throw new ConflitoNotaException(
                    "Nota " + notaId + " não está emitida; estado atual: " + n.status);
        }
        RelatorioMensagem msg = new RelatorioMensagem(
                UUID.randomUUID().toString(), RelatorioMensagem.TIPO_NOTA_FISCAL,
                n.id, n.valor, n.numero, OffsetDateTime.now());
        relatorios.publicar(msg);
        return msg;
    }

    /** Recebe a solicitação (RF-016) e tenta emitir (RF-017/018). */
    @Transactional
    public NotaFiscal emitir(BigDecimal valor, String referencia, boolean simularFalha) {
        NotaFiscal n = new NotaFiscal();
        n.valor = valor;
        n.referencia = referencia;
        n.persist();
        aplicarEmissao(n, simularFalha);
        return n;
    }

    /** Reprocessa uma nota em falha (RF-020); reprocessamento bem-sucedido emite a nota. */
    @Transactional
    public NotaFiscal reprocessar(Long id) {
        NotaFiscal n = buscar(id);
        if (!n.falha()) {
            throw new ConflitoNotaException(
                    "Nota " + id + " não está em falha; estado atual: " + n.status);
        }
        aplicarEmissao(n, false);
        return n;
    }

    public NotaFiscal buscar(Long id) {
        NotaFiscal n = NotaFiscal.findById(id);
        if (n == null) {
            throw new NotFoundException("Nota fiscal " + id + " não encontrada");
        }
        return n;
    }

    /** Lista as notas, opcionalmente filtrando por status (RF-019). */
    public List<NotaFiscal> listar(StatusNota status) {
        if (status == null) {
            return NotaFiscal.list("order by id");
        }
        return NotaFiscal.list("status = ?1 order by id", status);
    }

    private void aplicarEmissao(NotaFiscal n, boolean simularFalha) {
        n.tentativas++;
        n.atualizadoEm = OffsetDateTime.now();
        if (simularFalha) {
            n.status = StatusNota.FALHA;
            n.motivoFalha = "Falha simulada na emissão";
            n.numero = null;
        } else {
            n.status = StatusNota.EMITIDA;
            n.motivoFalha = null;
            n.numero = String.format("NF-%08d", n.id);
        }
    }
}
