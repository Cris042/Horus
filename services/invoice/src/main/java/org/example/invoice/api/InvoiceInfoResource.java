package org.example.invoice.api;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Endpoint mínimo de identificação do Invoice Service (scaffold T-101).
 *
 * <p>Health/readiness saem automaticamente do SmallRye Health em {@code /q/health}.
 * Os endpoints de domínio (emitir NF, listar, reprocessar) entram em T-104.
 */
@Path("/invoice/info")
public class InvoiceInfoResource {

    @ConfigProperty(name = "quarkus.application.name", defaultValue = "invoice-service")
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
