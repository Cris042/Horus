package org.example.horus.api;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.example.horus.ai.anomaly.AnomalyDetector;
import org.example.horus.ai.anomaly.AnomalyModel.AnomalyReport;
import org.example.horus.ai.anomaly.AnomalyModel.AnomalyRule;

import java.util.List;

/** API do detector de anomalias baseado em regras (T-606, RF-H-008). */
@Path("/horus/ai/anomalies")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class HorusAnomalyResource {

    private final AnomalyDetector detector;

    public HorusAnomalyResource(AnomalyDetector detector) {
        this.detector = detector;
    }

    @POST
    public AnomalyReport detect(List<AnomalyRule> rules) {
        if (rules == null || rules.isEmpty()) {
            throw new BadRequestException("informe ao menos uma regra de anomalia");
        }
        return detector.detect(rules);
    }
}
