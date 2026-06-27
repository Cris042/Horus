package org.example.invoice.relatorio;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Contrato da **mensagem de solicitação de relatório** (T-301, RF-021).
 *
 * <p>Publicada no RabbitMQ e consumida pelo worker Rust (T-303), que gera o relatório e
 * envia o e-mail. O **contexto de tracing** (W3C `traceparent`) viaja nos **headers** da
 * mensagem (RF-029), injetado automaticamente pelo OpenTelemetry — não faz parte do corpo.
 *
 * @param id          identificador único da solicitação (idempotência no consumidor)
 * @param tipo        tipo do relatório (ex.: {@code NOTA_FISCAL})
 * @param notaId      id da nota fiscal de referência
 * @param valor       valor da nota (parâmetro do relatório)
 * @param numero      número da NF emitida
 * @param solicitadoEm instante da solicitação (UTC, ISO-8601)
 */
public record RelatorioMensagem(
        String id,
        String tipo,
        Long notaId,
        BigDecimal valor,
        String numero,
        OffsetDateTime solicitadoEm) {

    public static final String TIPO_NOTA_FISCAL = "NOTA_FISCAL";
}
