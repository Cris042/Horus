# Plano de Execução — T-106: Isolamento (credenciais/schema separados)

> ⚠️ **Regra R2 ([WORKFLOW](../../WORKFLOW.md)).** Sempre que **criar ou modificar** um arquivo desta tabela, atualize **na mesma hora** o seu `Status` e `Atualizado`, e registre em *Registro de alterações*.

| Campo | Valor |
|---|---|
| **Task** | `T-106` |
| **Branch** | `task/T-106-db-isolation` |
| **PRD** | [`./PRD.md`](./PRD.md) |
| **Progresso** | `7/7` arquivos |

## Arquivos a criar/modificar

| # | Arquivo | Ação | Propósito | Status | Atualizado |
|---|---|---|---|---|---|
| 1 | `deploy/docker-compose.yml` | Modificar | Credenciais próprias por `postgres-*`; healthcheck por `$POSTGRES_USER` | ✅ Concluído | 2026-06-27 |
| 2 | `services/prontuario/.../application.properties` | Modificar | Datasource user/pass = `prontuario_svc` | ✅ Concluído | 2026-06-27 |
| 3 | `services/payment/.../application.properties` | Modificar | `payment_svc` | ✅ Concluído | 2026-06-27 |
| 4 | `services/invoice/.../application.properties` | Modificar | `invoice_svc` | ✅ Concluído | 2026-06-27 |
| 5 | `services/saga-orchestrator/.../application.properties` | Modificar | `saga_svc` | ✅ Concluído | 2026-06-27 |
| 6 | `docs/architecture/db-isolation.md` | Criar | Estratégia de isolamento (instância + credenciais) | ✅ Concluído | 2026-06-27 |
| 7 | `docs/tasks/T-106/PRD.md`, `PLAN.md` | Criar | PRD e plano | ✅ Concluído | 2026-06-27 |

**Legenda:** ⬜ Pendente · 🟡 Em progresso · ✅ Concluído

## Passos de implementação

1. ✅ Branch `task/T-106-db-isolation` a partir de `main` (pós-merge de T-107).
2. ✅ Credenciais próprias por `postgres-*` no compose; healthcheck por `$POSTGRES_USER`.
3. ✅ Datasource user/pass por serviço (4 serviços).
4. ✅ Documentar em `docs/architecture/db-isolation.md`.
5. ✅ Validação: `compose config` ok; build de produção dos 4 módulos verde.
6. ✅ Atualizar `state.md`; abrir o PR [#14](https://github.com/mclovin137/Horus/pull/14); nº preenchido aqui e no PRD.

## Verificação / testes

- [x] `compose config` válido; **build de produção** dos 4 módulos verde.
- [ ] Testes de fluxo seguem verdes no **CI** (Dev Services provisiona a credencial configurada por serviço).
- [x] Cada serviço usa credenciais exclusivas do seu banco; isolamento documentado.

## Registro de alterações

| Data | Arquivo | Mudança |
|---|---|---|
| 2026-06-27 | `deploy/docker-compose.yml` | Credenciais por `postgres-*`; healthcheck por `$POSTGRES_USER`. |
| 2026-06-27 | `services/*/application.properties` | Datasource user/pass segregados por serviço. |
| 2026-06-27 | `docs/architecture/db-isolation.md` | Estratégia de isolamento (RNF-002/003). |
| 2026-06-27 | `docs/tasks/T-106/PRD.md`, `PLAN.md` | PRD e plano de execução. |
