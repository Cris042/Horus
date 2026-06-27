# Estratégia de migrações de banco (T-105)

| Campo | Valor |
|---|---|
| **Requisitos** | RF-028 (migrações independentes), RNF-015 (evolução versionada/reproduzível) |
| **ADRs** | ADR-0002 (banco por serviço), ADR-0005 (Flyway) |
| **Status** | Aceito (T-105) |

## Princípio

Cada microsserviço de domínio **é dono exclusivo do schema do seu banco** e o evolui por **migrações Flyway próprias** — não há schema compartilhado nem migração cruzada (ADR-0002). O Hibernate **não** gera DDL (`quarkus.hibernate-orm.schema-management.strategy=none`); o **Flyway é a única fonte de verdade do schema**.

## Layout

| Serviço | Banco | Migrações |
|---|---|---|
| `prontuario-service` | `prontuario_db` | `services/prontuario/src/main/resources/db/migration/` (`V1__init`, `V2__consulta`) |
| `payment-service` | `payment_db` | `services/payment/src/main/resources/db/migration/` (`V1__init`, `V2__movimentacao_pagamento`) |
| `invoice-service` | `invoice_db` | `services/invoice/src/main/resources/db/migration/` (`V1__init`) |

Cada banco tem sua **própria tabela de histórico** (`flyway_schema_history`); as numerações `V*` são **independentes por serviço**.

## Convenções

- **Nomenclatura:** `V<n>__<descricao>.sql` (versionadas, imutáveis depois de aplicadas).
- **Ids via sequência:** as entidades Panache geram id por **sequência `<tabela>_seq`** (otimizador PooledLo, `INCREMENT BY 50`); as migrações criam a sequência correspondente — **não** usar coluna `IDENTITY` (senão `INSERT` falha em `nextval`).
- **Tipos:** dinheiro em `NUMERIC(18,2)`; timestamps em `TIMESTAMPTZ`.

## Configuração (hardening — RNF-015)

Aplicada em cada serviço (`application.properties`):

```properties
quarkus.flyway.migrate-at-start=true       # aplica migrações pendentes no boot
quarkus.flyway.validate-on-migrate=true    # valida checksums (migração reproduzível)
quarkus.flyway.clean-disabled=true         # proíbe `clean` (evita drop acidental do schema)
```

## Fora do escopo (próximas tasks)

- **Isolamento por credenciais/usuário de banco** e proibição de acesso cruzado → **T-106** (RNF-002/003).
- **Migrações de dados** (não-DDL) e estratégias de zero-downtime → quando houver necessidade de produção.

## Referências

- ADR-0002, ADR-0005 · [`../PRD.md`](../PRD.md) (RF-028, RNF-015) · [`../ROADMAP.md`](../ROADMAP.md)
