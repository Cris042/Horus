# PRD — T-1012: Endurecimentos menores

| Campo | Valor |
|---|---|
| **Task** | `T-1012` |
| **Fase do roadmap** | `Fase 10 — Produto utilizável` |
| **Status** | `Entregue` |
| **Branch** | `claude/beautiful-cray-sfblsr` |
| **PR** | — |
| **Depende de** | `T-608`, `T-803`, `T-901`, `T-1010` |
| **Requisitos atendidos** | `RNF-H-003`, `RNF-005`, `RNF-017` |
| **ADRs relacionados** | `ADR-0011`, `ADR-0013` |
| **Data** | `2026-09-28` |

## Escopo (o que entra)

- **TTL no cache de IA** (`horus.ai.cache.ttl`, 10m): o cache (T-608) servia para sempre a mesma
  resposta para o mesmo prompt — um resumo antigo podia ser servido como atual.
- **KEDA opcional** para o `report-worker` (profundidade da fila `relatorios.worker`), fora da
  kustomization base (evita quebrar clusters sem KEDA); instruções para trocar o HPA de CPU.
- **TLS interno** documentado por conexão (JDBC `sslmode=verify-full`, `amqps` no invoice e no
  worker, OTLP com TLS) em `deploy/k8s/README.md` — sem ativar por padrão (não há CA em dev).
- **Documentação alinhada ao que foi construído:** PRD, ROADMAP e `lib.md` citavam MicroProfile LRA +
  coordenador; a SAGA usa orquestrador próprio. Adendo na ADR-0013; LRA e coordenador saem do `lib.md`.

## Critérios de aceite

- [x] Resposta expirada não é servida e sai do cache (teste).
- [x] ScaledObject KEDA válido (kubeconform; CRD ignorada) e documentado.
- [x] Nenhuma menção operacional a LRA nos docs de referência (só histórico na ADR).
- [x] `./mvnw -pl horus test` verde (128).

## Referências

- [`./PLAN.md`](./PLAN.md)
