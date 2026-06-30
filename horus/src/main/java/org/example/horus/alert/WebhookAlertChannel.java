package org.example.horus.alert;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.example.horus.alert.AlertModel.Alert;
import org.example.horus.alert.AlertModel.ChannelResult;
import org.jboss.logging.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;

/**
 * Canal de alerta por **webhook** (T-703, RF-H-013): {@code POST} JSON best-effort.
 *
 * <p>Habilitado apenas quando {@code horus.alert.webhook.url} está definido. O envio é
 * best-effort com timeout curto e nunca lança — uma falha de webhook não pode derrubar
 * o fluxo de alerta nem o serviço.
 */
@ApplicationScoped
public class WebhookAlertChannel implements AlertChannel {

    private static final Logger LOG = Logger.getLogger(WebhookAlertChannel.class);

    private final ObjectMapper mapper;
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();

    @ConfigProperty(name = "horus.alert.webhook.url")
    Optional<String> webhookUrl;

    public WebhookAlertChannel(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public String name() {
        return "webhook";
    }

    @Override
    public boolean enabled() {
        return webhookUrl.filter(url -> !url.isBlank()).isPresent();
    }

    private String url() {
        return webhookUrl.orElseThrow();
    }

    @Override
    public ChannelResult dispatch(Alert alert) {
        try {
            String body = mapper.writeValueAsString(Map.of(
                    "title", alert.title(),
                    "severity", alert.severity().name(),
                    "traceId", alert.traceId() == null ? "" : alert.traceId(),
                    "summary", alert.summary()));
            HttpRequest request = HttpRequest.newBuilder(URI.create(url()))
                    .timeout(Duration.ofSeconds(5))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<Void> resp = http.send(request, HttpResponse.BodyHandlers.discarding());
            boolean ok = resp.statusCode() >= 200 && resp.statusCode() < 300;
            return new ChannelResult(name(), ok, "HTTP " + resp.statusCode());
        } catch (Exception e) {
            LOG.warnf("Falha ao enviar webhook de alerta: %s", e.getMessage());
            return new ChannelResult(name(), false, "erro: " + e.getMessage());
        }
    }
}
