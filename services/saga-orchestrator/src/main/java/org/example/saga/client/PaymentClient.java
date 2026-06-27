package org.example.saga.client;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.math.BigDecimal;

/** Cliente REST do payment-service (passo de pagamento e sua compensação). */
@RegisterRestClient(configKey = "payment-api")
@Produces(MediaType.APPLICATION_JSON)
public interface PaymentClient {

    @POST
    @Path("/carteiras/{carteiraId}/pagamentos")
    PagamentoDto criarPagamento(@PathParam("carteiraId") Long carteiraId, CriarPagamento body);

    @POST
    @Path("/pagamentos/{id}/aprovar")
    PagamentoDto aprovar(@PathParam("id") Long id);

    /** Compensação do passo de pagamento (RF-014). */
    @POST
    @Path("/pagamentos/{id}/estornar")
    PagamentoDto estornar(@PathParam("id") Long id);

    record CriarPagamento(BigDecimal valor) {
    }
}
