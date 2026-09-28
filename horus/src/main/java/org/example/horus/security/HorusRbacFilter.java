package org.example.horus.security;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Filtro de RBAC do Horus (T-704, RNF-H-010): autoriza requisições à plataforma conforme
 * o papel do chamador e a matriz de {@code docs/ROLES.md}.
 *
 * <p><b>Identidade (T-1007).</b> Modo {@code jwt} (padrão): o chamador envia
 * {@code Authorization: Bearer <token>} emitido pelo IdP (OIDC); a assinatura e o emissor são
 * verificados pelo SmallRye JWT ({@code mp.jwt.verify.*}) e os papéis vêm da claim {@code groups}
 * — com vários, vale o de maior privilégio. Modo {@code header} (só dev/testes, opt-in explícito):
 * papel no cabeçalho {@code X-Horus-Role}, sem prova de identidade. Ligado por padrão no perfil
 * {@code prod}; desligado em dev/test.
 *
 * <p>Princípios aplicados (ROLES §6): menor privilégio (papel inválido/ausente é negado quando
 * ligado) e observabilidade passiva (nenhuma escrita em domínio). O papel efetivo e o escopo de
 * serviços do {@code DEVELOPER} ficam na requisição para os recursos ({@link CallerScope}).
 */
@Provider
@PreMatching
public class HorusRbacFilter implements ContainerRequestFilter {

    /** Cabeçalho que transporta o papel no modo {@code header} (dev). */
    public static final String ROLE_HEADER = "X-Horus-Role";

    /** Claim do token com os serviços visíveis para um {@code DEVELOPER}. */
    public static final String SERVICES_CLAIM = "horus_services";

    private final boolean enabled;
    private final String mode;

    @Inject
    SecurityIdentity identity;

    public HorusRbacFilter(
            @ConfigProperty(name = "horus.rbac.enabled", defaultValue = "false") boolean enabled,
            @ConfigProperty(name = "horus.rbac.mode", defaultValue = "jwt") String mode) {
        this.enabled = enabled;
        this.mode = mode.trim().toLowerCase();
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

        Optional<HorusRole> role = "header".equals(mode)
                ? HorusRole.from(ctx.getHeaderString(ROLE_HEADER))
                : roleFromToken();
        if (role.isEmpty()) {
            ctx.abortWith(deny(401, "header".equals(mode)
                    ? "Papel ausente ou inválido. Informe o cabeçalho " + ROLE_HEADER + " com um papel válido do Horus."
                    : "Token ausente, inválido ou sem papel do Horus na claim 'groups'. Envie Authorization: Bearer <token do IdP>."));
            return;
        }
        if (!role.get().can(required.get())) {
            ctx.abortWith(deny(403, "Papel " + role.get()
                    + " não tem a capacidade " + required.get() + " para este recurso."));
            return;
        }
        ctx.setProperty(CallerScope.ROLE_PROPERTY, role.get());
        if (role.get() == HorusRole.DEVELOPER) {
            servicesFromToken().ifPresent(s -> ctx.setProperty(CallerScope.SERVICES_PROPERTY, s));
        }
    }

    /** Papel de maior privilégio entre os grupos do token verificado. */
    private Optional<HorusRole> roleFromToken() {
        if (identity == null || identity.isAnonymous()) {
            return Optional.empty();
        }
        return identity.getRoles().stream()
                .map(HorusRole::from)
                .flatMap(Optional::stream)
                .max(Comparator.comparingInt(HorusRole::privilege));
    }

    private Optional<Set<String>> servicesFromToken() {
        if (identity == null || !(identity.getPrincipal() instanceof JsonWebToken jwt)) {
            return Optional.empty();
        }
        Object claim = jwt.getClaim(SERVICES_CLAIM);
        if (claim == null) {
            return Optional.empty();
        }
        Set<String> services = new LinkedHashSet<>();
        if (claim instanceof Collection<?> values) {
            values.forEach(v -> services.add(unquote(String.valueOf(v))));
        } else if (claim instanceof jakarta.json.JsonArray array) {
            array.forEach(v -> services.add(unquote(v.toString())));
        } else {
            for (String s : String.valueOf(claim).split(",")) {
                services.add(unquote(s.trim()));
            }
        }
        services.removeIf(String::isBlank);
        return Optional.of(Set.copyOf(services));
    }

    private static String unquote(String s) {
        return s.length() >= 2 && s.startsWith("\"") && s.endsWith("\"") ? s.substring(1, s.length() - 1) : s;
    }

    private static Response deny(int status, String message) {
        return Response.status(status)
                .type(MediaType.APPLICATION_JSON)
                .entity(Map.of("error", "forbidden", "status", status, "message", message))
                .build();
    }
}
