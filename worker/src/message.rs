//! Contrato da mensagem de solicitação de relatório (espelha `RelatorioMensagem` do
//! invoice-service — T-301). O corpo é JSON; o contexto de tracing (`traceparent`)
//! viaja nos **headers** AMQP (extraído em T-403), não aqui.

use serde::Deserialize;

#[derive(Debug, Clone, Deserialize, PartialEq)]
pub struct RelatorioMensagem {
    /// Identificador único da solicitação (idempotência no consumidor).
    pub id: String,
    /// Tipo do relatório (ex.: `NOTA_FISCAL`).
    pub tipo: String,
    /// Id da nota fiscal de referência.
    #[serde(rename = "notaId")]
    pub nota_id: i64,
    /// Valor da nota (parâmetro do relatório).
    pub valor: f64,
    /// Número da NF emitida.
    pub numero: Option<String>,
    /// Instante da solicitação (UTC, ISO-8601).
    #[serde(rename = "solicitadoEm")]
    pub solicitado_em: String,
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn desserializa_do_json_do_invoice() {
        let json = r#"{
            "id": "req-123",
            "tipo": "NOTA_FISCAL",
            "notaId": 42,
            "valor": 1234.56,
            "numero": "NF-0042",
            "solicitadoEm": "2026-06-28T12:00:00Z"
        }"#;
        let msg: RelatorioMensagem = serde_json::from_str(json).unwrap();
        assert_eq!(msg.id, "req-123");
        assert_eq!(msg.tipo, "NOTA_FISCAL");
        assert_eq!(msg.nota_id, 42);
        assert_eq!(msg.numero.as_deref(), Some("NF-0042"));
    }

    #[test]
    fn aceita_numero_ausente() {
        let json = r#"{"id":"x","tipo":"NOTA_FISCAL","notaId":1,"valor":10.0,"numero":null,"solicitadoEm":"2026-06-28T12:00:00Z"}"#;
        let msg: RelatorioMensagem = serde_json::from_str(json).unwrap();
        assert!(msg.numero.is_none());
    }

    #[test]
    fn json_invalido_falha() {
        assert!(serde_json::from_str::<RelatorioMensagem>("{ not json").is_err());
    }
}
