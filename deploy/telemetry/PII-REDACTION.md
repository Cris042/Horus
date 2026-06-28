# Redação de PII na borda do Collector (T-406)

O sistema observado é um **prontuário médico**: dados clínicos e financeiros são
sensíveis (CONTRACT §6, RNF-010/RNF-H-002). A proteção é **em duas camadas**:

| Camada | Onde | Tarefa | Garantia |
|---|---|---|---|
| 1. Origem | serviços Quarkus (SQL parametrizado, sem valores crus) | `T-401` | **forte** — o dado nunca é emitido |
| 2. Borda | `transform/pii` no OTel Collector | `T-406` (este) | **reforço** — redige o que escapar |

A camada 1 é a garantia principal; a borda é defesa em profundidade (best-effort regex).

## O que o `transform/pii` faz

Aplicado nas pipelines **traces** e **logs** (após `memory_limiter`, antes de `batch`),
com `error_mode: ignore` (uma falha de OTTL nunca derruba o sinal).

### (a) Remoção de chaves proibidas (atributos de span e de log)

`cpf` · `documento` · `email` · `nome_paciente` · `cartao` · `prontuario.conteudo`
→ `delete_key(...)` (a chave some inteira).

### (b) Mascaramento por padrão (valores de atributo e corpo do log)

| Padrão | Regex | Vira |
|---|---|---|
| E-mail | `[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}` | `***@***` |
| CPF (com/sem pontuação) | `\b\d{3}\.?\d{3}\.?\d{3}-?\d{2}\b` | `***` |
| Cartão (13–19 dígitos) | `\b\d{13,19}\b` | `***` (só atributos) |

> IDs **opacos/substitutos** (UUID, ids de correlação) são permitidos pelo §6 e
> **não** são tocados — são a chave de correlação por `trace_id`.

## Verificação

1. **Validar o config:**
   ```bash
   docker run --rm \
     -v "$PWD/deploy/telemetry/otel-collector-config.yaml:/etc/otelcol/config.yaml:ro" \
     otel/opentelemetry-collector-contrib:0.118.0 \
     validate --config=/etc/otelcol/config.yaml   # exit 0
   ```
2. **Smoke funcional (com o stack de pé):** emitir um span/log de teste com um
   atributo `email=joao@x.com` e `cpf=123.456.789-00` e conferir que em Jaeger/Loki
   chegam como `***@***` / chave ausente. Roteiro completo no
   [`README.md`](./README.md) deste diretório.

## Limites (fora do escopo)

- Não há detecção semântica/ML — apenas chaves proibidas + padrões comuns BR.
- Métricas não passam por redação: por contrato não carregam PII (labels de baixa
  cardinalidade). Manter assim é responsabilidade da instrumentação (T-401/T-403).
- Worker Rust redige na origem em `T-403`.
