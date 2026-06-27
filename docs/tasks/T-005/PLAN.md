# Plano de Execução — T-005: Convenções de logging/spans (contrato de telemetria)

> ⚠️ **Regra R2 ([WORKFLOW](../../WORKFLOW.md)).** Sempre que **criar ou modificar** um arquivo desta tabela, atualize **na mesma hora** o seu `Status` e `Atualizado`, e registre em *Registro de alterações*.

| Campo | Valor |
|---|---|
| **Task** | `T-005` |
| **Branch** | `task/T-005-telemetry-conventions` |
| **PRD** | [`./PRD.md`](./PRD.md) |
| **Progresso** | `6/6` arquivos |

## Arquivos a criar/modificar

| # | Arquivo | Ação | Propósito | Status | Atualizado |
|---|---|---|---|---|---|
| 1 | `docs/tasks/T-005/PRD.md` | Criar | PRD desta task | ✅ Concluído | 2026-06-27 |
| 2 | `docs/tasks/T-005/PLAN.md` | Criar | Este plano | ✅ Concluído | 2026-06-27 |
| 3 | `docs/telemetry/CONTRACT.md` | Criar | **O contrato de telemetria** (recurso, propagação, spans, logs, métricas, PII) | ✅ Concluído | 2026-06-27 |
| 4 | `README.md` | Modificar | Linkar o contrato na tabela *Documentação* | ✅ Concluído | 2026-06-27 |
| 5 | `CLAUDE.md` | Modificar | Linkar o contrato na lista de docs a ler | ✅ Concluído | 2026-06-27 |
| 6 | `state.md` | Modificar | R1 — registrar T-005 | ✅ Concluído | 2026-06-27 |

**Legenda:** ⬜ Pendente · 🟡 Em progresso · ✅ Concluído

## Passos de implementação

1. ✅ Criar a branch `task/T-005-telemetry-conventions` a partir de `main`.
2. ✅ Ler ADR-0007/0009/0010/0013 e os requisitos (RF-029, RF-H-001..004/016, RNF-H-002) para ancorar o contrato.
3. ✅ Escrever `docs/telemetry/CONTRACT.md`.
4. ✅ Linkar em `README.md` e `CLAUDE.md`.
5. ✅ Criar PRD e este PLAN.
6. ✅ Atualizar `state.md`; abrir o PR [#5](https://github.com/mclovin137/Horus/pull/5); nº preenchido aqui e no PRD.

## Verificação / testes

- [x] O contrato cobre os 6 eixos (recurso, propagação, spans, logs, métricas, PII) e está coerente com os ADRs (sem redefini-los).
- [x] Define `service.name` canônico por componente e `trace_id` como chave de correlação (RF-H-004); exige `traceparent` em HTTP **e** nos headers AMQP (RF-029) e `db.query.text` parametrizado (RNF-H-002).
- [x] Links relativos válidos (ADRs, PRD) a partir de `docs/telemetry/`.
- [ ] CI verde no PR (esta task é só-docs; o build Java permanece intacto).

## Registro de alterações

| Data | Arquivo | Mudança |
|---|---|---|
| 2026-06-27 | `docs/telemetry/CONTRACT.md` | Contrato de telemetria v1.0 (recurso, W3C HTTP+AMQP, spans HTTP/DB/mensageria/SAGA, logging JSON correlacionado, métricas mínimas, PII). |
| 2026-06-27 | `docs/tasks/T-005/PRD.md`, `docs/tasks/T-005/PLAN.md` | PRD e plano de execução. |
| 2026-06-27 | `README.md` | Linha do contrato na tabela *Documentação*. |
| 2026-06-27 | `CLAUDE.md` | Item do contrato na lista de docs a ler antes de implementar. |
| 2026-06-27 | `state.md` | R1 — T-005 (em revisão/PR). |
