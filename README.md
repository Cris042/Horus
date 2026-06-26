# Horus 👁️ — Observabilidade Automatizada com IA

> **Horus é o "olho que tudo vê" da aplicação.** Captura o **ciclo de vida completo de cada request e de cada query**, centraliza **todos os logs de erro** e usa **IA (Claude)** para resumir, em linguagem natural, o que está acontecendo no sistema — estado, explicação de traces, análise de causa raiz e detecção de anomalias.

Este repositório contém **dois níveis**:

- **Horus** (produto principal): a plataforma de observabilidade aumentada por IA.
- **Sistema observado** (carga de referência): uma simulação de **prontuário médico** em microsserviços, derivada do *Documento Consolidado de Escopo e Requisitos v2.0*.

A documentação de engenharia lidera o código — comece por [`docs/PRD.md`](docs/PRD.md).

---

## Arquitetura (resumo)

```
Locust/FastAPI → Load Balancer → 3 microsserviços Quarkus → PostgreSQL por serviço
                                  (Prontuário, Payment, Invoice)
                                        │ (só relatório)
                                   RabbitMQ → Worker Rust → e-mail
                                        │
   Todos exportam OTLP → OTel Collector → Jaeger (traces) · Loki (logs) · Prometheus (métricas)
                                        │
                                   ┌────▼─────────────────────────┐
                                   │  HORUS  — correlaciona +       │
                                   │  camada de IA (Claude) +       │
                                   │  painel próprio                │
                                   └────────────────────────────────┘
```

Diagrama completo e decisões: [`docs/PRD.md`](docs/PRD.md) · [`docs/adr/`](docs/adr/).

### Componentes

| Componente | Stack | Papel |
|---|---|---|
| API de carga | Python · FastAPI + Locust | Controlar e gerar teste de carga |
| Entrada | NGINX ou Traefik | Load Balancer (sem API Gateway) |
| Prontuário / Payment / Invoice | Quarkus (Java 25) | Microsserviços de domínio (banco próprio) |
| Mensageria | RabbitMQ | Apenas solicitações de relatório |
| Worker | Rust | Gerar relatório + enviar e-mail |
| Telemetria | OpenTelemetry · Jaeger · Loki · Prometheus | Traces, logs e métricas |
| **Horus** | Quarkus + LangChain4j (Claude) | **Observabilidade com IA** |
| Plataforma | Docker · Kubernetes | Empacotamento e orquestração |

Versões completas em [`lib.md`](lib.md).

---

## 📚 Documentação

| Documento | Conteúdo |
|---|---|
| [`docs/PRD.md`](docs/PRD.md) | Escopo, requisitos (`RF-*`/`RNF-*` e `RF-H-*`/`RNF-H-*`), arquitetura, critérios de aceite |
| [`docs/adr/`](docs/adr/) | 12 Architecture Decision Records (a numeração pula 0006, removido) |
| [`docs/ROLES.md`](docs/ROLES.md) | Papéis (humanos, de máquina e de agentes de IA) + RBAC |
| [`docs/ROADMAP.md`](docs/ROADMAP.md) | Plano em fases, tasks `T-xxx`, caminho crítico |
| [`docs/WORKFLOW.md`](docs/WORKFLOW.md) | Fluxo de trabalho (processo canônico) |
| [`lib.md`](lib.md) | Dependências e versões por componente |
| [`state.md`](state.md) | **Estado atual do projeto** (última entrega) |

---

## 🔄 Fluxo de desenvolvimento

O projeto segue um fluxo de governança explícito (detalhe em [`docs/WORKFLOW.md`](docs/WORKFLOW.md)):

```
ROADMAP  →  TASK  →  PRD da task  →  PLANO DE EXECUÇÃO  →  branch  →  PR  →  merge
```

- Cada task (`T-xxx` do roadmap) ganha uma pasta `docs/tasks/T-xxx/` com **`PRD.md`** (objetivo, critérios) e **`PLAN.md`** (plano **arquivo por arquivo**), a partir de [`docs/tasks/_TEMPLATE/`](docs/tasks/_TEMPLATE/).
- **Uma task = uma branch (`task/T-xxx-<slug>`) = um PR.** `main` permanece sempre integrável.
- **Regras obrigatórias:**
  - **R1** — atualizar [`state.md`](state.md) **a cada entrega**.
  - **R2** — atualizar o `PLAN.md` da task **sempre que criar/modificar** um arquivo previsto.

---

## 🧠 Skills adotadas (review e monitoramento)

Instaladas via [`skillfish`](https://www.npmjs.com/package/skillfish) a partir de `affaan-m/everything-claude-code`:

```bash
npx skillfish add affaan-m/everything-claude-code docker-patterns
npx skillfish add affaan-m/everything-claude-code postgres-patterns
npx skillfish add affaan-m/everything-claude-code security-review
npx skillfish add affaan-m/everything-claude-code backend-patterns
npx skillfish add affaan-m/everything-claude-code database-migrations
npx skillfish add affaan-m/everything-claude-code benchmark
```

| Skill | Uso |
|---|---|
| `security-review` | Segurança: injeção, segredos, authz, vazamento de PII |
| `backend-patterns` | Boas práticas de API, camadas, tratamento de erro |
| `postgres-patterns` | Modelagem, índices, queries sargáveis, anti-padrões |
| `jpa-patterns` | **N+1**, fetch join, `@BatchSize`, `EntityGraph` |
| `database-migrations` | Migrações seguras, reversíveis, zero-downtime |
| `docker-patterns` | Imagens, multi-stage, segurança de contêiner |
| `benchmark` | Regressões e custo de desempenho |

---

## 🤖 CI/CD e automações de IA

Todos os workflows estão em [`.github/workflows/`](.github/workflows/) e usam **Claude** via `anthropics/claude-code-action`.

### Review automatizado por PR
- **[`code-review.yml`](.github/workflows/code-review.yml)** — em cada Pull Request, revisa o diff usando as skills, com foco em **boas práticas, segurança e desempenho (N+1 e full table scan)**. Publica achados como comentários por severidade. Rubrica em [`.github/code-review-guidelines.md`](.github/code-review-guidelines.md).

### Rotinas diárias de monitoramento com IA
Executam 1× por dia (cron) e **pulam automaticamente** se faltarem secrets:

| Rotina | Workflow | O que faz |
|---|---|---|
| **Auditoria de dependências** | [`daily-dependency-audit.yml`](.github/workflows/daily-dependency-audit.yml) | Mapeia todas as dependências (Trivy), e se houver vulnerabilidade **crítica**, **abre um PR** com a sugestão de correção (bump de versão). |
| **Análise de logs** | [`daily-log-analysis.yml`](.github/workflows/daily-log-analysis.yml) | Lê os logs de erro das últimas 24h (Loki) e **abre uma issue** com os problemas prováveis no código e causas raiz. |
| **Análise de banco/queries** | [`daily-db-analysis.yml`](.github/workflows/daily-db-analysis.yml) | Analisa `pg_stat_statements` e full scans, identifica **onde o banco é mais exigido**, N+1 e índices ausentes, e **abre uma issue** com as ações de maior impacto. |

### Secrets necessários (Settings → Secrets and variables → Actions)

| Secret | Usado por | Obrigatório |
|---|---|---|
| `ANTHROPIC_API_KEY` | todos os workflows de IA | **Sim** |
| `LOKI_URL` (e opcional `LOKI_TOKEN`) | análise de logs | Para a rotina de logs |
| `DB_DSNS` (URLs psql separadas por espaço) | análise de banco | Para a rotina de banco |

> Sem `ANTHROPIC_API_KEY`, as rotinas de IA são puladas sem falhar. A rotina de banco requer a extensão `pg_stat_statements` habilitada. Endpoints internos (Loki/DB) podem exigir um **runner self-hosted** com acesso de rede.

---

## 🧭 Estratégias adotadas

Resumo das decisões-chave (cada uma é um ADR em [`docs/adr/`](docs/adr/)):

- **Observabilidade com IA é o foco** — Horus supera o "somente Jaeger" (ADR-0008); captura de ciclo de vida de request e **query** (ADR-0009); pipeline unificado traces+logs+métricas (ADR-0010); IA com Claude para resumo/RCA/anomalias (ADR-0011).
- **Microsserviços poliglotas** Python/Java/Rust como caso de estudo (ADR-0001).
- **Banco por serviço**, isolado, com migrações independentes (ADR-0003).
- **Mensageria restrita** a relatórios; comunicação de negócio síncrona por HTTP (ADR-0004).
- **Worker Rust** de responsabilidade limitada (ADR-0005).
- **SAGA adotada desde o início** (orquestração via MicroProfile LRA) para fluxos entre serviços — reverte a decisão anterior de não-SAGA — porque os fluxos de SAGA (passos + compensações) são o material distribuído que o Horus observa (ADR-0013). Sem rollback ACID/2PC.
- **OpenTelemetry** como contrato único de telemetria (ADR-0007); **Docker + Kubernetes** (ADR-0012).
- **Privacidade**: sanitização de PII antes de persistir telemetria e antes de enviar à IA (RNF-H-002).

---

## 🚦 Estado atual

Projeto em **Fase 0 (Fundação)**. Documentação e governança estabelecidas; próxima task de execução: **`T-001` — estrutura de monorepo**.
Veja sempre [`state.md`](state.md) para o estado mais recente.

## Pré-requisitos de desenvolvimento

- JDK 25, Maven 3.9+ (serviços Quarkus e Horus)
- Python 3.13 (API de carga), Rust 1.85+ (worker)
- Docker + Docker Compose (infra local); Kubernetes (deploy)

> O código ainda é um scaffold (`src/main/java/org/example/Main.java`); será substituído pelo bootstrap Quarkus do Horus na task `T-002`. Detalhes para contribuidores e para o Claude Code em [`CLAUDE.md`](CLAUDE.md).
