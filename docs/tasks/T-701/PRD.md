# PRD — T-701: Painel Horus (saúde + resumo de IA + busca)

| Campo | Valor |
|---|---|
| **Task** | `T-701` |
| **Fase do roadmap** | `Fase 7 — Painel / Experiência` |
| **Status** | `Em revisão (PR)` |
| **Branch** | `task/T-701-horus-panel` |
| **PR** | [#28](https://github.com/mclovin137/Horus/pull/28) |
| **Depende de** | `T-603` (resumo de IA), `T-604/605` (explain/RCA), `T-607` (ask), `T-501` (query) |
| **Requisitos atendidos** | `RF-H-012` |
| **ADRs relacionados** | `ADR-0011` (camada de IA) |
| **Data** | `2026-06-28` |

## Objetivo

Entregar a 1ª fatia do **painel Horus** (RF-H-012): uma única visão que reúne a **saúde**
da plataforma (motor de IA, cache, backends configurados) e os **pontos de entrada** das
capacidades de IA/consulta já entregues (resumo, explicação, RCA, "pergunte ao Horus",
queries), consumível por uma página estática leve.

## Escopo (o que entra)

- API de agregação `GET /horus/panel/overview` — saúde + capacidades num só payload.
- Página estática `horus-panel.html` (servida pelo Quarkus) que consome a API: cartões de
  saúde, "pergunte ao Horus", busca por trace (explain/RCA) e lista de capacidades.
- Teste `@QuarkusTest` da API de overview e de que a página é servida.

## Fora do escopo

- Visualização do fluxo de vida em waterfall (spans+queries+logs) → **T-702**.
- Busca avançada sobre agregação de erros (T-505) e correlação por `trace_id` (T-502).
- RBAC do painel → **T-704**.
- Build/empacotamento de front-end (SPA/bundler): a fatia usa HTML estático sem build.

## Premissas e dependências

- As capacidades consumidas já existem: `/horus/ai/*` (T-603/604/605/607) e `/horus/query/*` (T-501).
- Sem `ANTHROPIC_API_KEY`, o motor roda em modo `stub` (`isLive()=false`) — o painel reflete isso.
- URLs de backend vêm de `quarkus.rest-client.{jaeger,loki,prometheus}.url`.

## Critérios de aceite

- [x] `GET /horus/panel/overview` retorna `service`, `ai` (modo/live/cache), `backends` e `endpoints`.
- [x] Página `horus-panel.html` é servida (200) e contém a marca "Horus".
- [x] `mvn -pl horus test` verde.

## Riscos

| Risco | Mitigação |
|---|---|
| Acoplamento a contratos de resposta dos agentes | Painel só lê endpoints públicos estáveis; mudanças ficam isoladas no JS. |
| Exposição de URLs internas de backend | Apenas configuração já presente; RBAC do painel virá em T-704. |

## Referências

- [`../../PRD.md`](../../PRD.md) — PRD global
- [`./PLAN.md`](./PLAN.md) — plano de execução desta task
- `ADR-0011` — camada de IA desacoplada
