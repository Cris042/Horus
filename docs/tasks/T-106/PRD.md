# PRD — T-106: Isolamento (credenciais/schema separados)

| Campo | Valor |
|---|---|
| **Task** | `T-106` |
| **Fase do roadmap** | Fase 1 — Microsserviços de domínio |
| **Status** | `Entregue` |
| **Branch** | `task/T-106-db-isolation` |
| **PR** | [#14](https://github.com/mclovin137/Horus/pull/14) |
| **Depende de** | `T-105` |
| **Requisitos atendidos** | RNF-002, RNF-003 |
| **ADRs relacionados** | ADR-0002 (banco por serviço) |
| **Data** | 2026-06-27 |

## Objetivo

Garantir **isolamento de persistência por serviço**: cada serviço acessa **apenas o seu banco**, com **credenciais exclusivas** (RNF-002/003). Fecha a Fase 1 do ponto de vista de dados.

## Escopo (o que entra)

- **Credenciais segregadas** (compose dev): cada `postgres-*` recebe `POSTGRES_USER`/`POSTGRES_PASSWORD` próprios (`prontuario_svc`, `payment_svc`, `invoice_svc`, `saga_svc`); healthcheck comum passa a usar `$POSTGRES_USER`.
- **Datasource por serviço:** `quarkus.datasource.username`/`password` de cada serviço apontando às suas credenciais.
- **`docs/architecture/db-isolation.md`** — camadas de isolamento (instância dedicada + credenciais), tabela serviço→banco→usuário, nota sobre secrets em produção.

## Fora do escopo

- **Secrets reais de produção** (Kubernetes/Vault) → Fase 8 (deploy); aqui são credenciais de **dev**, locais e não-secretas.
- **GRANTs finos / usuário não-superusuário** dentro de cada instância — desnecessário com instâncias dedicadas; pode ser endurecido depois.

## Premissas e dependências

- T-105 (migrações por banco) entregue.
- Instâncias PostgreSQL dedicadas por serviço (ADR-0002, compose T-003).
- Testes via Dev Services (provisiona a credencial configurada) — seguem verdes.

## Critérios de aceite

- [ ] Cada serviço usa credenciais **exclusivas** para o **seu** banco (RNF-003).
- [ ] Nenhum serviço referencia banco/credencial de outro (RNF-002).
- [ ] Isolamento documentado; build/test verde; `PLAN.md` 100% ✅ e `state.md` atualizado.

## Riscos

| Risco | Mitigação |
|---|---|
| Healthcheck do compose quebrar com novo usuário | Healthcheck passa a usar `$POSTGRES_USER`; validado por `compose config`. |
| Confundir credencial de dev com produção | Doc deixa claro: dev local/não-secreto; prod via secrets (Fase 8). |

## Referências

- [`../../architecture/db-isolation.md`](../../architecture/db-isolation.md) — entregável de documentação
- ADR-0002 · [`../../PRD.md`](../../PRD.md) (RNF-002/003)
- [`./PLAN.md`](./PLAN.md)
