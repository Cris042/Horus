# ADR-0001 — Microsserviços poliglotas como caso de estudo

- **Status:** Aceito
- **Data:** 2026-06-26
- **Origem:** Herdado do documento-fonte (DA-003)
- **Requisitos:** RF-006..RF-020, RNF-014

## Contexto

O caso de estudo precisa exercitar uma arquitetura distribuída realista com responsabilidades de domínio claras: prontuário, pagamento e emissão de nota fiscal. Cada domínio tem regras próprias e deve evoluir de forma independente. O estudo também quer exercitar tracing distribuído em um ambiente **poliglota** (Python, Java, Rust).

## Decisão

Implementar três microsserviços de domínio **independentes** em **Quarkus** (Java): **Prontuário Service**, **Payment Service** e **Invoice Service**. A comunicação de negócio entre eles é **síncrona por HTTP**. Cada serviço separa regra de negócio, interface HTTP, persistência e integração.

A poliglossia do conjunto (Python para carga, Java para domínio, Rust para o worker) é uma escolha **deliberada** para o estudo de observabilidade ponta a ponta.

## Consequências

**Positivas**
- Limites de domínio explícitos e evolução independente.
- Ambiente ideal para validar correlação de traces entre linguagens.

**Negativas**
- Maior esforço de padronização e manutenção (mitigado pelo OTel como contrato único — ADR-0007).
- Exige disciplina para não acoplar serviços por dados (ADR-0003).
