# PRD — T-405: Validar correlação ponta a ponta (request→query→log→mensagem→worker)

| Campo | Valor |
|---|---|
| **Task** | `T-405` |
| **Fase do roadmap** | Fase 4 — Telemetria ponta a ponta (OTel) |
| **Status** | `Entregue` |
| **Branch** | `task/T-405-e2e-correlation` |
| **Depende de** | `T-401`, `T-402`, `T-403`, `T-404` |
| **Requisitos atendidos** | `RF-H-004` |
| **ADRs relacionados** | `ADR-0007` (OTel), `ADR-0010` (pipeline de telemetria) |
| **Data** | 2026-07-02 |

## Objetivo

Validar **de fato** — com infraestrutura real, não apenas leitura de código — que um único
`trace_id` amarra **request → query SQL → log → mensagem RabbitMQ → processamento do worker**
(RF-H-004), incluindo a fronteira **HTTP→AMQP** que o contrato de telemetria (`CONTRACT.md`
§2) chama explicitamente de "o ponto crítico que mais quebra correlação". Esta task ficou
pendente desde a Fase 4 (bloqueada por "sem cluster real disponível" em sessões anteriores);
esta sessão tinha Docker + Maven + toolchain Rust funcionais e pôde executá-la de fato.

## Escopo

- Subir a infraestrutura real via compose (`postgres-invoice`, `rabbitmq`, `otel-collector`,
  `jaeger`) e rodar `invoice-service` (JAR Quarkus) + `worker` (binário Rust) de verdade.
- Exercitar o fluxo RF-021: `POST /notas` → `POST /notas/{id}/relatorio` (publica em
  RabbitMQ) → worker consome, gera relatório, envia e-mail (stub).
- Extrair o `trace_id` do log estruturado do `invoice-service` e consultar a API do Jaeger
  para inspecionar a árvore de spans e confirmar parenteamento correto entre serviços.
- **Corrigir** qualquer quebra de correlação encontrada (não é só "documentar que está
  quebrado" — a task pede validação, e validação real encontrou 2 bugs reais, corrigidos
  abaixo).

## Fora de escopo

- Ativar o Load Balancer (NGINX) no compose/K8s — gap pré-existente, já registrado em
  `state.md` desde T-901 (o teste chamou os serviços diretamente por porta).
- Medir overhead de instrumentação sob carga real (T-903, que agora fica desbloqueada por
  esta entrega).
- Correlação com métricas (Prometheus) — RF-H-004 não lista métricas na cadeia de correlação
  por `trace_id` (métricas não carregam `trace_id` por design, ver `CONTRACT.md` §5).

## O que foi encontrado (2 bugs reais na fronteira HTTP→AMQP)

### Bug 1 — o publicador nunca injetava o `traceparent` nos headers AMQP

`RelatorioPublisher.publicar` (invoice-service) fazia só `emitter.send(msg)` — um comentário no
código assumia que "o OpenTelemetry cria um span PRODUCER e injeta o `traceparent`
automaticamente", mas o conector SmallRye/RabbitMQ **não** faz essa injeção sozinho. O worker
recebia a mensagem **sem** o header e abria um trace novo (raiz), sem nenhuma relação com o
trace do publicador — quebra total da correlação HTTP→AMQP.

**Corrigido** em `RelatorioPublisher.java`: injeta explicitamente `traceparent`/`tracestate` do
`Context` OTel ativo nos headers da `OutgoingRabbitMQMetadata` antes de `emitter.send`.

### Bug 2 — o parser do propagador W3C do Rust rejeitava o `trace-flags` real do Java

Depois de corrigir o Bug 1, o header **chegava** na mensagem (confirmado inspecionando a fila
via API do RabbitMQ), mas o worker **ainda** abria um trace novo. Causa: o OTel Java do Quarkus
injeta `trace-flags = 03` (bit `sampled` + o "random trace id flag" do W3C Trace Context Level
2); o `opentelemetry_sdk` (Rust) 0.27 só reconhece `00`/`01` e **rejeita o `traceparent`
inteiro** para qualquer outro valor — o `SpanContext` extraído vinha inválido/zerado
(`has_active_span() == false`), silenciosamente virando span raiz.

**Corrigido** em `worker/src/telemetry.rs`: `sanitizar_trace_flags` normaliza o byte de
`trace-flags` ao bit `sampled` antes de repassar ao propagador — os demais bits são reservados
pelo W3C e não usados no parenteamento local do span.

### Gap adicional — logs do worker não carregavam `trace_id`/`span_id`

Mesmo com o span corretamente parenteado (visível no Jaeger), os logs JSON do worker não
traziam `trace_id`/`span_id` como campo — violação de `CONTRACT.md` §4.1
(`trace_id`/`span_id` **DEVEM**¹ ser injetados quando há trace ativo; ¹obrigatório quando há
trace ativo — que é exatamente o caso aqui). Os logs do Java já faziam isso via MDC; o worker
não tinha o equivalente.

**Corrigido**: `worker/src/telemetry.rs::registrar_trace_context` grava `trace_id`/`span_id`
como campos do span (`tracing::field::Empty` declarado na criação, preenchido via
`OpenTelemetrySpanExt::context()` depois do `set_parent`), chamado em `main.rs` no laço de
consumo.

## Evidência (comandos + saída real)

Ambiente: `docker compose up -d postgres-invoice rabbitmq otel-collector jaeger` +
`invoice-service` (JAR Quarkus, perfil dev) + `worker` (binário Rust em container
`rust:1`, rede do compose).

```
$ curl -s -X POST http://localhost:8083/notas -d '{"valor":11.11,"referencia":"...","simularFalha":false}'
{"id":54,"status":"EMITIDA", ...}

$ curl -s -i -X POST http://localhost:8083/notas/54/relatorio
HTTP/1.1 202 Accepted
{"id":"68050ceb-...","tipo":"NOTA_FISCAL","notaId":54, ...}
```

**Log do invoice-service** (estruturado, MDC):
```
{"message":"Publicando solicitação de relatório 68050ceb-...",
 "mdc":{"spanId":"8714df49e7bb9b80","traceId":"06bacc12c0ef38cbebfc206ce2869d73","sampled":"true"}}
```

**Log do worker** (estruturado, campos do span — após a correção do gap de logs):
```
{"message":"relatório gerado e e-mail enviado", "id":"68050ceb-...", "nota_id":54,
 "span":{"messaging.system":"rabbitmq",
         "span_id":"346c063e61866e6c",
         "trace_id":"06bacc12c0ef38cbebfc206ce2869d73",
         "name":"processar_relatorio"}}
```

**Mesmo `trace_id` (`06bacc12c0ef38cbebfc206ce2869d73`) nos dois serviços** — correlação de
log confirmada.

**Árvore de spans no Jaeger** (`GET /api/traces/{trace_id}`) para essa mesma requisição:

```
POST /notas/{id}/relatorio   (invoice-service, SERVER, raiz)
├── SELECT invoice_db.nota_fiscal   (invoice-service, CLIENT)
└── relatorios publish              (invoice-service, PRODUCER)
    └── processar_relatorio         (report-worker, INTERNAL)   ← filho correto, cross-service
```

`processar_relatorio` (processo `report-worker`) aparece como **filho direto** do span
`relatorios publish` (processo `invoice-service`) — a fronteira HTTP→AMQP preserva o mesmo
`trace_id` e a relação de parentesco, confirmando RF-H-004 de ponta a ponta: request → query →
mensagem → processamento do worker, tudo sob um único `trace_id`, com logs de ambos os
serviços correlacionados pelo mesmo identificador.

Ambiente desmontado ao final (`docker rm -f` do worker, `kill` do processo Quarkus,
`docker compose down`).

## Testes de regressão adicionados

- `RelatorioPublicacaoTest.emitirEPublicarRelatorio` (invoice): agora também verifica que a
  mensagem publicada carrega `OutgoingRabbitMQMetadata` com o header `traceparent` — usa um
  novo `OtelEnabledTestProfile` (reativa o SDK OTel, desligado por padrão em `%test`, com
  exportador `none` para não depender de um Collector real).
- `worker/src/telemetry.rs`: `traceparent_com_bits_reservados_no_trace_flags_e_extraido`
  (reproduz o `trace-flags=03` real do OTel Java) e
  `sanitizar_trace_flags_preserva_sampled_e_zera_bits_extras` (unitário da normalização).

## Critérios de aceite

- [x] `trace_id` único amarra request → query → mensagem RabbitMQ → processamento do worker,
      verificado via Jaeger com infraestrutura real (não só leitura de código).
- [x] Logs estruturados de `invoice-service` e `worker` carregam o **mesmo** `trace_id`.
- [x] Bugs de correlação encontrados foram **corrigidos** (não só documentados) e cobertos por
      teste de regressão.
- [x] `./mvnw -pl services/invoice -am test` verde (8/8, incluindo o novo teste).
- [x] `cargo fmt --check` / `cargo clippy -- -D warnings` / `cargo test` verdes no worker
      (14/14, incluindo os 2 novos).

## Riscos

| Risco | Mitigação |
|---|---|
| `opentelemetry_sdk` (Rust) 0.27 pode ganhar mais bits reservados no futuro que `sanitizar_trace_flags` não previu | A função só preserva o bit `sampled` e zera o resto — já é a forma mais conservadora; revisar se o W3C spec adicionar semântica nova a outros bits |
| Regressão do Bug 1 se alguém reescrever `RelatorioPublisher` sem os headers | Teste de regressão (`RelatorioPublicacaoTest`) falha explicitamente se o header sumir |

## Referências

- [`../../PRD.md`](../../PRD.md) — PRD global (RF-H-004)
- [`./PLAN.md`](./PLAN.md) — plano de execução desta task
- `docs/telemetry/CONTRACT.md` §2 (propagação), §4.1 (logs)
