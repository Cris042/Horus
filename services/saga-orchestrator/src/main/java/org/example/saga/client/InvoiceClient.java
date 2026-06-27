package org.example.saga.client;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.math.BigDecimal;

/** Cliente REST do invoice-service (emissão de NF e solicitação de relatório). */
@RegisterRestClient(configKey = "invoice-api")
@Produces(MediaType.APPLICATION_JSON)
public interface InvoiceClient {

    @POST
    @Path("/notas")
    NotaDto emitir(EmitirNota body);

    /** Solicita (assíncrono no invoice) o relatório/e-mail da NF emitida (RF-021/T-302). */
    @POST
    @Path("/notas/{id}/relatorio")
    void solicitarRelatorio(@PathParam("id") Long id);

    record EmitirNota(BigDecimal valor, String referencia, boolean simularFalha) {
    }
}
