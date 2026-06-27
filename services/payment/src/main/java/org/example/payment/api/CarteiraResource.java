package org.example.payment.api;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.example.payment.api.dto.CarteiraResponse;
import org.example.payment.api.dto.CriarCarteiraRequest;
import org.example.payment.api.dto.CriarPagamentoRequest;
import org.example.payment.api.dto.MovimentacaoRequest;
import org.example.payment.api.dto.MovimentacaoResponse;
import org.example.payment.api.dto.PagamentoResponse;
import org.example.payment.domain.Carteira;
import org.example.payment.domain.Movimentacao;
import org.example.payment.domain.Pagamento;
import org.example.payment.service.CarteiraService;
import org.example.payment.service.PagamentoService;

import java.net.URI;
import java.util.List;

/** Carteira/saldo (RF-011), movimentações (RF-012/015) e abertura de pagamentos (RF-013). */
@Path("/carteiras")
@Produces(MediaType.APPLICATION_JSON)
public class CarteiraResource {

    @Inject
    CarteiraService carteiras;

    @Inject
    PagamentoService pagamentos;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response criar(@Valid CriarCarteiraRequest req, @Context UriInfo uriInfo) {
        Carteira c = carteiras.criar(req.titularId(), req.saldoInicialOuZero());
        URI location = uriInfo.getAbsolutePathBuilder().path(String.valueOf(c.id)).build();
        return Response.created(location).entity(CarteiraResponse.from(c)).build();
    }

    @GET
    @Path("/{id}")
    public CarteiraResponse buscar(@PathParam("id") Long id) {
        return CarteiraResponse.from(carteiras.buscar(id));
    }

    @POST
    @Path("/{id}/movimentacoes")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response movimentar(@PathParam("id") Long id, @Valid MovimentacaoRequest req,
                               @Context UriInfo uriInfo) {
        Movimentacao m = carteiras.movimentar(id, req.tipo(), req.valor(), req.descricao());
        return Response.status(Response.Status.CREATED).entity(MovimentacaoResponse.from(m)).build();
    }

    @GET
    @Path("/{id}/movimentacoes")
    public List<MovimentacaoResponse> historico(@PathParam("id") Long id) {
        return carteiras.historico(id).stream().map(MovimentacaoResponse::from).toList();
    }

    @POST
    @Path("/{id}/pagamentos")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response criarPagamento(@PathParam("id") Long id, @Valid CriarPagamentoRequest req,
                                   @Context UriInfo uriInfo) {
        Pagamento p = pagamentos.criar(id, req.valor());
        URI location = uriInfo.getBaseUriBuilder().path("pagamentos").path(String.valueOf(p.id)).build();
        return Response.created(location).entity(PagamentoResponse.from(p)).build();
    }
}
