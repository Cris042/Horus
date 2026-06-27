# Prontuário Service

Microsserviço de **prontuário médico** — núcleo do domínio do sistema observado.

- **Stack:** Quarkus (Java 25), REST, Hibernate Panache, Flyway.
- **Banco:** `prontuario_db` (PostgreSQL dedicado).
- **Requisitos:** RF-006..010, RF-025.

## Responsabilidades

- Criar e consultar prontuários.
- Registrar, atualizar e **finalizar** consultas.
- Manter o histórico clínico associado ao paciente.

## Observabilidade

Exporta traces (HTTP + JDBC), logs estruturados e métricas via OTLP. Dados sensíveis (PII clínica) são **sanitizados** antes de qualquer telemetria (RNF-H-002).

## Tasks

`T-101` (scaffold) · `T-102` (domínio) · `T-105` (migrações) · `T-401` (OTel).
