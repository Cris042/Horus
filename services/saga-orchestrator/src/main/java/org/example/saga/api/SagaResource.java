package org.example.saga.api;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.example.saga.api.dto.PagarEEmitirRequest;
import org.example.saga.api.dto.SagaResponse;
import org.example.saga.domain.Saga;
import org.example.saga.domain.StatusSaga;
import org.example.saga.service.SagaService;

/** Inicia e consulta SAGAs pagar→emitir NF (ADR-0013, RF-H-016). */
@Path("/sagas")
@Produces(MediaType.APPLICATION_JSON)
public class SagaResource {

    @Inject
    SagaService sagas;

    @POST
    @Path("/pagar-e-emitir")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response pagarEEmitir(@Valid PagarEEmitirRequest req) {
        Saga saga = sagas.pagarEEmitir(req.carteiraId(), req.valor(), req.simularFalhaNota());
        // 201 quando concluída; 200 quando compensada (a SAGA executou, mas não completou).
        Response.Status code = saga.status == StatusSaga.CONCLUIDA
                ? Response.Status.CREATED
                : Response.Status.OK;
        return Response.status(code).entity(SagaResponse.from(saga)).build();
    }

    @GET
    @Path("/{id}")
    public SagaResponse buscar(@PathParam("id") Long id) {
        return SagaResponse.from(sagas.buscar(id));
    }
}
