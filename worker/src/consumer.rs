//! Orquestração do processamento de uma mensagem (RF-022..024), idempotente por `id`
//! (RNF-012). Sem I/O de broker — toda a lógica é testável offline.

use std::collections::HashSet;

use anyhow::Result;

use crate::email::EmailSender;
use crate::message::RelatorioMensagem;
use crate::report;

pub struct Processor {
    email: Box<dyn EmailSender + Send>,
    processados: HashSet<String>,
}

impl Processor {
    pub fn new(email: Box<dyn EmailSender + Send>) -> Self {
        Self {
            email,
            processados: HashSet::new(),
        }
    }

    /// Processa uma mensagem já desserializada. Retorna `Ok(true)` se gerou+enviou,
    /// `Ok(false)` se era duplicada (mesmo `id` já processado).
    pub fn processar(&mut self, msg: &RelatorioMensagem) -> Result<bool> {
        if self.processados.contains(&msg.id) {
            tracing::info!(id = %msg.id, "mensagem duplicada ignorada (idempotência)");
            return Ok(false);
        }
        let corpo = report::gerar_relatorio(msg);
        self.email.enviar(msg, &corpo)?;
        self.processados.insert(msg.id.clone());
        tracing::info!(id = %msg.id, nota_id = msg.nota_id, "relatório gerado e e-mail enviado");
        Ok(true)
    }

    /// Desserializa o corpo (JSON) e processa.
    pub fn parse_e_processar(&mut self, bytes: &[u8]) -> Result<bool> {
        let msg: RelatorioMensagem = serde_json::from_slice(bytes)?;
        self.processar(&msg)
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use std::sync::atomic::{AtomicUsize, Ordering};
    use std::sync::Arc;

    struct CountingSender(Arc<AtomicUsize>);
    impl EmailSender for CountingSender {
        fn enviar(&self, _msg: &RelatorioMensagem, _corpo: &str) -> Result<()> {
            self.0.fetch_add(1, Ordering::SeqCst);
            Ok(())
        }
    }

    fn json(id: &str) -> Vec<u8> {
        format!(
            r#"{{"id":"{id}","tipo":"NOTA_FISCAL","notaId":1,"valor":10.0,"numero":"NF-1","solicitadoEm":"2026-06-28T12:00:00Z"}}"#
        )
        .into_bytes()
    }

    #[test]
    fn processa_e_envia_uma_vez() {
        let count = Arc::new(AtomicUsize::new(0));
        let mut p = Processor::new(Box::new(CountingSender(count.clone())));
        assert!(p.parse_e_processar(&json("a")).unwrap());
        assert_eq!(count.load(Ordering::SeqCst), 1);
    }

    #[test]
    fn idempotente_para_id_repetido() {
        let count = Arc::new(AtomicUsize::new(0));
        let mut p = Processor::new(Box::new(CountingSender(count.clone())));
        assert!(p.parse_e_processar(&json("dup")).unwrap());
        assert!(!p.parse_e_processar(&json("dup")).unwrap());
        assert_eq!(count.load(Ordering::SeqCst), 1, "envia só na primeira vez");
    }

    #[test]
    fn json_invalido_propaga_erro() {
        let count = Arc::new(AtomicUsize::new(0));
        let mut p = Processor::new(Box::new(CountingSender(count)));
        assert!(p.parse_e_processar(b"{ invalido").is_err());
    }
}
