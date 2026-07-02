package org.example.saga.service;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
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
import java.util.function.Supplier;

/**
 * Orquestrador da SAGA {@code pay-then-invoice} (ADR-0013): estado persistido + compensação.
 *
 * <p>Passo 1 aprova um pagamento (compensável por estorno); passo 2 emite a NF. Se o passo 2
 * falhar (HTTP ou emissão {@code FALHA}), o passo 1 é compensado e a SAGA termina {@code COMPENSADA}.
 * Cada passo e a compensação são chamadas HTTP instrumentadas pelo OTel (mesmo {@code trace_id}),
 * envolvidas por um span {@code saga.{flow}.{step}} próprio (RF-H-016, contrato T-005 §3.2/3.4)
 * — é esse span que a visualização de SAGA do Horus (T-507) usa para montar a linha do tempo.
 * <b>Achado em T-905:</b> antes desta task, nenhum span com esse nome era emitido — a
 * visualização só tinha sido validada com dados sintéticos, nunca contra uma SAGA real.
 */
@ApplicationScoped
public class SagaService {

    private static final Logger LOG = Logger.getLogger(SagaService.class);
    private static final String FLOW = "pay-then-invoice";

    @RestClient
    PaymentClient payment;

    @RestClient
    InvoiceClient invoice;

    @Inject
    Tracer tracer;

    @Transactional
    public Saga pagarEEmitir(Long carteiraId, BigDecimal valor, boolean simularFalhaNota) {
        Saga saga = new Saga();
        saga.carteiraId = carteiraId;
        saga.valor = valor;
        saga.persist();

        try {
            // Passo 1 — aprovar pagamento (compensável)
            PagamentoDto pag = executarPasso(saga, "reserve-payment", () -> {
                PagamentoDto p = payment.criarPagamento(carteiraId, new PaymentClient.CriarPagamento(valor));
                payment.aprovar(p.id());
                return p;
            });
            saga.pagamentoId = pag.id();
            marcar(saga, StatusSaga.PAGAMENTO_APROVADO);

            // Passo 2 — emitir NF (último passo)
            NotaDto nota = executarPasso(saga, "issue-invoice", () -> invoice.emitir(
                    new InvoiceClient.EmitirNota(valor, "saga-" + saga.id, simularFalhaNota)));
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
                executarCompensacao(saga, "reserve-payment", () -> payment.estornar(saga.pagamentoId));
            } catch (RuntimeException ex) {
                LOG.errorf(ex, "Falha ao compensar pagamento %d da SAGA %d", saga.pagamentoId, saga.id);
            }
        }
        saga.motivoFalha = motivo;
        marcar(saga, StatusSaga.COMPENSADA);
        LOG.warnf("SAGA %d compensada: %s", saga.id, motivo);
    }

    /**
     * Executa um passo da SAGA dentro de um span {@code saga.{flow}.{step}} (RF-H-016,
     * contrato T-005 §3.2/3.4) — INTERNAL, com os atributos {@code horus.saga.*}.
     */
    private <T> T executarPasso(Saga saga, String step, Supplier<T> acao) {
        Span span = novoSpan(saga, step, false);
        try (Scope scope = span.makeCurrent()) {
            T resultado = acao.get();
            span.setStatus(StatusCode.OK);
            return resultado;
        } catch (RuntimeException e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, e.getMessage());
            throw e;
        } finally {
            span.end();
        }
    }

    /** Mesmo contrato do passo, mas nomeado {@code saga.{flow}.{step}.compensate}. */
    private void executarCompensacao(Saga saga, String step, Runnable acao) {
        Span span = novoSpan(saga, step, true);
        try (Scope scope = span.makeCurrent()) {
            acao.run();
            span.setStatus(StatusCode.OK);
        } catch (RuntimeException e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, e.getMessage());
            throw e;
        } finally {
            span.end();
        }
    }

    private Span novoSpan(Saga saga, String step, boolean compensation) {
        String name = "saga." + FLOW + "." + step + (compensation ? ".compensate" : "");
        var builder = tracer.spanBuilder(name)
                .setSpanKind(SpanKind.INTERNAL)
                .setAttribute("horus.saga.id", String.valueOf(saga.id))
                .setAttribute("horus.saga.flow", FLOW)
                .setAttribute("horus.saga.step", step);
        if (compensation) {
            builder.setAttribute("horus.saga.compensation", true);
        }
        return builder.startSpan();
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
