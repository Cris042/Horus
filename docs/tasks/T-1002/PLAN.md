# Plano de Execução — T-1002: Compose com as aplicações + e2e no CI

> ⚠️ **Regra R2 ([WORKFLOW](../../WORKFLOW.md)).** Sempre que **criar ou modificar** um arquivo desta tabela, atualize **na mesma hora** o seu `Status` e `Atualizado`, e registre a mudança em *Registro de alterações*. Arquivo não previsto que precise ser tocado deve ser **adicionado** à tabela.

| Campo | Valor |
|---|---|
| **Task** | `T-1002` |
| **Branch** | `claude/beautiful-cray-sfblsr` |
| **PRD** | [`./PRD.md`](./PRD.md) |
| **Progresso** | `12/12` arquivos |

## Arquivos a criar/modificar

| # | Arquivo | Ação | Propósito | Status | Atualizado |
|---|---|---|---|---|---|
| 1 | `deploy/docker-compose.yml` | Modificar | Profile `apps`: serviços, SAGA, Horus, worker, loadtest, LB; imagem Quarkus inline; healthchecks | ✅ Concluído | 2026-09-28 |
| 2 | `Makefile` | Modificar | `package`, `up-apps`, `down-apps`, `e2e`; `clean` inclui o profile | ✅ Concluído | 2026-09-28 |
| 3 | `scripts/e2e.sh` | Criar | E2E contra o stack real (SAGA feliz/compensada + APIs do Horus) | ✅ Concluído | 2026-09-28 |
| 4 | `.github/workflows/ci.yml` | Modificar | Job `e2e` | ✅ Concluído | 2026-09-28 |
| 5 | `deploy/README.md` | Modificar | Documentação do profile `apps` e do e2e | ✅ Concluído | 2026-09-28 |
| 6 | `deploy/k8s/secrets.example.yaml` | Modificar | Não definir `ANTHROPIC_API_KEY` vazia (derrubava o Horus) | ✅ Concluído | 2026-09-28 |
| 7 | `services/saga-orchestrator/src/main/java/org/example/saga/service/SagaService.java` | Modificar | Checagem `FALHA` dentro do span do passo (span em ERROR) | ✅ Concluído | 2026-09-28 |
| 8 | `docs/tasks/T-1001/PRD.md` | Modificar | Premissa de validação atualizada (JDK 25 + e2e) | ✅ Concluído | 2026-09-28 |
| 9 | `docs/tasks/T-1001/PLAN.md` | Modificar | Verificação ao vivo registrada | ✅ Concluído | 2026-09-28 |
| 10 | `docs/tasks/T-1002/PRD.md` | Criar | PRD | ✅ Concluído | 2026-09-28 |
| 11 | `docs/tasks/T-1002/PLAN.md` | Criar | Este plano | ✅ Concluído | 2026-09-28 |
| 12 | `state.md` | Modificar | R1 | ✅ Concluído | 2026-09-28 |

**Legenda:** ⬜ Pendente · 🟡 Em progresso · ✅ Concluído

## Passos de implementação

1. Profile `apps` no compose (nomes de serviço = hosts do profile `prod` e upstreams do NGINX).
2. Alvos do Makefile e `scripts/e2e.sh`.
3. Subir o stack real, rodar o e2e, corrigir o que falhar (2 defeitos reais encontrados).
4. Job `e2e` no CI.

## Verificação / testes

- [x] `docker compose --profile apps config` válido; `make up` (sem profile) inalterado.
- [x] Stack real no ar (16 contêineres saudáveis) e `scripts/e2e.sh` → **todas as verificações passaram**
  (1ª rodada: Horus não subia; 2ª: busca por erro falhava — ambos corrigidos).
- [x] `./mvnw -pl services/saga-orchestrator test` (JDK 25) → verde.
- [ ] Job `e2e` no GitHub Actions — roda no próximo PR/push em `main`.

## Registro de alterações

| Data | Arquivo | Mudança |
|---|---|---|
| `2026-09-28` | (todos acima) | Compose com as aplicações + e2e (T-1002) |
