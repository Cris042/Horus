# PRD — T-1008: Alertas automáticos + e-mail real

| Campo | Valor |
|---|---|
| **Task** | `T-1008` |
| **Fase do roadmap** | `Fase 10 — Produto utilizável` |
| **Status** | `Entregue` |
| **Branch** | `claude/beautiful-cray-sfblsr` |
| **PR** | — |
| **Depende de** | `T-606`, `T-703`, `T-1001` |
| **Requisitos atendidos** | `RF-H-008`, `RF-H-013` |
| **ADRs relacionados** | `ADR-0011` |
| **Data** | `2026-09-28` |

## Objetivo

O mecanismo de alerta (T-703) e o detector (T-606) existiam, mas **nada os disparava**: um alerta
só saía se alguém chamasse `POST /horus/alerts` com a anomalia em mãos, e o e-mail era um stub.

## Escopo (o que entra)

- `AlertWatcher` agendado (`horus.alert.watch.interval`, `off` por padrão):
  - regras de limiar da config (`horus.alert.watch.rules.<nome>.{promql,threshold,comparison,severity}`)
    avaliadas pelo `AnomalyDetector` — cada série violada vira alerta;
  - **traces com erro na janela** (`lookback`, busca da T-1001) acima de `error-traces` → alerta
    `critical` com o `traceId` mais recente (ponto de partida da RCA) e os serviços envolvidos;
  - deduplicação por regra+rótulos dentro de `dedup-window`; falha de um backend não impede a outra
    checagem nem derruba o Horus.
- `EmailAlertChannel` com `quarkus-mailer`: múltiplos destinatários, assunto com severidade, link para
  o waterfall do trace; SMTP por `QUARKUS_MAILER_*`; mock automático em dev/test; falha SMTP →
  `dispatched=false`.

## Fora do escopo

- Baseline estatístico/ML de anomalias (continua por limiar).
- Persistência/histórico dos alertas; silenciamento manual.

## Critérios de aceite

- [x] Regra violada gera alerta; repetição dentro da janela é suprimida; após a janela volta a alertar.
- [x] N traces com erro na janela geram um alerta com o `traceId` mais recente.
- [x] Backend fora do ar em uma checagem não impede a outra.
- [x] E-mail entregue a todos os destinatários (MockMailbox), com severidade e link do trace.
- [x] `./mvnw -pl horus test` verde (123 testes).

## Riscos

| Risco | Mitigação |
|---|---|
| Tempestade de alertas | Deduplicação por janela; `SKIP` de execução concorrente |
| Custo de IA por alerta | Resumo na camada FAST; dedup limita a frequência |

## Referências

- [`../T-703/PRD.md`](../T-703/PRD.md) · [`./PLAN.md`](./PLAN.md)
