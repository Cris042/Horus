package org.example.prontuario.api;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.example.prontuario.service.ConflitoConsultaException;

/** Mapeia conflito de estado de consulta para HTTP 409. */
@Provider
public class ConflitoConsultaMapper implements ExceptionMapper<ConflitoConsultaException> {

    @Override
    public Response toResponse(ConflitoConsultaException e) {
        return Response.status(Response.Status.CONFLICT)
                .entity(new Erro(e.getMessage()))
                .build();
    }

    /** Corpo de erro mínimo. */
    public record Erro(String mensagem) {
    }
}
