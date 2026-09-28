package org.example.horus.ai;

/**
 * Porta desacoplada de acesso ao LLM (ADR-0011 §Salvaguardas — "provedor desacoplado").
 *
 * <p>Toda a IA do Horus (resumo de estado, explique-este-trace, RCA, "pergunte ao Horus")
 * passa por aqui. Nenhum chamador conhece o SDK nem a API Anthropic: dependem só
 * desta interface, o que permite trocar de modelo/provedor sem tocar no restante do Horus.
 *
 * <p><b>Privacidade (RNF-H-006):</b> o {@code prompt} deve conter apenas telemetria
 * <em>sanitizada</em> (a redação de PII vive no Collector — T-406 — e na origem — T-401). Desde a
 * T-1004 a fronteira é <b>garantida</b> pelo {@code AuditingLlmEngine}: todo pedido passa pelo
 * {@code PromptSanitizer} e fica registrado na trilha de auditoria, sem depender do chamador.
 */
public interface LlmEngine {

    /**
     * Pedido ao LLM: instrução de sistema, prompt do usuário, a camada de modelo e o
     * {@code purpose} (qual capacidade do Horus pediu — rótulo da trilha de auditoria, T-1004).
     */
    record LlmRequest(String system, String prompt, ModelTier tier, String purpose) {

        public LlmRequest {
            if (prompt == null || prompt.isBlank()) {
                throw new IllegalArgumentException("prompt obrigatório");
            }
            if (tier == null) {
                tier = ModelTier.BALANCED;
            }
            if (purpose == null || purpose.isBlank()) {
                purpose = "adhoc";
            }
        }

        /** Pedido sem {@code purpose} explícito (registrado como {@code adhoc}). */
        public LlmRequest(String system, String prompt, ModelTier tier) {
            this(system, prompt, tier, null);
        }

        /** Conveniência: pedido só com prompt, na camada equilibrada. */
        public static LlmRequest of(String prompt) {
            return new LlmRequest(null, prompt, ModelTier.BALANCED);
        }
    }

    /**
     * Resposta do LLM. {@code live=false} indica que o motor está em modo stub
     * (sem {@code ANTHROPIC_API_KEY}) — a saída é um placeholder, não uma resposta real.
     */
    record LlmResponse(String text, String modelId, boolean live) {
    }

    /** Executa um pedido de completação. Implementações não devem lançar por falta de chave. */
    LlmResponse complete(LlmRequest request);

    /** {@code true} se há um provedor de IA real configurado (chave presente). */
    boolean isLive();
}
