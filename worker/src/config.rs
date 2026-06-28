//! Configuração via ambiente (12-factor). Defaults batem com o compose de dev.

pub struct Config {
    pub amqp_url: String,
    pub exchange: String,
    pub queue: String,
    pub routing_key: String,
    pub smtp_host: Option<String>,
    pub email_from: String,
    pub email_to: String,
}

impl Config {
    pub fn from_env() -> Self {
        Self {
            amqp_url: var("AMQP_URL", "amqp://horus:horus@localhost:5672/%2f"),
            exchange: var("RELATORIOS_EXCHANGE", "relatorios"),
            queue: var("RELATORIOS_QUEUE", "relatorios.worker"),
            routing_key: var("RELATORIOS_ROUTING_KEY", "nota-fiscal"),
            smtp_host: std::env::var("SMTP_HOST").ok().filter(|s| !s.is_empty()),
            email_from: var("EMAIL_FROM", "horus@medrec.local"),
            email_to: var("EMAIL_TO", "ops@medrec.local"),
        }
    }
}

fn var(key: &str, default: &str) -> String {
    std::env::var(key).unwrap_or_else(|_| default.to_string())
}
