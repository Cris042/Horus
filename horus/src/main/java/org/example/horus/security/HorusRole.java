package org.example.horus.security;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

/**
 * Papéis de acesso ao Horus (T-704, RNF-H-010), conforme {@code docs/ROLES.md} §1.1/§4.
 *
 * <p>Cada papel carrega o conjunto de {@link Capability} que pode exercer — a transcrição
 * da matriz de permissões. Os serviços de domínio permanecem sem autenticação (ADR-0002);
 * este RBAC vale apenas para a plataforma Horus (painel, telemetria e IA).
 */
public enum HorusRole {

    /** Administrador da plataforma — acesso total. */
    PLATFORM_ADMIN(EnumSet.allOf(Capability.class)),

    /** Operador principal (SRE/Observability): investiga e configura, não gerencia usuários. */
    SRE(EnumSet.of(Capability.VIEW_TELEMETRY, Capability.AI_INSIGHTS, Capability.CONFIGURE_PLATFORM)),

    /**
     * Desenvolvedor: vê telemetria e usa IA. Com token JWT que traga a claim
     * {@code horus_services}, a busca de traces fica restrita a esses serviços (T-1007).
     */
    DEVELOPER(EnumSet.of(Capability.VIEW_TELEMETRY, Capability.AI_INSIGHTS)),

    /** Auditor/Stakeholder: leitura de painéis, relatórios e resumos de IA. */
    AUDITOR(EnumSet.of(Capability.VIEW_TELEMETRY, Capability.AI_INSIGHTS)),

    /** Operador de teste de carga: correlaciona o efeito da carga na telemetria. */
    LOAD_TEST_OP(EnumSet.of(Capability.VIEW_TELEMETRY, Capability.AI_INSIGHTS));

    private final Set<Capability> capabilities;

    HorusRole(Set<Capability> capabilities) {
        this.capabilities = capabilities;
    }

    /** Quantidade de capacidades — critério do papel efetivo quando o token traz vários (T-1007). */
    int privilege() {
        return capabilities.size();
    }

    /** {@code true} se este papel pode exercer a capacidade dada. */
    public boolean can(Capability capability) {
        return capabilities.contains(capability);
    }

    /** Resolve um papel a partir do nome do cabeçalho/token (case-insensitive); vazio se inválido. */
    public static Optional<HorusRole> from(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(valueOf(raw.trim().toUpperCase().replace('-', '_')));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
