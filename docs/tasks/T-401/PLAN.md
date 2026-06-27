# Plano de Execução — T-401: Instrumentação OTel dos 3 serviços

> ⚠️ **Regra R2 ([WORKFLOW](../../WORKFLOW.md)).** Sempre que **criar ou modificar** um arquivo desta tabela, atualize **na mesma hora** o seu `Status` e `Atualizado`, e registre em *Registro de alterações*.

| Campo | Valor |
|---|---|
| **Task** | `T-401` |
| **Branch** | `task/T-401-otel-services` |
| **PRD** | [`./PRD.md`](./PRD.md) |
| **Progresso** | `8/8` arquivos |

## Arquivos a criar/modificar

| # | Arquivo | Ação | Propósito | Status | Atualizado |
|---|---|---|---|---|---|
| 1 | `services/prontuario/pom.xml` | Modificar | + `quarkus-opentelemetry`, `quarkus-logging-json` | ✅ Concluído | 2026-06-27 |
| 2 | `services/prontuario/.../application.properties` | Modificar | OTel OTLP + JDBC telemetry + logs JSON + `%test` SDK off | ✅ Concluído | 2026-06-27 |
| 3 | `services/payment/pom.xml` | Modificar | idem | ✅ Concluído | 2026-06-27 |
| 4 | `services/payment/.../application.properties` | Modificar | idem | ✅ Concluído | 2026-06-27 |
| 5 | `services/invoice/pom.xml` | Modificar | idem | ✅ Concluído | 2026-06-27 |
| 6 | `services/invoice/.../application.properties` | Modificar | idem | ✅ Concluído | 2026-06-27 |
| 7 | `docs/tasks/T-401/PRD.md` | Criar | PRD desta task | ✅ Concluído | 2026-06-27 |
| 8 | `docs/tasks/T-401/PLAN.md` | Criar | Este plano | ✅ Concluído | 2026-06-27 |

**Legenda:** ⬜ Pendente · 🟡 Em progresso · ✅ Concluído

## Passos de implementação

1. ✅ Branch `task/T-401-otel-services` a partir de `main` (pós-merge de T-104).
2. ✅ Adicionar `quarkus-opentelemetry` + `quarkus-logging-json` aos 3 módulos.
3. ✅ Config OTel por serviço: OTLP por perfil, `service.namespace`/ambiente, JDBC telemetry, logs JSON, SDK off em teste.
4. ✅ Build de produção verde (`-DskipTests`); testes de fluxo (Dev Services) no CI com SDK OTel desligado.
5. ✅ Atualizar `state.md`; abrir o PR [#11](https://github.com/mclovin137/Horus/pull/11); nº preenchido aqui e no PRD.

## Verificação / testes

- [ ] **Build dos 3 módulos verde** (extensões resolvem, augmentation ok) sob JDK 25 + Quarkus 3.37.
- [ ] Testes de fluxo (T-102/103/104) seguem **verdes** no CI com `%test.quarkus.otel.sdk.disabled=true`.
- [x] `service.name` canônico, OTLP por perfil, `db.query.text` parametrizado (JDBC telemetry), logs JSON.

## Registro de alterações

| Data | Arquivo | Mudança |
|---|---|---|
| 2026-06-27 | `services/*/pom.xml` | + `quarkus-opentelemetry`, `quarkus-logging-json`. |
| 2026-06-27 | `services/*/application.properties` | OTel OTLP por perfil, `service.namespace`/ambiente, `quarkus.datasource.jdbc.telemetry`, logs JSON, `%test` SDK off. |
| 2026-06-27 | `docs/tasks/T-401/PRD.md`, `PLAN.md` | PRD e plano de execução. |
