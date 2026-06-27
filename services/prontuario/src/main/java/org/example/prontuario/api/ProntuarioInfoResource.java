package org.example.prontuario.api;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Endpoint mínimo de identificação do Prontuário Service (scaffold T-101).
 *
 * <p>Health/readiness saem automaticamente do SmallRye Health em {@code /q/health}.
 * Os endpoints de domínio (criar/consultar prontuário, consultas) entram em T-102.
 */
@Path("/prontuario/info")
public class ProntuarioInfoResource {

    @ConfigProperty(name = "quarkus.application.name", defaultValue = "prontuario-service")
    String service;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Info info() {
        return new Info(service, "ok");
    }

    /** Identificação básica do serviço. */
    public record Info(String service, String status) {
    }
}
