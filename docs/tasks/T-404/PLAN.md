# Plano de Execução — T-404: OTel Collector + backends (Jaeger / Loki / Prometheus)

> ⚠️ **Regra R2 ([WORKFLOW](../../WORKFLOW.md)).** Sempre que **criar ou modificar** um arquivo desta tabela, atualize **na mesma hora** o seu `Status` e `Atualizado`, e registre a mudança em *Registro de alterações*. Arquivo não previsto que precise ser tocado deve ser **adicionado** à tabela.

| Campo | Valor |
|---|---|
| **Task** | `T-404` |
| **Branch** | `task/T-404-otel-collector-backends` |
| **PRD** | [`./PRD.md`](./PRD.md) |
| **Progresso** | `6/6` arquivos |

## Arquivos a criar/modificar

| # | Arquivo | Ação | Propósito | Status | Atualizado |
|---|---|---|---|---|---|
| 1 | `deploy/telemetry/otel-collector-config.yaml` | Modificar | Extensão `health_check` (`:13133`) no Collector | ✅ Concluído | 2026-06-27 |
| 2 | `deploy/docker-compose.yml` | Modificar | `healthcheck` p/ Collector/Jaeger/Loki/Prometheus + serviço Grafana | ✅ Concluído | 2026-06-27 |
| 3 | `deploy/telemetry/grafana/datasources.yaml` | Criar | Datasources provisionados (Prometheus, Loki, Jaeger) | ✅ Concluído | 2026-06-27 |
| 4 | `deploy/telemetry/README.md` | Criar | Doc de validação do pipeline (portas, fluxo, roteiro) | ✅ Concluído | 2026-06-27 |
| 5 | `docs/tasks/T-404/PRD.md` | Criar | PRD da task | ✅ Concluído | 2026-06-27 |
| 6 | `docs/tasks/T-404/PLAN.md` | Criar | Este plano | ✅ Concluído | 2026-06-27 |

**Legenda:** ⬜ Pendente · 🟡 Em progresso · ✅ Concluído

## Passos de implementação

1. Adicionar extensão `health_check` ao Collector e referenciá-la em `service.extensions`.
2. Adicionar `healthcheck` aos 4 componentes de telemetria no compose.
3. Adicionar serviço Grafana (porta 3000) com provisioning de datasources via volume read-only.
4. Escrever `deploy/telemetry/README.md` com portas, fluxo OTLP→backends e roteiro de verificação ponta a ponta.
5. Validar com `docker compose config`.

## Verificação / testes

- [x] `docker compose -f deploy/docker-compose.yml config` valida sem erro.
- [x] Grafana com datasources Prometheus/Loki/Jaeger provisionados.
- [x] Collector com `health_check` em `:13133`.

## Registro de alterações

| Data | Arquivo | Mudança |
|---|---|---|
| `2026-06-27` | `otel-collector-config.yaml` | Extensão `health_check` (`:13133`) |
| `2026-06-27` | `docker-compose.yml` | Healthchecks + serviço Grafana |
| `2026-06-27` | `grafana/datasources.yaml` | Datasources provisionados |
| `2026-06-27` | `README.md` | Doc de validação do pipeline |
| `2026-06-27` | `T-404/PRD.md`, `T-404/PLAN.md` | Docs da task |
