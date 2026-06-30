package org.example.horus.lifecycle;

import java.util.List;

/**
 * Modelo do **mapa de serviços/dependências** de um trace (T-506, RF-H-015).
 *
 * <p>Projeta os spans de um {@code traceId} num grafo direcionado: cada serviço
 * participante é um nó; cada chamada inter-serviço (span filho num serviço
 * diferente do pai) é uma aresta chamador → chamado. É a base topológica do
 * Horus, complementar à waterfall (T-503).
 */
public final class ServiceMapModel {

    private ServiceMapModel() {
    }

    /** Nó do grafo: um serviço participante do trace. */
    public record ServiceNode(
            String serviceName,
            int spanCount,
            long totalDurationMicros,
            boolean entryPoint) {
    }

    /** Aresta direcionada: dependência {@code from → to} observada no trace. */
    public record ServiceEdge(
            String from,
            String to,
            int callCount,
            long totalDurationMicros) {
    }

    /** Mapa de serviços/dependências de uma request por {@code traceId}. */
    public record ServiceMap(
            String traceId,
            int spanCount,
            int nodeCount,
            int edgeCount,
            List<ServiceNode> nodes,
            List<ServiceEdge> edges) {
    }
}
