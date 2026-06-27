# Plano de Execução — T-105: Migrações Flyway independentes por banco

> ⚠️ **Regra R2 ([WORKFLOW](../../WORKFLOW.md)).** Sempre que **criar ou modificar** um arquivo desta tabela, atualize **na mesma hora** o seu `Status` e `Atualizado`, e registre em *Registro de alterações*.

| Campo | Valor |
|---|---|
| **Task** | `T-105` |
| **Branch** | `task/T-105-flyway-migrations` |
| **PRD** | [`./PRD.md`](./PRD.md) |
| **Progresso** | `6/6` arquivos |

## Arquivos a criar/modificar

| # | Arquivo | Ação | Propósito | Status | Atualizado |
|---|---|---|---|---|---|
| 1 | `services/prontuario/.../application.properties` | Modificar | Flyway `validate-on-migrate` + `clean-disabled` | ✅ Concluído | 2026-06-27 |
| 2 | `services/payment/.../application.properties` | Modificar | idem | ✅ Concluído | 2026-06-27 |
| 3 | `services/invoice/.../application.properties` | Modificar | idem | ✅ Concluído | 2026-06-27 |
| 4 | `docs/architecture/db-migrations.md` | Criar | Estratégia de migrações (princípio, layout, convenções, config) | ✅ Concluído | 2026-06-27 |
| 5 | `docs/tasks/T-105/PRD.md` | Criar | PRD desta task | ✅ Concluído | 2026-06-27 |
| 6 | `docs/tasks/T-105/PLAN.md` | Criar | Este plano | ✅ Concluído | 2026-06-27 |

**Legenda:** ⬜ Pendente · 🟡 Em progresso · ✅ Concluído

## Passos de implementação

1. ✅ Branch `task/T-105-flyway-migrations` a partir de `main` (pós-merge de T-401).
2. ✅ Hardening do Flyway nos 3 serviços (`validate-on-migrate`, `clean-disabled`).
3. ✅ Documentar a estratégia em `docs/architecture/db-migrations.md`.
4. ✅ Build de produção verde (`-DskipTests`); testes de fluxo (que exercem migrações) no CI.
5. ✅ Atualizar `state.md`; abrir o PR [#12](https://github.com/mclovin137/Horus/pull/12); nº preenchido aqui e no PRD.

## Verificação / testes

- [ ] **Build dos 3 módulos verde** sob JDK 25 + Quarkus 3.37.
- [ ] Testes de fluxo (T-102/103/104) seguem verdes no CI — as migrações `V*` são aplicadas e validadas por banco.
- [x] Estratégia (banco por serviço, Flyway dono do schema, id por sequência) documentada.

## Registro de alterações

| Data | Arquivo | Mudança |
|---|---|---|
| 2026-06-27 | `services/*/application.properties` | Flyway `validate-on-migrate=true` + `clean-disabled=true`. |
| 2026-06-27 | `docs/architecture/db-migrations.md` | Estratégia de migrações por banco (RF-028/RNF-015). |
| 2026-06-27 | `docs/tasks/T-105/PRD.md`, `PLAN.md` | PRD e plano de execução. |
