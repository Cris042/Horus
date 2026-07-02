# PRD — T-903: Orçamento de overhead de instrumentação medido sob carga (Locust)

| Campo | Valor |
|---|---|
| **Task** | `T-903` |
| **Fase do roadmap** | Fase 9 — Endurecimento e aceite |
| **Status** | `Entregue` |
| **Branch** | `task/T-903-instrumentation-overhead` |
| **Depende de** | `T-405`, `T-803` |
| **Requisitos atendidos** | `RNF-H-001`, `RNF-H-007` |
| **Data** | 2026-07-02 |

## Objetivo

**Definir** um orçamento de overhead de instrumentação (RNF-H-001: "a instrumentação não pode
degradar significativamente a latência dos serviços observados") e **medi-lo** de fato, sob
carga gerada pelo Locust — comparando o mesmo serviço com o SDK OTel ligado vs. desligado.
Confirmar também, en passant, que o pipeline de ingestão (Collector→Jaeger) absorve o volume de
spans gerado sem erros/descartes (RNF-H-007).

## Orçamento definido

| Métrica | Orçamento |
|---|---|
| Overhead de latência **p95** | ≤ 10% relativo **ou** ≤ 10ms absoluto (o que for maior) vs. baseline sem instrumentação |
| Taxa de erro | Instrumentação não pode introduzir falhas (`failures/s` deve permanecer 0) |
| Ingestão (RNF-H-007) | Collector não descarta/erra sob o volume gerado pelo Locust neste teste |

Critério "o que for maior" evita que ruído de medição em latências já muito baixas (poucos ms)
dispare falso-positivo — comum em cargas leves como a deste teste.

## Metodologia

Serviço-alvo: `prontuario-service` (representativo — mesmo padrão de instrumentação HTTP+JDBC
dos outros 3 serviços de domínio; escolhido pelo mesmo motivo de T-902/T-405: evidência real
em vez de repetir o teste 4×).

- Infra real via compose: `postgres-prontuario`, `otel-collector`, `jaeger`.
- **Cenário Locust reaproveitado sem modificação** (T-203): `ProntuarioUser` de
  `loadtest/app/locustfile.py`, apontado direto para `http://localhost:8081` (os paths batem
  1:1 com as rotas do serviço — dispensa o LB, que não está ativo no compose; ver pendência
  registrada em `state.md` desde T-901).
- Duas condições, cada uma com **2 repetições** (após um warmup de 15s descartado, para
  reduzir ruído de JIT/conexão fria):
  - **OTel ON** (padrão): `java -Dquarkus.profile=dev -jar quarkus-run.jar`.
  - **OTel OFF**: `java -Dquarkus.profile=dev -Dquarkus.otel.sdk.disabled=true -jar quarkus-run.jar`
    (mesma flag usada em `%test.quarkus.otel.sdk.disabled=true`; confirmado no log de boot que
    a extensão continua carregada mas o SDK não roda).
- Locust: `-u 20 -r 10 --run-time 30-45s` (concorrência moderada, adequada a uma única
  instância JVM num sandbox compartilhado — não é um rig de benchmarking dedicado).

## Resultados (bruto, 4 execuções)

| Execução | Condição | Avg (ms) | p50 | p95 | p99 | Reqs | RPS | Falhas |
|---|---|---|---|---|---|---|---|---|
| A  | OTel **ON**  | 3.87 | 3 | 6 | 10 | 1831 | 40.72 | 0 |
| A2 | OTel **ON**  | 4.50 | 4 | 8 | 16 | 1177 | 39.29 | 0 |
| B  | OTel **OFF** | 3.89 | 3 | 6 | 12 | 1772 | 39.39 | 0 |
| B2 | OTel **OFF** | 5.12 | 3 | 6 | 14 | 1228 | 38.75 | 0 |

**Médias por condição:**

| Condição | Avg (ms) | p95 (ms) | p99 (ms) | RPS |
|---|---|---|---|---|
| OTel ON  | 4.19 | 7.0 | 13.0 | 40.00 |
| OTel OFF | 4.51 | 6.0 | 13.0 | 39.07 |

## Análise

A diferença entre ON e OFF (p95: +1ms; avg: **OFF é 0.32ms mais lento**, não ON) está **dentro
do ruído** de medição deste ambiente — não há um sinal direcional claro de overhead. Isso é
esperado: `RNF-H-001` pede um orçamento **medido**, não necessariamente uma degradação
mensurável — nesta carga (~40 req/s, 20 VUs, instância JVM única em ambiente compartilhado), o
overhead de instrumentação HTTP+JDBC do OTel está **abaixo do piso de ruído do ambiente**
(poucos ms), portanto **dentro do orçamento por larga margem** em ambas as métricas (p95 e
taxa de erro). Uma amostra (B2) teve um outlier de 1700ms (99.99º percentil, 1 requisição em
1228) — consistente com uma pausa de GC/agendamento do host, não relacionado à instrumentação
(ocorreu na condição **OFF**, o que reforça que não é causado pelo OTel).

**RNF-H-007 (ingestão sob carga):** as duas execuções com OTel ligado (A + A2, ~3000
requisições, cada uma gerando múltiplos spans — HTTP server + query SQL) não produziram
nenhum erro/descarte nos logs do `otel-collector`; consulta à API do Jaeger após os testes
confirma centenas de traces com spans completos armazenados. Pipeline absorve o volume gerado
sem degradação visível.

## Limitações (honestidade da medição)

- Ambiente sandboxed/compartilhado (não um rig de benchmark dedicado) — resultados são
  **indicativos**, não uma medição de precisão de laboratório; ruído de poucos ms é esperado.
  n=2 repetições por condição é pequeno; suficiente para concluir "sem degradação
  perceptível dentro do orçamento definido", não para cravar um percentual exato de overhead.
- Carga moderada (~40 req/s) — não testa o orçamento sob carga de pico/saturação; se o
  orçamento precisar ser revalidado sob stress real, é uma extensão natural desta task (não
  bloqueia o critério de aceite, que é ter um orçamento definido e medido nesta carga).
- Testado 1 serviço representativo (`prontuario-service`) — os outros 3 usam o mesmo padrão de
  instrumentação (HTTP+JDBC via extensão Quarkus OTel), risco de variância entre eles é baixo.

## Critérios de aceite

- [x] Orçamento de overhead definido (tabela acima).
- [x] Overhead medido sob carga real do Locust, comparando com/sem instrumentação.
- [x] Resultado dentro do orçamento (diferença ON vs. OFF dentro do ruído de medição, bem
      abaixo de 10%/10ms).
- [x] Zero falhas introduzidas pela instrumentação em ambas as condições.
- [x] Pipeline de ingestão (Collector→Jaeger) absorve o volume gerado sem erros/descartes
      (RNF-H-007).

## Referências

- [`../../PRD.md`](../../PRD.md) — PRD global (RNF-H-001/007)
- [`./PLAN.md`](./PLAN.md) — plano de execução desta task
- `loadtest/app/locustfile.py` (T-203) — cenário reaproveitado sem modificação
