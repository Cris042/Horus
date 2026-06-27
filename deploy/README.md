# deploy/ — Empacotamento e orquestração

Artefatos de implantação do Horus e do sistema observado.

- **`docker-compose`** de desenvolvimento (T-003): Postgres ×3, RabbitMQ, OTel Collector, Jaeger, Loki, Prometheus — `make up` sobe tudo localmente.
- **Imagens Docker** de todos os executáveis (T-801, RF-032).
- **Manifests / Helm** para Kubernetes (T-802, RF-033): deployments, services, configmaps, secrets.
- **Topologia:** worker e Horus implantados **separados** dos serviços de domínio (ADR-0012, T-804); escala horizontal independente por componente (RNF-005, T-803).

> Fase 0: apenas o diretório e este README. O conteúdo real chega em **T-003** (compose) e na **Fase 8** (Kubernetes).

## Tasks

`T-003` (compose dev) · `T-404` (Collector + backends) · `T-801`..`T-804` (Docker/K8s).
