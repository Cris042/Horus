package org.example.horus.ai.anomaly;

import java.util.List;
import java.util.Map;

/**
 * Modelos do **detector de anomalias baseado em regras** (T-606, RF-H-008).
 *
 * <p>Uma regra avalia uma série temporal (PromQL instantâneo) contra um limiar; cada
 * série que viola o limiar vira uma anomalia. É a primeira fatia: determinística e
 * baseada em limiar (baseline estatístico fica para evolução).
 */
public final class AnomalyModel {

    private AnomalyModel() {
    }

    /** Direção da comparação do valor da série contra o limiar. */
    public enum Comparison {
        GT, LT
    }

    /** Regra de anomalia: nome, PromQL, comparação, limiar e severidade atribuída. */
    public record AnomalyRule(
            String name,
            String promQl,
            Comparison comparison,
            double threshold,
            String severity) {

        public AnomalyRule {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("name obrigatório");
            }
            if (promQl == null || promQl.isBlank()) {
                throw new IllegalArgumentException("promQl obrigatório");
            }
            if (comparison == null) {
                comparison = Comparison.GT;
            }
            if (severity == null || severity.isBlank()) {
                severity = "warning";
            }
        }
    }

    /** Uma anomalia detectada: a regra violada, a série (rótulos) e o valor observado. */
    public record Anomaly(
            String rule,
            String severity,
            Comparison comparison,
            double threshold,
            double value,
            Map<String, String> labels) {
    }

    /** Relatório da avaliação de um conjunto de regras. */
    public record AnomalyReport(
            int evaluatedRules,
            int anomalyCount,
            List<Anomaly> anomalies) {
    }
}
