package org.example.horus.ai.context;

/**
 * Orçamento de tokens para o contexto enviado ao LLM (RNF-H-003).
 *
 * <p>Estimativa heurística (~4 chars/token) — suficiente para <em>bound</em> o prompt sem
 * depender da API de contagem da Anthropic (que exige chave). O ponto é nunca estourar o
 * contexto/custo; a contagem exata pode ser refinada quando houver chave.
 */
public final class TokenBudget {

    /** Razão aproximada de caracteres por token para texto técnico/JSON. */
    private static final int CHARS_PER_TOKEN = 4;

    private final int maxTokens;

    public TokenBudget(int maxTokens) {
        if (maxTokens <= 0) {
            throw new IllegalArgumentException("maxTokens deve ser > 0");
        }
        this.maxTokens = maxTokens;
    }

    public int maxTokens() {
        return maxTokens;
    }

    /** Estimativa de tokens de um texto. */
    public static int estimateTokens(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        return (text.length() + CHARS_PER_TOKEN - 1) / CHARS_PER_TOKEN;
    }

    /** Quantos tokens ainda cabem dado um consumo atual. */
    public int remaining(int usedTokens) {
        return Math.max(0, maxTokens - usedTokens);
    }

    /** {@code true} se adicionar {@code text} ao consumo atual ainda respeita o orçamento. */
    public boolean fits(int usedTokens, String text) {
        return usedTokens + estimateTokens(text) <= maxTokens;
    }
}
