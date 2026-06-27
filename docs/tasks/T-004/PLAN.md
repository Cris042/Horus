# Plano de Execução — T-004: CI de build/test + Maven Wrapper

> ⚠️ **Regra R2 ([WORKFLOW](../../WORKFLOW.md)).** Sempre que **criar ou modificar** um arquivo desta tabela, atualize **na mesma hora** o seu `Status` e `Atualizado`, e registre em *Registro de alterações*.

| Campo | Valor |
|---|---|
| **Task** | `T-004` |
| **Branch** | `task/T-004-ci-build` |
| **PRD** | [`./PRD.md`](./PRD.md) |
| **Progresso** | `10/10` arquivos |

## Arquivos a criar/modificar

| # | Arquivo | Ação | Propósito | Status | Atualizado |
|---|---|---|---|---|---|
| 1 | `docs/tasks/T-004/PRD.md` | Criar | PRD desta task | ✅ Concluído | 2026-06-27 |
| 2 | `docs/tasks/T-004/PLAN.md` | Criar | Este plano | ✅ Concluído | 2026-06-27 |
| 3 | `.github/workflows/ci.yml` | Criar | Pipeline build/test/lint (job `build-java`) | ✅ Concluído | 2026-06-27 |
| 4 | `mvnw` | Criar | Maven Wrapper — script Unix (gerado, `only-script`) | ✅ Concluído | 2026-06-27 |
| 5 | `mvnw.cmd` | Criar | Maven Wrapper — script Windows | ✅ Concluído | 2026-06-27 |
| 6 | `.mvn/wrapper/maven-wrapper.properties` | Criar | Fixa Maven 3.9.9 (download do Central) | ✅ Concluído | 2026-06-27 |
| 7 | `CLAUDE.md` | Modificar | Build via `./mvnw` (wrapper commitado; CI valida o build) | ✅ Concluído | 2026-06-27 |
| 8 | `lib.md` | Modificar | Wrapper adicionado (3.9.9, `only-script`); nota de validação JDK 25 | ✅ Concluído | 2026-06-27 |
| 9 | `README.md` | Modificar | Badge de CI no topo | ✅ Concluído | 2026-06-27 |
| 10 | `state.md` | Modificar | R1 — registrar entrega de T-004 | ✅ Concluído | 2026-06-27 |

**Legenda:** ⬜ Pendente · 🟡 Em progresso · ✅ Concluído

## Passos de implementação

1. ✅ Criar a branch `task/T-004-ci-build` a partir de `main`.
2. ✅ Gerar o Maven Wrapper (`maven-wrapper-plugin:3.3.2:wrapper -Dmaven=3.9.9`) → `mvnw`, `mvnw.cmd`, `.mvn/wrapper/maven-wrapper.properties` (variante `only-script`).
3. ✅ **Validar o build localmente** com o wrapper: `./mvnw -B -ntp clean verify` → **BUILD SUCCESS** em Java 25.0.3 + Quarkus 3.20.0; `Tests run: 2, Failures: 0`.
4. ✅ Criar `.github/workflows/ci.yml` (setup-java 25 + cache + `verify` + upload de relatórios).
5. ✅ Criar PRD e este PLAN.
6. ✅ Atualizar `CLAUDE.md`, `lib.md`, `README.md`.
7. ✅ Re-rodar o build com o comando exato da CI (flags de lint) para confirmar paridade.
8. ⬜ Atualizar `state.md`; abrir o PR `T-004: CI de build/test + Maven Wrapper`; preencher o nº do PR aqui e no PRD.

## Verificação / testes

- [x] `./mvnw -version` bootstrapa Maven 3.9.9 com Java 25 (download `only-script` a partir de `repo.maven.apache.org`).
- [x] `./mvnw -B -ntp clean verify` → **BUILD SUCCESS** (módulo `horus`), Quarkus sobe com features `[cdi, rest, rest-jackson, smallrye-health, vertx]`, jar gerado e augmentation concluída.
- [x] Comando exato da CI (`verify` + `-Dmaven.compiler.showWarnings=true -Dmaven.compiler.showDeprecation=true`) roda verde localmente.
- [ ] Run verde do workflow `CI` no GitHub Actions ao abrir o PR (confirmação final em pipeline).

> Nota de ambiente: a 1ª tentativa de build falhou por `AccessDeniedException` em `~/.m2/repository/io/quarkus/...` (diretórios `root:root` de um run anterior) — artefato local, **não** do código. Reexecutar com `-Dmaven.repo.local` num diretório próprio resolveu. Irrelevante para a CI (runner limpo).

## Registro de alterações

| Data | Arquivo | Mudança |
|---|---|---|
| 2026-06-27 | `mvnw`, `mvnw.cmd`, `.mvn/wrapper/maven-wrapper.properties` | Maven Wrapper gerado (plugin 3.3.2, `only-script`, Maven 3.9.9). |
| 2026-06-27 | `.github/workflows/ci.yml` | Pipeline de CI: `build-java` (setup-java 25 + cache Maven + `./mvnw … verify` + upload surefire). |
| 2026-06-27 | `docs/tasks/T-004/PRD.md`, `docs/tasks/T-004/PLAN.md` | PRD e plano de execução da task. |
| 2026-06-27 | `CLAUDE.md` | Seção *Build & Run*: preferir `./mvnw`; wrapper commitado; CI valida o build (JDK 25/Quarkus 3.20 confirmado). |
| 2026-06-27 | `lib.md` | Maven Wrapper marcado como adicionado (3.9.9, `only-script`); nota de validação do JDK 25. |
| 2026-06-27 | `README.md` | Badge do workflow `CI` no topo. |
| 2026-06-27 | `state.md` | R1 — entrega de T-004 (próxima ação: T-003/T-005). |
