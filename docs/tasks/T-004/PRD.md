# PRD — T-004: CI de build/test por componente + Maven Wrapper

| Campo | Valor |
|---|---|
| **Task** | `T-004` |
| **Fase do roadmap** | Fase 0 — Fundação |
| **Status** | `Entregue` |
| **Branch** | `task/T-004-ci-build` |
| **PR** | [#4](https://github.com/mclovin137/Horus/pull/4) |
| **Depende de** | `T-002` |
| **Requisitos atendidos** | — (valida indiretamente RNF-014: o Horus compila) |
| **ADRs relacionados** | — |
| **Data** | 2026-06-27 |

## Objetivo

Estabelecer a **integração contínua** do monorepo: um pipeline que **compila, testa e faz lint** de cada componente a cada push em `main` e a cada PR, gateando o fluxo `task → branch → PR`. Como parte disso, adicionar o **Maven Wrapper** (`./mvnw`) para um build reproduzível sem depender de um `mvn` instalado no host. Esta task **fecha a lacuna de T-002**, que não pôde ser buildado localmente: o build agora roda em pipeline (e foi validado localmente neste ciclo).

## Escopo (o que entra)

- **Maven Wrapper** commitado: `mvnw`, `mvnw.cmd` e `.mvn/wrapper/maven-wrapper.properties`, fixado em **Maven 3.9.9** (variante `only-script` do plugin `maven-wrapper-plugin:3.3.2` — sem jar binário no repo).
- **Workflow `.github/workflows/ci.yml`** (job `build-java`):
  - dispara em `push` para `main`, em todo `pull_request` e via `workflow_dispatch`;
  - `actions/setup-java` (Temurin **25**) + cache Maven;
  - roda `./mvnw -B -ntp verify` (compila + testes do módulo `horus`) com flags de warning fazendo o **lint leve**;
  - publica os relatórios de teste (surefire) como artefato.
- **Docs alinhadas:** `CLAUDE.md` (build via `./mvnw`), `lib.md` (wrapper adicionado), `README.md` (badge de CI).

## Fora do escopo

- Jobs de CI para **Rust** (`worker/`) e **Python** (`loadtest/`): hoje são apenas scaffolding (sem código compilável) → entram com **T-303** e **T-203**.
- **Análise estática pesada** (Spotless/Checkstyle/SpotBugs): fast-follow deliberado — não vale fixar um padrão de formatação sobre um bootstrap de 3 arquivos, e formatadores Java têm fricção com os internals do JDK 25 (acesso a `jdk.compiler`). O lint desta task limita-se a **warnings de compilação** (deprecation/unchecked), não-falhantes.
- Build/publicação de **imagem Docker** → T-801.
- Configuração de **branch protection / required checks** no GitHub → ação do mantenedor (não versionável no repo).

## Premissas e dependências

- T-002 entregue (parent/aggregator + módulo `horus`).
- O runner `ubuntu-latest` tem rede para baixar Maven 3.9.9 (via wrapper) e as dependências do Quarkus.
- **Java 25 é GA/LTS** e disponível no `setup-java` (distro Temurin).

## Critérios de aceite

- [ ] Maven Wrapper commitado (`mvnw` com bit de execução, `mvnw.cmd`, `.mvn/wrapper/maven-wrapper.properties`) fixando Maven 3.9.9.
- [ ] `.github/workflows/ci.yml` roda em `push`/`pull_request`/`dispatch` e executa `./mvnw … verify` no módulo `horus`.
- [ ] O build compila e os testes passam (validado localmente: **Java 25.0.3 + Quarkus 3.20.0 → BUILD SUCCESS**, `Tests run: 2, Failures: 0`) — confirma T-002 e o par JDK 25/Quarkus 3.20 (fallback Java 21 **não** necessário).
- [ ] Lint (warnings de compilação) habilitado no passo de CI.
- [ ] `CLAUDE.md`, `lib.md` e `README.md` refletem o wrapper/CI.
- [ ] `PLAN.md` 100% ✅ e `state.md` atualizado.

## Riscos

| Risco | Mitigação |
|---|---|
| `setup-java` não encontrar Temurin 25 | JDK 25 é GA/LTS; alternativa imediata é trocar `distribution` para `corretto`/`zulu` (Corretto 25 é o usado localmente). |
| Bit de execução do `mvnw` se perder no commit | Conferir `git ls-files -s mvnw` = modo `100755`; `git update-index --chmod=+x` se necessário. |
| Re-download do Maven do wrapper a cada run (o cache do `setup-java` cobre `~/.m2/repository`, não `~/.m2/wrapper/dists`) | Custo pequeno (~10 MB); cachear `~/.m2/wrapper/dists` é um follow-up opcional. |

## Referências

- [`../../PRD.md`](../../PRD.md), [`../../ROADMAP.md`](../../ROADMAP.md), [`../../../lib.md`](../../../lib.md)
- [`./PLAN.md`](./PLAN.md) — plano de execução desta task
- [`../T-002/PRD.md`](../T-002/PRD.md) — bootstrap validado por esta CI
