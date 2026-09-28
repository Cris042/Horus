# PRD — T-1007: RBAC com autenticação real

| Campo | Valor |
|---|---|
| **Task** | `T-1007` |
| **Fase do roadmap** | `Fase 10 — Produto utilizável` |
| **Status** | `Entregue` |
| **Branch** | `claude/beautiful-cray-sfblsr` |
| **PR** | — |
| **Depende de** | `T-704` |
| **Requisitos atendidos** | `RNF-H-010`, `RNF-017` |
| **ADRs relacionados** | `ADR-0002` (domínio segue sem auth) |
| **Data** | `2026-09-28` |

## Objetivo

O RBAC da T-704 confiava no cabeçalho `X-Horus-Role` — qualquer cliente se declarava
`PLATFORM_ADMIN` — e vinha desligado por padrão. Esta task liga a autenticação real (JWT do IdP)
e o escopo por serviço do Desenvolvedor.

## Escopo (o que entra)

- `quarkus-smallrye-jwt`: verificação de assinatura/emissor do token (`mp.jwt.verify.*` — JWKS do
  IdP OIDC ou PEM). Papéis da claim `groups`; vale o de maior privilégio.
- `horus.rbac.mode=jwt|header` (header só dev/testes); **`%prod.horus.rbac.enabled=true`**.
- Escopo do `DEVELOPER` pela claim `horus_services` (`CallerScope`): busca de traces restrita
  (serviço fora do escopo → 403; sem `service` e um único permitido → assume) e lista de serviços
  filtrada.
- Painel e waterfall: campo de token (guardado só na aba, `sessionStorage`) enviado como Bearer.
- Compose de dev desliga o RBAC explicitamente; job `ai-live` idem; K8s documenta o IdP.
- `docs/ROLES.md` §7.

## Fora do escopo

- Login interativo (fluxo authorization-code) no painel — o token vem do IdP/CLI.
- Filtro por serviço nas consultas por `traceId` conhecido (documentado como limitação).

## Critérios de aceite

- [x] Sem token → 401; rotas públicas abertas.
- [x] Token forjado (`alg: none`) ou sem papel do Horus → 401; cabeçalho `X-Horus-Role` ignorado no modo JWT.
- [x] Matriz de permissões aplicada (Auditor lê telemetria, não configura cache).
- [x] Maior privilégio vence entre vários grupos.
- [x] DEVELOPER com `horus_services` só vê/busca seus serviços.
- [x] Perfil prod liga o RBAC; `HORUS_RBAC_ENABLED=false` (env) desliga (verificado no jar empacotado).
- [x] Par RSA dos testes gerado em runtime (nenhuma chave no repositório).
- [x] `./mvnw -pl horus test` verde (118 testes).

## Riscos

| Risco | Mitigação |
|---|---|
| K8s sem IdP configurado → tudo 401 | Seguro por padrão; `config.yaml` documenta as variáveis |
| Token no navegador | `sessionStorage` (só a aba), campo tipo senha |

## Referências

- [`../../ROLES.md`](../../ROLES.md) §7 · [`../T-704/PRD.md`](../T-704/PRD.md) · [`./PLAN.md`](./PLAN.md)
