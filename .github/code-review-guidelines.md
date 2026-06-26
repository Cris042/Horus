# Rubrica de Code Review (IA)

Guia usado pela rotina de review automatizado ([`.github/workflows/code-review.yml`](./workflows/code-review.yml)) e por revisores humanos. O revisor de IA **deve usar as skills** instaladas e priorizar os achados por severidade: **Crítico → Alto → Médio → Baixo**.

## Skills a utilizar

| Skill | Para quê |
|---|---|
| `security-review` | Segurança: injeção, segredos, authz, vazamento de PII |
| `backend-patterns` | Boas práticas de API, camadas, tratamento de erro, idempotência |
| `postgres-patterns` | Modelagem, índices, queries sargáveis, anti-padrões |
| `jpa-patterns` | **N+1**, fetch joins, `@BatchSize`, `EntityGraph`, paginação |
| `database-migrations` | Migrações seguras, reversíveis, zero-downtime |
| `docker-patterns` | Imagens, multi-stage, segurança de contêiner, compose |
| `benchmark` | Regressões de desempenho, custo de operações |

## 1. Segurança (use `security-review`)

- Injeção (SQL/command), uso de queries parametrizadas.
- Segredos em código/log; credenciais por serviço (RNF-003).
- **Vazamento de PII/dados sensíveis em logs, traces ou parâmetros de query** (RNF-010, RNF-H-002) — **bloqueante**.
- Validação de entrada nas bordas HTTP.

## 2. Desempenho (use `benchmark`, `postgres-patterns`, `jpa-patterns`)

- **N+1 queries** — sinalizar acesso a associações lazy dentro de laços, ausência de `JOIN FETCH`/`EntityGraph`/`@BatchSize`. **Alto/Crítico.**
- **Full table scan** — predicados não sargáveis (função sobre coluna, `LIKE '%x'`), ausência de índice para filtros/joins frequentes, `SELECT *` em caminho quente. **Alto.**
- Paginação ausente em listagens (RF-007, RF-015, RF-019).
- Operações O(n) escondidas em caminho síncrono de request.

## 3. Boas práticas (use `backend-patterns`, `docker-patterns`, `database-migrations`)

- Separação de regra de negócio, HTTP, persistência e integração (RNF-014).
- Migrações versionadas, reversíveis e idempotentes (RF-028, RNF-015).
- Dockerfile multi-stage, imagem mínima, usuário não-root.
- Tratamento de erro e timeouts em chamadas HTTP entre serviços.

## 4. Regras específicas do projeto (ADRs)

- **Isolamento de banco por serviço** — nenhum acesso direto ao banco de outro serviço (ADR-0003). **Bloqueante.**
- **Propagação de contexto OTel** em HTTP **e** nos headers do RabbitMQ (RF-029) — não perder o trace na fronteira HTTP→AMQP.
- **Sanitização de telemetria** antes de persistir/enviar à IA (RNF-H-002).
- **Worker Rust** limitado a relatório + e-mail (ADR-0005) — sem regra de negócio.
- **SAGA (orquestração) para fluxos entre serviços** — cada passo idempotente e com compensação; sem rollback ACID/2PC (ADR-0013). Verificar idempotência e existência de compensação.

## Formato da saída

- Comentários inline por arquivo:linha, agrupados por severidade.
- Um comentário-resumo no PR com a contagem por severidade e o veredito.
- Se nada for encontrado, declarar explicitamente "Sem achados".
