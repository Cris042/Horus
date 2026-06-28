# PRD — T-002: Bootstrap Quarkus do Horus

| Campo | Valor |
|---|---|
| **Task** | `T-002` |
| **Fase do roadmap** | Fase 0 — Fundação |
| **Status** | `Entregue` |
| **Branch** | `task/T-002-bootstrap-quarkus` |
| **PR** | [`#3`](https://github.com/mclovin137/Horus/pull/3) |
| **Depende de** | `T-001` |
| **Requisitos atendidos** | RNF-014 |
| **ADRs relacionados** | ADR-0008, ADR-0011 |
| **Data** | 2026-06-27 |

## Objetivo

Substituir o scaffold "Hello World" (`src/main/java/org/example/Main.java`) por um **bootstrap Quarkus** real do Horus e reorganizar o build do monorepo: o `pom.xml` raiz vira **parent/agregador** e o build do Horus passa para o módulo `horus/`. Entrega a base compilável sobre a qual as Fases 5-7 do Horus serão construídas (RNF-014).

## Escopo (o que entra)

- `pom.xml` raiz como **parent + aggregator** (`packaging=pom`): propriedades comuns (Java 25, encoding), BOM do Quarkus 3.20 em `dependencyManagement`, e `horus` como `module`.
- `horus/pom.xml` — app Quarkus herdando do parent, com extensões **mínimas** de bootstrap: `quarkus-rest`, `quarkus-rest-jackson`, `quarkus-smallrye-health`, `quarkus-arc` (+ `quarkus-junit5`/`rest-assured` em teste).
- Código mínimo: `HorusInfoResource` (`GET /horus/info`) e `application.properties`; health automático em `/q/health` via SmallRye Health.
- Teste de fumaça (`HorusInfoResourceTest`) para o endpoint e o liveness.
- Remoção do `Main.java` placeholder.
- Atualização de `CLAUDE.md`, `README.md` e `horus/README.md` para refletir a nova estrutura/build.

## Fora do escopo

- Extensões de OTel, LangChain4j (Claude), Postgres/Flyway, mailer, websockets → tasks das Fases 4-7 (T-401, T-501, T-601, ...).
- Imagem Docker / Jib → T-801.
- Maven Wrapper (`mvnw`) → follow-up (ver Riscos).
- Qualquer lógica de domínio.

## Premissas e dependências

- T-001 entregue (estrutura de monorepo com `horus/`).
- Java 25 disponível (confirmado: OpenJDK 25 no ambiente). Maven 3.9+ necessário para o build real.
- **Decisão:** o app Quarkus usa **Java 25 padrão (sem preview features)**. As features de preview do placeholder (`main` de instância, `IO.println`) são abandonadas — o Quarkus gerencia o ponto de entrada; manter `--enable-preview` no build seria custo sem benefício.

## Critérios de aceite

- [ ] `pom.xml` raiz é parent/aggregator (`packaging=pom`) com o módulo `horus` e o BOM do Quarkus gerenciado.
- [ ] `horus/pom.xml` é um app Quarkus válido (plugin Quarkus + extensões de bootstrap), herdando do parent.
- [ ] Existe ao menos um endpoint REST (`/horus/info`) e health em `/q/health`.
- [ ] `Main.java` placeholder removido; sem referências remanescentes a ele.
- [ ] `CLAUDE.md` reflete o novo build (módulo `horus/`, sem preview features, comandos Maven/Quarkus).
- [ ] `PLAN.md` 100% ✅ e `state.md` atualizado.

## Riscos

| Risco | Mitigação |
|---|---|
| Quarkus 3.20 pode não suportar oficialmente o JDK 25 | `lib.md` registra **Java 21 (LTS)** como fallback; trocar `maven.compiler.release` se o build acusar incompatibilidade. Decisão isolada no parent (um único ponto de mudança). |
| Build não verificável neste ambiente (sem `mvn` e sem rede para baixar o BOM) | POMs validados por well-formedness XML + revisão estrutural; build real (`mvn package` / `quarkus:dev`) a rodar pelo dev/CI com rede. Versões fixadas conforme `lib.md`. |
| Ausência de Maven Wrapper reduz reprodutibilidade | Follow-up: adicionar `mvnw` (`mvn wrapper:wrapper`) quando houver rede; `lib.md` já registra a recomendação. |

## Referências

- [`../../PRD.md`](../../PRD.md), [`../../ROADMAP.md`](../../ROADMAP.md), [`../../../lib.md`](../../../lib.md)
- [`./PLAN.md`](./PLAN.md)
- ADR-0008 (Horus supera "somente Jaeger"), ADR-0011 (IA com Claude/LangChain4j)
