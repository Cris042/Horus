# Plano de Execução — T-406: Sanitização/redação de PII na borda do Collector

> ⚠️ **Regra R2 ([WORKFLOW](../../WORKFLOW.md)).** Sempre que **criar ou modificar** um arquivo desta tabela, atualize **na mesma hora** o seu `Status` e `Atualizado`, e registre a mudança em *Registro de alterações*.

| Campo | Valor |
|---|---|
| **Task** | `T-406` |
| **Branch** | `task/T-406-pii-redaction` |
| **PRD** | [`./PRD.md`](./PRD.md) |
| **Progresso** | `4/4` arquivos |

## Arquivos a criar/modificar

| # | Arquivo | Ação | Propósito | Status | Atualizado |
|---|---|---|---|---|---|
| 1 | `deploy/telemetry/otel-collector-config.yaml` | Modificar | Processor `transform/pii` + wiring em traces/logs | ✅ Concluído | 2026-06-27 |
| 2 | `deploy/telemetry/PII-REDACTION.md` | Criar | Doc de regras de redação e verificação | ✅ Concluído | 2026-06-27 |
| 3 | `docs/tasks/T-406/PRD.md` | Criar | PRD da task | ✅ Concluído | 2026-06-27 |
| 4 | `docs/tasks/T-406/PLAN.md` | Criar | Este plano | ✅ Concluído | 2026-06-27 |

**Legenda:** ⬜ Pendente · 🟡 Em progresso · ✅ Concluído

## Passos de implementação

1. Adicionar `transform/pii` (OTTL) com `delete_key` das chaves proibidas e `replace_all_patterns`/`replace_pattern` para e-mail/CPF/cartão.
2. Inserir `transform/pii` nas pipelines de traces e logs (após `memory_limiter`, antes de `batch`).
3. Validar com `otelcol validate`.
4. Documentar em `PII-REDACTION.md`.

## Verificação / testes

- [x] `otel/opentelemetry-collector-contrib:0.118.0 validate` → exit 0.
- [x] `docker compose -f deploy/docker-compose.yml config` continua OK.

## Registro de alterações

| Data | Arquivo | Mudança |
|---|---|---|
| `2026-06-27` | `otel-collector-config.yaml` | Processor `transform/pii` + wiring traces/logs |
| `2026-06-27` | `PII-REDACTION.md` | Doc de regras e verificação |
| `2026-06-27` | `T-406/PRD.md`, `T-406/PLAN.md` | Docs da task |
