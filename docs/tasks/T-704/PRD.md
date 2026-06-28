# PRD — T-704: RBAC do Horus

| Campo | Valor |
|---|---|
| **Task** | `T-704` |
| **Fase do roadmap** | `Fase 7 — Painel / Experiência` |
| **Status** | `Entregue` |
| **Branch** | `task/T-704-horus-rbac` |
| **PR** | [#29](https://github.com/mclovin137/Horus/pull/29) |
| **Depende de** | `T-701` (painel) |
| **Requisitos atendidos** | `RNF-H-010` |
| **ADRs relacionados** | `ADR-0002` (sem auth no domínio), `ADR-0008` |
| **Data** | `2026-06-28` |

## Objetivo

Aplicar **controle de acesso por papel (RBAC)** à plataforma Horus (RNF-H-010), conforme a
matriz de [`docs/ROLES.md`](../../ROLES.md): cada papel só exerce as capacidades previstas
sobre painel, telemetria e IA. Os serviços de domínio permanecem **sem autenticação** (ADR-0002).

## Escopo (o que entra)

- Modelo de RBAC: `HorusRole` (PLATFORM_ADMIN, SRE, DEVELOPER, AUDITOR, LOAD_TEST_OP) e
  `Capability` (VIEW_TELEMETRY, AI_INSIGHTS, CONFIGURE_PLATFORM, MANAGE_USERS) com a matriz papel→capacidade.
- `RequiredCapability` — mapa caminho→capacidade (públicos: `/q/health`, `/horus/info`).
- `HorusRbacFilter` — `ContainerRequestFilter` que autoriza por papel; papel via cabeçalho
  `X-Horus-Role`; desligado por padrão (`horus.rbac.enabled=false`).
- Teste `@QuarkusTest` com perfil que liga o RBAC (público sem papel; 401 sem/ inválido; 200/403 por capacidade).

## Fora do escopo

- Autenticação real (OIDC/JWT, login, gestão de usuários) — o cabeçalho `X-Horus-Role` é o ponto de extensão.
- Escopo por serviço do papel `DEVELOPER` (🟡 "somente seus serviços") — granularidade fina fica para fatia posterior.
- RBAC nos serviços de domínio (não se aplica — ADR-0002).

## Premissas e dependências

- Painel/endpoints da plataforma (T-701, T-501, T-6xx) já existem e são os recursos protegidos.
- Desligado por padrão para não afetar build/CI nem dev local.

## Critérios de aceite

- [x] Com RBAC ligado, endpoint público responde sem papel; protegido sem papel → 401.
- [x] Papel inválido → 401; papel sem a capacidade → 403; papel com a capacidade → 200.
- [x] Com RBAC desligado (default), todos os endpoints respondem como antes.
- [x] `mvn -pl horus test` verde.

## Riscos

| Risco | Mitigação |
|---|---|
| Cabeçalho de papel é falsificável (sem auth real) | Explícito como 1ª fatia; OIDC/JWT entra depois. Off por padrão. |
| Novo endpoint sem mapeamento de capacidade | Fallback: qualquer `/horus/*` exige no mínimo `VIEW_TELEMETRY`. |

## Referências

- [`../../ROLES.md`](../../ROLES.md) — papéis e matriz de permissões (§1.1, §4, §6)
- [`./PLAN.md`](./PLAN.md) — plano de execução desta task
- `ADR-0002` (sem API Gateway/auth no domínio), `ADR-0008` (Horus)
