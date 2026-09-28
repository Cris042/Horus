package org.example.horus.alert;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import org.example.horus.ai.LlmEngine;
import org.example.horus.ai.LlmEngine.LlmRequest;
import org.example.horus.ai.LlmEngine.LlmResponse;
import org.example.horus.ai.ModelTier;
import org.example.horus.alert.AlertModel.Alert;
import org.example.horus.alert.AlertModel.AlertRequest;
import org.example.horus.alert.AlertModel.AlertResult;
import org.example.horus.alert.AlertModel.AlertSeverity;
import org.example.horus.alert.AlertModel.ChannelResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Orquestra um alerta (T-703, RF-H-013): monta um <b>resumo de IA</b> do gatilho e faz
 * <b>fan-out</b> a todos os {@link AlertChannel} habilitados.
 *
 * <p>O resumo usa a camada {@link ModelTier#FAST} (alto volume / baixo custo, ADR-0011)
 * e funciona com o modo stub do {@code AnthropicLlmEngine} (sem chave → {@code live=false}). O fan-out é
 * best-effort: cada canal reporta sucesso/falha sem interromper os demais.
 */
@ApplicationScoped
public class AlertService {

    private static final String SYSTEM = """
            Você é o Horus, observabilidade aumentada por IA. Resuma, em português claro e
            em no máximo duas frases, o alerta abaixo (anomalia ou erro crítico) para uma
            equipe de plantão: o que está acontecendo e o impacto provável. Não invente
            dados nem PII; trate como assistência, não verdade absoluta.""";

    private final LlmEngine engine;
    private final Instance<AlertChannel> channels;

    public AlertService(LlmEngine engine, Instance<AlertChannel> channels) {
        this.engine = engine;
        this.channels = channels;
    }

    public AlertResult raise(AlertRequest request) {
        AlertSeverity severity = AlertSeverity.from(request.severity());
        LlmResponse summary = engine.complete(new LlmRequest(SYSTEM, buildPrompt(request, severity), ModelTier.FAST));

        Alert alert = new Alert(
                blankToDefault(request.title(), "Alerta sem título"),
                severity,
                request.traceId(),
                summary.text(),
                summary.modelId(),
                summary.live());

        List<ChannelResult> results = new ArrayList<>();
        for (AlertChannel channel : channels) {
            if (channel.enabled()) {
                results.add(channel.dispatch(alert));
            }
        }
        int dispatched = (int) results.stream().filter(ChannelResult::dispatched).count();
        return new AlertResult(alert, dispatched, results);
    }

    /** Lista os canais e seu estado (para {@code GET /horus/alerts/channels}). */
    public List<ChannelResult> channelStates() {
        List<ChannelResult> states = new ArrayList<>();
        for (AlertChannel channel : channels) {
            states.add(new ChannelResult(channel.name(), channel.enabled(),
                    channel.enabled() ? "habilitado" : "desabilitado"));
        }
        return states;
    }

    private static String buildPrompt(AlertRequest request, AlertSeverity severity) {
        StringBuilder sb = new StringBuilder();
        sb.append("Severidade: ").append(severity).append('\n');
        sb.append("Título: ").append(blankToDefault(request.title(), "(sem título)")).append('\n');
        if (request.traceId() != null && !request.traceId().isBlank()) {
            sb.append("traceId: ").append(request.traceId()).append('\n');
        }
        if (request.details() != null && !request.details().isBlank()) {
            sb.append("Detalhes:\n").append(request.details());
        }
        return sb.toString();
    }

    private static String blankToDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
