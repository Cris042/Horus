# PRD — T-101: Scaffold Quarkus dos 3 serviços de domínio

| Campo | Valor |
|---|---|
| **Task** | `T-101` |
| **Fase do roadmap** | Fase 1 — Microsserviços de domínio |
| **Status** | `Em revisão (PR)` |
| **Branch** | `task/T-101-scaffold-services` |
| **PR** | [#7](https://github.com/mclovin137/Horus/pull/7) |
| **Depende de** | `T-002` (build/parent), `T-003` (infra dev), `T-005` (contrato de telemetria) |
| **Requisitos atendidos** | RF-006..020 (scaffold; domínio detalhado em T-102..104) |
| **ADRs relacionados** | ADR-0002 (banco por serviço), ADR-0003 (Quarkus), ADR-0005 (Flyway) |
| **Data** | 2026-06-27 |

## Objetivo

Criar o **scaffold Quarkus** dos três serviços de domínio — **Prontuário**, **Payment** e **Invoice** — cada um como módulo Maven filho do parent, com **REST + Hibernate Panache + Flyway** ligados ao seu **PostgreSQL dedicado**. Estabelece a base compilável e testável sobre a qual o domínio real (T-102..104), as migrações (T-105) e a instrumentação OTel (T-401) são construídos.

## Escopo (o que entra)

- **3 módulos** em `services/{prontuario,payment,invoice}`, registrados no `pom.xml` parent:
  - `pom.xml` por módulo (REST, REST-Jackson, Hibernate ORM Panache, JDBC PostgreSQL, Flyway, SmallRye Health, Arc).
  - `application.properties`: `service.name` **canônico** (contrato T-005), porta dedicada (8081/8082/8083), datasource por perfil (`%dev` → compose, `%prod` → host do serviço, `%test` → Dev Services), Flyway `migrate-at-start`.
  - **Migração `V1__init.sql`** mínima (uma tabela por serviço) e **entidade Panache** correspondente, provando a fiação ORM↔banco.
  - **Endpoint `/{svc}/info`** + health do SmallRye, com **teste de fumaça `@QuarkusTest`** (info + liveness).

## Fora do escopo

- **Domínio real** (regras, endpoints CRUD, fluxos) → T-102 (Prontuário), T-103 (Payment), T-104 (Invoice).
- **Migrações completas** de schema → T-105.
- **SAGA / orquestração** → T-107.
- **Instrumentação OTel** (HTTP/JDBC, logs JSON, métricas) → T-401.
- **Load Balancer** na frente dos serviços → T-201.

## Desvio aprovado (toolchain)

- **Quarkus 3.20.0 → 3.37.0** no parent `pom.xml`. O enhancement de entidades **Panache** do 3.20 não suporta bytecode **JDK 25** (`Unsupported class file major version 69`); o Horus (T-002) não expunha o problema por não ter entidades. Subir o Quarkus (escolha do usuário) mantém **Java 25 + Panache** e valida nos 5 módulos. Ver `lib.md` §1 e `CLAUDE.md` (Key constraints).

## Premissas e dependências

- Parent `org.example:horus-parent` (Java 25, BOM Quarkus 3.20) — T-002.
- `docker-compose` de dev (T-003) provê os bancos `prontuario_db`/`payment_db`/`invoice_db` para `quarkus:dev`.
- Testes usam **Quarkus Dev Services** (PostgreSQL efêmero via Testcontainers) — requer Docker no runner (CI tem).

## Critérios de aceite

- [ ] `./mvnw package` compila os 3 módulos e os testes de fumaça passam (Flyway migra o banco efêmero, REST/health respondem).
- [ ] Cada serviço usa o `service.name` canônico do contrato de telemetria (T-005) e seu banco dedicado (ADR-0002).
- [ ] Schema é dono do **Flyway** (Hibernate não gera DDL).
- [ ] Módulos registrados no parent; `PLAN.md` 100% ✅ e `state.md` atualizado.

## Riscos

| Risco | Mitigação |
|---|---|
| CI sem Docker → Dev Services falha | Runner GitHub `ubuntu-latest` tem Docker; imagem fixada `postgres:17.2-alpine`. |
| Divergência de `service.name`/banco vs. contrato | Valores canônicos do CONTRACT.md aplicados diretamente no `application.properties`. |
| Tabela do scaffold conflitar com T-105 | Migrações mínimas e versionadas (`V1`); T-105 evolui com novas versões Flyway. |

## Referências

- [`../../PRD.md`](../../PRD.md), [`../../ROADMAP.md`](../../ROADMAP.md) — RF-006..020
- [`../../telemetry/CONTRACT.md`](../../telemetry/CONTRACT.md) — `service.name` canônico, nomes de banco
- ADR-0002 (banco por serviço), ADR-0003 (Quarkus), ADR-0005 (Flyway)
- [`./PLAN.md`](./PLAN.md)
