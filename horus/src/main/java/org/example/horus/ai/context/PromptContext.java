package org.example.horus.ai.context;

import java.util.List;

/**
 * Contexto montado para o LLM: texto sanitizado e dentro do orçamento, mais metadados
 * de proveniência (o que entrou, quanto custou, se houve truncamento) — T-602.
 */
public record PromptContext(String text, int estimatedTokens, boolean truncated,
                            List<String> includedSignals) {
}
