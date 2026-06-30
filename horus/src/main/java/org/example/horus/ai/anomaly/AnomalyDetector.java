package org.example.horus.ai.anomaly;

import jakarta.enterprise.context.ApplicationScoped;
import org.example.horus.ai.anomaly.AnomalyModel.Anomaly;
import org.example.horus.ai.anomaly.AnomalyModel.AnomalyReport;
import org.example.horus.ai.anomaly.AnomalyModel.AnomalyRule;
import org.example.horus.ai.anomaly.AnomalyModel.Comparison;
import org.example.horus.query.MetricQueryPort;
import org.example.horus.query.QueryModel.MetricSample;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Agente <b>Anomaly Detector</b> (T-606, RF-H-008): avalia regras de limiar sobre
 * métricas (PromQL instantâneo via {@link MetricQueryPort}) e emite uma anomalia para
 * cada série que viola o limiar.
 */
@ApplicationScoped
public class AnomalyDetector {

    private final MetricQueryPort metrics;

    public AnomalyDetector(MetricQueryPort metrics) {
        this.metrics = metrics;
    }

    public AnomalyReport detect(List<AnomalyRule> rules) {
        List<AnomalyRule> safe = rules == null ? List.of() : rules;
        List<Anomaly> anomalies = new ArrayList<>();

        for (AnomalyRule rule : safe) {
            for (MetricSample sample : metrics.instantQuery(rule.promQl())) {
                if (breaches(rule, sample.value())) {
                    anomalies.add(new Anomaly(
                            rule.name(), rule.severity(), rule.comparison(),
                            rule.threshold(), sample.value(), sample.labels()));
                }
            }
        }

        anomalies.sort(Comparator.comparingDouble((Anomaly a) -> deviation(a)).reversed());
        return new AnomalyReport(safe.size(), anomalies.size(), anomalies);
    }

    private static boolean breaches(AnomalyRule rule, double value) {
        return rule.comparison() == Comparison.LT
                ? value < rule.threshold()
                : value > rule.threshold();
    }

    /** Quão longe do limiar a série está — usado para ranquear as anomalias mais severas. */
    private static double deviation(Anomaly a) {
        return Math.abs(a.value() - a.threshold());
    }
}
