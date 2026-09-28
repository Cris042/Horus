package org.example.horus.security;

import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.container.ContainerRequestContext;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Escopo do chamador definido pelo {@link HorusRbacFilter} (T-1007): o papel efetivo e, para o
 * {@code DEVELOPER} com a claim {@code horus_services}, os serviços que ele pode consultar.
 * Sem RBAC (ou sem a claim) não há restrição.
 */
public final class CallerScope {

    static final String ROLE_PROPERTY = "horus.rbac.role";
    static final String SERVICES_PROPERTY = "horus.rbac.services";

    private CallerScope() {
    }

    /** Serviços permitidos, quando o chamador é restrito. */
    @SuppressWarnings("unchecked")
    public static Optional<Set<String>> allowedServices(ContainerRequestContext ctx) {
        return Optional.ofNullable((Set<String>) ctx.getProperty(SERVICES_PROPERTY));
    }

    /**
     * Resolve o serviço de uma busca respeitando o escopo: sem restrição devolve o pedido; com
     * restrição exige um serviço permitido (ou assume o único permitido). Fora do escopo → 403.
     */
    public static String scopedService(ContainerRequestContext ctx, String requested) {
        Optional<Set<String>> allowed = allowedServices(ctx);
        if (allowed.isEmpty()) {
            return requested;
        }
        Set<String> services = allowed.get();
        if (requested == null || requested.isBlank()) {
            if (services.size() == 1) {
                return services.iterator().next();
            }
            throw new ForbiddenException("Informe 'service' — seu acesso é restrito a: " + services);
        }
        if (!services.contains(requested)) {
            throw new ForbiddenException("Serviço fora do seu escopo: " + requested);
        }
        return requested;
    }

    /** Filtra uma lista de serviços pelo escopo do chamador. */
    public static List<String> filterServices(ContainerRequestContext ctx, List<String> services) {
        return allowedServices(ctx).map(a -> services.stream().filter(a::contains).toList()).orElse(services);
    }
}
