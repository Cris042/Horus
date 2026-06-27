package org.example.saga.client;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.math.BigDecimal;

/** Cliente REST do invoice-service (passo de emissão de NF). */
@RegisterRestClient(configKey = "invoice-api")
@Produces(MediaType.APPLICATION_JSON)
public interface InvoiceClient {

    @POST
    @Path("/notas")
    NotaDto emitir(EmitirNota body);

    record EmitirNota(BigDecimal valor, String referencia, boolean simularFalha) {
    }
}
