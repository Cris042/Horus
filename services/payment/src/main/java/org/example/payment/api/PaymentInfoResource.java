package org.example.payment.api;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Endpoint mínimo de identificação do Payment Service (scaffold T-101).
 *
 * <p>Health/readiness saem automaticamente do SmallRye Health em {@code /q/health}.
 * Os endpoints de domínio (carteira/saldo, movimentações, aprovar/estornar) entram em T-103.
 */
@Path("/payment/info")
public class PaymentInfoResource {

    @ConfigProperty(name = "quarkus.application.name", defaultValue = "payment-service")
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
