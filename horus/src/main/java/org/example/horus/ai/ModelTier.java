package org.example.horus.ai;

/**
 * Camada de modelo por tarefa (ADR-0011, lib.md §2): seleção custo/qualidade.
 *
 * <p>Os IDs canônicos da API Anthropic ficam aqui, num único ponto, para que o resto
 * do Horus fale em termos de <em>tarefa</em> (resumo, explicação, RCA) e não de IDs de
 * modelo. Trocar de modelo/provedor é alterar este enum + o adapter — nunca os chamadores.
 */
public enum ModelTier {

    /** Alto volume / baixo custo — resumo periódico, clusterização (RF-H-005/008/009). */
    FAST("claude-haiku-4-5"),

    /** Equilíbrio — explicação de trace, "pergunte ao Horus" (RF-H-006/010). */
    BALANCED("claude-sonnet-4-6"),

    /** Raciocínio mais forte — RCA profunda de incidentes (RF-H-007). */
    DEEP("claude-opus-4-8");

    private final String modelId;

    ModelTier(String modelId) {
        this.modelId = modelId;
    }

    /** ID de modelo da API Anthropic correspondente a esta camada. */
    public String modelId() {
        return modelId;
    }
}
