package org.example.horus.api;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.example.horus.ai.LlmAuditTrail;
import org.example.horus.ai.LlmAuditTrail.AuditRecord;
import org.example.horus.ai.LlmAuditTrail.AuditStats;

import java.util.List;

/**
 * Trilha de auditoria do LLM (T-1004, RNF-H-006): o que saiu para a IA, quando, por qual
 * capacidade, em qual modelo e quantas redações de PII foram aplicadas — sem o texto do prompt.
 */
@Path("/horus/ai/audit")
@Produces(MediaType.APPLICATION_JSON)
public class HorusAuditResource {

    private final LlmAuditTrail trail;

    public HorusAuditResource(LlmAuditTrail trail) {
        this.trail = trail;
    }

    /** Registros mais recentes primeiro. */
    @GET
    public AuditView recent(@QueryParam("limit") @DefaultValue("50") int limit) {
        return new AuditView(trail.stats(), trail.recent(Math.max(1, Math.min(limit, 500))));
    }

    /** Totais + registros recentes. */
    public record AuditView(AuditStats stats, List<AuditRecord> records) {
    }
}
