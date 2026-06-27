package org.example.saga.domain;

/** Estado de uma SAGA pagar→emitir NF (ADR-0013). */
public enum StatusSaga {
    /** Iniciada, nenhum passo concluído. */
    INICIADA,
    /** Pagamento aprovado (passo 1 ok). */
    PAGAMENTO_APROVADO,
    /** Todos os passos concluídos com sucesso. */
    CONCLUIDA,
    /** Um passo falhou e os anteriores foram compensados. */
    COMPENSADA
}
