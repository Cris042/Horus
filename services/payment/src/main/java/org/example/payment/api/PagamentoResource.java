package org.example.payment.api;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.example.payment.api.dto.PagamentoResponse;
import org.example.payment.service.PagamentoService;

/** Processamento de pagamento: aprovar/rejeitar (RF-013) e estornar (RF-014). */
@Path("/pagamentos")
@Produces(MediaType.APPLICATION_JSON)
public class PagamentoResource {

    @Inject
    PagamentoService pagamentos;

    @GET
    @Path("/{id}")
    public PagamentoResponse buscar(@PathParam("id") Long id) {
        return PagamentoResponse.from(pagamentos.buscar(id));
    }

    @POST
    @Path("/{id}/aprovar")
    public PagamentoResponse aprovar(@PathParam("id") Long id) {
        return PagamentoResponse.from(pagamentos.aprovar(id));
    }

    @POST
    @Path("/{id}/rejeitar")
    public PagamentoResponse rejeitar(@PathParam("id") Long id) {
        return PagamentoResponse.from(pagamentos.rejeitar(id));
    }

    @POST
    @Path("/{id}/estornar")
    public PagamentoResponse estornar(@PathParam("id") Long id) {
        return PagamentoResponse.from(pagamentos.estornar(id));
    }
}
