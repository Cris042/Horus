# Architecture Decision Records (ADR)

Registros de decisão arquitetural do projeto **Horus** (plataforma de observabilidade com IA) e do sistema observado (simulação de prontuário médico).

Formato: [Michael Nygard / MADR](https://adr.github.io/). Cada ADR é imutável; mudanças geram um novo ADR que **supersede** o anterior.

## Índice

| ADR | Título | Status | Origem |
|---|---|---|---|
| [0001](./ADR-0001-microsservicos-poliglotas.md) | Microsserviços poliglotas como caso de estudo | Aceito | Herdado (DA-003) |
| [0002](./ADR-0002-load-balancer-sem-api-gateway.md) | Load Balancer como ponto de entrada, sem API Gateway | Aceito | Herdado (DA-001/002) |
| [0003](./ADR-0003-banco-por-servico.md) | Um banco PostgreSQL por serviço, isolado | Aceito | Herdado (DA-004) |
| [0004](./ADR-0004-rabbitmq-restrito-relatorios.md) | RabbitMQ restrito a relatórios | Aceito | Herdado (DA-005) |
| [0005](./ADR-0005-worker-rust-responsabilidade-limitada.md) | Worker em Rust com responsabilidade limitada | Aceito | Herdado (DA-006) |
| 0006 | _(removido — decisão de não-SAGA descartada)_ | ➡️ Ver [ADR-0013](./ADR-0013-padrao-saga.md) | Herdado (DA-007) |
| [0007](./ADR-0007-opentelemetry-telemetria.md) | OpenTelemetry como padrão de telemetria | Aceito | Herdado (DA-008) |
| [0008](./ADR-0008-horus-observabilidade-ia.md) | **Horus: plataforma de observabilidade com IA (supersede "sem dashboard")** | Aceito | **Novo** |
| [0009](./ADR-0009-captura-request-e-query.md) | Captura do ciclo de vida de request e query | Aceito | **Novo** |
| [0010](./ADR-0010-pipeline-telemetria-unificado.md) | Pipeline unificado de traces, logs e métricas | Aceito | **Novo** |
| [0011](./ADR-0011-camada-ia-claude.md) | Camada de IA com Claude para resumo, RCA e anomalias | Aceito | **Novo** |
| [0012](./ADR-0012-docker-kubernetes.md) | Empacotamento Docker e orquestração Kubernetes | Aceito | Herdado (DA-009) |
| [0013](./ADR-0013-padrao-saga.md) | **Adotar SAGA desde o início (orquestração)** | Aceito | **Novo** |

## Convenções

- **Status:** Proposto · Aceito · Substituído por ADR-xxxx · Descontinuado.
- **Origem "Herdado":** decisão registrada no *Documento Consolidado de Escopo e Requisitos v2.0*.
- **Origem "Novo":** decisão introduzida pelo foco em observabilidade com IA (Horus).
