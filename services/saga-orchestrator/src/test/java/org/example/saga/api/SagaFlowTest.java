package org.example.saga.api;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.example.saga.client.InvoiceClient;
import org.example.saga.client.NotaDto;
import org.example.saga.client.PagamentoDto;
import org.example.saga.client.PaymentClient;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    }
}
