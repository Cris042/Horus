# Plano de Execução — T-003: `docker-compose` de desenvolvimento

> ⚠️ **Regra R2 ([WORKFLOW](../../WORKFLOW.md)).** Sempre que **criar ou modificar** um arquivo desta tabela, atualize **na mesma hora** o seu `Status` e `Atualizado`, e registre em *Registro de alterações*.

| Campo | Valor |
|---|---|
| **Task** | `T-003` |
| **Branch** | `task/T-003-docker-compose-dev` |
| **PRD** | [`./PRD.md`](./PRD.md) |
| **Progresso** | `8/8` arquivos |

## Arquivos a criar/modificar

| # | Arquivo | Ação | Propósito | Status | Atualizado |
|---|---|---|---|---|---|
| 1 | `docs/tasks/T-003/PRD.md` | Criar | PRD desta task | ✅ Concluído | 2026-06-27 |
| 2 | `docs/tasks/T-003/PLAN.md` | Criar | Este plano | ✅ Concluído | 2026-06-27 |
| 3 | `deploy/docker-compose.yml` | Criar | Infra dev: Postgres ×3, RabbitMQ, Collector, Jaeger, Loki, Prometheus | ✅ Concluído | 2026-06-27 |
| 4 | `deploy/telemetry/otel-collector-config.yaml` | Criar | Pipeline OTLP→Jaeger/Loki/Prometheus | ✅ Concluído | 2026-06-27 |
| 5 | `deploy/telemetry/prometheus.yml` | Criar | Config mínima do Prometheus (remote_write receiver) | ✅ Concluído | 2026-06-27 |
| 6 | `deploy/telemetry/loki-config.yaml` | Criar | Config mínima do Loki (single-binary, OTLP) | ✅ Concluído | 2026-06-27 |
| 7 | `Makefile` | Modificar | `up`/`down`/`ps`/`logs`/`restart`/`clean` reais | ✅ Concluído | 2026-06-27 |
| 8 | `deploy/README.md` | Modificar | Tabela de serviços/portas + fluxo de telemetria | ✅ Concluído | 2026-06-27 |

**Legenda:** ⬜ Pendente · 🟡 Em progresso · ✅ Concluído

## Passos de implementação

1. ✅ Criar a branch `task/T-003-docker-compose-dev` a partir de `main`.
2. ✅ Escrever `deploy/docker-compose.yml` (8 serviços, rede, volumes, healthchecks) alinhado ao contrato de telemetria (nomes de banco, Collector único).
3. ✅ Escrever as configs em `deploy/telemetry/` (Collector, Prometheus, Loki).
4. ✅ Ligar `Makefile` (`up`/`down`/`ps`/`logs`/`restart`/`clean`) ao compose.
5. ✅ Atualizar `deploy/README.md`.
6. ✅ Validar `docker compose ... config -q` (sem erro).
7. ⬜ Atualizar `state.md`; abrir o PR `T-003: …`; preencher o nº do PR aqui e no PRD.

## Verificação / testes

- [x] `docker compose -f deploy/docker-compose.yml config -q` → sem erro (validado).
- [ ] `make up` sobe os 8 contêineres; bancos/RabbitMQ *healthy* (requer Docker no host — validação manual do revisor).
- [ ] UIs acessíveis (Jaeger/RabbitMQ/Prometheus); Collector aceita OTLP.
- [x] É só-infra (sem código de app); o build Java/CI permanece intacto.

## Registro de alterações

| Data | Arquivo | Mudança |
|---|---|---|
| 2026-06-27 | `deploy/docker-compose.yml` | Compose dev: Postgres ×3, RabbitMQ 4.0, OTel Collector 0.118, Jaeger 2, Loki 3.4, Prometheus 3.2. |
| 2026-06-27 | `deploy/telemetry/*.yaml` | Configs do Collector (OTLP→3 backends), Prometheus (remote_write) e Loki (single-binary). |
| 2026-06-27 | `Makefile` | Alvos reais de ciclo de vida do compose. |
| 2026-06-27 | `deploy/README.md` | Tabela de serviços/portas e fluxo de telemetria. |
| 2026-06-27 | `docs/tasks/T-003/PRD.md`, `PLAN.md` | PRD e plano de execução. |
