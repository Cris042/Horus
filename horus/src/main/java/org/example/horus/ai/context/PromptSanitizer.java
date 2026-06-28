package org.example.horus.ai.context;

import java.util.regex.Pattern;

/**
 * Guarda final de PII na fronteira do prompt (RNF-H-006, defesa em profundidade).
 *
 * <p>As camadas primárias são a origem (T-401) e a borda do Collector (T-406). Esta é a
 * última rede antes de o texto sair para o LLM: mascara e-mail/CPF/cartão que tenham
 * escapado. Best-effort regex — a garantia forte continua sendo não emitir na origem.
 */
public final class PromptSanitizer {

    private static final Pattern EMAIL =
            Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");
    private static final Pattern CPF =
            Pattern.compile("\\b\\d{3}\\.?\\d{3}\\.?\\d{3}-?\\d{2}\\b");
    private static final Pattern CARD =
            Pattern.compile("\\b\\d{13,19}\\b");

    private PromptSanitizer() {
    }

    /** Mascara e-mail (→ {@code ***@***}), CPF e cartão (→ {@code ***}) no texto. */
    public static String sanitize(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        String out = EMAIL.matcher(text).replaceAll("***@***");
        out = CPF.matcher(out).replaceAll("***");
        out = CARD.matcher(out).replaceAll("***");
        return out;
    }
}
