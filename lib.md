# lib.md — Dependências e Versões

Catálogo de dependências de **todos os componentes** do projeto **Horus** (plataforma de observabilidade com IA) e do sistema observado (simulação de prontuário médico).

| Campo | Valor |
|---|---|
| **Versão do documento** | 1.0 |
| **Data** | 2026-06-26 |
| **Relacionado** | [`docs/PRD.md`](./docs/PRD.md) · [`docs/adr/`](./docs/adr/) |

> ⚠️ **Sobre as versões.** Os números abaixo são **versões-alvo recomendadas** para o contexto de meados de 2026, escolhidas por compatibilidade entre os componentes. **Confirme a versão estável mais recente e fixe (pin) cada dependência** ao implementar — especialmente as de IA, OTel e Quarkus, que evoluem rápido. Os IDs de modelo Claude estão corretos; **preços e limites devem ser verificados na referência oficial da API**.

---

## 1. Plataforma e build (comum)

| Dependência | Versão-alvo | Uso |
|---|---|---|
| Java (OpenJDK) | **25 (LTS)** | Runtime dos serviços Quarkus e do Horus (configurado no `pom.xml`) |
| Maven | **3.9.x** | Build dos módulos Java |
| Maven Wrapper (`mvnw`) | **3.9.9** | Build reproduzível — **commitado** (T-004; variante `only-script`, sem jar no repo) |
| Docker Engine | **27.x** | Empacotamento de contêineres |
| Docker Compose | **2.3x** | Ambiente de desenvolvimento local |
| Kubernetes | **1.3x** | Orquestração |
| Helm | **3.16.x** | Empacotamento de deploy |
| kubectl | 1.3x | Operação do cluster |

> **Nota Java 25:** o projeto está em Java 25 (`maven.compiler.release=25` no parent `pom.xml`; o bootstrap Quarkus do Horus em T-002 abandonou as features de preview do antigo `Main.java`). O suporte de **Quarkus 3.20 ao JDK 25 foi confirmado** (T-004: build local + CI → BUILD SUCCESS, testes verdes); **Java 21 (LTS)** permanece documentado como alternativa conservadora caso surja incompatibilidade futura.

---

## 2. Horus — plataforma de observabilidade com IA (Java / Quarkus)

**Componente principal do repositório.**

| Dependência (extensão Quarkus, salvo nota) | Versão-alvo | Uso |
|---|---|---|
| Quarkus BOM / Platform | **3.20.x** | Base do framework (linha 2026) |
| `quarkus-rest` (+ `quarkus-rest-jackson`) | (via BOM) | API REST do Horus |
| `quarkus-websockets-next` | (via BOM) | Streaming do painel em tempo real (RF-H-012) |
| `quarkus-opentelemetry` | (via BOM) | Ingestão/propagação OTel |
| `quarkus-micrometer-registry-prometheus` | (via BOM) | Métricas do próprio Horus |
| `quarkus-hibernate-orm-panache` | (via BOM) | Persistência de metadados/configuração do Horus |
| `quarkus-jdbc-postgresql` | (via BOM) | Acesso ao Postgres do Horus |
| `quarkus-flyway` | (via BOM) | Migrações do banco do Horus |
| **`quarkus-langchain4j-anthropic`** | **0.25.x** | **Integração com Claude (camada de IA — ADR-0011)** |
| LangChain4j (core) | **1.0.x** | Abstrações de IA (atrás de interface) |
| `quarkus-rest-client` | (via BOM) | Consulta às APIs de Jaeger/Tempo, Loki, Prometheus |
| `quarkus-container-image-jib` | (via BOM) | Build da imagem Docker |
| `quarkus-smallrye-health` | (via BOM) | Health/readiness |
| `quarkus-mailer` | (via BOM) | Alertas por e-mail (RF-H-013) |
| pgvector (extensão PostgreSQL) | **0.8.x** | *(Opcional / v2)* RAG sobre runbooks e erros históricos |

### Modelos de IA (Anthropic Claude — ADR-0011)

| Tarefa | Modelo | ID |
|---|---|---|
| Resumos de alto volume / clusterização | Claude Haiku 4.5 | `claude-haiku-4-5-20251001` |
| Explicação de trace / consulta em linguagem natural | Claude Sonnet 4.6 | `claude-sonnet-4-6` |
| RCA profunda de incidentes | Claude Opus 4.8 | `claude-opus-4-8` |

> Acesso via API Anthropic (chave `ANTHROPIC_API_KEY`). Preços, limites de tokens e parâmetros: **consultar a referência oficial da API Claude no momento da implementação** (não fixados aqui de propósito).

---

## 3. Microsserviços de domínio (Quarkus) — Prontuário, Payment, Invoice

Mesma base para os três serviços.

| Dependência | Versão-alvo | Uso |
|---|---|---|
| Quarkus BOM / Platform | **3.20.x** | Base do framework |
| `quarkus-rest` (+ `quarkus-rest-jackson`) | (via BOM) | APIs HTTP de domínio (RF-006..020) |
| `quarkus-hibernate-orm-panache` | (via BOM) | Persistência |
| `quarkus-jdbc-postgresql` | (via BOM) | Driver PostgreSQL |
| `quarkus-flyway` | (via BOM) | Migrações independentes por serviço (RF-028) |
| `quarkus-hibernate-validator` | (via BOM) | Validação de entrada |
| `quarkus-opentelemetry` | (via BOM) | Tracing + **instrumentação de JDBC/Hibernate (RF-H-002)** |
| `quarkus-micrometer-registry-prometheus` | (via BOM) | Métricas |
| `quarkus-messaging-rabbitmq` (SmallRye Reactive Messaging) | (via BOM) | Publicar solicitação de relatório (RF-021) |
| `quarkus-narayana-lra` | (via BOM) | **SAGA por orquestração (MicroProfile LRA) — passos e compensações (ADR-0013)** |
| `quarkus-smallrye-health` | (via BOM) | Health/readiness |
| `quarkus-container-image-jib` | (via BOM) | Imagem Docker |

---

## 4. API de teste de carga (Python)

| Dependência | Versão-alvo | Uso |
|---|---|---|
| Python | **3.13** | Runtime |
| FastAPI | **0.118.x** | API de controle dos testes (RF-001..003) |
| Uvicorn | **0.34.x** | Servidor ASGI |
| Pydantic | **2.11.x** | Modelos/validação |
| Locust | **2.32.x** | Geração de carga / usuários virtuais (RF-004) |
| httpx | **0.28.x** | Cliente HTTP |
| opentelemetry-distro | **0.5x** (API/SDK 1.3x) | Instrumentação OTel |
| opentelemetry-instrumentation-fastapi | 0.5x | Auto-instrumentação FastAPI |
| opentelemetry-exporter-otlp | 1.3x | Exportação OTLP (RF-029) |

> Gerenciador recomendado: **uv** (ou Poetry) para lockfile reproduzível.

---

## 5. Report & Email Worker (Rust)

| Crate | Versão-alvo | Uso |
|---|---|---|
| Rust (toolchain) | **1.85+** (edição 2024) | Compilador |
| tokio | **1.43.x** | Runtime assíncrono |
| lapin | **2.5.x** | Cliente RabbitMQ/AMQP (RF-022) |
| lettre | **0.11.x** | Envio de e-mail (RF-024) |
| serde / serde_json | **1.x** | (De)serialização do contrato de mensagem |
| opentelemetry | **0.27.x** | Tracing |
| opentelemetry-otlp | 0.27.x | Exportação OTLP |
| tracing + tracing-subscriber | 0.1.x / 0.3.x | Logging estruturado |
| tracing-opentelemetry | 0.28.x | Ponte tracing→OTel (continuar o trace — RF-029) |
| anyhow / thiserror | 1.x | Tratamento de erros |

> O worker **não** acessa bancos de domínio (ADR-0005); por isso não há driver SQL na lista.

---

## 6. Infraestrutura observada e pipeline de telemetria

| Componente | Imagem / Versão-alvo | Uso |
|---|---|---|
| PostgreSQL | **17.x** | Banco por serviço (`prontuario_db`/`payment_db`/`invoice_db`) + banco do Horus |
| RabbitMQ | **4.0.x** (com management) | Mensageria restrita a relatórios (ADR-0004) |
| LRA Coordinator (Narayana) | **quay.io/jbosstm/lra-coordinator** (compatível com Narayana 7.x) | Coordenador da SAGA por orquestração (ADR-0013) |
| NGINX | **1.27.x** | Load Balancer (opção A) |
| Traefik | **3.3.x** | Load Balancer (opção B) |
| OpenTelemetry Collector (contrib) | **0.118.x** | Coleta/roteamento OTLP (ADR-0010) |
| Jaeger | **2.x** | UI de traces (RF-031) |
| Grafana Tempo | **2.7.x** | *(Opcional)* backend de traces ao lado do Jaeger |
| Grafana Loki | **3.4.x** | Backend de logs (RF-H-003) |
| Prometheus | **3.2.x** | Backend de métricas |
| Grafana | **11.x** | *(Opcional)* visualização complementar |

---

## 7. Resumo por linguagem

| Stack | Componentes | Núcleo de dependências |
|---|---|---|
| **Java 25 / Quarkus 3.20** | Horus, Prontuário, Payment, Invoice | Quarkus REST, Hibernate Panache, Flyway, OTel, RabbitMQ, LangChain4j-Anthropic |
| **Python 3.13** | API de carga | FastAPI, Uvicorn, Locust, OTel |
| **Rust 1.85** | Worker de relatório/e-mail | tokio, lapin, lettre, opentelemetry, tracing |
| **Infra** | Bancos, fila, LB, telemetria | PostgreSQL, RabbitMQ, NGINX/Traefik, OTel Collector, Jaeger, Loki, Prometheus |

## 8. Como manter este arquivo

- Fixe (pin) versões nos manifestos reais: `pom.xml`, `pyproject.toml`/`requirements.txt`, `Cargo.toml`, imagens Docker e charts Helm.
- Ao subir uma versão, atualize a linha correspondente aqui e registre no `docs/adr/` se a mudança tiver impacto arquitetural.
- Trate este `lib.md` como a **fonte única de verdade** das versões-alvo; os arquivos de build são a fonte de verdade do que está **efetivamente** instalado.
