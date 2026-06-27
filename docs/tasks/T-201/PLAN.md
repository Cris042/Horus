# Plano de Execução — T-201: Load Balancer de entrada (NGINX)

> ⚠️ **Regra R2 ([WORKFLOW](../../WORKFLOW.md)).** Sempre que **criar ou modificar** um arquivo desta tabela, atualize **na mesma hora** o seu `Status` e `Atualizado`, e registre em *Registro de alterações*.

| Campo | Valor |
|---|---|
| **Task** | `T-201` |
| **Branch** | `task/T-201-load-balancer` |
| **PRD** | [`./PRD.md`](./PRD.md) |
| **Progresso** | `4/4` arquivos |

## Arquivos a criar/modificar

| # | Arquivo | Ação | Propósito | Status | Atualizado |
|---|---|---|---|---|---|
| 1 | `deploy/lb/nginx.conf` | Criar | Config do LB (roteamento + traceparent passthrough + resolver) | ✅ Concluído | 2026-06-27 |
| 2 | `deploy/lb/README.md` | Criar | Papel, roteamento, observabilidade, ativação (T-801) | ✅ Concluído | 2026-06-27 |
| 3 | `docs/tasks/T-201/PRD.md` | Criar | PRD desta task | ✅ Concluído | 2026-06-27 |
| 4 | `docs/tasks/T-201/PLAN.md` | Criar | Este plano | ✅ Concluído | 2026-06-27 |

**Legenda:** ⬜ Pendente · 🟡 Em progresso · ✅ Concluído

## Passos de implementação

1. ✅ Branch `task/T-201-load-balancer` a partir de `main` (pós-merge de T-302).
2. ✅ Escrever `nginx.conf` (roteamento por prefixo, passthrough de `traceparent`, `resolver`+variável).
3. ✅ Documentar (`deploy/lb/README.md`): roteamento, observabilidade, ativação em T-801.
4. ✅ Validar sintaxe: `nginx -t` → **ok**.
5. ⬜ Atualizar `state.md`; abrir o PR `T-201: …`; preencher o nº do PR aqui e no PRD.

## Verificação / testes

- [x] **`nginx -t`** (`nginx:1.27-alpine`) → *syntax is ok / test is successful*.
- [x] Roteamento por prefixo cobre os 3 serviços + orquestrador; `traceparent` repassado (não encerra).
- [ ] Subida real do LB junto aos serviços → **T-801** (serviços containerizados).

## Registro de alterações

| Data | Arquivo | Mudança |
|---|---|---|
| 2026-06-27 | `deploy/lb/nginx.conf` | LB NGINX: roteamento por prefixo, passthrough de `traceparent`, `resolver`+variável no `proxy_pass`. |
| 2026-06-27 | `deploy/lb/README.md` | Doc do LB (papel, roteamento, observabilidade, ativação T-801). |
| 2026-06-27 | `docs/tasks/T-201/PRD.md`, `PLAN.md` | PRD e plano de execução. |
