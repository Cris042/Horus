# PRD — T-102: Prontuário Service (domínio)

| Campo | Valor |
|---|---|
| **Task** | `T-102` |
| **Fase do roadmap** | Fase 1 — Microsserviços de domínio |
| **Status** | `Em revisão (PR)` |
| **Branch** | `task/T-102-prontuario-domain` |
| **PR** | [#8](https://github.com/mclovin137/Horus/pull/8) |
| **Depende de** | `T-101` (scaffold) |
| **Requisitos atendidos** | RF-006, RF-007, RF-008, RF-009, RF-010, RF-025 |
| **ADRs relacionados** | ADR-0002 (banco por serviço), ADR-0003 (Quarkus), ADR-0005 (Flyway) |
| **Data** | 2026-06-27 |

## Objetivo

Implementar o **domínio do Prontuário Service** sobre o scaffold de T-101: criar e consultar prontuários (RF-006/007) e **registrar, atualizar e finalizar consultas** vinculadas (RF-008..010), persistindo **somente em `prontuario_db`** (RF-025). Primeiro dos três serviços de domínio da Fase 1.

## Escopo (o que entra)

- **Entidades Panache:** `Consulta` (vinculada a `Prontuario` via `@ManyToOne`) + enum `StatusConsulta` (`EM_ANDAMENTO`→`FINALIZADA`).
- **Migração Flyway `V2__consulta.sql`** (tabela `consulta` + índice por `prontuario_id`).
- **Serviços** (`ProntuarioService`, `ConsultaService`) com regras transacionais; finalizar torna a consulta imutável (editar/finalizar após finalizada → conflito).
- **REST** (JSON, Bean Validation):
  - `POST /prontuarios`, `GET /prontuarios`, `GET /prontuarios/{id}` (RF-006/007);
  - `POST /prontuarios/{id}/consultas`, `GET /prontuarios/{id}/consultas` (RF-008);
  - `GET /consultas/{id}`, `PUT /consultas/{id}` (RF-009), `POST /consultas/{id}/finalizar` (RF-010).
- **Mapeamento de erros:** 404 (não encontrado), 409 (consulta finalizada), 400 (validação).
- **Teste de fluxo `@QuarkusTest`** cobrindo o ciclo completo + casos de erro.

## Fora do escopo

- **Payment/Invoice** → T-103/T-104. **SAGA** cross-service → T-107.
- **Migrações completas/consolidadas** e isolamento de credenciais → T-105/T-106.
- **Instrumentação OTel** (HTTP/JDBC, logs JSON, métricas) e **sanitização de PII clínica** → T-401/T-406 (o contrato T-005 já define as regras).
- **AuthN/AuthZ (RBAC)** → fases posteriores.

## Premissas e dependências

- Scaffold T-101 (módulo `prontuario-service`, datasource por perfil, Flyway, Dev Services em teste).
- Testes rodam no CI (runner com Docker / Dev Services PostgreSQL).

## Critérios de aceite

- [ ] É possível criar (RF-006) e consultar (RF-007) prontuário via REST.
- [ ] É possível registrar (RF-008), atualizar (RF-009) e finalizar (RF-010) consulta; consulta finalizada é imutável (409).
- [ ] Persistência só em `prontuario_db` (RF-025); schema versionado por Flyway.
- [ ] Build verde; `PLAN.md` 100% ✅ e `state.md` atualizado.

## Riscos

| Risco | Mitigação |
|---|---|
| PII clínica em `descricao` vazar em telemetria | Fora do escopo aqui; o contrato T-005 exige sanitização, aplicada em T-401/T-406. |
| Divergência de schema entre Flyway (Postgres) e enhancement | Flyway é dono do schema (`schema-management.strategy=none`); testes via Dev Services PostgreSQL real. |

## Referências

- [`../../PRD.md`](../../PRD.md), [`../../ROADMAP.md`](../../ROADMAP.md) — RF-006..010, RF-025
- ADR-0002, ADR-0003, ADR-0005
- [`./PLAN.md`](./PLAN.md)
