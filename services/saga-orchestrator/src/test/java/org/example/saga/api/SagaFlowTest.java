package org.example.saga.api;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.example.saga.client.InvoiceClient;
import org.example.saga.client.NotaDto;
import org.example.saga.client.PagamentoDto;
import org.example.saga.client.PaymentClient;
import io.quarkus.narayana.jta.QuarkusTransaction;
import jakarta.inject.Inject;
import org.example.saga.domain.Saga;
import org.example.saga.domain.StatusSaga;
import org.example.saga.service.SagaRecovery;
import org.example.saga.service.SagaStore;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

/**
 * SAGA pagar→emitir NF (ADR-0013) com os participantes mockados (sem HTTP real):
 * verifica conclusão no caminho feliz e compensação (estorno) quando a emissão falha.
 * O estado da SAGA é persistido em {@code saga_db} (Dev Services) — roda no CI.
 */
@QuarkusTest
class SagaFlowTest {

    @InjectMock
    @RestClient
    PaymentClient payment;

    @InjectMock
    @RestClient
    InvoiceClient invoice;

    @Test
    void caminhoFelizConclui() {
        when(payment.criarPagamento(any(), any())).thenReturn(new PagamentoDto(1L, "PENDENTE"));
        when(payment.aprovar(1L)).thenReturn(new PagamentoDto(1L, "APROVADO"));
        when(invoice.emitir(any())).thenReturn(new NotaDto(10L, "EMITIDA", "NF-00000010"));

        given()
                .contentType("application/json")
                .body("{\"carteiraId\":5,\"valor\":30.00}")
                .when().post("/sagas/pagar-e-emitir")
                .then()
                .statusCode(201)
                .body("status", is("CONCLUIDA"))
                .body("pagamentoId", is(1))
                .body("notaId", is(10));

        verify(payment, never()).estornar(any());
        // T-302: após concluir, solicita o relatório da NF emitida
        verify(invoice).solicitarRelatorio(eq(10L));
    }

    @Test
    void falhaNaEmissaoCompensaPagamento() {
        when(payment.criarPagamento(any(), any())).thenReturn(new PagamentoDto(2L, "PENDENTE"));
        when(payment.aprovar(2L)).thenReturn(new PagamentoDto(2L, "APROVADO"));
        // Emissão retorna FALHA -> passo 2 falha
        when(invoice.emitir(any())).thenReturn(new NotaDto(null, "FALHA", null));
        when(payment.estornar(2L)).thenReturn(new PagamentoDto(2L, "ESTORNADO"));

        given()
                .contentType("application/json")
                .body("{\"carteiraId\":5,\"valor\":30.00,\"simularFalhaNota\":true}")
                .when().post("/sagas/pagar-e-emitir")
                .then()
                .statusCode(200)
                .body("status", is("COMPENSADA"))
                .body("pagamentoId", is(2))
                .body("motivoFalha", notNullValue());

        // Compensação do passo de pagamento foi disparada
        verify(payment).estornar(eq(2L));
        // Sem emissão concluída, não há solicitação de relatório
        verify(invoice, never()).solicitarRelatorio(any());
    }

    @Inject
    SagaStore store;

    @Inject
    SagaRecovery recovery;

    /** SAGA "pendurada" (orquestrador caiu depois de aprovar o pagamento), com a última transição há 10 min. */
    private Long sagaInterrompida(long pagamentoId, int minutosAtras) {
        Long id = store.iniciar(7L, new BigDecimal("12.00"));
        store.registrarPagamento(id, pagamentoId);
        store.marcar(id, StatusSaga.PAGAMENTO_APROVADO, null, null);
        OffsetDateTime quando = OffsetDateTime.now().minusMinutes(minutosAtras);
        QuarkusTransaction.requiringNew().run(() -> Saga.update("atualizadoEm = ?1 where id = ?2", quando, id));
        return id;
    }

    @Test
    void sagaInterrompida_eCompensadaPelaRecuperacao() {
        when(payment.estornar(40L)).thenReturn(new PagamentoDto(40L, "ESTORNADO"));
        Long id = sagaInterrompida(40L, 10);

        assertTrue(recovery.recover() >= 1);

        Saga saga = store.buscar(id);
        assertEquals(StatusSaga.COMPENSADA, saga.status);
        assertTrue(saga.motivoFalha.contains("timeout"));
        verify(payment).estornar(eq(40L));
    }

    @Test
    void sagaRecente_naoETocadaPelaRecuperacao() {
        Long id = sagaInterrompida(41L, 0);

        recovery.recover();

        assertEquals(StatusSaga.PAGAMENTO_APROVADO, store.buscar(id).status);
        verify(payment, never()).estornar(eq(41L));
    }

    @Test
    void falhaNaAprovacao_aindaCompensa_porquePagamentoFoiGravadoAntes() {
        when(payment.criarPagamento(any(), any())).thenReturn(new PagamentoDto(42L, "PENDENTE"));
        doThrow(new RuntimeException("timeout no payment")).when(payment).aprovar(42L);

        given().contentType("application/json")
                .body("{\"carteiraId\":5,\"valor\":30.00}")
                .when().post("/sagas/pagar-e-emitir")
                .then().statusCode(200)
                .body("status", is("COMPENSADA"))
                .body("pagamentoId", is(42));

        verify(payment).estornar(eq(42L));
    }
}
