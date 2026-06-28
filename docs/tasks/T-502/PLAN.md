# Plano de Execução — T-502: Modelo de correlação por `trace_id`

> ⚠️ **Regra R2 ([WORKFLOW](../../WORKFLOW.md)).** Atualize `Status`/`Atualizado` e o *Registro de alterações* ao criar/modificar cada arquivo.

| Campo | Valor |
|---|---|
| **Task** | `T-502` |
| **Branch** | `task/T-502-correlation-model` |
| **PRD** | [`./PRD.md`](./PRD.md) |
| **Progresso** | `4/4` arquivos |

## Arquivos a criar/modificar

| # | Arquivo | Ação | Propósito | Status | Atualizado |
|---|---|---|---|---|---|
| 1 | `horus/src/main/java/org/example/horus/correlation/CorrelationModel.java` | Criar | `RequestCorrelation` + `ServiceInvolvement` | ✅ Concluído | 2026-06-28 |
| 2 | `horus/src/main/java/org/example/horus/correlation/CorrelationService.java` | Criar | Costura trace+logs → modelo (portas T-501) | ✅ Concluído | 2026-06-28 |
| 3 | `horus/src/main/java/org/example/horus/api/HorusCorrelationResource.java` | Criar | `GET /horus/correlation/trace/{id}` | ✅ Concluído | 2026-06-28 |
| 4 | `horus/src/test/java/org/example/horus/correlation/CorrelationResourceTest.java` | Criar | Testes com portas mockadas | ✅ Concluído | 2026-06-28 |

**Legenda:** ⬜ Pendente · 🟡 Em progresso · ✅ Concluído

## Passos de implementação

1. Modelo (`RequestCorrelation`/`ServiceInvolvement`).
2. `CorrelationService`: agrupa spans por serviço, detecta mensageria/worker, conta erros.
3. REST + testes com `@InjectMock` das portas.

## Verificação / testes

- [x] `mvn -pl horus test` verde (35/35, 3 novos em `CorrelationResourceTest`).

## Registro de alterações

| Data | Arquivo | Mudança |
|---|---|---|
| `2026-06-28` | `correlation/*`, `api/HorusCorrelationResource.java`, teste | Modelo de correlação por `trace_id` criado |
