//! Geração do relatório a partir da mensagem (RF-022). Simulação textual — o foco do
//! projeto é o fluxo assíncrono observável, não o formato do relatório em si.

use crate::message::RelatorioMensagem;

/// Produz o corpo textual do relatório para uma solicitação.
pub fn gerar_relatorio(msg: &RelatorioMensagem) -> String {
    format!(
        "Relatório: {tipo}\n\
         Solicitação: {id}\n\
         Nota fiscal: {numero} (id {nota})\n\
         Valor: R$ {valor:.2}\n\
         Solicitado em: {dt}\n",
        tipo = msg.tipo,
        id = msg.id,
        numero = msg.numero.as_deref().unwrap_or("-"),
        nota = msg.nota_id,
        valor = msg.valor,
        dt = msg.solicitado_em,
    )
}

#[cfg(test)]
mod tests {
    use super::*;

    fn msg() -> RelatorioMensagem {
        RelatorioMensagem {
            id: "req-1".into(),
            tipo: "NOTA_FISCAL".into(),
            nota_id: 7,
            valor: 99.9,
            numero: Some("NF-7".into()),
            solicitado_em: "2026-06-28T12:00:00Z".into(),
        }
    }

    #[test]
    fn inclui_campos_chave() {
        let r = gerar_relatorio(&msg());
        assert!(r.contains("NOTA_FISCAL"));
        assert!(r.contains("req-1"));
        assert!(r.contains("NF-7"));
        assert!(r.contains("99.90"));
    }
}
