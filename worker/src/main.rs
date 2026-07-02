//! Worker de relatórios (Rust) — T-303, RF-022..024 / RNF-012.
//!
//! Consome solicitações de relatório do RabbitMQ (exchange `relatorios`, routing key
//! `nota-fiscal`), gera o relatório e envia o e-mail. Fica **fora** do caminho síncrono
//! de negócio (ADR-0005) e não acessa bancos de domínio.
//!
//! A continuação do trace HTTP→AMQP (extrai o `traceparent` dos headers da mensagem e abre
//! o span de processamento como filho) é feita via `telemetry` (T-403, RF-H-004).

mod config;
mod consumer;
mod email;
mod message;
mod report;
mod telemetry;

use anyhow::Result;
use futures_lite::StreamExt;
use lapin::options::{
    BasicAckOptions, BasicConsumeOptions, BasicNackOptions, ExchangeDeclareOptions,
    QueueBindOptions, QueueDeclareOptions,
};
use lapin::types::FieldTable;
use lapin::{Connection, ConnectionProperties, ExchangeKind};
use tracing_opentelemetry::OpenTelemetrySpanExt;

use crate::config::Config;
use crate::consumer::Processor;
use crate::email::{EmailSender, LogSender, SmtpSender};

fn build_processor(cfg: &Config) -> Processor {
    let sender: Box<dyn EmailSender + Send> = match &cfg.smtp_host {
        Some(host) => match SmtpSender::new(host, cfg.email_from.clone(), cfg.email_to.clone()) {
            Ok(s) => {
                tracing::info!(host = %host, "transporte SMTP configurado");
                Box::new(s)
            }
            Err(e) => {
                tracing::error!(error = %e, "falha ao configurar SMTP; usando stub de log");
                Box::new(LogSender)
            }
        },
        None => {
            tracing::info!("SMTP_HOST ausente; e-mails serão apenas registrados (stub)");
            Box::new(LogSender)
        }
    };
    Processor::new(sender)
}

#[tokio::main]
async fn main() -> Result<()> {
    let provider = telemetry::init()?;

    let cfg = Config::from_env();
    let mut processor = build_processor(&cfg);

    let conn = Connection::connect(
        &cfg.amqp_url,
        ConnectionProperties::default()
            .with_executor(tokio_executor_trait::Tokio::current())
            .with_reactor(tokio_reactor_trait::Tokio),
    )
    .await?;
    let channel = conn.create_channel().await?;

    channel
        .exchange_declare(
            &cfg.exchange,
            ExchangeKind::Topic,
            ExchangeDeclareOptions {
                durable: true,
                ..Default::default()
            },
            FieldTable::default(),
        )
        .await?;
    channel
        .queue_declare(
            &cfg.queue,
            QueueDeclareOptions {
                durable: true,
                ..Default::default()
            },
            FieldTable::default(),
        )
        .await?;
    channel
        .queue_bind(
            &cfg.queue,
            &cfg.exchange,
            &cfg.routing_key,
            QueueBindOptions::default(),
            FieldTable::default(),
        )
        .await?;

    let mut consumer = channel
        .basic_consume(
            &cfg.queue,
            "horus-report-worker",
            BasicConsumeOptions::default(),
            FieldTable::default(),
        )
        .await?;

    tracing::info!(queue = %cfg.queue, exchange = %cfg.exchange, "worker de relatórios iniciado");

    while let Some(delivery) = consumer.next().await {
        let delivery = delivery?;

        // Span de processamento que CONTINUA o trace do publicador (HTTP→AMQP, RF-H-004):
        // extrai o contexto W3C dos headers AMQP e o define como pai do span.
        let span = tracing::info_span!(
            "processar_relatorio",
            messaging.system = "rabbitmq",
            trace_id = tracing::field::Empty,
            span_id = tracing::field::Empty,
        );
        if let Some(headers) = delivery.properties.headers() {
            span.set_parent(telemetry::extrair_contexto(headers));
        }
        // Injeta trace_id/span_id nos logs deste span (contrato T-005 §4.1, RF-H-004).
        telemetry::registrar_trace_context(&span);
        let _guard = span.enter();

        match processor.parse_e_processar(&delivery.data) {
            Ok(_) => delivery.ack(BasicAckOptions::default()).await?,
            Err(e) => {
                tracing::error!(error = %e, "falha ao processar relatório; nack sem requeue");
                delivery
                    .nack(BasicNackOptions {
                        requeue: false,
                        ..Default::default()
                    })
                    .await?;
            }
        }
    }

    provider.shutdown()?;
    Ok(())
}
