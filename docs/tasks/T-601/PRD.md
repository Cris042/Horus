# PRD — T-601: Integração Quarkus LangChain4j (Anthropic) atrás de interface desacoplada

| Campo | Valor |
|---|---|
| **Task** | `T-601` |
| **Fase do roadmap** | `Fase 6 — IA (resumo, RCA, anomalias)` |
| **Status** | `Entregue` |
| **Branch** | `task/T-601-llm-anthropic-integration` |
| **PR** | `#21` |
| **Depende de** | `T-501` |
| **Requisitos atendidos** | `ADR-0011` (RF-H-005..010/013, RNF-H-003/004/006) |
| **ADRs relacionados** | `ADR-0011` |
| **Data** | `2026-06-27` |

## Objetivo

Estabelecer a **camada de IA** do Horus: acesso ao LLM (Claude/Anthropic via Quarkus
LangChain4j) **atrás de uma porta desacoplada** (`LlmEngine`), com seleção de modelo por
camada (Haiku/Sonnet/Opus). Base sobre a qual os agentes de produto (Summarizer, RCA,
"pergunte ao Horus" — T-603+) são construídos sem conhecer LangChain4j nem a API Anthropic.

## Escopo (o que entra)

- Porta `LlmEngine` + DTOs (`LlmRequest`/`LlmResponse`) e enum `ModelTier`
  (`FAST`→`claude-haiku-4-5`, `BALANCED`→`claude-sonnet-4-6`, `DEEP`→`claude-opus-4-8`).
- `StubLlmEngine` (default, `horus.ai.enabled=false`): app sobe e CI roda **sem** chave.
- `LangChain4jLlmEngine` (ativo com `horus.ai.enabled=true`): adapter sobre o `ChatModel`
  da extensão `quarkus-langchain4j-anthropic`.
- API REST `GET /horus/ai/health` e `POST /horus/ai/complete`.
- Config (modelo default Opus, chave por env, timeout) + dependência fixada (1.1.0).

## Fora do escopo

- Roteamento por camada com **modelos nomeados** simultâneos (Haiku+Sonnet+Opus) — fatia seguinte.
- Os agentes de produto: Summarizer (T-603), RCA (T-605), "pergunte ao Horus", etc.
- Montador de contexto telemetria→prompt com orçamento de tokens (T-602).
- Salvaguardas finas de custo/cache/amostragem (RNF-H-003) — endereçadas com os agentes.

## Premissas e dependências

- `quarkus-langchain4j-anthropic:1.1.0` resolve e **compila/testa sob Quarkus 3.37 + JDK 25**
  (validado: `BUILD SUCCESS`, 8 testes). Versão fixada em property no parent `pom.xml`.
- A IA real exige `ANTHROPIC_API_KEY` (segredo pendente do usuário) + `horus.ai.enabled=true`.
- Privacidade (RNF-H-006): só telemetria **sanitizada** (T-401/T-406) deve ir ao `prompt`.

## Critérios de aceite

- [ ] `LlmEngine` desacopla o resto do Horus de LangChain4j/Anthropic.
- [ ] Sem chave (default), o stub responde e a app sobe; **CI verde**.
- [ ] Com `horus.ai.enabled=true`, o adapter LangChain4j assume (1 bean, sem ambiguidade).
- [ ] `./mvnw -pl horus test` verde.

## Riscos

| Risco | Mitigação |
|---|---|
| Extensão LangChain4j incompatível com Quarkus 3.37/JDK 25 | **Validado** localmente (BUILD SUCCESS); versão fixada em property |
| Build exigir chave real | Stub default + chave com default `dummy-key`; adapter real só com flag de build |
| Alucinação do LLM | ADR-0011: tratar saída como assistência; sempre permitir ir ao trace cru |

## Referências

- [`../../PRD.md`](../../PRD.md) · [`./PLAN.md`](./PLAN.md)
- [`../../adr/ADR-0011-camada-ia-claude.md`](../../adr/ADR-0011-camada-ia-claude.md)
- [`../../../lib.md`](../../../lib.md) §2 (versões de IA)
