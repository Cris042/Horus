package org.example.saga.service;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.example.saga.domain.Saga;
import org.jboss.logging.Logger;

import java.time.Duration;
import java.time.OffsetDateTime;

/**
 * Recuperação automática de SAGAs interrompidas (T-1010, ADR-0013 — risco registrado na T-107:
 * "falha do orquestrador entre passos deixa SAGA pendurada").
 *
 * <p>A cada {@code saga.recovery.interval}, toda SAGA parada em {@code INICIADA} ou
 * {@code PAGAMENTO_APROVADO} há mais de {@code saga.recovery.timeout} é compensada (estorno do
 * pagamento, se houver) e marcada {@code COMPENSADA} com o motivo "timeout". A compensação sai com
 * o span {@code saga.pay-then-invoice.reserve-payment.compensate} + {@code horus.saga.recovery=true}.
 */
@ApplicationScoped
public class SagaRecovery {

    private static final Logger LOG = Logger.getLogger(SagaRecovery.class);

    @Inject
    SagaStore store;

    @Inject
    SagaService sagas;

    @ConfigProperty(name = "saga.recovery.timeout", defaultValue = "5m")
    Duration timeout;

    @Scheduled(every = "{saga.recovery.interval:1m}", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    void scheduled() {
        recover();
    }

    /** Recupera as SAGAs interrompidas; devolve quantas foram compensadas. */
    public int recover() {
        int count = 0;
        for (Saga saga : store.interrompidas(OffsetDateTime.now().minus(timeout))) {
            try {
                sagas.compensar(saga.id, saga.pagamentoId,
                        "timeout: SAGA interrompida em " + saga.status + " (recuperação automática)", true);
                count++;
            } catch (RuntimeException e) {
                LOG.errorf(e, "Recuperação da SAGA %d falhou — nova tentativa no próximo ciclo", saga.id);
            }
        }
        if (count > 0) {
            LOG.warnf("Recuperação de SAGAs: %d SAGA(s) interrompida(s) compensada(s)", count);
        }
        return count;
    }
}
