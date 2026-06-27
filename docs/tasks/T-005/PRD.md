# PRD — T-005: Convenções de logging/spans (contrato de telemetria)

| Campo | Valor |
|---|---|
| **Task** | `T-005` |
| **Fase do roadmap** | Fase 0 — Fundação |
| **Status** | `Em progresso` |
| **Branch** | `task/T-005-telemetry-conventions` |
| **PR** | `#<n>` (preencher ao abrir) |
| **Depende de** | `T-001` |
| **Requisitos atendidos** | RF-029, RF-H-004 (+ guia RF-030, RF-H-001/002/003/016, RNF-008, RNF-H-002) |
| **ADRs relacionados** | ADR-0007, ADR-0009, ADR-0010, ADR-0013 |
| **Data** | 2026-06-27 |

## Objetivo

Definir o **contrato de telemetria** do projeto: as **convenções de logging estruturado** e de **nomes/atributos de span** que **todo** componente (Python, Load Balancer, Quarkus, Rust, Horus) deve seguir para que a telemetria seja **correlacionável ponta a ponta** por `trace_id` e **livre de PII crua**. É uma task pequena de **alta alavancagem**: guia toda a instrumentação OTel das fases seguintes (T-401/T-402/T-403/T-405/T-406) e o modelo de correlação do Horus — sem ele, cada componente inventaria nomes/campos divergentes e a correlação quebraria.

## Escopo (o que entra)

- **`docs/telemetry/CONTRACT.md`** — o contrato, derivado dos ADRs 0007/0009/0010/0013 e ancorado nas *semantic conventions* do OTel. Cobre:
  - atributos de **recurso** (`service.name` canônico por componente, namespace, versão, ambiente);
  - **propagação** W3C `traceparent`/`tracestate` em **HTTP e nos headers do RabbitMQ** (RF-029);
  - **spans**: nomes de baixa cardinalidade e atributos por tipo (HTTP, **query SQL** parametrizada, mensageria, **SAGA/compensação**);
  - **logging JSON** correlacionado (`trace_id`/`span_id`, campos de erro);
  - **métricas** mínimas (RED + query);
  - **sanitização/PII** (normativo) e aplicação por componente.
- **Indexação:** linkar o contrato em `README.md` (tabela *Documentação*) e em `CLAUDE.md` (lista de docs).

## Fora do escopo

- **Instrumentação real** de qualquer componente → T-401 (Quarkus HTTP/JDBC), T-402 (FastAPI/LB), T-403 (Rust/AMQP).
- **Config do Collector** e redação na borda → T-404 / T-406.
- Mudanças de código no módulo `horus` (ex.: ligar `quarkus.log.console.json`) → quando a instrumentação entrar (T-401/Fase 5).
- Fixar a **versão** exata do semconv do OTel (vive em `lib.md`/implementação).

## Premissas e dependências

- T-001 entregue (estrutura `docs/`).
- ADR-0007/0009/0010/0013 aceitos — o contrato os **operacionaliza**, não os redefine.

## Critérios de aceite

- [ ] Existe `docs/telemetry/CONTRACT.md` cobrindo recurso, propagação (HTTP+AMQP), spans (HTTP/DB/mensageria/SAGA), logging JSON correlacionado, métricas mínimas e PII.
- [ ] Define **`service.name` canônico** por componente e o uso de **`trace_id`** como chave de correlação (RF-H-004).
- [ ] Exige `traceparent` em HTTP **e nos headers do RabbitMQ** (RF-029) e `db.query.text` **parametrizado** (RF-H-002/RNF-H-002).
- [ ] Linkado em `README.md` e `CLAUDE.md`.
- [ ] `PLAN.md` 100% ✅ e `state.md` atualizado.

## Riscos

| Risco | Mitigação |
|---|---|
| Grafias do semconv do OTel mudam entre versões | O contrato marca a **intenção** como normativa e manda **confirmar a versão fixada na implementação** (mesmo padrão do PRD para a API Claude). |
| Contrato divergir da prática ao instrumentar | É **vivo e versionado** (§8); mudanças de campo obrigatório são ADR-worthy. |

## Referências

- [`../../PRD.md`](../../PRD.md), [`../../ROADMAP.md`](../../ROADMAP.md)
- [`../../telemetry/CONTRACT.md`](../../telemetry/CONTRACT.md) — o entregável
- ADR-0007, ADR-0009, ADR-0010, ADR-0013
- [`./PLAN.md`](./PLAN.md)
