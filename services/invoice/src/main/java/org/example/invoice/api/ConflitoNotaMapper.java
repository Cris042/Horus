package org.example.invoice.api;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.example.invoice.service.ConflitoNotaException;

/** Mapeia conflito de estado da nota para HTTP 409. */
@Provider
public class ConflitoNotaMapper implements ExceptionMapper<ConflitoNotaException> {

    @Override
    public Response toResponse(ConflitoNotaException e) {
        return Response.status(Response.Status.CONFLICT)
                .entity(new Erro(e.getMessage()))
                .build();
    }

    /** Corpo de erro mínimo. */
    public record Erro(String mensagem) {
    }
}
