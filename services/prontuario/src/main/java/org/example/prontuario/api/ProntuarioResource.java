package org.example.prontuario.api;

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
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.core.Context;
import org.example.prontuario.api.dto.ConsultaResponse;
import org.example.prontuario.api.dto.CriarProntuarioRequest;
import org.example.prontuario.api.dto.ProntuarioResponse;
import org.example.prontuario.api.dto.RegistrarConsultaRequest;
import org.example.prontuario.domain.Consulta;
import org.example.prontuario.domain.Prontuario;
import org.example.prontuario.service.ConsultaService;
import org.example.prontuario.service.ProntuarioService;

import java.net.URI;
import java.util.List;

/** Endpoints de prontuário (RF-006/007) e listagem de suas consultas. */
@Path("/prontuarios")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProntuarioResource {

    @Inject
    ProntuarioService prontuarios;

    @Inject
    ConsultaService consultas;

    @POST
    public Response criar(@Valid CriarProntuarioRequest req, @Context UriInfo uriInfo) {
        Prontuario p = prontuarios.criar(req.pacienteId());
        URI location = uriInfo.getAbsolutePathBuilder().path(String.valueOf(p.id)).build();
        return Response.created(location).entity(ProntuarioResponse.from(p)).build();
    }

    @GET
    @Path("/{id}")
    public ProntuarioResponse buscar(@PathParam("id") Long id) {
        return ProntuarioResponse.from(prontuarios.buscar(id));
    }

    @GET
    public List<ProntuarioResponse> listar() {
        return prontuarios.listar().stream().map(ProntuarioResponse::from).toList();
    }

    @GET
    @Path("/{id}/consultas")
    public List<ConsultaResponse> consultasDoProntuario(@PathParam("id") Long id) {
        return consultas.listarDoProntuario(id).stream().map(ConsultaResponse::from).toList();
    }

    @POST
    @Path("/{id}/consultas")
    public Response registrarConsulta(@PathParam("id") Long id,
                                      @Valid RegistrarConsultaRequest req,
                                      @Context UriInfo uriInfo) {
        Consulta c = consultas.registrar(id, req.descricao());
        URI location = uriInfo.getBaseUriBuilder().path("consultas").path(String.valueOf(c.id)).build();
        return Response.created(location).entity(ConsultaResponse.from(c)).build();
    }
}
