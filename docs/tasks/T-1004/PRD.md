# PRD — T-1004: Auditoria de prompts + sanitização obrigatória

| Campo | Valor |
|---|---|
| **Task** | `T-1004` |
| **Fase do roadmap** | `Fase 10 — Produto utilizável` |
| **Status** | `Entregue` |
| **Branch** | `claude/beautiful-cray-sfblsr` |
| **PR** | — |
| **Depende de** | `T-602`, `T-904` |
| **Requisitos atendidos** | `RNF-H-002`, `RNF-H-006` |
| **ADRs relacionados** | `ADR-0011` |
| **Data** | `2026-09-28` |

## Objetivo

RNF-H-006 pede "apenas dados sanitizados saem para o LLM; **trilha do que foi enviado**". A
sanitização dependia de cada agente lembrar de chamar o `PromptSanitizer` (a T-904 achou um que
esqueceu e aceitou o risco de reincidência), e não havia trilha alguma. Esta task torna a
fronteira **estrutural** e cria a trilha.

## Escopo (o que entra)

- `AuditingLlmEngine`: decorator CDI da porta `LlmEngine`, externo ao cache — todo pedido é
  sanitizado (system + prompt) antes de seguir e registrado, inclusive falhas e acertos de cache.
- `LlmAuditTrail`: últimos N registros em memória (`horus.ai.audit.max-entries`, 500) + totais.
  Cada registro: instante, `purpose`, camada, modelo pedido/respondido, live, tamanho, tokens
  estimados, **SHA-256** do conteúdo enviado, nº de redações, latência, desfecho — **sem o texto**.
- Trilha durável: log estruturado `horus.ai.audit` (vai ao Loki pelo pipeline OTel).
- `LlmRequest.purpose` (qual capacidade pediu); todos os agentes o preenchem.
- `PromptSanitizer.sanitizeCounting` (conta as redações).
- `GET /horus/ai/audit?limit=` (RBAC `AI_INSIGHTS` — inclui o papel Auditor).

## Fora do escopo

- Persistência própria da trilha (banco) — o Loki já é o armazenamento durável de logs.
- Detecção semântica de PII (continua regex: e-mail/CPF/cartão; defesa primária = origem).

## Critérios de aceite

- [x] Um agente que **não** sanitiza não consegue enviar PII ao modelo (teste com motor capturador).
- [x] Toda chamada ao LLM gera registro na trilha, sem o texto do prompt (teste via API real do CDI).
- [x] Falhas também são auditadas e propagadas.
- [x] Trilha limitada e ordenada do mais recente.
- [x] Risco aceito da T-904 marcado como fechado.
- [x] `./mvnw -pl horus test` verde (103 testes).

## Riscos

| Risco | Mitigação |
|---|---|
| Ordem dos decorators | Prioridade explícita (`APPLICATION - 10` < cache); teste `@QuarkusTest` exercita o wiring real |
| Log da trilha com dado sensível | Só metadados + hash; o texto nunca é logado |

## Referências

- [`../T-904/PRD.md`](../T-904/PRD.md) · [`./PLAN.md`](./PLAN.md)
