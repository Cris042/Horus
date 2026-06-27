package org.example.payment.api;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.example.payment.service.ConflitoPagamentoException;

/** Mapeia conflito de estado/saldo para HTTP 409. */
@Provider
public class ConflitoPagamentoMapper implements ExceptionMapper<ConflitoPagamentoException> {

    @Override
    public Response toResponse(ConflitoPagamentoException e) {
        return Response.status(Response.Status.CONFLICT)
                .entity(new Erro(e.getMessage()))
                .build();
    }

    /** Corpo de erro mínimo. */
    public record Erro(String mensagem) {
    }
}
