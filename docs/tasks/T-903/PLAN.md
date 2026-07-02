# Plano de Execução — T-903: Orçamento de overhead de instrumentação medido sob carga

> ⚠️ **Regra R2 ([WORKFLOW](../../WORKFLOW.md)).** Sempre que **criar ou modificar** um arquivo desta tabela, atualize **na mesma hora** o seu `Status` e `Atualizado`, e registre a mudança em *Registro de alterações*. Arquivo não previsto que precise ser tocado deve ser **adicionado** à tabela.

| Campo | Valor |
|---|---|
| **Task** | `T-903` |
| **Branch** | `task/T-903-instrumentation-overhead` |
| **PRD** | [`./PRD.md`](./PRD.md) |
| **Progresso** | `2/2` arquivos |

## Arquivos a criar/modificar

| # | Arquivo | Ação | Propósito | Status | Atualizado |
|---|---|---|---|---|---|
| 1 | `docs/tasks/T-903/PRD.md` | Criar | Orçamento definido + metodologia + resultados medidos | ✅ Concluído | 2026-07-02 |
| 2 | `docs/tasks/T-903/PLAN.md` | Criar | Plano de execução desta task | ✅ Concluído | 2026-07-02 |

> Task de **medição/verificação** (RNF-H-001/007) — não altera código nem manifests; sem
> mudanças fora de `docs/tasks/T-903/`. `state.md` atualizado ao final.

## Passos de implementação

1. Subir infra real (`postgres-prontuario`, `otel-collector`, `jaeger` via compose).
2. Build de `prontuario-service` (inalterado).
3. Rodar `ProntuarioUser` (Locust, T-203, sem modificação) direto contra a porta do serviço
   (`http://localhost:8081`) — paths batem 1:1 com as rotas, dispensa o LB (não ativo em
   compose).
4. Warmup descartado (15s) + 2 repetições medidas (30-45s) com o serviço em
   `-Dquarkus.otel.sdk.disabled=false` (default).
5. Repetir passo 4 com `-Dquarkus.otel.sdk.disabled=true`.
6. Comparar `avg`/`p95`/`p99`/RPS/falhas entre as duas condições; checar orçamento definido.
7. Checar logs do `otel-collector` (erros/descartes) e a API do Jaeger (volume de traces
   armazenado) para RNF-H-007.
8. Desmontar o ambiente.

## Verificação / testes

- [x] 4 execuções do Locust (`ProntuarioUser`, 20 VUs, 30-45s cada) — 2 com OTel ligado, 2 com
      OTel desligado; 0 falhas em todas.
- [x] Overhead ON vs. OFF dentro do orçamento definido (≤10% relativo / ≤10ms absoluto em p95)
      — diferença observada dentro do ruído de medição.
- [x] `otel-collector`: sem erros/descartes nos logs durante as execuções com OTel ligado.
- [x] API do Jaeger: centenas de traces com spans completos armazenados após os testes.

## Registro de alterações

| Data | Arquivo | Mudança |
|---|---|---|
| `2026-07-02` | `docs/tasks/T-903/*` | PRD (orçamento, metodologia, resultados) e plano criados |
