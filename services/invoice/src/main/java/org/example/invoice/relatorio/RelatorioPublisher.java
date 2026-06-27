package org.example.invoice.relatorio;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;

/**
 * Publica solicitações de relatório no canal {@code relatorios} (→ RabbitMQ, RF-021).
 *
 * <p>O envio é assíncrono e **não bloqueia** o fluxo síncrono (RNF-011). O OpenTelemetry
 * cria um span PRODUCER e injeta o {@code traceparent} nos headers da mensagem (RF-029).
 */
@ApplicationScoped
public class RelatorioPublisher {

    private static final Logger LOG = Logger.getLogger(RelatorioPublisher.class);

    @Channel("relatorios")
    Emitter<RelatorioMensagem> emitter;

    public void publicar(RelatorioMensagem msg) {
        LOG.infof("Publicando solicitação de relatório %s (tipo=%s, nota=%d)", msg.id(), msg.tipo(), msg.notaId());
        emitter.send(msg);
    }
}
