package org.example.invoice.api;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.example.invoice.api.dto.EmitirNotaRequest;
import org.example.invoice.api.dto.NotaFiscalResponse;
import org.example.invoice.domain.NotaFiscal;
import org.example.invoice.domain.StatusNota;
import org.example.invoice.service.NotaFiscalService;

import java.net.URI;
import java.util.List;

/** Notas fiscais simuladas: emitir (RF-016/017/018), listar (RF-019), reprocessar (RF-020). */
@Path("/notas")
@Produces(MediaType.APPLICATION_JSON)
public class NotaFiscalResource {

    @Inject
    NotaFiscalService notas;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response emitir(@Valid EmitirNotaRequest req, @Context UriInfo uriInfo) {
        NotaFiscal n = notas.emitir(req.valor(), req.referencia(), req.simularFalha());
        URI location = uriInfo.getAbsolutePathBuilder().path(String.valueOf(n.id)).build();
        return Response.created(location).entity(NotaFiscalResponse.from(n)).build();
    }

    @GET
    @Path("/{id}")
    public NotaFiscalResponse buscar(@PathParam("id") Long id) {
        return NotaFiscalResponse.from(notas.buscar(id));
    }

    @GET
    public List<NotaFiscalResponse> listar(@QueryParam("status") StatusNota status) {
        return notas.listar(status).stream().map(NotaFiscalResponse::from).toList();
    }

    @POST
    @Path("/{id}/reprocessar")
    public NotaFiscalResponse reprocessar(@PathParam("id") Long id) {
        return NotaFiscalResponse.from(notas.reprocessar(id));
    }

    /** Solicita (assíncrono) a geração de relatório/e-mail da NF emitida (RF-021). */
    @POST
    @Path("/{id}/relatorio")
    public Response solicitarRelatorio(@PathParam("id") Long id) {
        return Response.accepted(notas.solicitarRelatorio(id)).build();
    }
}
