package org.example.invoice.relatorio;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapSetter;
import io.smallrye.reactive.messaging.rabbitmq.OutgoingRabbitMQMetadata;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Publica solicitações de relatório no canal {@code relatorios} (→ RabbitMQ, RF-021).
 *
 * <p>O envio é assíncrono e **não bloqueia** o fluxo síncrono (RNF-011). O conector
 * SmallRye/RabbitMQ **não** injeta automaticamente o contexto de trace nos headers AMQP
 * (confirmado empiricamente em T-405 — o worker recebia a mensagem sem {@code traceparent}
 * e abria um trace novo); por isso o {@code traceparent}/{@code tracestate} é injetado
 * **explicitamente** aqui a partir do `Context` OTel ativo (RF-029/RF-H-004 — fronteira
 * HTTP→AMQP), espelhando a extração já feita pelo worker Rust (T-403).
 */
@ApplicationScoped
public class RelatorioPublisher {

    private static final Logger LOG = Logger.getLogger(RelatorioPublisher.class);
    private static final TextMapSetter<Map<String, Object>> HEADER_SETTER = Map::put;

    @Channel("relatorios")
    Emitter<RelatorioMensagem> emitter;

    public void publicar(RelatorioMensagem msg) {
        LOG.infof("Publicando solicitação de relatório %s (tipo=%s, nota=%d)", msg.id(), msg.tipo(), msg.notaId());

        Map<String, Object> headers = new HashMap<>();
        GlobalOpenTelemetry.getPropagators().getTextMapPropagator()
                .inject(Context.current(), headers, HEADER_SETTER);

        OutgoingRabbitMQMetadata metadata = OutgoingRabbitMQMetadata.builder()
                .withHeaders(headers)
                .build();

        emitter.send(Message.of(msg).addMetadata(metadata));
    }
}
