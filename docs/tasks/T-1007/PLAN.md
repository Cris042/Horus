# Plano de Execução — T-1007: RBAC com autenticação real

> ⚠️ **Regra R2 ([WORKFLOW](../../WORKFLOW.md)).** Sempre que **criar ou modificar** um arquivo desta tabela, atualize **na mesma hora** o seu `Status` e `Atualizado`, e registre a mudança em *Registro de alterações*. Arquivo não previsto que precise ser tocado deve ser **adicionado** à tabela.

| Campo | Valor |
|---|---|
| **Task** | `T-1007` |
| **Branch** | `claude/beautiful-cray-sfblsr` |
| **PRD** | [`./PRD.md`](./PRD.md) |
| **Progresso** | `17/17` arquivos |

## Arquivos a criar/modificar

| # | Arquivo | Ação | Propósito | Status | Atualizado |
|---|---|---|---|---|---|
| 1 | `horus/pom.xml` | Modificar | `quarkus-smallrye-jwt` (+ `-build` em teste) | ✅ Concluído | 2026-09-28 |
| 2 | `horus/src/main/java/org/example/horus/security/HorusRbacFilter.java` | Modificar | Modos jwt/header; papel do token; escopo | ✅ Concluído | 2026-09-28 |
| 3 | `horus/src/main/java/org/example/horus/security/CallerScope.java` | Criar | Escopo por serviço do DEVELOPER | ✅ Concluído | 2026-09-28 |
| 4 | `horus/src/main/java/org/example/horus/security/HorusRole.java` | Modificar | `privilege()`; Javadoc | ✅ Concluído | 2026-09-28 |
| 5 | `horus/src/main/java/org/example/horus/api/HorusTraceSearchResource.java` | Modificar | Busca/serviços respeitam o escopo | ✅ Concluído | 2026-09-28 |
| 6 | `horus/src/main/resources/application.properties` | Modificar | `%prod` liga RBAC; modo jwt; docs | ✅ Concluído | 2026-09-28 |
| 7 | `horus/src/main/resources/META-INF/resources/horus-panel.html` | Modificar | Campo de token + Bearer | ✅ Concluído | 2026-09-28 |
| 8 | `horus/src/main/resources/META-INF/resources/horus-waterfall.html` | Modificar | Bearer | ✅ Concluído | 2026-09-28 |
| 9 | `horus/src/test/java/org/example/horus/security/HorusJwtRbacTest.java` | Criar | JWT real (par RSA gerado no teste) | ✅ Concluído | 2026-09-28 |
| 10 | `horus/src/test/java/org/example/horus/security/HorusRbacFilterTest.java` | Modificar | Modo header explícito | ✅ Concluído | 2026-09-28 |
| 11 | `deploy/docker-compose.yml` | Modificar | Dev desliga RBAC | ✅ Concluído | 2026-09-28 |
| 12 | `deploy/k8s/config.yaml` | Modificar | Variáveis do IdP | ✅ Concluído | 2026-09-28 |
| 13 | `.github/workflows/ci.yml` | Modificar | `ai-live` sem RBAC | ✅ Concluído | 2026-09-28 |
| 14 | `docs/ROLES.md` | Modificar | §7 | ✅ Concluído | 2026-09-28 |
| 15 | `docs/tasks/T-1007/PRD.md` | Criar | PRD | ✅ Concluído | 2026-09-28 |
| 16 | `docs/tasks/T-1007/PLAN.md` | Criar | Este plano | ✅ Concluído | 2026-09-28 |
| 17 | `state.md` | Modificar | R1 | ✅ Concluído | 2026-09-28 |

**Legenda:** ⬜ Pendente · 🟡 Em progresso · ✅ Concluído

## Verificação / testes

- [x] `./mvnw -pl horus test` (JDK 25) → **118 testes, 0 falhas**.
- [x] Jar empacotado (perfil prod): sem env → rota protegida 401; `HORUS_RBAC_ENABLED=false` → RBAC desligado.

## Registro de alterações

| Data | Arquivo | Mudança |
|---|---|---|
| `2026-09-28` | (todos acima) | RBAC com JWT + escopo por serviço (T-1007) |
