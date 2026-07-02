# PRD — T-904: Auditoria de privacidade (nenhum dado sensível cru em telemetria nem na IA)

| Campo | Valor |
|---|---|
| **Task** | `T-904` |
| **Fase do roadmap** | Fase 9 — Endurecimento e aceite |
| **Status** | `Entregue` |
| **Branch** | `task/T-904-privacy-audit` |
| **Depende de** | `T-406`, `T-602` |
| **Requisitos atendidos** | `RNF-H-002`, `RNF-H-006` |
| **Data** | 2026-07-02 |

## Objetivo

Auditar — com dados reais fluindo pelo pipeline, não só releitura de código — as **três
camadas de defesa em profundidade** contra PII (CONTRACT §6) e confirmar que nenhum dado
sensível cru chega à telemetria persistida (Jaeger/Loki) nem ao LLM:

1. **Origem** (`T-401`): SQL parametrizado, sem valores crus nos spans.
2. **Borda do Collector** (`T-406`): `transform/pii` (OTTL) remove chaves proibidas e
   mascara e-mail/CPF/cartão em spans e logs.
3. **Fronteira do prompt** (`T-602`): `PromptSanitizer` — última rede antes do LLM.

## Metodologia (auditoria real, não só leitura)

- **Camada 1**: `POST /prontuarios` com um valor PII-like (e-mail+CPF) no campo `pacienteId`;
  inspecionada a árvore de spans real no Jaeger (`db.statement` do INSERT) e os logs reais no
  Loki via `{service_namespace="medrec"} |= "..."`.
- **Camada 2**: enviado um span sintético e um log sintético **diretamente** ao endpoint OTLP
  do Collector (`:4318/v1/traces` e `/v1/logs`) com chaves proibidas (`email`, `cpf`,
  `nome_paciente`, `cartao`) e padrões de PII embutidos em texto livre; inspecionado o que
  efetivamente chegou ao Jaeger/Loki via suas APIs.
- **Camada 3**: auditoria de código dos **pontos de chamada** — todo componente que monta um
  prompt e chama `LlmEngine.complete(...)` foi localizado (`grep` por `LlmRequest`/
  `engine.complete`) e verificado se o texto passa por `PromptSanitizer` (direto ou via
  `ContextAssembler`, que já o aplica uma vez sobre o texto final montado).

## O que foi encontrado

### Camadas 1 e 2 — confirmadas funcionando (evidência real)

- **`db.statement`** do INSERT real: `insert into prontuario (criado_em,paciente_id,id)
  values (?,?,?)` — **totalmente parametrizado**, nenhum valor cru no span, mesmo com um
  valor PII-like no campo. Nenhum log da aplicação ecoou o valor (`grep` no Loki vazio).
- **Span sintético** enviado direto ao Collector com `email`/`cpf`/`nome_paciente`/`cartao`
  como atributos e um atributo `value` com o padrão embutido em texto livre:
  - As 4 chaves proibidas **desapareceram completamente** do span armazenado no Jaeger
    (`delete_key` funcionando).
  - `value: "contato: paciente.real@example.com cpf 123.456.789-09"` chegou ao Jaeger como
    `value: "contato: ***@*** cpf ***"` (mascaramento de padrão funcionando).
- **Log sintético** com o mesmo conteúdo no `body`: chegou ao Loki como
  `"falha ao processar ***@*** cpf ***"` — mascaramento do corpo do log confirmado.

### Camada 3 — 1 gap real encontrado e corrigido

`ErrorClusterer.label()` (T-606, RF-H-009) monta o prompt do rótulo de IA **diretamente** a
partir de `log.line()` (telemetria vinda de `LogQueryPort`, i.e. do Loki) via `StringBuilder`,
e chamava `engine.complete(...)` **sem nunca passar pelo `PromptSanitizer`** — diferente de
todos os outros agentes (`NlQueryAgent`, `RootCauseAnalyst`, `TraceExplainer`,
`StateSummarizer`), que consomem `PromptContext.text()` do `ContextAssembler` (que já aplica o
sanitizer uma vez sobre o texto final). Como `log.line()` vem do Loki (já passou pela Camada 2
no Collector), o gap **não é uma exposição sem nenhuma proteção** — mas é uma inconsistência
real: este agente tinha só 2 das 3 camadas de defesa documentadas pelo próprio contrato
(`LlmEngine`'s Javadoc: *"o prompt deve conter apenas telemetria sanitizada"*), diferente de
todos os demais.

**Corrigido**: `ErrorClusterer.label()` agora passa o prompt montado por
`PromptSanitizer.sanitize(...)` antes de `engine.complete(...)` — mesma rede de segurança dos
outros agentes.

### Revisado, fora de escopo (sem mudança)

- **`AlertService.buildPrompt`** usa `title`/`details` de `AlertRequest`, que só é construído
  via `POST /horus/alerts` — **entrada do operador**, não telemetria automática pulled do
  Loki/Jaeger. RNF-H-002/006 fala de dados de **telemetria** que vazam para a IA; texto que um
  operador humano digita deliberadamente num alerta é uma fronteira de confiança diferente
  (mesma categoria da `question` do `NlQueryAgent`, que também não passa por
  `PromptSanitizer` — nem deveria, é o que o usuário perguntou).
- **`AnomalyDetector`** é puramente baseado em regras de limiar sobre métricas — não chama
  `LlmEngine`, não monta prompt.

## Critérios de aceite

- [x] Camada 1 (origem) confirmada com uma requisição real + inspeção do span/logs reais.
- [x] Camada 2 (borda do Collector) confirmada com sondas sintéticas de span e log via OTLP
      direto — chaves proibidas removidas, padrões mascarados, em ambas as pipelines.
- [x] Camada 3 (fronteira do prompt): todos os pontos de chamada de `LlmEngine.complete`
      auditados; gap real encontrado (`ErrorClusterer`) e **corrigido**, não só documentado.
- [x] Teste de regressão adicionado (`ErrorClustererTest.promptDoLabelNaoCarregaPiiCrua`)
      capturando o `LlmRequest` real via mock e afirmando ausência de PII crua + presença do
      marcador de redação.
- [x] `./mvnw -pl horus -am test` verde (64/64, incluindo o novo teste).

## Riscos

| Risco | Mitigação |
|---|---|
| Regex de PII (camadas 2/3) não cobre todos os formatos (ex.: telefone, endereço, nome sem padrão) | Defesa primária continua sendo a origem (T-401: nunca emitir o valor cru); risco residual documentado, não é objetivo desta task expandir os padrões de regex |
| Novo agente futuro repetir o mesmo erro (montar prompt sem `PromptSanitizer`) | `LlmEngine`'s Javadoc já documenta o contrato; nenhum mecanismo automático (ex. wrapper obrigatório) impede reincidência — risco aceito, fica para um hardening futuro se necessário |

## Referências

- [`../../PRD.md`](../../PRD.md) — PRD global (RNF-H-002/006)
- [`./PLAN.md`](./PLAN.md) — plano de execução desta task
- `docs/telemetry/CONTRACT.md` §6 (regras de PII)
- `docs/tasks/T-406/PRD.md`, `docs/tasks/T-602/PRD.md` (camadas 2 e 3 originais)
