# Isolamento de persistência por serviço (T-106)

| Campo | Valor |
|---|---|
| **Requisitos** | RNF-002 (isolamento de persistência), RNF-003 (credenciais segregadas) |
| **ADRs** | ADR-0002 (banco por serviço) |
| **Status** | Aceito (T-106) |

## Princípio

Cada serviço **só acessa o seu próprio banco**, e **com credenciais exclusivas**. Não há usuário compartilhado nem acesso direto às estruturas de outro serviço (RNF-002/003). O isolamento é **estrutural** (instâncias separadas) **e** por **credenciais** (usuário/senha distintos).

## Camadas de isolamento

1. **Instância dedicada por banco (ADR-0002):** `prontuario_db`, `payment_db`, `invoice_db` e `saga_db` rodam em **contêineres PostgreSQL separados** — não há como uma query de um serviço alcançar a tabela de outro.
2. **Credenciais segregadas (RNF-003):** cada instância tem **usuário/senha próprios**; o serviço correspondente conecta apenas com os seus.

| Serviço | Banco | Usuário (dev) |
|---|---|---|
| `prontuario-service` | `prontuario_db` | `prontuario_svc` |
| `payment-service` | `payment_db` | `payment_svc` |
| `invoice-service` | `invoice_db` | `invoice_svc` |
| `saga-orchestrator` | `saga_db` | `saga_svc` |

> A SAGA (T-107) é **orquestração por HTTP** — o orquestrador **não** acessa os bancos dos participantes; chama suas APIs. O isolamento de dados é preservado.

## Configuração

- **Compose (dev):** cada `postgres-*` define `POSTGRES_USER`/`POSTGRES_PASSWORD`/`POSTGRES_DB` próprios; o healthcheck usa `$POSTGRES_USER`.
- **Serviço:** `quarkus.datasource.username`/`password` = credenciais do seu banco.

> **Senhas de dev são locais e não-secretas.** Em produção, as credenciais vêm de **secrets** (Kubernetes/Vault) — ver Fase 8 (deploy). Nunca commitar segredos reais.

## Verificação (revisão)

- Nenhum serviço referencia o banco/credencial de outro (checado na revisão — ver risco "Isolamento lógico de bancos" no PRD).
- Cada serviço tem datasource único apontando ao seu banco.

## Referências

- ADR-0002 · [`./db-migrations.md`](./db-migrations.md) · [`../PRD.md`](../PRD.md) (RNF-002/003)
