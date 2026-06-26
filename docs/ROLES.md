# ROLES — Papéis do Sistema

| Campo | Valor |
|---|---|
| **Documento** | Definição de papéis (RBAC) e responsabilidades |
| **Versão** | 1.0 |
| **Data** | 2026-06-26 |
| **Relacionado** | [`PRD.md`](./PRD.md), [`adr/ADR-0008-horus-observabilidade-ia.md`](./adr/ADR-0008-horus-observabilidade-ia.md) |

> **Importante.** O documento-fonte definiu que os **serviços de domínio não têm autenticação/autorização** (o ponto de entrada é o Load Balancer, sem API Gateway — ADR-0002). Portanto, os papéis abaixo se aplicam, sobretudo, ao **Horus** (acesso ao painel, às telemetrias e à IA — RNF-H-010) e descrevem os **atores funcionais** da simulação. Os "papéis de domínio" representam *quem gera* a carga/telemetria observada, não um RBAC aplicado nos microsserviços nesta versão.

---

## 1. Papéis humanos

### 1.1 Papéis de plataforma / observabilidade (Horus)

| Papel | Descrição | Responsabilidades |
|---|---|---|
| **Platform Admin** | Administrador do Horus e da infraestrutura de observabilidade | Configurar ingestão, backends, retenção, RBAC, integrações de IA, chaves e alertas; gerenciar usuários |
| **SRE / Observability Engineer** | Operador principal do Horus | Investigar incidentes, ler todos os traces/logs/métricas, rodar RCA/consultas de IA, criar e ajustar alertas e detecção de anomalias |
| **Developer** | Desenvolvedor de um ou mais serviços | Ver o ciclo de vida de request/query e os logs de erro **do(s) seu(s) serviço(s)**, ler resumos de IA, abrir investigações |
| **Auditor / Stakeholder** | Visão de leitura para auditoria/negócio | Ler painéis, relatórios e resumos de IA; sem configurar nada |
| **Load Test Operator** | Responsável pelos testes de carga | Disparar/consultar/interromper cenários (FastAPI + Locust) e correlacionar o efeito na telemetria do Horus |

### 1.2 Papéis de domínio (atores da simulação observada)

> Representam os usuários funcionais que originam requests no sistema observado. Não impõem RBAC nos microsserviços nesta versão (ADR-0002); existem para modelar fluxos e telemetria.

| Papel | Domínio | Ações típicas (RF) |
|---|---|---|
| **Médico / Clínico** | Prontuário | Criar/consultar prontuário; registrar/atualizar/finalizar consulta (RF-006..RF-010) |
| **Operador Financeiro** | Payment | Consultar carteira/saldo; registrar movimentações; aprovar/rejeitar/estornar pagamento; consultar histórico (RF-011..RF-015) |
| **Operador Fiscal** | Invoice | Solicitar emissão; gerar NF simulada; consultar/listar; reprocessar com erro (RF-016..RF-020) |

## 2. Papéis de máquina (contas de serviço)

| Papel | Tipo | Permissões |
|---|---|---|
| **Horus Service Account** | Conta de serviço do Horus | Ingerir OTLP; **ler** (consultar) os backends de traces/logs/métricas; **chamar a API da IA** com dados sanitizados; **não** escreve nos serviços de domínio |
| **OTel Collector Account** | Pipeline de telemetria | Receber OTLP dos serviços; exportar para Jaeger/Tempo, Loki, Prometheus |
| **Service Telemetry Emitter** | Cada microsserviço/worker | **Somente emitir** telemetria (traces/logs/métricas) via OTLP |

## 3. Papéis de agentes de IA (Horus)

A automação de IA do Horus é organizada em **agentes funcionais** (papéis lógicos, não usuários). Todos operam **apenas sobre dados sanitizados** (RNF-H-002/006) e em modo **somente leitura** sobre a telemetria — nunca atuam nos serviços de domínio.

| Agente | Função (RF) | Modelo sugerido (ADR-0011) |
|---|---|---|
| **Summarizer** | Resumo periódico/sob demanda do estado da aplicação (RF-H-005) | Haiku |
| **Trace Explainer** | Explica o caminho de uma request/trace em linguagem natural (RF-H-006) | Sonnet |
| **Root-Cause Analyst** | Correlaciona traces+logs+métricas e propõe causa raiz (RF-H-007) | Opus |
| **Anomaly Detector** | Sinaliza desvios de latência/erro/queries lentas (RF-H-008) | Haiku/regra + LLM |
| **Error Clusterer** | Agrupa e resume erros semelhantes (RF-H-009) | Haiku |
| **NL Query Agent** | Traduz perguntas em linguagem natural para consultas aos backends (RF-H-010) | Sonnet |

## 4. Matriz de permissões (Horus)

Legenda: ✅ total · 🟡 limitado/escopo próprio · 👁️ leitura · ❌ sem acesso

| Capacidade | Platform Admin | SRE | Developer | Auditor | Load Test Op | Horus SA / Agentes IA |
|---|:---:|:---:|:---:|:---:|:---:|:---:|
| Ver ciclo de vida de request/query (RF-H-001/002) | ✅ | ✅ | 🟡 | 👁️ | 👁️ | 👁️ |
| Ver logs de erro (RF-H-003) | ✅ | ✅ | 🟡 | 👁️ | 👁️ | 👁️ |
| Resumo / explicação / RCA por IA (RF-H-005/006/007) | ✅ | ✅ | 🟡 | 👁️ | 👁️ | (executa) |
| Consulta em linguagem natural (RF-H-010) | ✅ | ✅ | 🟡 | 👁️ | 👁️ | (executa) |
| Configurar alertas / anomalias (RF-H-008/013) | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ |
| Configurar ingestão / backends / retenção | ✅ | 🟡 | ❌ | ❌ | ❌ | ❌ |
| Gerenciar usuários / RBAC / chaves de IA | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| Disparar/parar teste de carga (RF-001/003) | ✅ | 🟡 | 🟡 | ❌ | ✅ | ❌ |
| Atuar/escrever nos serviços de domínio | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |

> "Developer 🟡" = restrito aos serviços sob sua responsabilidade. Nenhum papel (humano ou de IA) tem permissão de **agir** sobre os serviços de domínio via Horus: a plataforma **observa, explica e alerta**, mas não remedia (ver "Fora do escopo" no PRD).

## 5. Responsabilidades de manutenção (governança do fluxo)

Além dos papéis de acesso, o sistema define **responsabilidades de processo** que recaem sobre quem executa o trabalho (pessoa ou agente, incluindo o Claude Code). Estas responsabilidades são **obrigatórias** e estão detalhadas em [`WORKFLOW.md`](./WORKFLOW.md).

| Responsabilidade | Quem | Regra |
|---|---|---|
| **Manter o `state.md`** | Executor da task | Atualizar [`../state.md`](../state.md) **a cada entrega** (task concluída ou marco): *última entrega*, *próxima ação* e *log* (regra **R1**) |
| **Manter o plano de execução** | Executor da task | Atualizar o `PLAN.md` da task **sempre que criar/modificar** um arquivo previsto: `Status`, `Atualizado` e *Registro de alterações* (regra **R2**) |
| **Seguir o fluxo** | Executor da task | Respeitar `roadmap → task → PRD → plano de execução`, com **branch por task** e **merge via PR** |
| **Revisar antes do merge** | Revisor do PR | Garantir critérios de aceite do PRD da task, plano 100% ✅ e `state.md` atualizado |

> Estas regras existem para que o estado do projeto seja sempre auditável a partir de `state.md` e dos `PLAN.md` — sem depender de memória de sessão.

## 6. Princípios de RBAC

- **Menor privilégio:** cada papel recebe apenas o necessário.
- **Separação de deveres:** quem configura a plataforma (Admin) é distinto de quem só investiga (SRE) ou só lê (Auditor).
- **Observabilidade passiva:** contas de serviço e agentes de IA são **somente leitura** sobre telemetria e **não** escrevem nos domínios (RNF-H-008).
- **Privacidade por padrão:** agentes de IA recebem somente dados **sanitizados** (RNF-H-002/006).
