# PRD — T-201: Load Balancer de entrada (NGINX)

| Campo | Valor |
|---|---|
| **Task** | `T-201` |
| **Fase do roadmap** | Fase 2 — Entrada e carga |
| **Status** | `Em revisão (PR)` |
| **Branch** | `task/T-201-load-balancer` |
| **PR** | [#17](https://github.com/mclovin137/Horus/pull/17) |
| **Depende de** | `T-101` |
| **Requisitos atendidos** | RF-005, RNF-006 |
| **ADRs relacionados** | ADR-0007 (OTel) — LB repassa contexto |
| **Data** | 2026-06-27 |

## Objetivo

Definir o **Load Balancer de entrada** (NGINX) que recebe as chamadas e as roteia, por prefixo de caminho, aos serviços de domínio e ao orquestrador de SAGA (RF-005/RNF-006). Crucial para a observabilidade: o LB **repassa** o `traceparent` e **não encerra** o trace (RF-029, contrato T-005 §2/§7), de modo que o `trace_id` começa no cliente e segue intacto pelos serviços.

## Escopo (o que entra)

- **`deploy/lb/nginx.conf`** — roteamento por prefixo (`/prontuarios`,`/consultas`→prontuário; `/carteiras`,`/pagamentos`→payment; `/notas`→invoice; `/sagas`→saga), `traceparent` repassado, `resolver` + variável no `proxy_pass` (resolução em tempo de requisição; resiliente a restart/escala), `/healthz` do LB.
- **`deploy/lb/README.md`** — papel, tabela de roteamento, nota de observabilidade e como será ativado (T-801).

## Fora do escopo

- **Ativar o LB no `docker-compose`** — depende dos serviços **containerizados** (T-801); hoje o compose sobe só infra e os serviços rodam no host.
- **TLS/HTTPS**, rate limiting, autenticação na borda.
- **API FastAPI de controle de carga** (T-202) e geração de carga (T-204) que atravessam o LB.

## Premissas e dependências

- Serviços de domínio (T-101+) com portas conhecidas (8081/8082/8083) e orquestrador (8084).
- Topologia containerizada (nomes de serviço) chega em T-801.

## Critérios de aceite

- [ ] `nginx.conf` roteia os prefixos aos serviços corretos (RF-005).
- [ ] O LB **repassa** `traceparent`/`tracestate` e não encerra o trace (RF-029).
- [ ] Sintaxe validada (`nginx -t` → ok).
- [ ] Documentado; `PLAN.md` 100% ✅ e `state.md` atualizado.

## Riscos

| Risco | Mitigação |
|---|---|
| LB não roda no compose atual (serviços no host) | Entregue como config + doc; ativação em T-801 (esboço de serviço no README). |
| Resolução de nomes derrubar o LB se um serviço cair | `resolver` + variável: resolve por requisição (502 pontual), sem recarregar o NGINX. |

## Referências

- [`../../telemetry/CONTRACT.md`](../../telemetry/CONTRACT.md) (§2/§7 — LB repassa, não encerra) · [`../../PRD.md`](../../PRD.md) (RF-005/RNF-006)
- [`../../../deploy/lb/nginx.conf`](../../../deploy/lb/nginx.conf) · [`./PLAN.md`](./PLAN.md)
