# PRD — Horus: Plataforma de Observabilidade Automatizada com IA

| Campo | Valor |
|---|---|
| **Produto** | Horus — Observabilidade aumentada por IA |
| **Versão do documento** | 1.0 |
| **Data** | 2026-06-26 |
| **Status** | Proposto |
| **Fonte de requisitos herdados** | *Documento Consolidado de Escopo e Requisitos — Simulação de Sistema de Prontuário Médico, v2.0 (Junho/2026)* |

> **Nota de leitura.** Este PRD consolida dois níveis. O **sistema observado** é a simulação de prontuário médico em microsserviços descrita no documento-fonte (requisitos `RF-0xx` / `RNF-0xx`). O **produto principal** deste repositório é o **Horus**, a camada de observabilidade automatizada com IA que enxerga e explica esse sistema (requisitos `RF-H-xxx` / `RNF-H-xxx`). O foco de engenharia está no Horus; a simulação é a carga de trabalho de referência que ele observa.

---

## 1. Visão

**Horus é o "olho que tudo vê" da aplicação.** Ele captura o ciclo de vida completo de cada *request* e de cada *query* executada, centraliza todos os logs de erro e usa IA (Claude) para transformar telemetria bruta — traces, logs e métricas — em explicações em linguagem natural: o que está acontecendo agora, por que algo ficou lento, qual a causa provável de um erro e o que fazer a respeito.

O objetivo é eliminar a tarefa manual de "ler dezenas de telas para entender um incidente". Em vez de o operador navegar por traces e logs, o Horus **lê a telemetria por ele** e responde em texto.

## 2. Problema

A simulação de prontuário médico é poliglota (Python, Java/Quarkus, Rust) e distribuída (3 microsserviços, 3 bancos, mensageria, worker). O documento-fonte previu apenas o **Jaeger UI** para traces e proibiu dashboard próprio. Na prática isso significa:

- O fluxo de uma request precisa ser reconstruído manualmente, span a span.
- As **queries SQL** não têm visão de ciclo de vida própria (quem disparou, com quais parâmetros, quanto durou, em qual banco).
- Os **logs de erro** ficam dispersos por componente, sem correlação com o trace que os originou.
- Não há síntese: nada resume o estado do sistema nem aponta causa raiz.

Horus resolve isso adicionando uma camada de correlação + IA por cima da telemetria padrão.

## 3. Objetivos e metas

| # | Objetivo | Métrica de sucesso |
|---|---|---|
| O1 | Ver o ciclo de vida completo de qualquer request | 100% das requests do fluxo síncrono representadas como trace navegável (entrada→serviço→DB→resposta) |
| O2 | Ver o ciclo de vida de cada query | Toda query SQL aparece como span filho correlacionado, com duração e parâmetros sanitizados |
| O3 | Centralizar todos os logs de erro | 100% das exceções dos componentes coletadas e vinculadas a `trace_id`/`span_id` |
| O4 | Resumir o que está acontecendo, via IA | Resumo de saúde sob demanda em ≤ 30 s; resumo periódico automático |
| O5 | Acelerar diagnóstico | RCA assistida por IA reduz o tempo de "do erro à causa provável" para minutos |
| O6 | Não intrusividade | Falha do Horus/IA nunca degrada os serviços de domínio |

## 4. Escopo

### 4.1 Dentro do escopo

**Sistema observado (herdado do documento-fonte):**
- API de teste de carga em Python (FastAPI para controle + Locust para geração de carga).
- Load Balancer (NGINX ou Traefik) como ponto de entrada — **sem** API Gateway.
- Três microsserviços Quarkus independentes: **Prontuário**, **Payment**, **Invoice**.
- Um banco PostgreSQL exclusivo por serviço (`prontuario_db`, `payment_db`, `invoice_db`) com migrações independentes.
- RabbitMQ **restrito** a solicitações assíncronas de relatório.
- Report & Email Worker em **Rust** (gerar relatório + enviar e-mail, nada além disso).
- **Coordenação de fluxos entre serviços via SAGA (orquestração)**, com passos idempotentes e compensações (ADR-0013).
- Empacotamento Docker e orquestração Kubernetes.

**Horus (produto principal):**
- Ingestão de telemetria via **OTLP** (traces, logs e métricas) de todos os componentes.
- Captura e correlação do **ciclo de vida da request** ponta a ponta.
- Captura do **ciclo de vida da query** (instrumentação de JDBC/Hibernate e equivalentes).
- Agregação e correlação de **todos os logs de erro**.
- **Camada de IA (Claude)**: resumo de estado, explicação de trace, RCA, detecção de anomalias, clusterização de erros e consulta em linguagem natural.
- **Painel próprio do Horus** (que supera a restrição "somente Jaeger" do documento-fonte — ver ADR-0008).
- Backends de armazenamento de telemetria (traces, logs, métricas) e o Jaeger UI mantido para inspeção de traces crus.

### 4.2 Fora do escopo

- MongoDB, auditoria centralizada de negócio e armazenamento próprio de logs de negócio (mantém-se o documento-fonte).
- Rollback distribuído ACID / two-phase commit (2PC) entre serviços — **SAGA com compensações é usada no lugar** (ADR-0013).
- Pagamentos reais, atendimento clínico real ou emissão fiscal real (tudo simulado).
- API Gateway com autenticação/autorização **para os serviços de domínio** (o ponto de entrada continua o Load Balancer). **Exceção:** o Horus possui seu próprio RBAC para acesso ao painel e às telemetrias (ver `ROLES.md`).
- Ações corretivas automáticas (auto-remediação): o Horus **observa, explica e alerta**, mas não atua sobre os serviços nesta versão.

### 4.3 Premissas

- Os três bancos podem dividir a mesma infraestrutura física no ambiente de estudo, mas permanecem **logicamente isolados** (banco/schema/credenciais).
- Nenhum microsserviço consulta diretamente as tabelas de outro; a interação é por API HTTP, e o RabbitMQ é só para relatórios.
- A integração fiscal e o processamento financeiro são **simulados**.
- O envio de dados à IA usa apenas conteúdo **sanitizado** (sem PII/dados sensíveis crus).

## 5. Personas e papéis (resumo)

Detalhamento completo, matriz de permissões e papéis de agentes de IA em [`ROLES.md`](./ROLES.md).

| Persona | Necessidade central no Horus |
|---|---|
| SRE / Engenheiro de Observabilidade | Ver saúde, investigar incidentes, configurar alertas, perguntar à IA |
| Desenvolvedor (de um serviço) | Ver o ciclo de vida das suas requests/queries e os resumos de erro |
| Operador de teste de carga | Disparar carga (Locust) e correlacionar o efeito na telemetria |
| Auditor / Stakeholder | Ler relatórios e resumos de IA, somente leitura |
| Operadores de domínio (Médico, Financeiro, Fiscal) | Operar os serviços simulados; geram a telemetria observada |
| Conta de serviço Horus / Agentes de IA | Ler telemetria sanitizada e chamar o LLM (papéis de máquina) |

## 6. Requisitos funcionais

### 6.1 Sistema observado (herdados do documento-fonte)

| ID | Capacidade | Descrição |
|---|---|---|
| RF-001 | Iniciar teste de carga | API Python inicia um cenário de carga |
| RF-002 | Consultar teste de carga | Consultar estado/resultados de um teste |
| RF-003 | Interromper teste de carga | Interromper teste em execução |
| RF-004 | Gerar requisições concorrentes | Locust gera usuários virtuais e chamadas HTTP concorrentes |
| RF-005 | Balancear requisições | Load Balancer recebe e distribui chamadas entre instâncias |
| RF-006 | Criar prontuário | Prontuário Service cria prontuário |
| RF-007 | Consultar prontuário | Prontuário Service consulta prontuários |
| RF-008 | Registrar consulta | Registrar consulta vinculada ao prontuário |
| RF-009 | Atualizar consulta | Atualizar dados de uma consulta |
| RF-010 | Finalizar consulta | Finalizar uma consulta |
| RF-011 | Consultar carteira e saldo | Payment Service consulta carteira/saldo |
| RF-012 | Registrar entradas e saídas | Registrar movimentações financeiras |
| RF-013 | Aprovar/rejeitar pagamento | Processar e registrar aprovação/rejeição |
| RF-014 | Estornar pagamento | Estornar pagamento processado |
| RF-015 | Consultar histórico financeiro | Histórico de pagamentos/movimentações |
| RF-016 | Receber solicitação de emissão | Invoice Service recebe pedido de NF simulada |
| RF-017 | Criar e gerar nota fiscal | Criar/gerar NF simulada |
| RF-018 | Registrar resultado da emissão | Registrar sucesso/falha |
| RF-019 | Consultar/listar notas fiscais | Listar NFs simuladas |
| RF-020 | Reprocessar notas com erro | Reprocessar NFs com emissão falha |
| RF-021 | Publicar solicitação de relatório | Microsserviços publicam pedido no RabbitMQ |
| RF-022 | Consumir solicitação de relatório | Worker Rust consome o pedido |
| RF-023 | Gerar relatório | Worker gera o relatório |
| RF-024 | Enviar relatório por e-mail | Worker envia por e-mail |
| RF-025..027 | Persistir por serviço | Cada serviço persiste só em seu banco (`prontuario_db`/`payment_db`/`invoice_db`) |
| RF-028 | Migrações independentes | Cada serviço evolui seu schema por migrações próprias |
| RF-029 | Propagar contexto de tracing | Propagar contexto em HTTP e mensagens RabbitMQ |
| RF-030 | Coletar traces distribuídos | OpenTelemetry instrumenta e coleta spans |
| RF-031 | Visualizar traces no Jaeger | Jaeger UI consulta caminho/duração |
| RF-032 | Executar em contêineres | Componentes em imagens Docker |
| RF-033 | Orquestrar componentes | Kubernetes implanta/gerencia/escala |

### 6.2 Horus — observabilidade automatizada com IA (foco principal)

| ID | Capacidade | Descrição | Prioridade |
|---|---|---|---|
| **RF-H-001** | Ciclo de vida da request | Registrar e exibir cada request ponta a ponta (LB → microsserviço → DB → resposta) como trace navegável, com timeline/waterfall de spans | **Must** |
| **RF-H-002** | Ciclo de vida da query | Instrumentar e registrar **cada query** (statement sanitizado, parâmetros mascarados, duração, banco/serviço de origem, plano quando disponível) como span correlacionado à request | **Must** |
| **RF-H-003** | Agregação de logs de erro | Coletar e centralizar **todos** os logs de erro de todos os componentes (Python, Quarkus, Rust, LB), com stack trace, vinculados a `trace_id`/`span_id` | **Must** |
| **RF-H-004** | Correlação ponta a ponta | Vincular request ↔ queries ↔ logs ↔ mensagem RabbitMQ ↔ processamento do worker por um identificador de correlação | **Must** |
| **RF-H-005** | Resumo de estado por IA | Gerar, sob demanda e periodicamente, resumo em linguagem natural do que está acontecendo (saúde, throughput, latências, erros, anomalias) | **Must** |
| **RF-H-006** | Explicação de trace por IA | Dado um trace/request, a IA descreve o caminho, onde houve lentidão/erro e a provável causa | **Must** |
| **RF-H-007** | RCA assistida por IA | Para um incidente, a IA correlaciona traces + logs + métricas e propõe causa raiz provável e próximos passos | **Must** |
| **RF-H-008** | Detecção de anomalias | Identificar desvios (pico de latência, taxa de erro, query lenta) e sinalizar | **Should** |
| **RF-H-009** | Clusterização de erros | Agrupar erros semelhantes (fingerprint + IA) e resumir cada grupo | **Should** |
| **RF-H-010** | Consulta em linguagem natural | Usuário pergunta ("quais as queries mais lentas na última hora?") e o Horus traduz para consultas aos backends e responde | **Should** |
| **RF-H-011** | Fluxo de vida visual | Visualizar o fluxo completo de uma request (waterfall de spans + queries + logs + eventos de mensageria) | **Must** |
| **RF-H-012** | Painel Horus | Dashboard próprio com saúde, resumos de IA e busca (supera "somente Jaeger" — ADR-0008) | **Must** |
| **RF-H-013** | Alertas com resumo | Enviar alerta (e-mail/webhook) com resumo de IA em anomalias/erros críticos | **Should** |
| **RF-H-014** | Ingestão e retenção | Receber telemetria via OTLP e persistir/consultar nos backends de traces, logs e métricas | **Must** |
| **RF-H-015** | Mapa de serviços | Derivar dos traces o mapa de serviços e dependências | **Could** |
| **RF-H-016** | Visualização de SAGA | Visualizar a SAGA completa — passos, compensações e falhas — correlacionada por `trace_id` (ADR-0013) | **Should** |

## 7. Requisitos não funcionais

### 7.1 Herdados (documento-fonte)

| ID | Tema | Descrição |
|---|---|---|
| RNF-001 | Finalidade de estudo | Identificado como simulação; não para uso clínico/financeiro/fiscal real |
| RNF-002 | Isolamento de persistência | Cada serviço é dono do seu banco; sem acesso direto a estruturas de outro |
| RNF-003 | Credenciais segregadas | Credenciais separadas por serviço |
| RNF-004 | Portabilidade | Todos os executáveis empacotados em Docker |
| RNF-005 | Escalabilidade horizontal | API de carga, serviços e worker escaláveis no K8s |
| RNF-006 | Balanceamento de carga | Entrada distribui entre instâncias |
| RNF-007 | Observabilidade distribuída | Traces distribuídos via OpenTelemetry |
| RNF-008 | Correlação ponta a ponta | Contexto preservado em HTTP e mensagens |
| RNF-009 | Visualização de traces | Traces consultáveis no Jaeger UI |
| RNF-010 | Proteção de dados sensíveis | Dados clínicos/financeiros sensíveis não gravados indevidamente em traces |
| RNF-011 | Desacoplamento assíncrono | Relatório/e-mail não bloqueiam o fluxo síncrono |
| RNF-012 | Worker limitado | Worker Rust não coordena regra de negócio |
| RNF-013 | Confiabilidade do processamento | Falha do worker é rastreável sem derrubar o domínio |
| RNF-014 | Manutenibilidade | Separar regra de negócio, HTTP, persistência e integração |
| RNF-015 | Evolução de banco | Mudanças versionadas por migrações reproduzíveis |
| RNF-016 | Compatibilidade com carga | Suportar cenários concorrentes do Locust |
| RNF-017 | Comunicação segura | Permitir TLS onde exigido |
| RNF-018 | Sem dashboard próprio (revisado) | **Revisado pelo Horus** — ver ADR-0008 |
| RNF-019 | Consistência via SAGA (revisado) | Fluxos entre serviços usam **SAGA (orquestração)** com passos idempotentes e compensações; **sem** rollback ACID/2PC (ADR-0013; substitui a decisão anterior de não-SAGA) |

### 7.2 Horus

| ID | Tema | Descrição |
|---|---|---|
| **RNF-H-001** | Overhead controlado | A instrumentação não pode degradar significativamente a latência dos serviços observados (orçamento de overhead definido e medido) |
| **RNF-H-002** | Redação de dados sensíveis | Parâmetros de query e campos de log com PII/dados clínicos/financeiros são mascarados **antes** de persistir e **antes** de enviar à IA (reforça RNF-010) |
| **RNF-H-003** | Controle de custo de LLM | Seleção de modelo por tarefa, amostragem, cache de resumos e limites de tokens/orçamento |
| **RNF-H-004** | Latência da IA | Resumos sob demanda em tempo aceitável; análises pesadas executadas de forma assíncrona |
| **RNF-H-005** | Retenção configurável | Retenção por tipo de telemetria (traces, logs, métricas) configurável |
| **RNF-H-006** | Governança de dados à IA | Apenas dados sanitizados saem para o LLM; trilha do que foi enviado |
| **RNF-H-007** | Escalabilidade de ingestão | Suportar alto volume de spans/logs sob carga do Locust |
| **RNF-H-008** | Não intrusividade | Observabilidade é passiva: falha do Horus/IA não afeta os serviços de domínio |
| **RNF-H-009** | Extensibilidade | Novos serviços/linguagens integram via OTel sem alterar o Horus |
| **RNF-H-010** | Segurança/RBAC | Acesso ao painel e às telemetrias controlado por papéis (ver `ROLES.md`) |

## 8. Arquitetura (visão de alto nível)

```
        ┌─────────────────────────────────────────────────────────────┐
        │                       SISTEMA OBSERVADO                       │
        │                                                               │
  Locust│   Load Balancer        ┌────────────┐                        │
 ──────►│   (NGINX/Traefik) ─────►│ Prontuário │──► prontuario_db       │
 FastAPI│        │                └────────────┘                        │
 (ctrl) │        ├───────────────►┌────────────┐                        │
        │        │                │  Payment   │──► payment_db          │
        │        │                └────────────┘                        │
        │        └───────────────►┌────────────┐                        │
        │                         │  Invoice   │──► invoice_db          │
        │                         └─────┬──────┘                        │
        │                  (só relatório)│ publica                      │
        │                         ┌──────▼──────┐    ┌──────────────┐   │
        │                         │  RabbitMQ   │───►│ Worker (Rust)│──►│ e-mail
        │                         └─────────────┘    └──────────────┘   │
        └───────────────┬───────────────────────────────────┬──────────┘
                        │ OTLP (traces, métricas, logs)      │
                        ▼                                     ▼
              ┌───────────────────────┐         ┌──────────────────────────┐
              │  OpenTelemetry        │         │        HORUS              │
              │  Collector            │────────►│  Ingestão (OTLP)          │
              └──────────┬────────────┘         │  Correlação request/query │
                         │                       │  Agregação de erros       │
        ┌────────────────┼─────────────┐        │  ┌─────────────────────┐  │
        ▼                ▼              ▼        │  │  Camada de IA       │  │
   Traces           Métricas         Logs       │  │  (Claude/LangChain4j)│ │
 (Jaeger/Tempo)   (Prometheus)     (Loki)  ◄────┤  │  resumo · RCA ·      │  │
        ▲                ▲              ▲        │  │  anomalias · NL query│  │
        └────────────────┴──────────────┴───────┤  └─────────────────────┘  │
                consulta backends               │  API + Painel Horus       │
                                                 └──────────────────────────┘
```

> **SAGA (orquestração):** os fluxos entre serviços são coordenados por uma SAGA sobre HTTP (MicroProfile LRA + coordenador), não desenhada no diagrama acima; cada passo e compensação é um span correlacionado que o Horus visualiza (RF-H-016). O RabbitMQ permanece restrito a relatórios. Ver ADR-0013.

Decisões detalhadas em [`adr/`](./adr/). Componentes e versões em [`../lib.md`](../lib.md). Plano de execução em [`ROADMAP.md`](./ROADMAP.md).

## 9. A camada de IA (detalhe)

- **Modelo padrão:** família Claude via extensão **Quarkus LangChain4j (Anthropic)**.
  - **Haiku** (`claude-haiku-4-5-20251001`) — resumos de alto volume e baixo custo (estado periódico, clusterização).
  - **Sonnet** (`claude-sonnet-4-6`) — explicação de trace e consultas em linguagem natural (equilíbrio).
  - **Opus** (`claude-opus-4-8`) — RCA profunda em incidentes (raciocínio mais forte).
- **Entrada do modelo:** contexto montado a partir de telemetria **sanitizada** (spans, métricas agregadas, amostras de logs de erro, fingerprints).
- **Padrões de uso:**
  1. *Resumo periódico* (cron) → estado geral em linguagem natural.
  2. *Explique este trace* (sob demanda) → narrativa do caminho da request.
  3. *RCA* (gatilho de anomalia/erro) → causa provável + próximos passos.
  4. *Pergunte ao Horus* (NL → consulta aos backends → resposta).
- **Custo e governança:** cache de resumos, amostragem de spans, orçamento de tokens, e redação de PII antes do envio (RNF-H-002/003/006). Para IDs de modelo, parâmetros, limites e preços vigentes, consultar a referência da API Claude/Anthropic no momento da implementação — **não fixar números neste documento**.

## 10. Critérios de aceite

**Herdados (sistema observado):**
1. A API Python inicia e controla um teste de carga executado pelo Locust.
2. O Load Balancer distribui as requisições para os serviços Quarkus.
3. Cada serviço persiste dados somente em seu banco PostgreSQL.
4. Uma solicitação de relatório percorre RabbitMQ e é processada pelo worker Rust.
5. O relatório é gerado e enviado por e-mail.
6. O trace pode ser visualizado no Jaeger UI com correlação entre componentes.
7. A solução executa em contêineres e é compatível com Kubernetes.
8. Fluxos entre serviços são coordenados por **SAGA com compensações** (ADR-0013), com cada passo idempotente; não há rollback ACID/2PC nem auditoria em MongoDB.

**Horus (produto principal):**
9. Qualquer request do fluxo síncrono é exibida como trace navegável de ponta a ponta (RF-H-001).
10. Toda query SQL aparece correlacionada à sua request, com duração e parâmetros sanitizados (RF-H-002).
11. Todos os logs de erro são coletados e correlacionados a `trace_id`/`span_id` (RF-H-003/004).
12. O Horus produz, sob demanda, um resumo em linguagem natural do estado da aplicação (RF-H-005).
13. Para um trace selecionado, a IA explica o caminho e aponta a provável causa de erro/lentidão (RF-H-006/007).
14. A falha simulada do Horus ou da camada de IA não afeta os serviços de domínio (RNF-H-008).
15. Nenhum dado sensível cru é persistido em telemetria nem enviado à IA (RNF-H-002/006).
16. Uma SAGA com ao menos uma compensação é executada e **visualizada de ponta a ponta no Horus**, com passos e compensações correlacionados (RF-H-016).

## 11. Riscos

| Risco | Impacto | Mitigação |
|---|---|---|
| Arquitetura poliglota (Python/Java/Rust) | Esforço de padronizar instrumentação | OTel como contrato único de telemetria (ADR-0007/0010) |
| Perda de contexto entre HTTP e RabbitMQ | Quebra a correlação ponta a ponta | Propagação obrigatória de `traceparent` nos headers da mensagem (RF-029/RF-H-004) |
| Isolamento lógico de bancos não respeitado | Acoplamento por dados | Credenciais/schema por serviço; verificação na revisão (RNF-002) |
| Custo/latência da IA | Inviabiliza uso contínuo | Modelo por tarefa, cache, amostragem, orçamento (RNF-H-003) |
| Vazamento de PII para a IA/telemetria | Privacidade | Redação antes de persistir e de enviar (RNF-H-002/006) |
| Overhead de instrumentação sob carga | Degrada o sistema observado | Amostragem e orçamento de overhead medido (RNF-H-001) |
| Horus vira ponto único de falha | Cegueira de observabilidade | Observabilidade passiva e isolada; serviços não dependem do Horus (RNF-H-008) |
| Complexidade de SAGA/compensações | Inconsistência entre serviços | Idempotência obrigatória, compensação por passo, coordenador LRA observável e visualização da SAGA no Horus (ADR-0013, RF-H-016) |

## 12. Questões em aberto

1. Backend de traces definitivo: manter **Jaeger** (exigido no fonte) e/ou adotar **Grafana Tempo** ao lado? (ver ADR-0010)
2. O painel Horus reaproveita Grafana ou é uma UI própria? (ver ADR-0008/0012)
3. RAG sobre runbooks/erros históricos com **pgvector** no Postgres — incluir já na v1 ou na v2?
4. Limites concretos de orçamento de tokens e de overhead de instrumentação a serem fixados antes da Fase 6.
