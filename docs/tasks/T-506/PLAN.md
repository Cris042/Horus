# Plano de Execução — T-506: Mapa de serviços/dependências a partir dos traces

> ⚠️ **Regra R2 ([WORKFLOW](../../WORKFLOW.md)).** Sempre que **criar ou modificar** um arquivo desta tabela, atualize **na mesma hora** o seu `Status` e `Atualizado`, e registre a mudança em *Registro de alterações*. Arquivo não previsto que precise ser tocado deve ser **adicionado** à tabela.

| Campo | Valor |
|---|---|
| **Task** | `T-506` |
| **Branch** | `task/T-506-service-map` |
| **PRD** | [`./PRD.md`](./PRD.md) |
| **Progresso** | `7/7` arquivos |

## Arquivos a criar/modificar

| # | Arquivo | Ação | Propósito | Status | Atualizado |
|---|---|---|---|---|---|
| 1 | `docs/tasks/T-506/PRD.md` | Criar | PRD da task | ✅ Concluído | 2026-06-30 |
| 2 | `docs/tasks/T-506/PLAN.md` | Criar | Plano arquivo por arquivo | ✅ Concluído | 2026-06-30 |
| 3 | `horus/src/main/java/org/example/horus/lifecycle/ServiceMapModel.java` | Criar | DTOs REST do mapa de serviços | ✅ Concluído | 2026-06-30 |
| 4 | `horus/src/main/java/org/example/horus/lifecycle/ServiceMapService.java` | Criar | Projetar trace em grafo de serviços/dependências | ✅ Concluído | 2026-06-30 |
| 5 | `horus/src/main/java/org/example/horus/api/HorusServiceMapResource.java` | Criar | `GET /horus/lifecycle/service-map/{traceId}` | ✅ Concluído | 2026-06-30 |
| 6 | `horus/src/test/java/org/example/horus/lifecycle/ServiceMapResourceTest.java` | Criar | Testes REST com `TraceQueryPort` mockado | ✅ Concluído | 2026-06-30 |
| 7 | `state.md` | Modificar | Alinhar estado ao progresso de T-506 | ✅ Concluído | 2026-06-30 |

**Legenda:** ⬜ Pendente · 🟡 Em progresso · ✅ Concluído

## Passos de implementação

1. Escrever testes da API de mapa de serviços.
2. Implementar modelo/serviço/resource da T-506.
3. Atualizar este plano e, ao final da entrega, o `state.md`.
4. Rodar `./mvnw -pl horus test`.

## Verificação / testes

- [x] `./mvnw -pl horus test` verde.

## Registro de alterações

| Data | Arquivo | Mudança |
|---|---|---|
| `2026-06-30` | `docs/tasks/T-506/PRD.md`, `docs/tasks/T-506/PLAN.md` | PRD e plano de execução criados |
| `2026-06-30` | `ServiceMapModel.java`, `ServiceMapService.java`, `api/HorusServiceMapResource.java` | Implementação do mapa de serviços/dependências |
| `2026-06-30` | `ServiceMapResourceTest.java` | Testes do contrato REST do mapa de serviços |
| `2026-06-30` | `state.md` | Estado do projeto alinhado ao progresso de T-506 |
