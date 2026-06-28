# PRD — T-406: Sanitização/redação de PII na borda do Collector

| Campo | Valor |
|---|---|
| **Task** | `T-406` |
| **Fase do roadmap** | `Fase 4 — Telemetria e correlação` |
| **Status** | `Em revisão (PR)` |
| **Branch** | `task/T-406-pii-redaction` |
| **PR** | `#<n>` (preencher ao abrir) |
| **Depende de** | `T-404` |
| **Requisitos atendidos** | `RNF-010`, `RNF-H-002` |
| **ADRs relacionados** | `ADR-0009`, `ADR-0010` |
| **Data** | `2026-06-27` |

## Objetivo

Implementar a **segunda camada** de defesa em profundidade contra PII (CONTRACT §6): a redação **na borda do Collector**. A 1ª camada é na **origem** (serviços, `T-401`: SQL parametrizado, sem valores crus). Aqui reforçamos sobre **spans** e **logs** antes do export aos backends, garantindo que prontuário médico/dados financeiros nunca cheguem crus ao Jaeger/Loki.

## Escopo (o que entra)

- Processor `transform/pii` (OTTL) no Collector, nas pipelines de **traces** e **logs**:
  - **remove** chaves proibidas de atributos (`cpf`, `documento`, `email`, `nome_paciente`, `cartao`, `prontuario.conteudo`);
  - **mascara** e-mail (`***@***`), CPF e cartão em valores de atributo e no corpo do log.
- `error_mode: ignore` — uma falha de OTTL nunca derruba o sinal.
- Doc de redação (`deploy/telemetry/PII-REDACTION.md`): regras, mapa chave→ação e como verificar.

## Fora do escopo

- Redação na origem (já em `T-401`; worker Rust em `T-403`).
- Métricas: por contrato **não** carregam PII (nomes de métrica/labels de baixa cardinalidade) — não há redação de métrica aqui.
- Detecção semântica/ML de PII — apenas chaves proibidas + padrões (e-mail/CPF/cartão).

## Premissas e dependências

- `T-404` entregou o pipeline do Collector (validável com `otelcol validate`).
- A redação é melhor-esforço regex; a **garantia forte** é não emitir na origem (`T-401`). As duas camadas juntas atendem RNF-H-002.

## Critérios de aceite

- [ ] `transform/pii` presente nas pipelines de traces e logs do Collector.
- [ ] Chaves proibidas removidas; e-mail/CPF/cartão mascarados em atributos e corpo de log.
- [ ] `otel/opentelemetry-collector-contrib:0.118.0 validate` → exit 0.
- [ ] Doc `PII-REDACTION.md` descreve regras e verificação.

## Riscos

| Risco | Mitigação |
|---|---|
| Regex de PII com falso-negativo (formato inesperado) | Defesa primária é na origem (T-401); borda é reforço. Padrões cobrem formatos comuns BR |
| OTTL inválido derruba o Collector | `error_mode: ignore` + validação `otelcol validate` no PR |
| Mascarar demais (ofuscar IDs de correlação) | IDs opacos são permitidos (§6); só chaves proibidas e padrões sensíveis são tocados |

## Referências

- [`../../PRD.md`](../../PRD.md) — PRD global
- [`./PLAN.md`](./PLAN.md) — plano de execução
- [`../../telemetry/CONTRACT.md`](../../telemetry/CONTRACT.md) §6 — regras de PII (normativo)
- `ADR-0009`, `ADR-0010`
