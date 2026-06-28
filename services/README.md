# services/ — Microsserviços de domínio

Camada de **domínio** do sistema observado: três microsserviços em **Quarkus (Java 25)**,
cada um dono do seu próprio banco PostgreSQL, mais um orquestrador de SAGA para o fluxo
cross-service. São o que gera o tráfego e a telemetria que o **Horus** observa.

| Serviço | Pasta | Papel | Banco |
|---|---|---|---|
| Prontuário | [`prontuario/`](prontuario/) | Registros e consultas médicas | `prontuario_db` |
| Payment | [`payment/`](payment/) | Carteira, movimentações, estornos | `payment_db` |
| Invoice | [`invoice/`](invoice/) | Emissão de nota fiscal simulada | `invoice_db` |
| SAGA Orchestrator | [`saga-orchestrator/`](saga-orchestrator/) | Orquestra pagar → emitir NF, com compensações | `saga_db` |

## Convenções comuns

- **Banco por serviço** — isolamento total, sem acesso cruzado a dados (ADR-0003, RNF-002/003).
- **Migrações Flyway** independentes por banco (T-105, RF-028).
- **REST + Hibernate Panache** (T-101).
- **Comunicação síncrona por HTTP** entre serviços; mensageria só para relatórios (ADR-0004).
- **SAGA (MicroProfile LRA)** para fluxos entre serviços, com compensações idempotentes (ADR-0013, T-107).
- **OpenTelemetry** em todos: traces HTTP + JDBC/Hibernate, logs e métricas via OTLP (T-401).

## Tasks relacionadas

`T-101` (scaffold) · `T-102`–`T-104` (cada serviço) · `T-105`/`T-106` (migrações/isolamento) · `T-107` (SAGA) · `T-401` (instrumentação OTel).
