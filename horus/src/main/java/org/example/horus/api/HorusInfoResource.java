package org.example.horus.api;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Endpoint mínimo de identificação do Horus (bootstrap T-002).
 *
 * <p>Health e readiness são expostos automaticamente pelo SmallRye Health em
 * {@code /q/health}, {@code /q/health/live} e {@code /q/health/ready}. As capacidades
 * reais do Horus (ingestão OTLP, correlação por {@code trace_id}, camada de IA) entram
 * nas Fases 5-7 do roadmap.
 */
@Path("/horus/info")
public class HorusInfoResource {

    @ConfigProperty(name = "quarkus.application.version", defaultValue = "dev")
    String version;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Info info() {
        return new Info("Horus", version, "ok");
    }

    /** Identificação básica do serviço. */
    public record Info(String name, String version, String status) {
    }
}
