package org.example.prontuario.api;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.example.prontuario.api.dto.AtualizarConsultaRequest;
import org.example.prontuario.api.dto.ConsultaResponse;
import org.example.prontuario.service.ConsultaService;

/** Endpoints de consulta: consultar, atualizar (RF-009) e finalizar (RF-010). */
@Path("/consultas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ConsultaResource {

    @Inject
    ConsultaService consultas;

    @GET
    @Path("/{id}")
    public ConsultaResponse buscar(@PathParam("id") Long id) {
        return ConsultaResponse.from(consultas.buscar(id));
    }

    @PUT
    @Path("/{id}")
    public ConsultaResponse atualizar(@PathParam("id") Long id, @Valid AtualizarConsultaRequest req) {
        return ConsultaResponse.from(consultas.atualizar(id, req.descricao()));
    }

    @POST
    @Path("/{id}/finalizar")
    public ConsultaResponse finalizar(@PathParam("id") Long id) {
        return ConsultaResponse.from(consultas.finalizar(id));
    }
}
