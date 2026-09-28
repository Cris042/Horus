# ADR-0011 — Camada de IA com Claude para resumo, RCA e anomalias

- **Status:** Aceito
- **Data:** 2026-06-26
- **Origem:** Novo (núcleo do foco do projeto)
- **Requisitos:** RF-H-005, RF-H-006, RF-H-007, RF-H-008, RF-H-009, RF-H-010, RF-H-013, RNF-H-003, RNF-H-004, RNF-H-006

## Contexto

O diferencial do Horus é **usar IA para resumir tudo que está acontecendo** na aplicação e para acelerar diagnóstico. É preciso escolher o provedor/modelos, o padrão de integração no stack Java/Quarkus e as salvaguardas de custo e privacidade.

## Decisão

Adotar a família **Claude (Anthropic)** como motor de IA, integrada via **Quarkus LangChain4j (extensão Anthropic)** — solução nativa do ecossistema Quarkus do projeto.

### Seleção de modelo por tarefa
| Tarefa | Modelo sugerido | Racional |
|---|---|---|
| Resumo periódico de estado / clusterização (alto volume) | **Haiku** (`claude-haiku-4-5`) | Baixo custo e latência |
| Explicação de trace / consulta em linguagem natural | **Sonnet** (`claude-sonnet-5`) | Equilíbrio qualidade/custo |
| RCA profunda em incidentes | **Opus** (`claude-opus-5`) | Raciocínio mais forte |

### Padrões de uso
1. **Resumo de estado** (sob demanda + agendado) — RF-H-005.
2. **Explique este trace** — narrativa do caminho da request — RF-H-006.
3. **RCA** acionada por anomalia/erro — causa provável + próximos passos — RF-H-007.
4. **Detecção/clusterização** de anomalias e erros — RF-H-008/009.
5. **Pergunte ao Horus** — linguagem natural → consulta aos backends → resposta — RF-H-010.

### Salvaguardas
- **Privacidade (RNF-H-006):** apenas telemetria **sanitizada** vai para o LLM; trilha do que foi enviado.
- **Custo (RNF-H-003):** seleção de modelo por tarefa, **cache** de resumos, **amostragem** de spans e **orçamento** de tokens.
- **Latência (RNF-H-004):** respostas curtas síncronas; análises pesadas assíncronas.
- **Provedor desacoplado:** o acesso ao LLM fica atrás de uma interface, permitindo trocar de modelo/provedor sem afetar o resto do Horus.

> IDs de modelo, parâmetros, limites e preços vigentes devem ser confirmados na referência oficial da API Claude/Anthropic no momento da implementação — **não fixar números neste ADR**.

## Consequências

**Positivas**
- Telemetria bruta vira explicação acionável em linguagem natural.
- Custo ajustável por tarefa graças à seleção de modelo.

**Negativas**
- Dependência de um serviço externo de IA (rede, custo, disponibilidade).
- Necessidade de avaliar a qualidade dos resumos/RCA (risco de "alucinação"): tratar saídas como **assistência**, não verdade absoluta; sempre permitir ir ao trace cru.

## Adendo (2026-09-28, T-1003) — SDK oficial em vez de Quarkus LangChain4j

**Contexto.** A integração da T-601 usava `quarkus-langchain4j-anthropic` 1.1.0. Ao preparar a IA
real (T-1003) constatou-se que ela **nunca teria funcionado** com os modelos atuais:

- a extensão envia sempre `temperature` e `top_k` (o `topK` nem é opcional, padrão 40) — os modelos
  atuais (Opus 4.7+/Opus 5/Sonnet 5) rejeitam parâmetros de amostragem com **400**;
- `max_tokens` padrão de 1024 e um **único** modelo global: o `ModelTier` era só rótulo, a seleção
  de modelo por tarefa (acima) não acontecia;
- `system` e prompt eram concatenados numa única mensagem de usuário;
- o motor real só era selecionável por flag de **build** (`@IfBuildProperty`): ligar
  `horus.ai.enabled` no compose/K8s não tinha efeito sem recompilar.

**Decisão.** O adapter passa a usar o **SDK oficial da Anthropic para Java** (`com.anthropic:anthropic-java`),
continuando **atrás da mesma porta `LlmEngine`** (nenhum chamador mudou — a decisão de "provedor
desacoplado" acima é o que tornou a troca barata). `AnthropicLlmEngine` é o único bean do motor e
escolhe em runtime entre *live* (`horus.ai.enabled=true` + `ANTHROPIC_API_KEY`) e *stub*.

| Camada | Modelo | `effort` | Observação |
|---|---|---|---|
| `FAST` | `claude-haiku-4-5` | — | Haiku 4.5 não aceita `effort` |
| `BALANCED` | `claude-sonnet-5` | `medium` | |
| `DEEP` | `claude-opus-5` | `high` | `fallbacks: "default"` (beta `server-side-fallback-2026-07-01`) contra recusas |

Nenhum parâmetro de amostragem é enviado; `system` vai separado; falhas da API viram **503**
(a IA é opcional — RNF-H-008). Smoke real por camada: `scripts/ai-smoke.sh` / job `ai-live` do CI.
