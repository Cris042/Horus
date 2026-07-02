# PRD — T-905: Validar critérios de aceite do PRD (itens 1-16)

| Campo | Valor |
|---|---|
| **Task** | `T-905` |
| **Fase do roadmap** | Fase 9 — Endurecimento e aceite (**última task do roadmap**) |
| **Status** | `Entregue` |
| **Branch** | `task/T-905-final-acceptance` |
| **Depende de** | Tudo |
| **Requisitos atendidos** | `PRD.md` §10 |
| **Data** | 2026-07-02 |

## Objetivo

Validar, com o sistema **realmente rodando** (não releitura de código/docs), cada um dos 16
critérios de aceite do `docs/PRD.md` §10 — a entrega final do roadmap. Onde a validação real
revelou um gap, ele foi **corrigido**, não só anotado — seguindo o padrão desta fase
(T-405/T-902/T-903/T-904).

## Metodologia

Ambiente real, com a stack inteira no ar: infra via compose (Postgres ×4, RabbitMQ, OTel
Collector, Jaeger, Loki, Prometheus, Grafana), os 4 serviços de domínio/SAGA + Horus rodando,
imagens Docker dos 7 executáveis (buildadas nesta sessão), um cluster **kind** local, e — pela
primeira vez na história do projeto — o **Load Balancer real** (NGINX) na frente dos serviços
containerizados.

## Resultado por critério

| # | Critério | Evidência | Status |
|---|---|---|---|
| 1 | API Python controla teste de carga via Locust | `pytest` 21/21 verde (T-202/T-203); cenário `ProntuarioUser` já usado ao vivo em T-903 | ✅ |
| 2 | Load Balancer distribui requisições aos serviços Quarkus | **Nunca tinha sido executado de verdade** (LB nunca ativado em compose). Containerizei os 4 serviços de domínio/SAGA + o NGINX real na rede do compose; `POST /prontuarios` e `GET /carteiras/1` via `:8088` roteados corretamente (`upstream` real no access log JSON); `traceparent` preservado ponta a ponta (span raiz no Jaeger com o span-id exato injetado) | ✅ (validado ao vivo pela 1ª vez) |
| 3 | Cada serviço persiste só no seu Postgres | Por construção (datasource fixo por serviço) + confirmado pelo dashboard `postgres.json` (T-904 e sessões anteriores) — 4 bancos isolados, credenciais segregadas (RNF-003) | ✅ |
| 4 | Relatório percorre RabbitMQ → worker Rust | Validado ao vivo em T-405 (`invoice-service` → RabbitMQ → `report-worker`, mesmo `trace_id`) | ✅ |
| 5 | Relatório gerado e enviado por e-mail | Gerado e "enviado" via `LogSender` (stub, sem `SMTP_HOST`); `SmtpSender` real testado unitariamente (T-303, protocolo SMTP mockado) — sem servidor de e-mail real neste ambiente de dev | ✅ (stub; ver nota) |
| 6 | Trace navegável no Jaeger com correlação entre componentes | Extensivamente validado (T-405, este task — trace da SAGA com 3 serviços, 32 spans) | ✅ |
| 7 | Executa em contêineres, compatível com Kubernetes | **Nunca tinha sido aplicado a um cluster real.** Buildei as 4 imagens de domínio faltantes, carreguei as 7 no `kind`, `kubectl apply -k deploy/k8s/` **sem erros** — Deployments/Services/HPAs/Ingress criados. Pods ficam `0/1` só pela ausência do overlay de infra K8s (gap **já documentado**, não é bug do manifesto) — confirmado via log real: `UnknownHostException: postgres-prontuario`, não erro de manifesto | ✅ (manifestos corretos; infra overlay é gap conhecido, fora de escopo) |
| 8 | SAGA com compensações, sem 2PC | Validado ao vivo (ver #16) — `estornar` idempotente, sem coordenador 2PC | ✅ |
| 9 | Request como trace navegável (RF-H-001) | `GET /horus/lifecycle/requests/{traceId}` real: 32 spans, 3 serviços, waterfall correto | ✅ |
| 10 | Query SQL correlacionada, duração e parâmetros sanitizados (RF-H-002) | `GET /horus/lifecycle/queries/{traceId}` real: 20 queries, `statement` parametrizado, banco correto por serviço | ✅ |
| 11 | Logs de erro correlacionados a trace_id/span_id (RF-H-003/004) | `GET /horus/lifecycle/errors/{traceId}`: confirmado que a agregação funciona corretamente para logs ERROR reais correlacionados; a tentativa inicial (falha de negócio 409) não gerou log ERROR na aplicação (gap de completude de log da app, não do agregador — registrado como observação) | ✅ (mecanismo confirmado) |
| 12 | Resumo em NL sob demanda (RF-H-005) | `GET /horus/ai/summary/trace/{id}` real: roteamento de camada correto (FAST/`claude-haiku-4-5`), `live=false` (sem `ANTHROPIC_API_KEY`, comportamento documentado) | ✅ (plumbing; IA real fora de escopo sem a secret) |
| 13 | IA explica trace + causa provável (RF-H-006/007) | `GET /horus/ai/explain/trace/{id}` e `/horus/ai/rca/trace/{id}` reais: roteamento BALANCED/DEEP corretos | ✅ (plumbing; idem #12) |
| 14 | Falha do Horus/IA não afeta domínio (RNF-H-008) | Validado ao vivo em **T-902** (Horus totalmente ausente, domínio funcionando integralmente) | ✅ |
| 15 | Nenhum dado sensível cru em telemetria/IA (RNF-H-002/006) | Validado ao vivo em **T-904** (3 camadas de defesa testadas com sondas reais/sintéticas; 1 gap real encontrado e corrigido) | ✅ |
| 16 | SAGA com compensação **visualizada de ponta a ponta no Horus** (RF-H-016) | **Achado um gap real e corrigido nesta task**: o `saga-orchestrator` nunca emitia os spans `saga.{flow}.{step}` que a visualização do Horus (T-507) espera — só span HTTP/DB automáticos. `GET /horus/lifecycle/saga/{traceId}` sempre retornava vazio para uma SAGA real, apesar de T-507 estar marcada "✅ Entregue" (os testes de T-507 só usavam dados sintéticos). **Corrigido**: `SagaService` agora envolve cada passo com um span próprio (`horus.saga.*`, contrato T-005 §3.4). Validado ao vivo: `outcome: "compensated"`, `flow: "pay-then-invoice"`, 2 passos + 1 compensação, timeline correta | ✅ (corrigido nesta task) |

## Achados corrigidos nesta task

### 1. Load Balancer nunca tinha sido testado com tráfego real (critério #2)

Containerizei os 4 serviços de domínio/SAGA (imagens buildadas nesta sessão) + o NGINX real
(`deploy/lb/nginx.conf`) na rede do compose, roteamento confirmado por `upstream` real no
access log e `traceparent` preservado ponta a ponta. Não exigiu mudança de código — só provou
que o desenho já funciona. Ambiente de teste desmontado ao final.

### 2. `kustomization.yaml` usava `commonLabels` (deprecado)

`kubectl` (1.36, usado no `kind`) avisa que `commonLabels` está deprecado. Trocado por
`labels: - pairs: {...}` (sintaxe atual do Kustomize). **Nota de migração**: em um cluster que
já tenha os Deployments aplicados com `commonLabels`, essa troca muda o `spec.selector`
(imutável) e exige recriar os Deployments — não é um problema para quem nunca aplicou (caso
de todo o histórico do projeto), documentado aqui para quem for migrar um cluster real.

### 3. SAGA orchestrator não emitia os spans que a visualização do Horus espera (critério #16)

O achado mais significativo desta task — ver tabela acima. `services/saga-orchestrator/.../SagaService.java`
agora cria spans `saga.{flow}.{step}` (e `.compensate`) com os atributos `horus.saga.*` do
contrato, usando o `Tracer` OTel injetado via CDI. Testes existentes (`SagaFlowTest`, 2/2)
continuam verdes — o comportamento da SAGA não mudou, só ganhou a instrumentação que faltava.

## Fora de escopo (limitações conhecidas, documentadas)

- **Overlay de infraestrutura K8s** (Postgres/RabbitMQ/Collector dentro do cluster) — sempre
  foi "fora de escopo" desde T-802; confirmado nesta task que os manifestos de aplicação são
  corretos e prontos para quando essa infra existir.
- **E-mail real** (SMTP de verdade) — o worker usa o `LogSender` (stub) neste ambiente de dev;
  `SmtpSender` (T-303) tem cobertura unitária própria, não integração com um servidor real.
- **IA real** (`ANTHROPIC_API_KEY`) — pendência do usuário, já documentada em `state.md` desde
  T-601; sem ela, os agentes respondem com `StubLlmEngine` (comportamento correto e testado).
- **Completude de log de erro na aplicação**: nem toda falha de negócio (ex.: 409 por saldo
  insuficiente) gera um log `ERROR` — é uma escolha de cada serviço, não um defeito do
  agregador do Horus (T-505), que foi confirmado funcionando corretamente para logs que existem.

## Critérios de aceite (desta task)

- [x] Todos os 16 itens do PRD §10 avaliados com evidência real (tabela acima).
- [x] Gaps reais encontrados foram corrigidos (LB nunca testado → testado; K8s nunca aplicado
      → aplicado; SAGA nunca visualizada de verdade → corrigida e visualizada).
- [x] `./mvnw verify` (raiz) verde — 86 testes Java, 5 módulos, `BUILD SUCCESS`.
- [x] `pytest` (loadtest) verde — 21/21.
- [x] `cargo fmt`/`clippy -D warnings`/`test` (worker) verdes — 14/14.
- [x] Ambientes de teste (containers de domínio + LB, recursos do `kind`) desmontados/limpos
      ao final; serviços de dev restaurados para uso contínuo.

## Referências

- [`../../PRD.md`](../../PRD.md) §10 — critérios de aceite (fonte desta task)
- [`./PLAN.md`](./PLAN.md) — plano de execução
- `docs/tasks/T-507/PRD.md` — visualização de SAGA original (span convention nunca implementada pelo orchestrator)
- `docs/telemetry/CONTRACT.md` §3.2/3.4 — convenção `saga.{flow}.{step}` / `horus.saga.*`
