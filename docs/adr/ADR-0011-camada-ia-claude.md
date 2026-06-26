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
| Resumo periódico de estado / clusterização (alto volume) | **Haiku** (`claude-haiku-4-5-20251001`) | Baixo custo e latência |
| Explicação de trace / consulta em linguagem natural | **Sonnet** (`claude-sonnet-4-6`) | Equilíbrio qualidade/custo |
| RCA profunda em incidentes | **Opus** (`claude-opus-4-8`) | Raciocínio mais forte |

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
