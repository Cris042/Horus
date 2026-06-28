package org.example.horus.security;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Map;
import java.util.Optional;

/**
 * Filtro de RBAC do Horus (T-704, RNF-H-010): autoriza requisições à plataforma conforme
 * o papel do chamador e a matriz de {@code docs/ROLES.md}.
 *
 * <p><b>1ª fatia.</b> A identidade vem do cabeçalho {@code X-Horus-Role} (ponto de extensão
 * para OIDC/JWT numa fatia posterior). Desligado por padrão ({@code horus.rbac.enabled=false})
 * para não afetar o build/CI nem os serviços de domínio (sem auth — ADR-0002).
 *
 * <p>Princípios aplicados (ROLES §6): menor privilégio (papel inválido/ausente é negado quando
 * ligado) e observabilidade passiva (nenhuma escrita em domínio).
 */
@Provider
@PreMatching
public class HorusRbacFilter implements ContainerRequestFilter {

    /** Cabeçalho que transporta o papel do chamador nesta fatia. */
    public static final String ROLE_HEADER = "X-Horus-Role";

    private final boolean enabled;

    public HorusRbacFilter(
            @ConfigProperty(name = "horus.rbac.enabled", defaultValue = "false") boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void filter(ContainerRequestContext ctx) {
        if (!enabled) {
            return;
        }

        String path = ctx.getUriInfo().getPath();
        Optional<Capability> required = RequiredCapability.forPath(path);
        if (required.isEmpty()) {
            return; // endpoint público / fora do RBAC
        }

        Optional<HorusRole> role = HorusRole.from(ctx.getHeaderString(ROLE_HEADER));
        if (role.isEmpty()) {
            ctx.abortWith(deny(401, "Papel ausente ou inválido. Informe o cabeçalho "
                    + ROLE_HEADER + " com um papel válido do Horus."));
            return;
        }
        if (!role.get().can(required.get())) {
            ctx.abortWith(deny(403, "Papel " + role.get()
                    + " não tem a capacidade " + required.get() + " para este recurso."));
        }
    }

    private static Response deny(int status, String message) {
        return Response.status(status)
                .type(MediaType.APPLICATION_JSON)
                .entity(Map.of("error", "forbidden", "status", status, "message", message))
                .build();
    }
}
