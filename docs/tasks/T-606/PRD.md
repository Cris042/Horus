# PRD — T-606: Anomaly Detector + Error Clusterer

| Campo | Valor |
|---|---|
| **Task** | `T-606` |
| **Fase** | Fase 6 — Camada de IA (Claude) |
| **Requisitos** | RF-H-008 (detecção de anomalias), RF-H-009 (clustering de erros) |
| **Dependências** | T-505 (agregação de logs de erro), T-501 (camada de consulta), T-601/T-602 (IA) |
| **Branch** | `task/T-606-anomaly-error-clustering` |

## Objetivo

Dois analisadores que elevam os sinais brutos a insumos acionáveis:

- **Error Clusterer (RF-H-009):** agrupa logs de erro por **fingerprint normalizado
  atravessando serviços** — cada cluster reúne a mesma assinatura de erro vista em um
  ou mais serviços, com contagem total, serviços afetados, severidade e amostra, e
  recebe um **rótulo em linguagem natural gerado por IA** (camada FAST). Complementa a
  agregação por serviço da T-505 com uma visão cross-service.
- **Anomaly Detector (RF-H-008):** avalia **regras sobre métricas** (PromQL via
  `MetricQueryPort`) — cada regra compara o valor de cada série a um limiar
  (`GT`/`LT`) e emite uma anomalia (rótulos, valor, limiar, severidade).

Ambos são primeira fatia: determinísticos e testáveis com portas mockadas e o
`StubLlmEngine` (sem `ANTHROPIC_API_KEY`).

## Escopo

- `ai/anomaly/ErrorClusterModel` + `ErrorClusterer` — clusteriza por fingerprint
  (reutiliza `LogQueryPort.findByTraceId`, como a T-505) e gera rótulo via `LlmEngine`.
- `ai/anomaly/AnomalyModel` + `AnomalyDetector` — avalia `List<AnomalyRule>` via
  `MetricQueryPort.instantQuery`.
- `api/HorusErrorClusterResource` — `GET /horus/ai/errors/clusters/{traceId}`.
- `api/HorusAnomalyResource` — `POST /horus/ai/anomalies` (corpo: lista de regras).
- Testes `@QuarkusTest` para os dois, com portas mockadas e Stub de IA.

## Fora de escopo

- Janela temporal de logs/métricas além do escopo por `traceId` / regra fornecida —
  exigiria novos métodos de porta (Loki range / Prometheus range); fica para fatia seguinte.
- Baseline estatístico / detecção não-supervisionada de anomalias — esta fatia é
  baseada em regras (limiar). ML/baseline fica para evolução.
- Alertas (e-mail/webhook) — T-703.

## Critérios de aceitação

1. `GET /horus/ai/errors/clusters/{traceId}` agrupa erros por fingerprint cross-service
   (contagem, serviços afetados, `crossService`), ordenados por contagem, com rótulo de IA.
2. `POST /horus/ai/anomalies` avalia as regras e retorna as séries que violam o limiar.
3. 404 para trace inexistente; 400 para `traceId` malformado (clusters); validação de regras.
4. Funciona com `StubLlmEngine` (sem chave) — `live=false`, rótulo placeholder.
5. `./mvnw -pl horus test` verde.
