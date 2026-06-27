# PRD — T-105: Migrações Flyway independentes por banco

| Campo | Valor |
|---|---|
| **Task** | `T-105` |
| **Fase do roadmap** | Fase 1 — Microsserviços de domínio |
| **Status** | `Em revisão (PR)` |
| **Branch** | `task/T-105-flyway-migrations` |
| **PR** | [#12](https://github.com/mclovin137/Horus/pull/12) |
| **Depende de** | `T-101` (scaffold) |
| **Requisitos atendidos** | RF-028, RNF-015 |
| **ADRs relacionados** | ADR-0002 (banco por serviço), ADR-0005 (Flyway) |
| **Data** | 2026-06-27 |

## Objetivo

Formalizar e **endurecer** a estratégia de migrações: cada serviço evolui o schema do **seu** banco por migrações Flyway próprias e independentes (RF-028), de forma versionada e reproduzível (RNF-015). As migrações em si (`V1`/`V2` por serviço) já foram entregues junto do domínio (T-102/103/104); esta task consolida a estratégia, adiciona proteções e a documenta.

## Escopo (o que entra)

- **Hardening do Flyway** nos 3 serviços (`application.properties`):
  - `validate-on-migrate=true` (valida checksums — reprodutibilidade);
  - `clean-disabled=true` (proíbe `clean` — evita drop acidental do schema).
- **`docs/architecture/db-migrations.md`** — princípio (banco por serviço; Flyway dono do schema), layout por serviço, convenções (nomenclatura, **id por sequência**, tipos) e configuração.

## Fora do escopo

- **Isolamento por credenciais/usuário** de banco e proibição de acesso cruzado → **T-106** (RNF-002/003).
- Novas migrações de domínio (pertencem às tasks de cada serviço).
- Migrações de dados / zero-downtime.

## Premissas e dependências

- Scaffold T-101 e domínios T-102/103/104 entregues (migrações `V*` já presentes).
- Schema é dono do Flyway (`hibernate-orm.schema-management.strategy=none`).

## Critérios de aceite

- [ ] Cada serviço tem migrações independentes no seu banco, com history próprio (RF-028).
- [ ] Flyway valida checksums e tem `clean` desabilitado (RNF-015).
- [ ] Estratégia documentada em `docs/architecture/db-migrations.md`.
- [ ] Build/test verde; `PLAN.md` 100% ✅ e `state.md` atualizado.

## Riscos

| Risco | Mitigação |
|---|---|
| Editar migração já aplicada quebra checksum | Migrações são imutáveis após aplicadas; mudanças entram como novas `V*` (documentado). |

## Referências

- [`../../architecture/db-migrations.md`](../../architecture/db-migrations.md) — o entregável de documentação
- [`../../PRD.md`](../../PRD.md) — RF-028, RNF-015; ADR-0002/0005
- [`./PLAN.md`](./PLAN.md)
