# PRD — T-506: Mapa de serviços/dependências a partir dos traces

| Campo | Valor |
|---|---|
| **Task** | `T-506` |
| **Fase** | Fase 5 — Horus core (correlação + ciclo de vida) |
| **Requisitos** | RF-H-015 |
| **Dependências** | T-502 (modelo de correlação), T-501 (camada de consulta) |
| **Branch** | `task/T-506-service-map` |

## Objetivo

Derivar, **a partir dos spans de um trace**, o **mapa de serviços e dependências**
percorridas pela request: quais serviços participaram (nós) e quais chamadas
inter-serviço ocorreram (arestas direcionadas chamador → chamado), com contagem de
chamadas e tempo somado. É a base de visualização topológica do Horus (RF-H-015),
complementar à waterfall (T-503) e construída sobre a mesma porta `TraceQueryPort`.

## Escopo

- `ServiceMapModel` — DTOs REST: nó de serviço, aresta direcionada, mapa do trace.
- `ServiceMapService` — projeta um `TraceResult` em grafo:
  - **nós**: um por `serviceName`, com `spanCount` e `totalDurationMicros`;
  - **arestas**: para cada span cujo *pai* está em **outro serviço**, aresta
    `pai.serviceName → span.serviceName`, agregando `callCount` e `totalDurationMicros`;
  - identifica o(s) serviço(s) **raiz** (spans sem pai conhecido) como pontos de entrada.
- `HorusServiceMapResource` — `GET /horus/lifecycle/service-map/{traceId}`
  (valida traceId hex de 32 chars; 404 se trace ausente; 400 se inválido).
- Teste REST `@QuarkusTest` com `TraceQueryPort` mockado.

## Fora de escopo

- Mapa **agregado de múltiplos traces** (visão global do sistema) — fica para fase de UI/análise.
- Renderização gráfica (front-end) — T-701/T-702.
- Métricas de saúde por aresta (taxa de erro/latência p95) — depende de T-606.

## Critérios de aceitação

1. `GET /horus/lifecycle/service-map/{traceId}` retorna nós (serviços) e arestas
   (dependências) derivados dos spans do trace.
2. Arestas só existem entre serviços **distintos**; chamadas repetidas entre o mesmo
   par somam `callCount`/`totalDurationMicros`.
3. Serviço de entrada (span raiz) é sinalizado.
4. 404 para trace inexistente; 400 para `traceId` malformado (sem consultar backend).
5. `./mvnw -pl horus test` verde.
