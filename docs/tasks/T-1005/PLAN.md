# Plano de Execução — T-1005: Retenção configurável por tipo de sinal

> ⚠️ **Regra R2 ([WORKFLOW](../../WORKFLOW.md)).** Sempre que **criar ou modificar** um arquivo desta tabela, atualize **na mesma hora** o seu `Status` e `Atualizado`, e registre a mudança em *Registro de alterações*. Arquivo não previsto que precise ser tocado deve ser **adicionado** à tabela.

| Campo | Valor |
|---|---|
| **Task** | `T-1005` |
| **Branch** | `claude/beautiful-cray-sfblsr` |
| **PRD** | [`./PRD.md`](./PRD.md) |
| **Progresso** | `7/7` arquivos |

## Arquivos a criar/modificar

| # | Arquivo | Ação | Propósito | Status | Atualizado |
|---|---|---|---|---|---|
| 1 | `deploy/telemetry/jaeger-config.yaml` | Criar | Jaeger v2: Badger + TTL por env | ✅ Concluído | 2026-09-28 |
| 2 | `deploy/telemetry/loki-config.yaml` | Modificar | Compactor + retention_period por env | ✅ Concluído | 2026-09-28 |
| 3 | `deploy/docker-compose.yml` | Modificar | Jaeger com config/volume/init; flags de retenção de Loki e Prometheus | ✅ Concluído | 2026-09-28 |
| 4 | `deploy/telemetry/README.md` | Modificar | Seção de retenção | ✅ Concluído | 2026-09-28 |
| 5 | `docs/tasks/T-1005/PRD.md` | Criar | PRD | ✅ Concluído | 2026-09-28 |
| 6 | `docs/tasks/T-1005/PLAN.md` | Criar | Este plano | ✅ Concluído | 2026-09-28 |
| 7 | `state.md` | Modificar | R1 | ✅ Concluído | 2026-09-28 |

**Legenda:** ⬜ Pendente · 🟡 Em progresso · ✅ Concluído

## Verificação / testes

- [x] Jaeger 2.2.0 com a config nova: sobe, recebe OTLP, trace persiste após restart.
- [x] `LOKI_RETENTION=72h PROMETHEUS_RETENTION=3d JAEGER_RETENTION=24h docker compose up --wait loki prometheus jaeger`
  → Loki `retention_period: 3d`, Prometheus `3d`, Jaeger pronto.

## Registro de alterações

| Data | Arquivo | Mudança |
|---|---|---|
| `2026-09-28` | (todos acima) | Retenção configurável (T-1005) |
