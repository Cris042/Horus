# Contrato de Telemetria — Horus

| Campo | Valor |
|---|---|
| **Versão** | 1.0 |
| **Status** | Aceito (T-005) |
| **Data** | 2026-06-27 |
| **Requisitos** | RF-029, RF-030, RF-H-001, RF-H-002, RF-H-003, RF-H-004, RF-H-016, RNF-008, RNF-H-002 |
| **ADRs** | [ADR-0007](../adr/ADR-0007-opentelemetry-telemetria.md) · [ADR-0009](../adr/ADR-0009-captura-request-e-query.md) · [ADR-0010](../adr/ADR-0010-pipeline-telemetria-unificado.md) · [ADR-0013](../adr/ADR-0013-padrao-saga.md) |

> **O que é.** O contrato **único e obrigatório** que todo componente (Python/FastAPI+Locust, Load Balancer, serviços Quarkus, worker Rust, Horus) DEVE seguir ao emitir **traces, logs e métricas**, para que a telemetria seja **correlacionável ponta a ponta** por `trace_id` e **segura** (sem PII crua). É a base sobre a qual as fases de instrumentação (T-401..T-406) e o próprio Horus operam.

**Palavras-chave** (RFC 2119): **DEVE/OBRIGATÓRIO**, **NÃO DEVE**, **DEVERIA/RECOMENDADO**, **PODE/OPCIONAL**.

**Princípios:**
1. **OpenTelemetry é o contrato** (ADR-0007). Toda emissão sai **somente via OTLP** para o Collector (ADR-0010).
2. **A base são as *semantic conventions* do OTel.** Use as chaves padronizadas do OTel sempre que existirem; só crie chaves próprias (namespace `horus.*`) para conceitos do projeto. As convenções do OTel evoluem — **confirme a versão do semconv fixada no momento da implementação** (não trate as grafias abaixo como imutáveis; trate a intenção como normativa).
3. **`trace_id` é a chave de correlação** que liga request ↔ queries ↔ logs ↔ mensagem RabbitMQ ↔ worker (ADR-0009).
4. **Sem PII crua, nunca** — sanitização **na origem** e reforço no Collector (RNF-H-002; §6).

---

## 1. Identidade de recurso (OTel `Resource`)

Todo componente **DEVE** anexar estes atributos de recurso a **todos** os sinais:

| Atributo | Obrigatoriedade | Exemplo | Nota |
|---|---|---|---|
| `service.name` | DEVE | `prontuario-service` | Valor canônico (tabela abaixo) |
| `service.namespace` | DEVE | `medrec` \| `horus` | `medrec` = sistema observado; `horus` = plataforma |
| `service.version` | DEVE | `1.0-SNAPSHOT` | Da build |
| `service.instance.id` | DEVE | `<uuid>`/nome do pod | Único por instância |
| `deployment.environment.name` | DEVE | `dev` | `dev`/`staging`/`prod` |

**Valores canônicos de `service.name`** (use exatamente estes):

| Componente | `service.name` | `service.namespace` |
|---|---|---|
| API de carga (FastAPI/Locust) | `loadtest-api` | `medrec` |
| Load Balancer (NGINX/Traefik) | `load-balancer` | `medrec` |
| Prontuário | `prontuario-service` | `medrec` |
| Payment | `payment-service` | `medrec` |
| Invoice | `invoice-service` | `medrec` |
| Worker de relatório (Rust) | `report-worker` | `medrec` |
| Plataforma Horus | `horus` | `horus` |

---

## 2. Propagação de contexto (W3C Trace Context)

A correlação ponta a ponta depende de **não perder o contexto nas fronteiras** (ADR-0007). O formato é **W3C Trace Context** (`traceparent` + `tracestate`) — **não** B3.

- **HTTP (RF-029):** todo serviço **DEVE** *extrair* o contexto dos headers de entrada e *injetá-lo* nos headers de saída. O Load Balancer **NÃO DEVE** encerrar o contexto — apenas repassa `traceparent`/`tracestate`.
- **RabbitMQ / AMQP (RF-029 — ponto crítico):** o publicador **DEVE** injetar `traceparent`/`tracestate` nos **headers da mensagem**; o worker Rust **DEVE** extraí-los e abrir o span de processamento como **filho/linked** do span publicador. Esta é a fronteira **HTTP→AMQP** que mais quebra correlação — é requisito de aceite (RF-H-004).
- **Baggage:** PODE ser usado para propagar metadados de baixo volume; **NÃO DEVE** carregar PII.

---

## 3. Convenções de span (traces)

### 3.1 Nomes e cardinalidade
- O **nome do span DEVE ser de baixa cardinalidade**: use *templates de rota* e operações, **nunca** IDs ou valores crus no nome. IDs vão em **atributos**, não no nome.
- `SpanKind` **DEVE** refletir o papel (`SERVER`, `CLIENT`, `PRODUCER`, `CONSUMER`, `INTERNAL`).

### 3.2 Por tipo de span

| Tipo | Nome (padrão) | `SpanKind` | Atributos-chave (semconv) |
|---|---|---|---|
| **HTTP server** | `{http.request.method} {http.route}` (ex.: `POST /prontuarios/{id}`) | SERVER | `http.request.method`, `http.route`, `url.path`, `http.response.status_code`, `server.address`, `client.address` |
| **HTTP client** | `{http.request.method}` | CLIENT | idem + `url.full` (sanitizado) |
| **Query SQL** (RF-H-002) | `{db.operation.name} {db.collection.name}` (ex.: `SELECT prontuario`) | CLIENT | `db.system.name=postgresql`, `db.namespace` (ex.: `prontuario_db`), `db.query.text` **(parametrizado)**, `db.operation.name` |
| **Mensageria — publish** (RF-021) | `{messaging.destination.name} publish` | PRODUCER | `messaging.system=rabbitmq`, `messaging.destination.name`, `messaging.operation.name=publish`, `messaging.message.id` |
| **Mensageria — process** | `{messaging.destination.name} process` | CONSUMER | idem + `messaging.operation.name=process` |
| **Passo de SAGA** (RF-H-016) | `saga.{flow}.{step}` | INTERNAL | `horus.saga.*` (§3.4) |
| **Compensação de SAGA** | `saga.{flow}.{step}.compensate` | INTERNAL | `horus.saga.*` + `horus.saga.compensation=true` |

### 3.3 Span de query — regra dura (RF-H-002, RNF-H-002)
- **DEVE** haver **um span por execução de SQL**, filho do span da request.
- `db.query.text` **DEVE** ser o *statement parametrizado* (`$1`, `?`) — os **valores ligados NÃO DEVEM** ser capturados (ou DEVEM ser mascarados). Ver §6.
- **DEVERIA** registrar duração e, quando disponível, indicadores de plano/linhas.

### 3.4 Atributos próprios do Horus (namespace `horus.*`)
Só para conceitos sem chave OTel padrão. Para SAGA (ADR-0013):

| Atributo | Tipo | Exemplo |
|---|---|---|
| `horus.saga.id` | string | id da SAGA no `saga_db` (ex.: `51`) |
| `horus.saga.flow` | string | `pay-then-invoice` |
| `horus.saga.step` | string | `reserve-payment` |
| `horus.saga.compensation` | bool | `true` |
| `horus.saga.recovery` | bool | `true` — compensação disparada pela recuperação automática (T-1010), em trace próprio |
| `horus.saga.outcome` | enum | `completed` \| `compensated` \| `failed` |

### 3.5 Erros em spans
- Em falha, o span **DEVE** ter `status = ERROR` e atributo `error.type`.
- A exceção **DEVE** ser registrada como **evento de span** `exception` com `exception.type`, `exception.message`, `exception.stacktrace` (mesmas chaves do log — §4).

---

## 4. Logging estruturado

- **Formato: JSON, um objeto por linha**, em **todos** os componentes (RF-H-003). Console JSON em dev; exportação **via OTLP** para o Collector → **Loki** (ADR-0010).
- **Correlação (RF-H-004):** `trace_id` e `span_id` **DEVEM** ser injetados a partir do contexto ativo (MDC/contexto de log), sempre que houver trace ativo.

### 4.1 Campos obrigatórios

| Campo | Obrig. | Exemplo |
|---|---|---|
| `timestamp` | DEVE | `2026-06-27T04:30:00.123Z` (ISO-8601, **UTC**, ms) |
| `level` | DEVE | `ERROR` (`TRACE`/`DEBUG`/`INFO`/`WARN`/`ERROR`) |
| `message` | DEVE | `falha ao aprovar pagamento` |
| `service.name` | DEVE | `payment-service` (igual ao recurso) |
| `trace_id` | DEVE¹ | `4bf92f3577b34da6a3ce929d0e0e4736` (32 hex, minúsculo) |
| `span_id` | DEVE¹ | `00f067aa0ba902b7` (16 hex) |
| `logger` | DEVERIA | `org.example...PaymentResource` |

¹ Obrigatório **quando há um trace ativo** no momento do log.

### 4.2 Campos de erro (RF-H-003) — adicionais quando `level=ERROR`
- `exception.type`, `exception.message`, `exception.stacktrace` (chaves OTel semconv; **mesmas** do evento de span).

### 4.3 Níveis
- `ERROR`: exceções (tratadas ou não) que afetam a request → **100% coletadas e correlacionadas** (objetivo O3 do PRD).
- `WARN`: condição recuperável. `INFO`: ciclo de vida. `DEBUG`/`TRACE`: diagnóstico, **desligados em prod**.

---

## 5. Métricas (mínimo)

Exportadas via OTLP → **Prometheus** (ADR-0010). **DEVERIA** seguir o semconv de métricas do OTel. Conjunto mínimo (**RED** + query):

- `http.server.request.duration` (histograma) — latência/throughput/erros de request.
- `db.client.operation.duration` (histograma) — latência de query.
- Taxa de erro derivável de `http.response.status_code` / `error.type`.

> **Cardinalidade:** *labels* **NÃO DEVEM** conter IDs crus nem PII. Métricas completas são detalhadas em T-401/T-404.

---

## 6. Sanitização e PII (RNF-H-002, ADR-0009) — normativo

> O sistema observado é um **prontuário médico**: dados clínicos e financeiros são sensíveis. Esta seção é **dura**.

- **NÃO DEVE** emitir valores sensíveis crus em **nenhum** sinal (span, log, métrica): nome de paciente, conteúdo clínico, CPF/documento, e-mail, número de cartão, valores financeiros vinculados a identidade.
- **SQL:** `db.query.text` **DEVE** ser parametrizado; **valores ligados NÃO DEVEM** ser capturados crus (§3.3).
- **Mascaramento:** e-mail → `a***@***`; documento → `***`. IDs **opacos/substitutos** (não-PII) PODEM ser mantidos como chave de correlação.
- **Duas camadas (defesa em profundidade):** (1) **na origem** — o componente não emite o dado; (2) **no Collector** — redação adicional (detalhe em **T-406**).
- **Chaves proibidas** em atributos/logs (exemplos, não exaustivo): `cpf`, `documento`, `email`, `nome_paciente`, `cartao`, `prontuario.conteudo`.

> Este contrato define **o que** sanitizar; a **aplicação/enforcement** vive na instrumentação de cada serviço (T-401/T-403) e na borda do Collector (T-406).

---

## 7. Aplicação por componente (resumo)

| Componente | Tracing | Logs JSON | Tarefa |
|---|---|---|---|
| FastAPI/Locust (Python) | `opentelemetry-instrumentation-fastapi`; propaga W3C | `logging`/`structlog` com filtro de `trace_id` | T-402 |
| Load Balancer (NGINX/Traefik) | **repassa** `traceparent` (não encerra) | access log JSON com IDs quando possível | T-402 |
| Serviços Quarkus | `quarkus-opentelemetry` (**HTTP + JDBC/Hibernate**) | `quarkus.log.console.json` + correlação OTel | T-401 |
| Worker Rust | `opentelemetry` + `tracing-opentelemetry`; **extrai `traceparent` dos headers AMQP** | `tracing-subscriber` JSON | T-403 |
| Horus | emite a própria telemetria por este contrato; **consome** os backends para correlacionar | idem | Fase 5+ |

---

## 8. Versionamento

Contrato **vivo**. Alterar **campo obrigatório**, **nome canônico** de `service.name` ou **chave** padronizada é mudança **ADR-worthy** (abrir/atualizar ADR e versionar aqui). Mudanças menores: registrar no histórico abaixo.

| Versão | Data | Mudança |
|---|---|---|
| 1.0 | 2026-06-27 | Versão inicial (T-005): recurso, propagação W3C (HTTP+AMQP), spans (HTTP/DB/mensageria/SAGA), logging JSON correlacionado, métricas mínimas, PII. |

## Referências
- ADRs: [0007](../adr/ADR-0007-opentelemetry-telemetria.md) (OTel), [0009](../adr/ADR-0009-captura-request-e-query.md) (request/query), [0010](../adr/ADR-0010-pipeline-telemetria-unificado.md) (pipeline), [0013](../adr/ADR-0013-padrao-saga.md) (SAGA).
- [`../PRD.md`](../PRD.md) — RF-029/030, RF-H-001..004/011/016, RNF-008, RNF-H-002.
- OpenTelemetry **Semantic Conventions** — confirmar a versão fixada na implementação (ver `lib.md`).
