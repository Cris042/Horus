package org.example.horus.ai.anomaly;

import java.util.List;

/**
 * Modelos do **clustering de erros** (T-606, RF-H-009).
 *
 * <p>Diferente da agregação por serviço da T-505, aqui os erros são agrupados por
 * <em>fingerprint normalizado atravessando serviços</em>: um cluster reúne a mesma
 * assinatura de erro onde quer que ela apareça, evidenciando falhas que se propagam
 * por vários serviços. O conjunto recebe um rótulo em linguagem natural gerado por IA.
 */
public final class ErrorClusterModel {

    private ErrorClusterModel() {
    }

    /** Um cluster: uma assinatura de erro e onde/quanto ela ocorreu. */
    public record ErrorCluster(
            String fingerprint,
            String sample,
            String severity,
            int totalCount,
            int serviceCount,
            boolean crossService,
            List<String> affectedServices) {
    }

    /** Resultado do clustering por {@code traceId}, com rótulo de IA e proveniência. */
    public record ErrorClustering(
            String traceId,
            int errorCount,
            int clusterCount,
            int crossServiceClusterCount,
            String label,
            String modelId,
            boolean live,
            List<ErrorCluster> clusters) {
    }
}
