//! Envio de e-mail do relatório (RF-024). Abstraído por um trait para permitir um
//! transporte SMTP real (lettre) e um stub que apenas registra (dev/CI sem servidor SMTP).

use anyhow::Result;
use lettre::message::Mailbox;
use lettre::{Message, SmtpTransport, Transport};

use crate::message::RelatorioMensagem;

/// Porta de envio de e-mail.
pub trait EmailSender {
    fn enviar(&self, msg: &RelatorioMensagem, corpo: &str) -> Result<()>;
}

/// Monta o e-mail (sem enviar) — isolado para teste determinístico.
pub fn montar_email(from: &str, to: &str, msg: &RelatorioMensagem, corpo: &str) -> Result<Message> {
    let from_mb: Mailbox = from.parse()?;
    let to_mb: Mailbox = to.parse()?;
    let email = Message::builder()
        .from(from_mb)
        .to(to_mb)
        .subject(format!(
            "Relatório {} — NF {}",
            msg.tipo,
            msg.numero.as_deref().unwrap_or("-")
        ))
        .body(corpo.to_string())?;
    Ok(email)
}

/// Stub: registra o e-mail em vez de enviá-lo (padrão quando `SMTP_HOST` não está setado).
pub struct LogSender;

impl EmailSender for LogSender {
    fn enviar(&self, msg: &RelatorioMensagem, corpo: &str) -> Result<()> {
        tracing::info!(id = %msg.id, nota_id = msg.nota_id, "e-mail (stub) do relatório:\n{corpo}");
        Ok(())
    }
}

/// Transporte SMTP real (plaintext via `builder_dangerous`; TLS entra em endurecimento).
pub struct SmtpSender {
    transport: SmtpTransport,
    from: String,
    to: String,
}

impl SmtpSender {
    pub fn new(host: &str, from: String, to: String) -> Result<Self> {
        let transport = SmtpTransport::builder_dangerous(host).build();
        Ok(Self {
            transport,
            from,
            to,
        })
    }
}

impl EmailSender for SmtpSender {
    fn enviar(&self, msg: &RelatorioMensagem, corpo: &str) -> Result<()> {
        let email = montar_email(&self.from, &self.to, msg, corpo)?;
        self.transport.send(&email)?;
        Ok(())
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::message::RelatorioMensagem;

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
    fn monta_email_com_enderecos_validos() {
        let email =
            montar_email("horus@medrec.local", "ops@medrec.local", &msg(), "corpo").unwrap();
        // Serializa o envelope; deve conter assunto e destinatário.
        let raw = String::from_utf8(email.formatted()).unwrap();
        assert!(raw.contains("Subject:"));
        assert!(raw.contains("ops@medrec.local"));
    }

    #[test]
    fn endereco_invalido_falha() {
        assert!(montar_email("não-é-email", "ops@medrec.local", &msg(), "x").is_err());
    }

    #[test]
    fn log_sender_ok() {
        assert!(LogSender.enviar(&msg(), "corpo").is_ok());
    }
}
