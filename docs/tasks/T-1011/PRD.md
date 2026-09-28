# PRD — T-1011: UI de service-map e SAGA

| Campo | Valor |
|---|---|
| **Task** | `T-1011` |
| **Fase do roadmap** | `Fase 10 — Produto utilizável` |
| **Status** | `Entregue` |
| **Branch** | `claude/beautiful-cray-sfblsr` |
| **PR** | — |
| **Depende de** | `T-506`, `T-507`, `T-702`, `T-1010` |
| **Requisitos atendidos** | `RF-H-015`, `RF-H-016`, `RF-H-012` |
| **ADRs relacionados** | `ADR-0008` |
| **Data** | `2026-09-28` |

## Objetivo

Os endpoints de mapa de serviços (T-506) e de SAGA (T-507) existiam só como JSON — nenhuma tela os
mostrava. RF-H-016 pede a SAGA **visualizada** de ponta a ponta no Horus.

## Escopo (o que entra)

- Página de waterfall (`horus-waterfall.html`) ganha dois cards para o trace carregado:
  - **Mapa de serviços:** SVG em camadas a partir dos pontos de entrada (borda azul), arestas
    com a contagem de chamadas, spans e duração por serviço;
  - **SAGA:** desfecho (concluída / compensada / falhou / recuperada — T-1010), passos na linha
    do tempo, compensações (↩) e o **passo que falhou** em destaque.
- Painel: filtro **ocultar ruído** (`GET /horus/traces?minSpans=2`) — validando com o stack real, a
  lista de traces vinha dominada por traces de 1 span (queries de boot/Flyway) que escondiam as
  requests.

## Critérios de aceite

- [x] Com uma SAGA compensada real, o waterfall mostra o mapa (saga → payment 3×, saga → invoice 1×)
  e a SAGA com "falhou em: issue-invoice" — ver `waterfall-saga-compensada.png`.
- [x] `minSpans` filtra traces de fundo e compensa o limite (teste).
- [x] `./mvnw -pl horus test` verde (127).

## Referências

- [`./waterfall-saga-compensada.png`](./waterfall-saga-compensada.png) · [`./PLAN.md`](./PLAN.md)
