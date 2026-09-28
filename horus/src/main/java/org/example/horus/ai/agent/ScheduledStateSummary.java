package org.example.horus.ai.agent;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.example.horus.ai.context.PromptContext;
import org.example.horus.ai.context.WindowContextCollector;
import org.example.horus.query.TimeWindow;
import org.jboss.logging.Logger;

import java.time.Instant;

/**
 * Resumo de estado <b>agendado</b> (T-603, RF-H-005).
 *
 * <p>Desligado por padrão ({@code horus.ai.summary.cron=off}); habilite com um cron
 * (ex.: {@code 0 0/15 * * * ?}). A cada disparo coleta os sinais da última janela
 * ({@code horus.ai.summary.lookback}, padrão 15m — T-1001: traces, erros, queries lentas,
 * logs de erro e a métrica de saúde), monta o contexto e gera um resumo (T-603), registrando-o
 * no log. Persistência/notificação do resumo é fatia seguinte.
 */
@ApplicationScoped
public class ScheduledStateSummary {

    private static final Logger LOG = Logger.getLogger(ScheduledStateSummary.class);

    private final WindowContextCollector windows;
    private final StateSummarizer summarizer;

    @ConfigProperty(name = "horus.ai.summary.lookback", defaultValue = "15m")
    String lookback;

    public ScheduledStateSummary(WindowContextCollector windows, StateSummarizer summarizer) {
        this.windows = windows;
        this.summarizer = summarizer;
    }

    /** Cron via config; {@code off} (default) desabilita o disparo. */
    @Scheduled(cron = "{horus.ai.summary.cron:off}")
    void run() {
        try {
            TimeWindow window = TimeWindow.last(TimeWindow.parseLookback(lookback), Instant.now());
            PromptContext context = windows.collect(window);
            var summary = summarizer.summarize(context);
            LOG.infof("Resumo de estado agendado (%s, live=%s): %s",
                    summary.modelId(), summary.live(), summary.summary());
        } catch (RuntimeException e) {
            // Best-effort: um disparo agendado nunca deve derrubar o serviço.
            LOG.warnf("Resumo de estado agendado falhou: %s", e.getMessage());
        }
    }
}
