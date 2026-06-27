# Plano de Execução — T-101: Scaffold Quarkus dos 3 serviços

> ⚠️ **Regra R2 ([WORKFLOW](../../WORKFLOW.md)).** Sempre que **criar ou modificar** um arquivo desta tabela, atualize **na mesma hora** o seu `Status` e `Atualizado`, e registre em *Registro de alterações*.

| Campo | Valor |
|---|---|
| **Task** | `T-101` |
| **Branch** | `task/T-101-scaffold-services` |
| **PRD** | [`./PRD.md`](./PRD.md) |
| **Progresso** | `23/23` arquivos |

## Arquivos a criar/modificar

| # | Arquivo | Ação | Propósito | Status | Atualizado |
|---|---|---|---|---|---|
| 1 | `pom.xml` (parent) | Modificar | Registrar os 3 módulos `services/*` **e elevar Quarkus 3.20→3.37** (Panache sob JDK 25) | ✅ Concluído | 2026-06-27 |
| 2 | `services/prontuario/pom.xml` | Criar | Módulo Quarkus (REST + Panache + Flyway + PG) | ✅ Concluído | 2026-06-27 |
| 3 | `services/prontuario/src/main/resources/application.properties` | Criar | Config: service.name, porta 8081, datasource por perfil, Flyway | ✅ Concluído | 2026-06-27 |
| 4 | `services/prontuario/src/main/resources/db/migration/V1__init.sql` | Criar | Migração inicial (tabela `prontuario`) | ✅ Concluído | 2026-06-27 |
| 5 | `services/prontuario/src/main/java/org/example/prontuario/domain/Prontuario.java` | Criar | Entidade Panache mínima | ✅ Concluído | 2026-06-27 |
| 6 | `services/prontuario/src/main/java/org/example/prontuario/api/ProntuarioInfoResource.java` | Criar | Endpoint `/prontuario/info` | ✅ Concluído | 2026-06-27 |
| 7 | `services/prontuario/src/test/java/org/example/prontuario/api/ProntuarioInfoResourceTest.java` | Criar | Teste de fumaça `@QuarkusTest` | ✅ Concluído | 2026-06-27 |
| 8 | `services/payment/pom.xml` | Criar | Módulo Quarkus | ✅ Concluído | 2026-06-27 |
| 9 | `services/payment/src/main/resources/application.properties` | Criar | Config: porta 8082, `payment_db` | ✅ Concluído | 2026-06-27 |
| 10 | `services/payment/src/main/resources/db/migration/V1__init.sql` | Criar | Migração inicial (tabela `carteira`) | ✅ Concluído | 2026-06-27 |
| 11 | `services/payment/src/main/java/org/example/payment/domain/Carteira.java` | Criar | Entidade Panache mínima | ✅ Concluído | 2026-06-27 |
| 12 | `services/payment/src/main/java/org/example/payment/api/PaymentInfoResource.java` | Criar | Endpoint `/payment/info` | ✅ Concluído | 2026-06-27 |
| 13 | `services/payment/src/test/java/org/example/payment/api/PaymentInfoResourceTest.java` | Criar | Teste de fumaça | ✅ Concluído | 2026-06-27 |
| 14 | `services/invoice/pom.xml` | Criar | Módulo Quarkus | ✅ Concluído | 2026-06-27 |
| 15 | `services/invoice/src/main/resources/application.properties` | Criar | Config: porta 8083, `invoice_db` | ✅ Concluído | 2026-06-27 |
| 16 | `services/invoice/src/main/resources/db/migration/V1__init.sql` | Criar | Migração inicial (tabela `nota_fiscal`) | ✅ Concluído | 2026-06-27 |
| 17 | `services/invoice/src/main/java/org/example/invoice/domain/NotaFiscal.java` | Criar | Entidade Panache mínima | ✅ Concluído | 2026-06-27 |
| 18 | `services/invoice/src/main/java/org/example/invoice/api/InvoiceInfoResource.java` | Criar | Endpoint `/invoice/info` | ✅ Concluído | 2026-06-27 |
| 19 | `services/invoice/src/test/java/org/example/invoice/api/InvoiceInfoResourceTest.java` | Criar | Teste de fumaça | ✅ Concluído | 2026-06-27 |
| 20 | `docs/tasks/T-101/PRD.md` | Criar | PRD desta task | ✅ Concluído | 2026-06-27 |
| 21 | `docs/tasks/T-101/PLAN.md` | Criar | Este plano | ✅ Concluído | 2026-06-27 |
| 22 | `lib.md` | Modificar | Quarkus 3.20→3.37 (nota JDK 25 + tabelas §1/2/3/7) | ✅ Concluído | 2026-06-27 |
| 23 | `CLAUDE.md` | Modificar | Atualizar a nota de Quarkus/JDK 25 (bump 3.37) | ✅ Concluído | 2026-06-27 |

**Legenda:** ⬜ Pendente · 🟡 Em progresso · ✅ Concluído

## Passos de implementação

1. ✅ Criar a branch `task/T-101-scaffold-services` a partir de `main` (pós-merge de T-003/T-005).
2. ✅ Registrar os 3 módulos no `pom.xml` parent.
3. ✅ Gerar cada módulo (pom, config, migração, entidade, resource, teste) seguindo o padrão do módulo `horus` e o contrato de telemetria.
4. ✅ Validar com `./mvnw package` (testes via Dev Services PostgreSQL).
5. ⬜ Atualizar `state.md`; abrir o PR `T-101: …`; preencher o nº do PR aqui e no PRD.

## Verificação / testes

- [x] **Build de produção dos 5 módulos verde** sob JDK 25 + Quarkus 3.37 (`./mvnw -DskipTests package` → BUILD SUCCESS) — confirma o enhancement Panache sob Java 25 (o bloqueio do 3.20).
- [ ] **Testes de fumaça (`@QuarkusTest`)**: rodam via Dev Services (PostgreSQL/Testcontainers). **Validação no CI** (runner com Docker) — o WSL local não expõe o socket Docker à JVM de teste de forma confiável.
- [x] Cada `application.properties` usa o `service.name` canônico e o banco dedicado.

## Notas de implementação

- **Quarkus 3.20.0 → 3.37.0** (parent `pom.xml`): o enhancement de entidades Panache do 3.20 usa um ASM que **não lê bytecode JDK 25** (`Unsupported class file major version 69`); falhava no `quarkus:build` (prod) e no boot de teste. O Horus (T-002/T-004) não expunha isso por **não ter entidades Panache**. Decisão do usuário: **subir o Quarkus** (vs. rebaixar bytecode ou abandonar Panache). 3.37.0 é a última estável e valida nos 5 módulos.
- **Testes sem Docker local:** o socket Docker do WSL não é alcançável pela JVM do Testcontainers aqui; o build de produção (parte sensível ao JDK 25) foi validado local, e os smoke tests via Dev Services ficam a cargo do CI.

## Registro de alterações

| Data | Arquivo | Mudança |
|---|---|---|
| 2026-06-27 | `pom.xml` | Registra `services/prontuario|payment|invoice` como módulos; **Quarkus 3.20.0→3.37.0** (JDK 25 + Panache). |
| 2026-06-27 | `services/**` | Scaffold dos 3 serviços: pom, config por perfil, `V1__init.sql`, entidade Panache, resource `/info`, teste de fumaça (Dev Services). |
| 2026-06-27 | `lib.md`, `CLAUDE.md` | Atualizam a versão do Quarkus (3.37) e a nota de compatibilidade JDK 25. |
| 2026-06-27 | `docs/tasks/T-101/PRD.md`, `PLAN.md` | PRD e plano de execução. |
