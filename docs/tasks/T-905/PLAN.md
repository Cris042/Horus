# Plano de Execução — T-905: Validar critérios de aceite (aceite final)

> ⚠️ **Regra R2 ([WORKFLOW](../../WORKFLOW.md)).** Sempre que **criar ou modificar** um arquivo desta tabela, atualize **na mesma hora** o seu `Status` e `Atualizado`, e registre a mudança em *Registro de alterações*. Arquivo não previsto que precise ser tocado deve ser **adicionado** à tabela.

| Campo | Valor |
|---|---|
| **Task** | `T-905` |
| **Branch** | `task/T-905-final-acceptance` |
| **PRD** | [`./PRD.md`](./PRD.md) |
| **Progresso** | `4/4` arquivos |

## Arquivos a criar/modificar

| # | Arquivo | Ação | Propósito | Status | Atualizado |
|---|---|---|---|---|---|
| 1 | `docs/tasks/T-905/PRD.md` | Criar | Validação dos 16 critérios de aceite + achados/correções | ✅ Concluído | 2026-07-02 |
| 2 | `docs/tasks/T-905/PLAN.md` | Criar | Plano de execução desta task | ✅ Concluído | 2026-07-02 |
| 3 | `services/saga-orchestrator/src/main/java/org/example/saga/service/SagaService.java` | Modificar | Spans `saga.{flow}.{step}`/`horus.saga.*` (gap real do critério #16) | ✅ Concluído | 2026-07-02 |
| 4 | `deploy/k8s/kustomization.yaml` | Modificar | `commonLabels` (deprecado) → `labels` (achado ao aplicar em cluster real) | ✅ Concluído | 2026-07-02 |

> `state.md` atualizado ao final.

## Passos de implementação

1. Percorrer os 16 critérios do `PRD.md` §10, priorizando os **nunca validados ao vivo**:
   LB (#2), K8s real (#7), SAGA visualizada (#16).
2. **LB**: buildar as 4 imagens de domínio faltantes; subir os 4 serviços + NGINX real na rede
   do compose com os nomes DNS esperados; tráfego real via `:8088`; inspecionar access log +
   Jaeger.
3. **K8s**: carregar as 7 imagens no cluster `kind` já existente; `kubectl apply -k
   deploy/k8s/`; inspecionar pods/logs — achado o deprecation de `commonLabels`, corrigido.
4. **SAGA**: criar carteira financiada, disparar `POST /sagas/pagar-e-emitir` com
   `simularFalhaNota=true`; confirmar compensação real (saldo estornado) via API; checar
   `GET /horus/lifecycle/saga/{traceId}` — **vazio**. Investigar: `SagaVisualizationService`
   espera spans `saga.{flow}.{step}`; `SagaService` nunca os criava. Corrigir + revalidar.
5. Validar #9/#10/#11 (lifecycle APIs) com o trace real da SAGA.
6. Validar #12/#13 (endpoints de IA) com o `StubLlmEngine`.
7. Validar #1 (`pytest` do loadtest) e #4/#5 (já cobertos por T-405/T-303).
8. Regressão completa: `./mvnw verify` (raiz), `pytest` (loadtest), `cargo fmt`/`clippy`/`test`
   (worker).
9. Desmontar os ambientes de teste (containers do LB/domínio, recursos de app do `kind`);
   restaurar os serviços de dev do host para uso contínuo.

## Verificação / testes

- [x] LB real: `upstream` correto no access log, `traceparent` preservado no Jaeger.
- [x] `kubectl apply -k deploy/k8s/` → sem erros, idempotente numa segunda aplicação.
- [x] SAGA real compensada: saldo estornado (API) + `outcome: "compensated"` no Horus.
- [x] `./mvnw verify` (raiz) → `BUILD SUCCESS`, 86 testes, 0 falhas.
- [x] `pytest` (loadtest) → 21/21.
- [x] `cargo fmt --check` / `cargo clippy --all-targets -- -D warnings` / `cargo test`
      (worker) → limpos, 14/14.

## Registro de alterações

| Data | Arquivo | Mudança |
|---|---|---|
| `2026-07-02` | `docs/tasks/T-905/*` | PRD (validação dos 16 critérios + achados) e plano criados |
| `2026-07-02` | `services/saga-orchestrator/.../SagaService.java` | Spans `saga.{flow}.{step}`/`.compensate` com atributos `horus.saga.*` (gap real do critério #16, T-507 nunca validado contra dados reais) |
| `2026-07-02` | `deploy/k8s/kustomization.yaml` | `commonLabels` → `labels` (deprecation real, achada ao aplicar em cluster `kind`) |
