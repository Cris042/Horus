package org.example.saga.service;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.example.saga.client.InvoiceClient;
import org.example.saga.client.NotaDto;
import org.example.saga.client.PagamentoDto;
import org.example.saga.client.PaymentClient;
import org.example.saga.domain.Saga;
import org.example.saga.domain.StatusSaga;
import org.jboss.logging.Logger;

import java.math.BigDecimal;
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

    @Inject
    SagaStore store;

    /**
     * Executa a SAGA. Sem transação própria (T-1010): cada transição é gravada pelo
     * {@link SagaStore} antes do próximo efeito remoto, para que um crash deixe estado recuperável.
     */
    public Saga pagarEEmitir(Long carteiraId, BigDecimal valor, boolean simularFalhaNota) {
        Long id = store.iniciar(carteiraId, valor);
        Long pagamentoId = null;
        try {
            // Passo 1 — aprovar pagamento (compensável). O id do pagamento é gravado ANTES da
            // aprovação: se o orquestrador cair entre os dois, a recuperação ainda sabe o que estornar.
            PagamentoDto pag = executarPasso(id, "reserve-payment", () -> {
                PagamentoDto p = payment.criarPagamento(carteiraId, new PaymentClient.CriarPagamento(valor));
                store.registrarPagamento(id, p.id());
                payment.aprovar(p.id());
                return p;
            });
            pagamentoId = pag.id();
            store.marcar(id, StatusSaga.PAGAMENTO_APROVADO, null, null);

            // Passo 2 — emitir NF (último passo). A checagem de FALHA fica DENTRO do passo: o span
            // do passo precisa terminar em ERROR quando a NF falha (T-1002).
            NotaDto nota = executarPasso(id, "issue-invoice", () -> {
                NotaDto n = invoice.emitir(new InvoiceClient.EmitirNota(valor, "saga-" + id, simularFalhaNota));
                if (n.status() != null && n.status().equals("FALHA")) {
                    throw new PassoSagaException("emissão da NF retornou FALHA");
                }
                return n;
            });
            store.marcar(id, StatusSaga.CONCLUIDA, nota.id(), null);
            LOG.infof("SAGA %d concluída (pagamento=%d, nota=%d)", id, pagamentoId, nota.id());

            // Passo final (best-effort, RF-021/T-302): solicita o relatório/e-mail da NF emitida.
            // Falha aqui NÃO compensa a SAGA (já concluída) — apenas é registrada.
            try {
                invoice.solicitarRelatorio(nota.id());
            } catch (RuntimeException ex) {
                LOG.warnf(ex, "SAGA %d: falha ao solicitar relatório da nota %d (não compensa)", id, nota.id());
            }
        } catch (RuntimeException e) {
            Saga atual = store.buscar(id);
            compensar(id, atual.pagamentoId, e.getMessage(), false);
        }
        return store.buscar(id);
    }

    /**
     * Compensa os passos já executados e marca a SAGA {@code COMPENSADA}. Idempotente: só estorna
     * se houve pagamento; usado pelo fluxo normal e pela recuperação automática ({@code recovery}).
     */
    void compensar(Long id, Long pagamentoId, String motivo, boolean recovery) {
        if (pagamentoId != null) {
            try {
                executarCompensacao(id, "reserve-payment", recovery, () -> payment.estornar(pagamentoId));
            } catch (RuntimeException ex) {
                LOG.errorf(ex, "Falha ao compensar pagamento %d da SAGA %d", pagamentoId, id);
            }
        }
        store.marcar(id, StatusSaga.COMPENSADA, null, motivo);
        LOG.warnf("SAGA %d compensada: %s", id, motivo);
    }

    /**
     * Executa um passo da SAGA dentro de um span {@code saga.{flow}.{step}} (RF-H-016,
     * contrato T-005 §3.2/3.4) — INTERNAL, com os atributos {@code horus.saga.*}.
     */
    private <T> T executarPasso(Long sagaId, String step, Supplier<T> acao) {
        Span span = novoSpan(sagaId, step, false, false);
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
    private void executarCompensacao(Long sagaId, String step, boolean recovery, Runnable acao) {
        Span span = novoSpan(sagaId, step, true, recovery);
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

    private Span novoSpan(Long sagaId, String step, boolean compensation, boolean recovery) {
        String name = "saga." + FLOW + "." + step + (compensation ? ".compensate" : "");
        var builder = tracer.spanBuilder(name)
                .setSpanKind(SpanKind.INTERNAL)
                .setAttribute("horus.saga.id", String.valueOf(sagaId))
                .setAttribute("horus.saga.flow", FLOW)
                .setAttribute("horus.saga.step", step);
        if (compensation) {
            builder.setAttribute("horus.saga.compensation", true);
        }
        if (recovery) {
            builder.setAttribute("horus.saga.recovery", true);
        }
        return builder.startSpan();
    }

    public Saga buscar(Long id) {
        return store.buscar(id);
    }
}
