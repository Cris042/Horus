# Plano de Execução — T-002: Bootstrap Quarkus do Horus

> ⚠️ **Regra R2 ([WORKFLOW](../../WORKFLOW.md)).** Sempre que **criar ou modificar** um arquivo desta tabela, atualize **na mesma hora** o seu `Status` e `Atualizado`, e registre em *Registro de alterações*.

| Campo | Valor |
|---|---|
| **Task** | `T-002` |
| **Branch** | `task/T-002-bootstrap-quarkus` |
| **PRD** | [`./PRD.md`](./PRD.md) |
| **Progresso** | `12/12` arquivos |

## Arquivos a criar/modificar

| # | Arquivo | Ação | Propósito | Status | Atualizado |
|---|---|---|---|---|---|
| 1 | `docs/tasks/T-002/PRD.md` | Criar | PRD desta task | ✅ Concluído | 2026-06-27 |
| 2 | `docs/tasks/T-002/PLAN.md` | Criar | Este plano | ✅ Concluído | 2026-06-27 |
| 3 | `pom.xml` | Modificar | Raiz → parent/aggregator (Java 25, BOM Quarkus, módulo `horus`) | ✅ Concluído | 2026-06-27 |
| 4 | `horus/pom.xml` | Criar | App Quarkus (REST + health), herda do parent | ✅ Concluído | 2026-06-27 |
| 5 | `horus/src/main/java/org/example/horus/api/HorusInfoResource.java` | Criar | Endpoint `GET /horus/info` | ✅ Concluído | 2026-06-27 |
| 6 | `horus/src/main/resources/application.properties` | Criar | Config base (nome, porta, health, log) | ✅ Concluído | 2026-06-27 |
| 7 | `horus/src/test/java/org/example/horus/api/HorusInfoResourceTest.java` | Criar | Teste de fumaça (endpoint + liveness) | ✅ Concluído | 2026-06-27 |
| 8 | `src/main/java/org/example/Main.java` | Remover | Placeholder substituído pelo bootstrap | ✅ Concluído | 2026-06-27 |
| 9 | `CLAUDE.md` | Modificar | Refletir novo build/estrutura (sem preview features) | ✅ Concluído | 2026-06-27 |
| 10 | `README.md` | Modificar | Mapa do repo + notas de scaffold/estado atualizados | ✅ Concluído | 2026-06-27 |
| 11 | `horus/README.md` | Modificar | Marcar bootstrap concluído | ✅ Concluído | 2026-06-27 |
| 12 | `lib.md` | Modificar | Nota Java 25 atualizada (preview features abandonadas) — *não previsto; add. via R2* | ✅ Concluído | 2026-06-27 |

**Legenda:** ⬜ Pendente · 🟡 Em progresso · ✅ Concluído

## Passos de implementação

1. ✅ Criar a branch `task/T-002-bootstrap-quarkus` a partir de `main`.
2. ✅ Criar PRD e este PLAN.
3. ✅ Reorganizar o build: `pom.xml` raiz → parent/aggregator; criar `horus/pom.xml` (app).
4. ✅ Criar as fontes do bootstrap (resource, `application.properties`, teste) e remover `Main.java`.
5. ✅ Atualizar `CLAUDE.md`, `README.md`, `horus/README.md`, `lib.md`.
6. ✅ Validar POMs (XML well-formed + estrutura) e marcar cada item ✅.
7. ⬜ Atualizar `state.md`; abrir o PR `T-002: bootstrap Quarkus do Horus`.

## Verificação / testes

- [x] Todos os `pom.xml` são XML bem-formado e estruturalmente válidos: root `org.example:horus-parent` (`packaging=pom`, módulo `horus`, BOM Quarkus 3.20.0, `release=25`); `horus` herda do parent (`../pom.xml`) com plugin Quarkus + extensões de bootstrap.
- [x] `git grep` não encontra mais referências a `org.example.Main` em código (apenas menções históricas em `ROADMAP.md`/`T-001`).
- [ ] Build real (`mvn -pl horus -am package` / `quarkus:dev`) — **não rodável neste ambiente** (sem `mvn` e sem rede para baixar o BOM); rodar em CI/dev com internet. Java 25 confirmado no ambiente (OpenJDK 25.0.3).

## Registro de alterações

| Data | Arquivo | Mudança |
|---|---|---|
| 2026-06-27 | `pom.xml` | Raiz convertida em parent/aggregator (`packaging=pom`): props comuns (Java 25, encoding), BOM Quarkus 3.20 em `dependencyManagement`, módulo `horus`. |
| 2026-06-27 | `horus/pom.xml` | Criado: app Quarkus herdando do parent; extensões `quarkus-rest`, `quarkus-rest-jackson`, `quarkus-smallrye-health`, `quarkus-arc` + testes; plugin Quarkus/compiler/surefire. |
| 2026-06-27 | `horus/src/.../HorusInfoResource.java` + `application.properties` + `HorusInfoResourceTest.java` | Bootstrap: endpoint `GET /horus/info`, config base e teste de fumaça. |
| 2026-06-27 | `src/main/java/org/example/Main.java` | Removido (placeholder substituído); `src/` raiz eliminado. |
| 2026-06-27 | `CLAUDE.md`, `README.md`, `horus/README.md`, `lib.md` | Docs alinhadas à nova estrutura/build (módulo `horus/`, sem preview features). |
