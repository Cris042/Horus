# lib.md — Dependências e Versões

Catálogo de dependências de **todos os componentes** do projeto **Horus** (plataforma de observabilidade com IA) e do sistema observado (simulação de prontuário médico).

| Campo | Valor |
|---|---|
| **Versão do documento** | 1.1 |
| **Data** | 2026-07-02 |
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

> **Nota Java 25:** o projeto está em Java 25 (`maven.compiler.release=25` no parent `pom.xml`; o bootstrap Quarkus do Horus em T-002 abandonou as features de preview do antigo `Main.java`). O bootstrap sem persistência (Horus, T-002/T-004) rodou em Quarkus 3.20; porém o **enhancement de entidades Panache do 3.20 não suporta o bytecode do JDK 25** (ASM anterior ao Java 25 → `Unsupported class file major version 69`). Por isso **T-101 elevou o Quarkus para 3.37.0** (linha que suporta JDK 25), confirmado com `BUILD SUCCESS` nos 5 módulos. **Java 21 (LTS)** permanece documentado como alternativa conservadora caso surja incompatibilidade futura.

---

## 2. Horus — plataforma de observabilidade com IA (Java / Quarkus)

**Componente principal do repositório.**

| Dependência (extensão Quarkus, salvo nota) | Versão-alvo | Uso |
|---|---|---|
| Quarkus BOM / Platform | **3.37.x** | Base do framework (bump em T-101: 3.20 não suporta Panache sob JDK 25) |
| `quarkus-rest` (+ `quarkus-rest-jackson`) | (via BOM) | API REST do Horus |
| `quarkus-websockets-next` | (via BOM) | Streaming do painel em tempo real (RF-H-012) |
| `quarkus-opentelemetry` | (via BOM) | Ingestão/propagação OTel — traces, logs **e métricas** (`quarkus.otel.metrics.enabled=true`; ver nota abaixo) |
| `quarkus-hibernate-orm-panache` | (via BOM) | Persistência de metadados/configuração do Horus |
| `quarkus-jdbc-postgresql` | (via BOM) | Acesso ao Postgres do Horus |
| `quarkus-flyway` | (via BOM) | Migrações do banco do Horus |
| **`com.anthropic:anthropic-java`** | **2.65.0** | **SDK oficial da Anthropic — camada de IA (ADR-0011, adendo T-1003)** |
| `quarkus-rest-client` | (via BOM) | Consulta às APIs de Jaeger/Tempo, Loki, Prometheus |
| `quarkus-container-image-jib` | (via BOM) | Build da imagem Docker |
| `quarkus-smallrye-health` | (via BOM) | Health/readiness |
| `quarkus-mailer` | (via BOM) | Alertas por e-mail (RF-H-013) |
| pgvector (extensão PostgreSQL) | **0.8.x** | *(Opcional / v2)* RAG sobre runbooks e erros históricos |

### Modelos de IA (Anthropic Claude — ADR-0011)

| Tarefa | Modelo | ID |
|---|---|---|
| Resumos de alto volume / clusterização | Claude Haiku 4.5 | `claude-haiku-4-5` |
| Explicação de trace / consulta em linguagem natural | Claude Sonnet 5 | `claude-sonnet-5` |
| RCA profunda de incidentes | Claude Opus 5 | `claude-opus-5` |

> Acesso via API Anthropic (chave `ANTHROPIC_API_KEY`). Preços, limites de tokens e parâmetros: **consultar a referência oficial da API Claude no momento da implementação** (não fixados aqui de propósito).

---

## 3. Microsserviços de domínio (Quarkus) — Prontuário, Payment, Invoice, SAGA Orchestrator

Mesma base para os quatro serviços.

| Dependência | Versão-alvo | Uso |
|---|---|---|
| Quarkus BOM / Platform | **3.37.x** | Base do framework |
| `quarkus-rest` (+ `quarkus-rest-jackson`) | (via BOM) | APIs HTTP de domínio (RF-006..020) |
| `quarkus-hibernate-orm-panache` | (via BOM) | Persistência |
| `quarkus-jdbc-postgresql` | (via BOM) | Driver PostgreSQL |
| `quarkus-flyway` | (via BOM) | Migrações independentes por serviço (RF-028) |
| `quarkus-hibernate-validator` | (via BOM) | Validação de entrada |
| `quarkus-opentelemetry` | (via BOM) | Tracing + **instrumentação de JDBC/Hibernate (RF-H-002)** + métricas RED/JVM (`quarkus.otel.metrics.enabled=true`) |
| `quarkus-messaging-rabbitmq` (SmallRye Reactive Messaging) | (via BOM) | Publicar solicitação de relatório (RF-021) |
| `quarkus-scheduler` | (via BOM) | **SAGA por orquestração própria (ADR-0013): recuperação agendada de SAGAs interrompidas (T-1010)** — MicroProfile LRA foi avaliado e **não** adotado |
| `quarkus-smallrye-health` | (via BOM) | Health/readiness |
| `quarkus-container-image-jib` | (via BOM) | Imagem Docker |

> **Nota métricas (T-903):** `quarkus.otel.metrics.enabled` é **`false` por padrão** na extensão
> `quarkus-opentelemetry` — sem essa flag, só traces/logs saem via OTLP, nenhuma métrica chega
> ao Prometheus (mesmo com `quarkus.datasource.jdbc.telemetry=true` e o Collector saudável). É
> `BUILD_AND_RUN_TIME_FIXED` (precisa rebuild). Ligada nos 4 serviços de domínio/SAGA. O projeto
> **não** usa `quarkus-micrometer-registry-prometheus` (nunca foi adicionado às dependências,
> apesar de versões anteriores deste documento o listarem como alvo) — métricas vêm inteiramente
> do SDK OTel.

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
| PostgreSQL | **17.x** | Banco por serviço (`prontuario_db`/`payment_db`/`invoice_db`/`saga_db`), isolado (RNF-003) |
| `postgres_exporter` | **quay.io/prometheuscommunity/postgres-exporter:v0.15.x** | Métricas de cada banco p/ Prometheus (conexões/tx/tamanho) — dashboard `postgres.json` |
| RabbitMQ | **4.0.x** (com management) | Mensageria restrita a relatórios (ADR-0004) |
| NGINX | **1.27.x** | Load Balancer (opção A) |
| Traefik | **3.3.x** | Load Balancer (opção B) |
| OpenTelemetry Collector (contrib) | **0.118.x** | Coleta/roteamento OTLP (ADR-0010) |
| Jaeger | **2.x** | UI de traces (RF-031) |
| Grafana Tempo | **2.7.x** | *(Opcional)* backend de traces ao lado do Jaeger |
| Grafana Loki | **3.4.x** | Backend de logs (RF-H-003) |
| Prometheus | **3.2.x** | Backend de métricas (`remote_write` do Collector + scrape do `postgres_exporter`) |
| Grafana | **11.x** | Lente unificada — dashboards provisionados por arquivo (`microservices.json`, `postgres.json`), sem cliques manuais |

---

## 7. Resumo por linguagem

| Stack | Componentes | Núcleo de dependências |
|---|---|---|
| **Java 25 / Quarkus 3.37** | Horus, Prontuário, Payment, Invoice | Quarkus REST, Hibernate Panache, Flyway, OTel, RabbitMQ, LangChain4j-Anthropic |
| **Python 3.13** | API de carga | FastAPI, Uvicorn, Locust, OTel |
| **Rust 1.85** | Worker de relatório/e-mail | tokio, lapin, lettre, opentelemetry, tracing |
| **Infra** | Bancos, fila, LB, telemetria | PostgreSQL, RabbitMQ, NGINX/Traefik, OTel Collector, Jaeger, Loki, Prometheus |

## 8. Como manter este arquivo

- Fixe (pin) versões nos manifestos reais: `pom.xml`, `pyproject.toml`/`requirements.txt`, `Cargo.toml`, imagens Docker e charts Helm.
- Ao subir uma versão, atualize a linha correspondente aqui e registre no `docs/adr/` se a mudança tiver impacto arquitetural.
- Trate este `lib.md` como a **fonte única de verdade** das versões-alvo; os arquivos de build são a fonte de verdade do que está **efetivamente** instalado.
