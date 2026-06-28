# PRD — T-302: Publicação de solicitação de relatório pelos serviços

| Campo | Valor |
|---|---|
| **Task** | `T-302` |
| **Fase do roadmap** | Fase 3 — Mensageria e worker |
| **Status** | `Entregue` |
| **Branch** | `task/T-302-saga-report-publication` |
| **PR** | [#16](https://github.com/mclovin137/Horus/pull/16) |
| **Depende de** | `T-301` |
| **Requisitos atendidos** | RF-021 |
| **ADRs relacionados** | ADR-0004 (RabbitMQ restrito a relatórios), ADR-0013 (SAGA), ADR-0009 (tracing) |
| **Data** | 2026-06-27 |

## Objetivo

Integrar a **publicação de solicitação de relatório** a um **fluxo real**: ao concluir a SAGA `pagar→emitir NF` (T-107), o orquestrador dispara o pedido de relatório da NF emitida (RF-021). O fluxo passa a ser `pagar → emitir → solicitar relatório`, todo correlacionado pelo mesmo `trace_id` — material rico para o Horus observar.

## Escopo (o que entra)

- **`InvoiceClient.solicitarRelatorio(notaId)`** (REST Client do orquestrador) → `POST /notas/{id}/relatorio` (mecanismo de T-301).
- **`SagaService`**: após `CONCLUIDA`, chama `solicitarRelatorio(notaId)` como **passo final best-effort** — falha **não** compensa a SAGA já concluída (apenas é registrada).
- **Teste**: caminho feliz verifica que o relatório é solicitado; caminho de compensação verifica que **não** é.
- **Doc**: atualiza `docs/architecture/report-message.md` (produtores por fluxo).

## Fora do escopo

- **Auto-publicação em toda emissão** do invoice (mantém-se sob demanda/por fluxo).
- **Worker Rust** consumindo o pedido → T-303.
- **Validação HTTP→AMQP ponta a ponta** → T-403.

## Premissas e dependências

- T-301 (contrato + publisher + endpoint no invoice) entregue.
- SAGA T-107 (orquestrador) entregue; usa o `InvoiceClient` existente.
- Testes com clients mockados (sem HTTP/RabbitMQ real) — CI-safe.

## Critérios de aceite

- [ ] Ao concluir a SAGA, o orquestrador solicita o relatório da NF (RF-021).
- [ ] Falha na solicitação **não** compensa a SAGA concluída.
- [ ] Sem emissão (compensação), nenhum relatório é solicitado.
- [ ] Build/test verde; `PLAN.md` 100% ✅ e `state.md` atualizado.

## Riscos

| Risco | Mitigação |
|---|---|
| Pedido de relatório falhar e afetar a SAGA | Chamado em `try/catch`, best-effort; não altera o estado `CONCLUIDA`. |
| Acoplar emissão a relatório | É disparo **de fluxo** (orquestrador), não no domínio do invoice; opcional e isolado. |

## Referências

- [`../../architecture/report-message.md`](../../architecture/report-message.md) · ADR-0004/0013 · [`../../PRD.md`](../../PRD.md) (RF-021)
- [`./PLAN.md`](./PLAN.md)
