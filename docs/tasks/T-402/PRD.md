# PRD — T-402: Instrumentação OTel da borda (FastAPI/Locust + Load Balancer)

| Campo | Valor |
|---|---|
| **Task** | `T-402` |
| **Fase do roadmap** | `Fase 4 — Telemetria base (OpenTelemetry)` |
| **Status** | `Em revisão (PR)` |
| **Branch** | `task/T-402-otel-loadtest-edge` |
| **PR** | [#32](https://github.com/mclovin137/Horus/pull/32) |
| **Depende de** | `T-203` (cenários Locust), `T-201` (LB) |
| **Requisitos atendidos** | `RF-029` |
| **ADRs relacionados** | `ADR-0007` (propagação de contexto) |
| **Data** | `2026-06-28` |

## Objetivo

Instrumentar a **borda** do sistema observado com OpenTelemetry (RF-029): a API/cliente de
carga (FastAPI + Locust) passa a **iniciar o contexto de trace** e propagá-lo via **W3C
Trace Context** rumo ao Load Balancer e aos serviços, e o LB registra o `traceparent`. É a
origem da correlação ponta a ponta que o Horus consome (caminho até T-405).

## Escopo (o que entra)

- `app/telemetry.py`: `TracerProvider` com recurso canônico (`service.name=loadtest-api`,
  `service.namespace=medrec` — contrato T-005) + exportador OTLP/HTTP (env padrão do OTel).
- FastAPI instrumentada (`FastAPIInstrumentor`) — habilitada por `HORUS_OTEL_ENABLED=true`.
- Locust: no `events.init`, configura telemetria e instrumenta o cliente `requests` para
  **injetar `traceparent`** em cada chamada (borda do trace).
- LB (`nginx.conf`): access log JSON com `traceparent` (não encerra o contexto — só registra).
- Testes offline (exportador em memória): recurso canônico, span de servidor, injeção W3C.

## Fora do escopo

- Instrumentação do worker Rust e a fronteira HTTP→AMQP → **T-403**.
- Validação de correlação ponta a ponta (mesmo `trace_id`) → **T-405**.
- Métricas/painéis de overhead de instrumentação → **T-903**.

## Premissas e dependências

- LB já repassa `traceparent` sem encerrar (T-201); aqui só se adiciona o log JSON.
- Padrão **desligado** na app FastAPI (testes/dev sem Collector); ligado no ambiente containerizado.
- Locust default **ligado** (é o gerador de carga / borda).

## Critérios de aceite

- [x] Recurso usa `service.name=loadtest-api` / `service.namespace=medrec`.
- [x] App FastAPI instrumentada emite span de servidor (verificado com exporter em memória).
- [x] Propagação injeta `traceparent` (W3C), não B3.
- [x] `pytest` verde; deps OTel no `pyproject` e no job de CI.

## Riscos

| Risco | Mitigação |
|---|---|
| Overhead/instabilidade da auto-instrumentação | Gated por env; medição de overhead fica para T-903. |
| `nginx.conf` não validável localmente (sem nginx) | Sintaxe padrão de `log_format`/`access_log`; validação real no `nginx -t`/T-801. |

## Referências

- [`../../telemetry/CONTRACT.md`](../../telemetry/CONTRACT.md) — §1 recurso, §2 propagação W3C, §5 tabela por componente
- [`../../../deploy/lb/nginx.conf`](../../../deploy/lb/nginx.conf)
- [`./PLAN.md`](./PLAN.md)
