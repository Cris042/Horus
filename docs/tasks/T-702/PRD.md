# PRD — T-702: Visualização do fluxo de vida (waterfall)

| Campo | Valor |
|---|---|
| **Task** | `T-702` |
| **Fase** | Fase 7 — Painel / UI do Horus |
| **Requisitos** | RF-H-011 |
| **Dependências** | T-503 (request lifecycle), T-504 (query lifecycle), T-505 (error logs) |
| **Branch** | `task/T-702-waterfall-ui` |

## Objetivo

Apresentar, para um `traceId`, o **fluxo de vida completo da request em formato
waterfall**: a timeline de spans (com profundidade, categoria e duração), as
**queries SQL** correlacionadas e os **logs de erro** agregados — tudo numa única
página, consumindo as APIs de ciclo de vida já entregues (T-503/504/505). É a
materialização visual do RF-H-011 sobre o painel estático do Horus (T-701).

## Escopo

- `horus-waterfall.html` (recurso estático em `META-INF/resources`):
  - campo de `traceId` + botão para carregar;
  - **waterfall de spans**: uma barra por span posicionada por `offsetMicros`/
    `durationMicros`, indentada por `depth`, colorida por `category`
    (`http`/`database`/`messaging`/`worker`/`internal`), com serviço e operação;
  - painel de **queries SQL** (statement sanitizado, banco, duração);
  - painel de **logs de erro** agregados (serviço, nível, amostra, contagem);
  - resumo do trace (nº de spans, serviços, duração total, mensageria/worker).
- `HorusPanelResource` — incluir os endpoints de ciclo de vida (request/query/
  errors/service-map/saga) na lista de capacidades do `overview`.
- Link da página do painel (`horus-panel.html`) para o waterfall.
- Teste `@QuarkusTest` garantindo que a página é servida e que o `overview` expõe
  o endpoint de ciclo de vida da request.

## Fora de escopo

- Renderização de **service-map**/**SAGA** como diagrama gráfico — esta fatia foca
  no waterfall; os endpoints já existem (T-506/T-507) e podem ganhar UI depois.
- Frameworks de front-end / build de assets — mantém-se HTML+JS estático, sem bundler,
  no mesmo padrão de T-701.

## Critérios de aceitação

1. `GET /horus-waterfall.html` é servido (200) e renderiza a waterfall a partir de
   um `traceId`, consumindo `/horus/lifecycle/requests|queries|errors/{traceId}`.
2. As barras de span refletem `offset`/`duração`/`depth`/`category`.
3. `GET /horus/panel/overview` passa a listar os endpoints de ciclo de vida.
4. A página do painel tem link para o waterfall.
5. `./mvnw -pl horus test` verde.
