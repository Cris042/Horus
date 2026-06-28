# Plano de Execução — T-605: Agente Root-Cause Analyst

> ⚠️ **Regra R2 ([WORKFLOW](../../WORKFLOW.md)).** Atualize `Status`/`Atualizado` e o registro ao criar/modificar cada arquivo.

| Campo | Valor |
|---|---|
| **Task** | `T-605` |
| **Branch** | `task/T-605-rca-agent` |
| **PRD** | [`./PRD.md`](./PRD.md) |
| **Progresso** | `6/6` arquivos |

## Arquivos a criar/modificar

| # | Arquivo | Ação | Propósito | Status | Atualizado |
|---|---|---|---|---|---|
| 1 | `horus/.../ai/context/ContextAssembler.java` | Modificar | `assembleForIncident` (3 sinais) | ✅ Concluído | 2026-06-28 |
| 2 | `horus/.../ai/agent/RootCauseAnalyst.java` | Criar | agente RCA (DEEP tier) | ✅ Concluído | 2026-06-28 |
| 3 | `horus/.../api/HorusRcaResource.java` | Criar | endpoint de RCA | ✅ Concluído | 2026-06-28 |
| 4 | `horus/.../ai/agent/RootCauseAnalystTest.java` | Criar | teste do agente (engine falso) | ✅ Concluído | 2026-06-28 |
| 5 | `horus/.../api/HorusRcaResourceTest.java` | Criar | teste do endpoint (assembler mockado) | ✅ Concluído | 2026-06-28 |
| 6 | `docs/tasks/T-605/{PRD,PLAN}.md` | Criar | docs da task | ✅ Concluído | 2026-06-28 |

**Legenda:** ⬜ Pendente · 🟡 Em progresso · ✅ Concluído

## Passos de implementação

1. `ContextAssembler.assembleForIncident` (trace+logs+métricas).
2. `RootCauseAnalyst`: system prompt de RCA + chamada DEEP.
3. Endpoint `GET /horus/ai/rca/trace/{traceId}`.
4. Testes (agente + endpoint).

## Verificação / testes

- [x] `./mvnw -pl horus test` → **BUILD SUCCESS** (17 testes; 2 novos).

## Registro de alterações

| Data | Arquivo | Mudança |
|---|---|---|
| `2026-06-28` | (todos acima) | Agente Root-Cause Analyst (T-605) + `assembleForIncident` |
