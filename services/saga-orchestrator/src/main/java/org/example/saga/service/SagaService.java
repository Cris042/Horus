package org.example.saga.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.example.saga.client.InvoiceClient;
import org.example.saga.client.NotaDto;
import org.example.saga.client.PagamentoDto;
import org.example.saga.client.PaymentClient;
import org.example.saga.domain.Saga;
import org.example.saga.domain.StatusSaga;
import org.jboss.logging.Logger;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Orquestrador da SAGA {@code pay-then-invoice} (ADR-0013): estado persistido + compensação.
 *
 * <p>Passo 1 aprova um pagamento (compensável por estorno); passo 2 emite a NF. Se o passo 2
 * falhar (HTTP ou emissão {@code FALHA}), o passo 1 é compensado e a SAGA termina {@code COMPENSADA}.
 * Cada passo e a compensação são chamadas HTTP instrumentadas pelo OTel (mesmo {@code trace_id}).
 */
@ApplicationScoped
public class SagaService {

    private static final Logger LOG = Logger.getLogger(SagaService.class);

    @RestClient
    PaymentClient payment;

    @RestClient
    InvoiceClient invoice;

    @Transactional
    public Saga pagarEEmitir(Long carteiraId, BigDecimal valor, boolean simularFalhaNota) {
        Saga saga = new Saga();
        saga.carteiraId = carteiraId;
        saga.valor = valor;
        saga.persist();

        try {
            // Passo 1 — aprovar pagamento (compensável)
            PagamentoDto pag = payment.criarPagamento(carteiraId, new PaymentClient.CriarPagamento(valor));
            payment.aprovar(pag.id());
            saga.pagamentoId = pag.id();
            marcar(saga, StatusSaga.PAGAMENTO_APROVADO);

            // Passo 2 — emitir NF (último passo)
            NotaDto nota = invoice.emitir(
                    new InvoiceClient.EmitirNota(valor, "saga-" + saga.id, simularFalhaNota));
            if (nota.status() != null && nota.status().equals("FALHA")) {
                throw new PassoSagaException("emissão da NF retornou FALHA");
            }
            saga.notaId = nota.id();
            marcar(saga, StatusSaga.CONCLUIDA);
            LOG.infof("SAGA %d concluída (pagamento=%d, nota=%d)", saga.id, saga.pagamentoId, saga.notaId);

            // Passo final (best-effort, RF-021/T-302): solicita o relatório/e-mail da NF emitida.
            // Falha aqui NÃO compensa a SAGA (já concluída) — apenas é registrada.
            try {
                invoice.solicitarRelatorio(saga.notaId);
            } catch (RuntimeException ex) {
                LOG.warnf(ex, "SAGA %d: falha ao solicitar relatório da nota %d (não compensa)",
                        saga.id, saga.notaId);
            }
        } catch (RuntimeException e) {
            compensar(saga, e.getMessage());
        }
        return saga;
    }

    /** Compensa os passos já executados (idempotente: só estorna se houve aprovação). */
    private void compensar(Saga saga, String motivo) {
        if (saga.pagamentoId != null) {
            try {
                payment.estornar(saga.pagamentoId);
            } catch (RuntimeException ex) {
                LOG.errorf(ex, "Falha ao compensar pagamento %d da SAGA %d", saga.pagamentoId, saga.id);
            }
        }
        saga.motivoFalha = motivo;
        marcar(saga, StatusSaga.COMPENSADA);
        LOG.warnf("SAGA %d compensada: %s", saga.id, motivo);
    }

    private void marcar(Saga saga, StatusSaga status) {
        saga.status = status;
        saga.atualizadoEm = OffsetDateTime.now();
    }

    public Saga buscar(Long id) {
        Saga s = Saga.findById(id);
        if (s == null) {
            throw new NotFoundException("SAGA " + id + " não encontrada");
        }
        return s;
    }
}
