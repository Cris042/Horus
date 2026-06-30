# Plano de Execução — T-606: Anomaly Detector + Error Clusterer

> ⚠️ **Regra R2 ([WORKFLOW](../../WORKFLOW.md)).** Sempre que **criar ou modificar** um arquivo desta tabela, atualize **na mesma hora** o seu `Status` e `Atualizado`, e registre a mudança em *Registro de alterações*. Arquivo não previsto que precise ser tocado deve ser **adicionado** à tabela.

| Campo | Valor |
|---|---|
| **Task** | `T-606` |
| **Branch** | `task/T-606-anomaly-error-clustering` |
| **PRD** | [`./PRD.md`](./PRD.md) |
| **Progresso** | `11/11` arquivos |

## Arquivos a criar/modificar

| # | Arquivo | Ação | Propósito | Status | Atualizado |
|---|---|---|---|---|---|
| 1 | `docs/tasks/T-606/PRD.md` | Criar | PRD da task | ✅ Concluído | 2026-06-30 |
| 2 | `docs/tasks/T-606/PLAN.md` | Criar | Plano arquivo por arquivo | ✅ Concluído | 2026-06-30 |
| 3 | `horus/src/main/java/org/example/horus/ai/anomaly/ErrorClusterModel.java` | Criar | DTOs do clustering de erros | ✅ Concluído | 2026-06-30 |
| 4 | `horus/src/main/java/org/example/horus/ai/anomaly/ErrorClusterer.java` | Criar | Cluster por fingerprint cross-service + rótulo IA | ✅ Concluído | 2026-06-30 |
| 5 | `horus/src/main/java/org/example/horus/ai/anomaly/AnomalyModel.java` | Criar | DTOs de regras/anomalias | ✅ Concluído | 2026-06-30 |
| 6 | `horus/src/main/java/org/example/horus/ai/anomaly/AnomalyDetector.java` | Criar | Avalia regras sobre métricas | ✅ Concluído | 2026-06-30 |
| 7 | `horus/src/main/java/org/example/horus/api/HorusErrorClusterResource.java` | Criar | `GET /horus/ai/errors/clusters/{traceId}` | ✅ Concluído | 2026-06-30 |
| 8 | `horus/src/main/java/org/example/horus/api/HorusAnomalyResource.java` | Criar | `POST /horus/ai/anomalies` | ✅ Concluído | 2026-06-30 |
| 9 | `horus/src/test/java/org/example/horus/ai/anomaly/ErrorClustererTest.java` | Criar | Teste REST do clustering (portas + Stub IA) | ✅ Concluído | 2026-06-30 |
| 10 | `horus/src/test/java/org/example/horus/ai/anomaly/AnomalyDetectorTest.java` | Criar | Teste REST do detector de anomalias | ✅ Concluído | 2026-06-30 |
| 11 | `state.md` | Modificar | Alinhar estado ao progresso de T-606 | ✅ Concluído | 2026-06-30 |

**Legenda:** ⬜ Pendente · 🟡 Em progresso · ✅ Concluído

## Passos de implementação

1. Escrever testes dos dois analisadores.
2. Implementar modelos/serviços/resources.
3. Atualizar este plano e, ao final, o `state.md`; rodar `./mvnw -pl horus test`.

## Verificação / testes

- [x] `./mvnw -pl horus test` verde.

## Registro de alterações

| Data | Arquivo | Mudança |
|---|---|---|
| `2026-06-30` | `docs/tasks/T-606/PRD.md`, `docs/tasks/T-606/PLAN.md` | PRD e plano criados |
| `2026-06-30` | `ai/anomaly/ErrorCluster*`, `api/HorusErrorClusterResource.java` | Error Clusterer (fingerprint cross-service + rótulo IA) |
| `2026-06-30` | `ai/anomaly/Anomaly*`, `api/HorusAnomalyResource.java` | Anomaly Detector baseado em regras sobre métricas |
| `2026-06-30` | `ai/anomaly/ErrorClustererTest.java`, `ai/anomaly/AnomalyDetectorTest.java` | Testes dos dois analisadores |
| `2026-06-30` | `state.md` | Estado do projeto alinhado ao progresso de T-606 |
