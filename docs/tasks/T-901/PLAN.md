# Plano de Execução — T-901: TLS onde exigido; revisão de segredos/credenciais

> ⚠️ **Regra R2 ([WORKFLOW](../../WORKFLOW.md)).** Sempre que **criar ou modificar** um arquivo desta tabela, atualize **na mesma hora** o seu `Status` e `Atualizado`, e registre a mudança em *Registro de alterações*. Arquivo não previsto que precise ser tocado deve ser **adicionado** à tabela.

| Campo | Valor |
|---|---|
| **Task** | `T-901` |
| **Branch** | `task/T-901-tls-secrets-review` |
| **PRD** | [`./PRD.md`](./PRD.md) |
| **Progresso** | `9/9` arquivos |

## Arquivos a criar/modificar

| # | Arquivo | Ação | Propósito | Status | Atualizado |
|---|---|---|---|---|---|
| 1 | `docs/tasks/T-901/PRD.md` | Criar | PRD + auditoria de credenciais | ✅ Concluído | 2026-07-02 |
| 2 | `docs/tasks/T-901/PLAN.md` | Criar | Plano arquivo por arquivo | ✅ Concluído | 2026-07-02 |
| 3 | `deploy/lb/locations.conf` | Criar | Snippet de roteamento compartilhado entre `nginx.conf` e `nginx.tls.conf` | ✅ Concluído | 2026-07-02 |
| 4 | `deploy/lb/nginx.conf` | Modificar | Passa a `include` o snippet compartilhado (sem mudar comportamento default HTTP) | ✅ Concluído | 2026-07-02 |
| 5 | `deploy/lb/nginx.tls.conf` | Criar | Variante **opt-in** com `server` HTTPS (443) + redirect de 80→443, mesmo roteamento | ✅ Concluído | 2026-07-02 |
| 6 | `deploy/lb/README.md` | Modificar | Documenta TLS (cert autoassinado dev / real prod, como trocar de conf) | ✅ Concluído | 2026-07-02 |
| 7 | `deploy/k8s/apps/ingress.yaml` | Criar | `Ingress` com `tls:` (Secret `horus-tls-secret`), roteamento por prefixo | ✅ Concluído | 2026-07-02 |
| 8 | `deploy/k8s/kustomization.yaml` | Modificar | Inclui `apps/ingress.yaml` nos `resources` | ✅ Concluído | 2026-07-02 |
| 9 | `deploy/k8s/README.md` | Modificar | Documenta Ingress+TLS e a auditoria de credenciais | ✅ Concluído | 2026-07-02 |

**Legenda:** ⬜ Pendente · 🟡 Em progresso · ✅ Concluído

> `state.md` atualizado ao final da entrega.

## Passos de implementação

1. Extrair as `location` do NGINX de compose para um snippet `locations.conf` reaproveitável.
2. Adicionar `server { listen 443 ssl; ... include locations.conf; }` ao `nginx.conf`,
   condicional à presença dos arquivos de cert (documentado, não bloqueia `nginx -t` mesmo sem
   cert montado — usa caminho fixo esperado).
3. Criar `deploy/k8s/apps/ingress.yaml` espelhando os prefixos do NGINX.
4. Incluir o Ingress no `kustomization.yaml`.
5. Documentar em ambos os READMEs (`deploy/lb/README.md`, `deploy/k8s/README.md`).
6. Validar YAML (parser) e `nginx -t`.
7. Atualizar `state.md` (R1).

## Verificação / testes

- [x] `nginx -t` com `nginx.conf` (HTTP) e `nginx.tls.conf` (HTTPS, cert autoassinado de teste) —
      ambos válidos em container `nginx:1.27-alpine`.
- [x] Todos os YAML de `deploy/k8s/` parseiam (`yaml.safe_load_all`), incluindo `ingress.yaml`.
- [ ] `kustomize build` real — sem `kubectl`/`kustomize` disponíveis nesta sessão; inspeção
      manual do `kustomization.yaml` feita. Fica para T-405 (validação ponta a ponta).

## Registro de alterações

| Data | Arquivo | Mudança |
|---|---|---|
| `2026-07-02` | `docs/tasks/T-901/*` | PRD (com auditoria de credenciais) e plano criados |
| `2026-07-02` | `deploy/lb/locations.conf` | Roteamento por prefixo extraído do `nginx.conf` para snippet compartilhado |
| `2026-07-02` | `deploy/lb/nginx.conf` | Passa a `include locations.conf` (comportamento HTTP inalterado; validado `nginx -t`) |
| `2026-07-02` | `deploy/lb/nginx.tls.conf` | Variante opt-in com HTTPS (443) + redirect 80→443; validado `nginx -t` com cert de teste |
| `2026-07-02` | `deploy/lb/README.md` | Documenta como habilitar TLS na borda (compose) |
| `2026-07-02` | `deploy/k8s/apps/ingress.yaml` | `Ingress` com TLS opcional, roteamento espelhando o LB de compose |
| `2026-07-02` | `deploy/k8s/kustomization.yaml` | Inclui `apps/ingress.yaml` |
| `2026-07-02` | `deploy/k8s/README.md` | Documenta Ingress+TLS e a auditoria de credenciais/segredos |
