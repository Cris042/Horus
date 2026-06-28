package org.example.horus.ai.agent;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.example.horus.ai.context.ContextAssembler;
import org.example.horus.ai.context.PromptContext;
import org.example.horus.query.MetricQueryPort;
import org.jboss.logging.Logger;

import java.util.List;

/**
 * Resumo de estado <b>agendado</b> (T-603, RF-H-005).
 *
 * <p>Desligado por padrão ({@code horus.ai.summary.cron=off}); habilite com um cron
 * (ex.: {@code 0 0/15 * * * ?}). A cada disparo consulta uma métrica de saúde via
 * {@link MetricQueryPort}, monta o contexto (T-602) e gera um resumo (T-603), registrando-o
 * no log. Persistência/notificação do resumo é fatia seguinte (T-608/T-701).
 */
@ApplicationScoped
public class ScheduledStateSummary {

    private static final Logger LOG = Logger.getLogger(ScheduledStateSummary.class);

    private final MetricQueryPort metrics;
    private final ContextAssembler assembler;
    private final StateSummarizer summarizer;

    @ConfigProperty(name = "horus.ai.summary.promql", defaultValue = "up")
    String promQl;

    public ScheduledStateSummary(MetricQueryPort metrics, ContextAssembler assembler,
                                 StateSummarizer summarizer) {
        this.metrics = metrics;
        this.assembler = assembler;
        this.summarizer = summarizer;
    }

    /** Cron via config; {@code off} (default) desabilita o disparo. */
    @Scheduled(cron = "{horus.ai.summary.cron:off}")
    void run() {
        try {
            PromptContext context = assembler.assemble(null, List.of(), metrics.instantQuery(promQl));
            var summary = summarizer.summarize(context);
            LOG.infof("Resumo de estado agendado (%s, live=%s): %s",
                    summary.modelId(), summary.live(), summary.summary());
        } catch (RuntimeException e) {
            // Best-effort: um disparo agendado nunca deve derrubar o serviço.
            LOG.warnf("Resumo de estado agendado falhou: %s", e.getMessage());
        }
    }
}
