package org.example.horus.security;

import java.util.Optional;

/**
 * Mapeia um caminho de requisição do Horus para a {@link Capability} exigida (T-704).
 *
 * <p>Caminhos públicos (saúde, info) não exigem capacidade. A granularidade é por grupo de
 * endpoint, alinhada à matriz de {@code docs/ROLES.md} §4.
 */
public final class RequiredCapability {

    private RequiredCapability() {
    }

    /**
     * Capacidade exigida para o caminho, ou vazio quando o endpoint é público
     * (não protegido pelo RBAC do Horus).
     */
    public static Optional<Capability> forPath(String path) {
        String p = path.startsWith("/") ? path : "/" + path;

        // Públicos: saúde e smoke endpoint.
        if (p.startsWith("/q/health") || p.equals("/horus/info")) {
            return Optional.empty();
        }
        // Observabilidade/configuração do cache de IA → configuração de plataforma.
        if (p.startsWith("/horus/ai/cache")) {
            return Optional.of(Capability.CONFIGURE_PLATFORM);
        }
        // Demais capacidades de IA (resumo/explicação/RCA/ask).
        if (p.startsWith("/horus/ai")) {
            return Optional.of(Capability.AI_INSIGHTS);
        }
        // Painel e consultas de telemetria.
        if (p.startsWith("/horus/panel") || p.startsWith("/horus/query")) {
            return Optional.of(Capability.VIEW_TELEMETRY);
        }
        // Qualquer outro caminho do Horus exige, no mínimo, visão de telemetria.
        if (p.startsWith("/horus")) {
            return Optional.of(Capability.VIEW_TELEMETRY);
        }
        // Recursos não-Horus (ex.: página estática) ficam fora do RBAC.
        return Optional.empty();
    }
}
