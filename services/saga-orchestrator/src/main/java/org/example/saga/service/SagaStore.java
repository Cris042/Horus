package org.example.saga.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import org.example.saga.domain.Saga;
import org.example.saga.domain.StatusSaga;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Persistência do estado da SAGA com <b>uma transação por transição</b> (T-1010).
 *
 * <p>Antes, toda a SAGA rodava numa única transação que atravessava as chamadas HTTP: um crash no
 * meio desfazia o registro local, mas o pagamento já aprovado no payment-service ficava órfão — sem
 * estado para a recuperação encontrar. Agora cada transição é gravada ({@code REQUIRES_NEW}) antes
 * do próximo efeito remoto, e nenhuma conexão de banco fica presa durante as chamadas HTTP.
 */
@ApplicationScoped
public class SagaStore {

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public Long iniciar(Long carteiraId, BigDecimal valor) {
        Saga saga = new Saga();
        saga.carteiraId = carteiraId;
        saga.valor = valor;
        saga.atualizadoEm = saga.criadoEm;
        saga.persist();
        return saga.id;
    }

    /** Registra o pagamento criado <em>antes</em> de aprová-lo — a recuperação sabe o que estornar. */
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void registrarPagamento(Long id, Long pagamentoId) {
        Saga saga = carregar(id);
        saga.pagamentoId = pagamentoId;
        saga.atualizadoEm = OffsetDateTime.now();
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void marcar(Long id, StatusSaga status, Long notaId, String motivoFalha) {
        Saga saga = carregar(id);
        saga.status = status;
        if (notaId != null) {
            saga.notaId = notaId;
        }
        if (motivoFalha != null) {
            saga.motivoFalha = motivoFalha.length() > 255 ? motivoFalha.substring(0, 255) : motivoFalha;
        }
        saga.atualizadoEm = OffsetDateTime.now();
    }

    /** SAGAs paradas num estado intermediário desde antes de {@code antesDe} (candidatas à recuperação). */
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public List<Saga> interrompidas(OffsetDateTime antesDe) {
        return Saga.list("status in ?1 and atualizadoEm < ?2",
                List.of(StatusSaga.INICIADA, StatusSaga.PAGAMENTO_APROVADO), antesDe);
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public Saga buscar(Long id) {
        return carregar(id);
    }

    private static Saga carregar(Long id) {
        Saga s = Saga.findById(id);
        if (s == null) {
            throw new NotFoundException("SAGA " + id + " não encontrada");
        }
        return s;
    }
}
