package org.example.horus.ai;

/**
 * Camada de modelo por tarefa (ADR-0011, lib.md §2): seleção custo/qualidade.
 *
 * <p>Os IDs canônicos da API Anthropic ficam aqui, num único ponto, para que o resto
 * do Horus fale em termos de <em>tarefa</em> (resumo, explicação, RCA) e não de IDs de
 * modelo. Trocar de modelo/provedor é alterar este enum + o adapter — nunca os chamadores.
 *
 * <p>T-1003: cada camada carrega também o {@code effort} (profundidade de raciocínio/gasto de
 * tokens). O Haiku 4.5 não aceita {@code effort} (a API devolve erro) — por isso é nulo no FAST.
 */
public enum ModelTier {

    /** Alto volume / baixo custo — resumo periódico, clusterização (RF-H-005/008/009). */
    FAST("claude-haiku-4-5", null),

    /** Equilíbrio — explicação de trace, "pergunte ao Horus" (RF-H-006/010). */
    BALANCED("claude-sonnet-5", "medium"),

    /** Raciocínio mais forte — RCA profunda de incidentes (RF-H-007). */
    DEEP("claude-opus-5", "high");

    private final String modelId;
    private final String effort;

    ModelTier(String modelId, String effort) {
        this.modelId = modelId;
        this.effort = effort;
    }

    /** ID de modelo da API Anthropic correspondente a esta camada. */
    public String modelId() {
        return modelId;
    }

    /** {@code output_config.effort} da camada, ou {@code null} quando o modelo não o aceita. */
    public String effort() {
        return effort;
    }
}
