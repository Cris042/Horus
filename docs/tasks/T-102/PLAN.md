# Plano de Execução — T-102: Prontuário Service (domínio)

> ⚠️ **Regra R2 ([WORKFLOW](../../WORKFLOW.md)).** Sempre que **criar ou modificar** um arquivo desta tabela, atualize **na mesma hora** o seu `Status` e `Atualizado`, e registre em *Registro de alterações*.

| Campo | Valor |
|---|---|
| **Task** | `T-102` |
| **Branch** | `task/T-102-prontuario-domain` |
| **PRD** | [`./PRD.md`](./PRD.md) |
| **Progresso** | `18/18` arquivos |

## Arquivos a criar/modificar

| # | Arquivo | Ação | Propósito | Status | Atualizado |
|---|---|---|---|---|---|
| 1 | `services/prontuario/pom.xml` | Modificar | + `quarkus-hibernate-validator` | ✅ Concluído | 2026-06-27 |
| 2 | `.../domain/StatusConsulta.java` | Criar | Enum de ciclo de vida da consulta | ✅ Concluído | 2026-06-27 |
| 3 | `.../domain/Consulta.java` | Criar | Entidade Panache `Consulta` (`@ManyToOne` Prontuario) | ✅ Concluído | 2026-06-27 |
| 4 | `.../resources/db/migration/V2__consulta.sql` | Criar | Tabela `consulta` + índice | ✅ Concluído | 2026-06-27 |
| 5 | `.../api/dto/CriarProntuarioRequest.java` | Criar | DTO de entrada (RF-006) | ✅ Concluído | 2026-06-27 |
| 6 | `.../api/dto/ProntuarioResponse.java` | Criar | DTO de saída de prontuário | ✅ Concluído | 2026-06-27 |
| 7 | `.../api/dto/RegistrarConsultaRequest.java` | Criar | DTO de entrada (RF-008) | ✅ Concluído | 2026-06-27 |
| 8 | `.../api/dto/AtualizarConsultaRequest.java` | Criar | DTO de entrada (RF-009) | ✅ Concluído | 2026-06-27 |
| 9 | `.../api/dto/ConsultaResponse.java` | Criar | DTO de saída de consulta | ✅ Concluído | 2026-06-27 |
| 10 | `.../service/ProntuarioService.java` | Criar | Casos de uso de prontuário (RF-006/007) | ✅ Concluído | 2026-06-27 |
| 11 | `.../service/ConsultaService.java` | Criar | Casos de uso de consulta (RF-008..010) | ✅ Concluído | 2026-06-27 |
| 12 | `.../service/ConflitoConsultaException.java` | Criar | Exceção de conflito de estado | ✅ Concluído | 2026-06-27 |
| 13 | `.../api/ConflitoConsultaMapper.java` | Criar | Mapper → HTTP 409 | ✅ Concluído | 2026-06-27 |
| 14 | `.../api/ProntuarioResource.java` | Criar | REST de prontuário + registrar/listar consultas | ✅ Concluído | 2026-06-27 |
| 15 | `.../api/ConsultaResource.java` | Criar | REST de consulta (buscar/atualizar/finalizar) | ✅ Concluído | 2026-06-27 |
| 16 | `.../test/.../ProntuarioFlowTest.java` | Criar | Teste de fluxo `@QuarkusTest` (ciclo + erros) | ✅ Concluído | 2026-06-27 |
| 17 | `docs/tasks/T-102/PRD.md` | Criar | PRD desta task | ✅ Concluído | 2026-06-27 |
| 18 | `docs/tasks/T-102/PLAN.md` | Criar | Este plano | ✅ Concluído | 2026-06-27 |

**Legenda:** ⬜ Pendente · 🟡 Em progresso · ✅ Concluído

## Passos de implementação

1. ✅ Branch `task/T-102-prontuario-domain` a partir de `main` (pós-merge de T-101).
2. ✅ Modelo: `Consulta` + `StatusConsulta` + migração `V2`.
3. ✅ Serviços transacionais com a regra de imutabilidade pós-finalização.
4. ✅ REST + DTOs + Bean Validation + mapper 409.
5. ✅ Teste de fluxo cobrindo RF-006..010 e erros (404/409/400).
6. ✅ Build de produção verde (`-DskipTests`); testes Dev Services no CI.
7. ✅ Atualizar `state.md`; abrir o PR [#8](https://github.com/mclovin137/Horus/pull/8); nº preenchido aqui e no PRD.

## Verificação / testes

- [x] **Build de produção verde** (`./mvnw -DskipTests -pl services/prontuario package` → BUILD SUCCESS) sob JDK 25 + Quarkus 3.37.
- [ ] **`ProntuarioFlowTest`** (ciclo RF-006..010 + 404/409/400) roda no **CI** (Dev Services PostgreSQL).
- [x] Endpoints e regras alinhados a RF-006..010; persistência só em `prontuario_db` (RF-025).

## Registro de alterações

| Data | Arquivo | Mudança |
|---|---|---|
| 2026-06-27 | `services/prontuario/**` | Domínio do Prontuário: entidade `Consulta`+enum, migração `V2`, serviços, REST+DTOs+validação, mapper 409, teste de fluxo. |
| 2026-06-27 | `services/prontuario/pom.xml` | + `quarkus-hibernate-validator`. |
| 2026-06-27 | `docs/tasks/T-102/PRD.md`, `PLAN.md` | PRD e plano de execução. |
