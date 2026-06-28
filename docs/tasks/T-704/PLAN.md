# Plano de Execução — T-704: RBAC do Horus

> ⚠️ **Regra R2 ([WORKFLOW](../../WORKFLOW.md)).** Sempre que **criar ou modificar** um arquivo desta tabela, atualize **na mesma hora** o seu `Status` e `Atualizado`, e registre a mudança em *Registro de alterações*. Arquivo não previsto que precise ser tocado deve ser **adicionado** à tabela.

| Campo | Valor |
|---|---|
| **Task** | `T-704` |
| **Branch** | `task/T-704-horus-rbac` |
| **PRD** | [`./PRD.md`](./PRD.md) |
| **Progresso** | `6/6` arquivos |

## Arquivos a criar/modificar

| # | Arquivo | Ação | Propósito | Status | Atualizado |
|---|---|---|---|---|---|
| 1 | `horus/src/main/java/org/example/horus/security/Capability.java` | Criar | Enum das capacidades protegidas (linhas da matriz) | ✅ Concluído | 2026-06-28 |
| 2 | `horus/src/main/java/org/example/horus/security/HorusRole.java` | Criar | Enum de papéis + matriz papel→capacidade + parsing | ✅ Concluído | 2026-06-28 |
| 3 | `horus/src/main/java/org/example/horus/security/RequiredCapability.java` | Criar | Mapa caminho→capacidade (públicos isentos) | ✅ Concluído | 2026-06-28 |
| 4 | `horus/src/main/java/org/example/horus/security/HorusRbacFilter.java` | Criar | `ContainerRequestFilter` que autoriza por papel (off por default) | ✅ Concluído | 2026-06-28 |
| 5 | `horus/src/main/resources/application.properties` | Modificar | Flag `horus.rbac.enabled=false` + doc dos papéis | ✅ Concluído | 2026-06-28 |
| 6 | `horus/src/test/java/org/example/horus/security/HorusRbacFilterTest.java` | Criar | Teste com perfil RBAC ligado (público/401/403/200) | ✅ Concluído | 2026-06-28 |

**Legenda:** ⬜ Pendente · 🟡 Em progresso · ✅ Concluído

## Passos de implementação

1. Modelar `Capability` e `HorusRole` (com a matriz de `ROLES.md` §4) e o parsing tolerante do papel.
2. `RequiredCapability.forPath` mapeia grupos de endpoint à capacidade; públicos (`/q/health`, `/horus/info`) isentos.
3. `HorusRbacFilter` (`@Provider @PreMatching`) lê `X-Horus-Role`, nega 401 (sem/ inválido) ou 403 (sem capacidade); no-op quando `horus.rbac.enabled=false`.
4. Flag + doc em `application.properties`; teste com `QuarkusTestProfile` ligando o RBAC.

## Verificação / testes

- [x] `mvn -pl horus test` verde (32/32, 6 novos em `HorusRbacFilterTest`).

## Registro de alterações

| Data | Arquivo | Mudança |
|---|---|---|
| `2026-06-28` | `Capability.java`, `HorusRole.java` | Modelo de papéis e capacidades (matriz ROLES.md) |
| `2026-06-28` | `RequiredCapability.java` | Mapa caminho→capacidade com públicos isentos |
| `2026-06-28` | `HorusRbacFilter.java` | Filtro de autorização por papel (off por default) |
| `2026-06-28` | `application.properties` | Flag `horus.rbac.enabled` + doc dos papéis |
| `2026-06-28` | `HorusRbacFilterTest.java` | Testes de autorização com RBAC ligado |
