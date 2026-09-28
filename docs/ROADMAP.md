# ROADMAP — Plano de Desenvolvimento (Horus + Sistema Observado)

| Campo | Valor |
|---|---|
| **Versão** | 1.1 (Fase 10 adicionada em 2026-09-28) |
| **Data** | 2026-06-26 |
| **Relacionado** | [`PRD.md`](./PRD.md) · [`adr/`](./adr/) · [`../lib.md`](../lib.md) |

## Estratégia

O plano sobe primeiro o **sistema observado** (o suficiente para gerar tráfego real e telemetria), depois constrói o **Horus** sobre essa telemetria. A telemetria base (Fase 4) é o **pré-requisito crítico** do Horus: sem ela, não há o que observar nem o que a IA possa resumir.

**Legenda de prioridade:** 🔴 crítico · 🟠 importante · 🟢 incremental
**Estimativas** em pontos relativos (P = pequeno, M = médio, G = grande); ajuste à sua capacidade.

> 📍 **Progresso atual (2026-09-28):** Fases 0-10 entregues. A **Fase 10 — Produto utilizável**
> (`T-1001`..`T-1012`), aberta pela análise de lacunas pós-aceite, foi concluída na branch
> `claude/beautiful-cray-sfblsr` (um commit por task). Pendências fora do código: secret
> `ANTHROPIC_API_KEY` (job `ai-live`) e 1ª execução verde do job `e2e-k8s` (kind).
>
> Histórico (2026-07-02): Fases 0-9 entregues, `T-905`
> (aceite final) validou os 16 critérios do `PRD.md` §10 com o sistema real rodando (LB real,
> cluster K8s real, SAGA real com compensação visualizada no Horus). `T-405` (Fase 4), pendente
> desde a criação deste plano por falta de infra real, também foi validada e corrigida nesta
> sessão. Este roadmap é o **plano estático**; status task-a-task e o log de entregas vivem em
> [`../state.md`](../state.md) (fonte de verdade), atualizado a cada entrega (regra R1).

---

## Fase 0 — Fundação do repositório 🔴

> Objetivo: monorepo organizado, build e CI mínimos.

| Task | Descrição | Dep. | Tam. | Requisitos |
|---|---|---|---|---|
| `T-001` | Estrutura de monorepo (`services/`, `worker/`, `loadtest/`, `horus/`, `deploy/`, `docs/`) | — | P | — |
| `T-002` | Definir build do Horus (substituir o `Main.java` placeholder por bootstrap Quarkus) | T-001 | M | RNF-014 |
| `T-003` | `docker-compose` de desenvolvimento (Postgres ×3, RabbitMQ, Collector, Jaeger, Loki, Prometheus) | T-001 | M | RF-032 |
| `T-004` | CI: build/test por componente + lint | T-002 | M | — |
| `T-005` | Convenções de logging estruturado e nomes de span (contrato de telemetria) | T-001 | P | RF-029, RF-H-004 |

**Saída:** `make up` sobe a infraestrutura local; cada componente compila.

---

## Fase 1 — Microsserviços de domínio 🔴

> Objetivo: Prontuário, Payment e Invoice em Quarkus, cada um com seu PostgreSQL e migrações.

| Task | Descrição | Dep. | Tam. | Requisitos |
|---|---|---|---|---|
| `T-101` | Scaffold Quarkus dos 3 serviços (REST + Hibernate Panache + Flyway) | T-002 | M | RF-006..020 |
| `T-102` | Prontuário Service: criar/consultar prontuário; registrar/atualizar/finalizar consulta | T-101 | G | RF-006..010, RF-025 |
| `T-103` | Payment Service: carteira/saldo, movimentações, aprovar/rejeitar/estornar, histórico | T-101 | G | RF-011..015, RF-026 |
| `T-104` | Invoice Service: receber emissão, gerar NF simulada, registrar resultado, listar, reprocessar | T-101 | G | RF-016..020, RF-027 |
| `T-105` | Migrações Flyway independentes por banco (`prontuario_db`/`payment_db`/`invoice_db`) | T-101 | M | RF-028, RNF-015 |
| `T-106` | Isolamento: credenciais/schema separados; proibição de acesso cruzado | T-105 | P | RNF-002, RNF-003 |
| `T-107` | **SAGA (orquestração)**: orquestrador próprio (estado em `saga_db`; LRA avaliado e preterido — ADR-0013) + 1º fluxo cross-service (ex.: pagar → emitir NF) com compensações idempotentes e spans correlacionados | T-102, T-103, T-104 | G | RNF-019, ADR-0013 |

**Saída:** três serviços funcionais, isolados por dado, com schema versionado, e uma **SAGA com compensações** coordenando um fluxo entre serviços.

---

## Fase 2 — Entrada e teste de carga 🔴

> Objetivo: Load Balancer + API de carga (FastAPI/Locust) gerando tráfego real.

| Task | Descrição | Dep. | Tam. | Requisitos |
|---|---|---|---|---|
| `T-201` | Load Balancer (NGINX **ou** Traefik) como entrada, roteando aos 3 serviços | T-101 | M | RF-005, RNF-006 |
| `T-202` | API FastAPI de controle: `POST /load-tests`, `GET /load-tests/{id}`, `POST /load-tests/{id}/stop` | T-201 | M | RF-001..003 |
| `T-203` | Cenários Locust (usuários virtuais, requisições concorrentes) | T-202 | M | RF-004, RNF-016 |

**Saída:** é possível disparar carga concorrente contra os serviços via Load Balancer.

---

## Fase 3 — Mensageria e worker Rust 🟠

> Objetivo: fluxo assíncrono de relatório isolado em RabbitMQ + worker Rust.

| Task | Descrição | Dep. | Tam. | Requisitos |
|---|---|---|---|---|
| `T-301` | RabbitMQ + contrato da mensagem de relatório (id, tipo, parâmetros, contexto de tracing) | T-101 | M | RF-021, RNF-011 |
| `T-302` | Publicação de solicitação de relatório pelos serviços | T-301 | M | RF-021 |
| `T-303` | Worker Rust: consumir, gerar relatório, enviar e-mail (responsabilidade limitada) | T-301 | G | RF-022..024, RNF-012 |

**Saída:** um pedido de relatório percorre RabbitMQ → worker → e-mail.

---

## Fase 4 — Telemetria base (OpenTelemetry) 🔴 *(pré-requisito do Horus)*

> Objetivo: todos os componentes emitindo traces, logs e métricas via OTLP, correlacionados ponta a ponta.

| Task | Descrição | Dep. | Tam. | Requisitos |
|---|---|---|---|---|
| `T-401` | Instrumentar os 3 serviços Quarkus com OTel (HTTP + **JDBC/Hibernate**) | T-102..104 | M | RF-029/030, RF-H-002 |
| `T-402` | Instrumentar FastAPI/Locust e o Load Balancer (propagação de contexto) | T-203 | M | RF-029 |
| `T-403` | Instrumentar o worker Rust e **propagar contexto HTTP→AMQP** nos headers | T-303 | G | RF-029, RF-H-004 |
| `T-404` | OTel Collector + backends: Jaeger (traces), Loki (logs), Prometheus (métricas) | T-003 | M | RF-031, RF-H-014 |
| `T-405` | Validar correlação ponta a ponta (request→query→log→mensagem→worker no mesmo `trace_id`) | T-401..404 | M | RF-H-004 |
| `T-406` | Sanitização/redação de PII na borda do Collector e dos serviços | T-404 | M | RNF-010, RNF-H-002 |

**Saída:** telemetria completa e correlacionada — base sobre a qual o Horus opera.

---

## Fase 5 — Horus core (ingestão, correlação, ciclo de vida) 🔴

> Objetivo: o produto principal — ver o fluxo de vida de request e query e os logs de erro.

| Task | Descrição | Dep. | Tam. | Requisitos |
|---|---|---|---|---|
| `T-501` | Serviço Horus (Quarkus): receptor OTLP / adaptadores de consulta a Jaeger-Tempo, Loki, Prometheus | T-404 | G | RF-H-014 |
| `T-502` | Modelo de correlação por `trace_id`: request ↔ queries ↔ logs ↔ mensagem ↔ worker | T-405 | G | RF-H-001/002/004 |
| `T-503` | API do ciclo de vida da **request** (waterfall de spans ponta a ponta) | T-502 | M | RF-H-001/011 |
| `T-504` | API do ciclo de vida da **query** (statement sanitizado, duração, banco de origem) | T-502 | M | RF-H-002 |
| `T-505` | Agregação de **logs de erro** correlacionados | T-502 | M | RF-H-003 |
| `T-506` | Mapa de serviços/dependências a partir dos traces | T-502 | P | RF-H-015 |
| `T-507` | **Visualização de SAGA**: exibir passos, compensações e falhas de uma SAGA correlacionados por `trace_id` | T-502, T-107 | M | RF-H-016, ADR-0013 |

**Saída:** Horus mostra, para qualquer request, o fluxo completo + queries + logs de erro, **e a visualização de SAGAs**.

---

## Fase 6 — Camada de IA (Claude) 🔴

> Objetivo: resumir o que está acontecendo e acelerar diagnóstico.

| Task | Descrição | Dep. | Tam. | Requisitos |
|---|---|---|---|---|
| `T-601` | Integração Quarkus LangChain4j (Anthropic) atrás de interface desacoplada | T-501 | M | ADR-0011 |
| `T-602` | Montador de contexto: telemetria sanitizada → prompt (com orçamento de tokens) | T-502, T-406 | G | RNF-H-003/006 |
| `T-603` | Agente **Summarizer** (resumo de estado sob demanda + agendado) | T-602 | M | RF-H-005 |
| `T-604` | Agente **Trace Explainer** (explica um trace em linguagem natural) | T-602 | M | RF-H-006 |
| `T-605` | Agente **Root-Cause Analyst** (RCA correlacionando 3 sinais) | T-602 | G | RF-H-007 |
| `T-606` | **Anomaly Detector** + **Error Clusterer** (fingerprint + IA) | T-505 | G | RF-H-008/009 |
| `T-607` | **NL Query Agent** (linguagem natural → consulta aos backends) | T-602 | G | RF-H-010 |
| `T-608` | Cache de resumos + amostragem + seleção de modelo por tarefa | T-603..607 | M | RNF-H-003/004 |

**Saída:** Horus resume o estado, explica traces, faz RCA e responde perguntas em linguagem natural.

---

## Fase 7 — Painel e alertas do Horus 🟠

> Objetivo: a interface que supera "somente Jaeger" (ADR-0008).

| Task | Descrição | Dep. | Tam. | Requisitos |
|---|---|---|---|---|
| `T-701` | Painel Horus: saúde + resumo de IA + busca | T-503..505, T-603 | G | RF-H-012 |
| `T-702` | Visualização do fluxo de vida (waterfall: spans + queries + logs + eventos de mensageria) | T-503/504/505 | G | RF-H-011 |
| `T-703` | Alertas (e-mail/webhook) com resumo de IA em anomalias/erros críticos | T-606 | M | RF-H-013 |
| `T-704` | RBAC do Horus (papéis de `ROLES.md`) | T-701 | M | RNF-H-010 |

**Saída:** experiência de observabilidade completa em um só painel.

---

## Fase 8 — Containerização e Kubernetes 🟠

| Task | Descrição | Dep. | Tam. | Requisitos |
|---|---|---|---|---|
| `T-801` | Imagens Docker de todos os executáveis (serviços, worker, FastAPI, Horus) | Fases 1-7 | M | RF-032, RNF-004 |
| `T-802` | Manifests/Helm: deployments, services, configmaps, secrets | T-801 | G | RF-033 |
| `T-803` | Escalabilidade horizontal independente por componente | T-802 | M | RNF-005 |
| `T-804` | Implantar worker e Horus **separados** dos serviços de domínio | T-802 | P | ADR-0012 |

---

## Fase 9 — Endurecimento e aceite 🟠

| Task | Descrição | Dep. | Tam. | Requisitos |
|---|---|---|---|---|
| `T-901` | TLS onde exigido; revisão de segredos/credenciais | T-802 | M | RNF-017 |
| `T-902` | Verificação de não intrusividade: derrubar Horus/IA não afeta o domínio | T-804 | M | RNF-H-008 |
| `T-903` | Orçamento de overhead de instrumentação medido sob carga (Locust) | T-405, T-803 | M | RNF-H-001/007 |
| `T-904` | Auditoria de privacidade: nenhum dado sensível cru em telemetria nem na IA | T-406, T-602 | M | RNF-H-002/006 |
| `T-905` | Validar **critérios de aceite** do PRD (itens 1-15) | Tudo | M | PRD §10 |

---

## Fase 10 — Produto utilizável (lacunas pós-aceite) 🔴

> Objetivo: fechar as lacunas identificadas após o aceite (T-905) — entregas que saíram como
> "1ª fatia" e RNFs sem task no plano original. Análise de lacunas registrada em
> [`../state.md`](../state.md) (2026-09-28). Prioridade: **P0** impede a proposta de produto;
> **P1** RNF sem cobertura; **P2** endurecimento/completude.

| Task | Descrição | Dep. | Tam. | Prio. | Requisitos |
|---|---|---|---|---|---|
| `T-1001` | **Busca e janela temporal**: portas `searchTraces(service, from, to, minDuration, hasError)` (Jaeger `/api/traces`), logs por range (Loki `query_range`), `rangeQuery` (Prometheus); `GET /horus/traces`; lista de traces recentes/com erro no painel; resumo agendado sobre janela real | T-501, T-701 | G | P0 | RF-H-005/010/012 |
| `T-1002` | **Compose com as aplicações + e2e no CI**: serviços de domínio, SAGA, worker e LB (NGINX) no `docker-compose.yml` (profile `apps`); job `e2e` que sobe o stack, dispara SAGA com compensação e verifica `/horus/lifecycle/{saga,queries,errors}` | T-801, T-905 | G | P0 | RF-005, RF-032, PRD §10 |
| `T-1003` | **Tiers de modelo reais + IA ao vivo**: modelos nomeados do quarkus-langchain4j por `ModelTier` (`@ModelName`), IDs de modelo revisados para a geração atual; job de CI opcional (gated pela secret) exercitando cada agente | T-601, T-608 | M | P0 | RNF-H-003, ADR-0011 |
| `T-1004` | **Auditoria de prompts + sanitização obrigatória**: decorator `AuditingLlmEngine` (log estruturado: agente, tier, hash do prompt, tokens) que aplica o `PromptSanitizer` na fronteira — nenhum agente consegue pular a redação | T-602, T-904 | M | P1 | RNF-H-002/006 |
| `T-1005` | **Retenção configurável** por tipo de sinal: Loki (`retention_period`), Prometheus (`--storage.tsdb.retention.time`), Jaeger (storage com TTL), via variáveis de ambiente | T-404 | P | P1 | RNF-H-005 |
| `T-1006` | **Análises pesadas assíncronas**: RCA/DEEP como job (`POST` → `jobId`, `GET /horus/ai/jobs/{id}`), executor gerenciado | T-605 | M | P1 | RNF-H-004 |
| `T-1007` | **RBAC com autenticação real**: `quarkus-oidc` (papéis via claims JWT) substituindo o cabeçalho `X-Horus-Role`; ligado por padrão fora do perfil dev; escopo por serviço do `DEVELOPER` | T-704 | M | P1 | RNF-H-010 |
| `T-1008` | **Alertas automáticos**: `AnomalyDetector` agendado com regras por config → `AlertService`, deduplicação por fingerprint/janela; e-mail real via `quarkus-mailer` | T-606, T-703, T-1001 | M | P1 | RF-H-008/013 |
| `T-1009` | **Overlay de infraestrutura K8s**: Postgres ×4, RabbitMQ, Collector e backends no cluster (overlay `dev-infra`) + Deployment/Service do LB; pods saem de `Pending` | T-802 | G | P2 | RF-033, RNF-005 |
| `T-1010` | **Robustez da SAGA**: recuperação/timeout de SAGAs interrompidas; status do span exposto em `SpanRef` para detectar falha sem depender de compensação | T-107, T-507 | M | P2 | RNF-019, RF-H-016 |
| `T-1011` | **UI de service-map e SAGA** no painel (endpoints T-506/T-507 já existem) | T-702 | M | P2 | RF-H-015/016 |
| `T-1012` | **Endurecimentos menores**: TTL no cache de IA; HPA do worker por profundidade de fila (KEDA); TLS interno (`sslmode`, `amqps`); PRD/ADR-0013 alinhados ao orquestrador próprio (não LRA) | T-608, T-803, T-901 | M | P2 | RNF-H-003, RNF-005, RNF-017 |

**Saída:** o Horus é navegável sem conhecer um `traceId` de antemão, usa a IA real com
custo controlado por tier, tem trilha auditável do que vai ao LLM e um teste ponta a ponta
automatizado que impede regressões como a do T-507.

---

## Marcos (milestones)

| Marco | Entrega | Fases |
|---|---|---|
| **M1 — Sistema observado no ar** | Carga real fluindo pelos 3 serviços + relatório assíncrono | 0-3 |
| **M2 — Telemetria correlacionada** | Traces, logs e métricas ponta a ponta via OTel | 4 |
| **M3 — Horus enxerga tudo** | Ciclo de vida de request/query + logs de erro no Horus | 5 |
| **M4 — Horus com IA** | Resumo, explicação, RCA, anomalias e NL query | 6 |
| **M5 — Produto** | Painel, alertas, K8s, endurecimento e aceite | 7-9 |
| **M6 — Produto utilizável** ✅ | Busca temporal, IA real por tier, e2e automatizado, RNFs pendentes | 10 |

## Caminho crítico

`T-001 → T-002 → T-101 → (T-102..104) → T-401 → T-404 → T-405 → T-501 → T-502 → T-601 → T-602 → T-603/605 → T-701 → T-905`

**Fase 10:** `T-1001 → T-1008` (alertas dependem da janela temporal) · `T-1002` e `T-1003` independentes e paralelizáveis.

> O gargalo de valor é **T-405 (correlação ponta a ponta)** e **T-602 (montador de contexto da IA)**: tudo que o Horus promete depende desses dois.
