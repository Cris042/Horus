# ADR-0012 — Empacotamento Docker e orquestração Kubernetes

- **Status:** Aceito
- **Data:** 2026-06-26
- **Origem:** Herdado do documento-fonte (DA-009)
- **Requisitos:** RF-032, RF-033, RNF-004, RNF-005

## Contexto

Os componentes são poliglotas e precisam de empacotamento padronizado e de capacidade de escalar de forma independente sob a carga gerada pelo Locust.

## Decisão

- Cada componente executável (FastAPI, 3 serviços Quarkus, worker Rust, **Horus**) é empacotado em **imagem Docker** própria.
- **Kubernetes** implanta, gerencia e escala os contêineres; cada serviço escala de forma independente.
- Infraestrutura de suporte (PostgreSQL ×3, RabbitMQ, OTel Collector, Jaeger, Loki, Prometheus) compõe o ambiente.
- O **worker Rust** e o **Horus** são implantados **separadamente** dos serviços de domínio.
- O Load Balancer (NGINX/Traefik) é a entrada da solução.

## Consequências

**Positivas**
- Ambiente reproduzível e escalável horizontalmente (RNF-005).
- O Horus escala independentemente da carga de domínio (reforça a não intrusividade — RNF-H-008).

**Negativas**
- Operação de um conjunto relativamente grande de componentes; recomenda-se Helm/manifests versionados para gerenciar a complexidade.
