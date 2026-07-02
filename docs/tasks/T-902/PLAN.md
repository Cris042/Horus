# Plano de Execução — T-902: Verificação de não intrusividade (RNF-H-008)

> ⚠️ **Regra R2 ([WORKFLOW](../../WORKFLOW.md)).** Sempre que **criar ou modificar** um arquivo desta tabela, atualize **na mesma hora** o seu `Status` e `Atualizado`, e registre a mudança em *Registro de alterações*. Arquivo não previsto que precise ser tocado deve ser **adicionado** à tabela.

| Campo | Valor |
|---|---|
| **Task** | `T-902` |
| **Branch** | `task/T-902-non-intrusiveness` |
| **PRD** | [`./PRD.md`](./PRD.md) |
| **Progresso** | `2/2` arquivos |

## Arquivos a criar/modificar

| # | Arquivo | Ação | Propósito | Status | Atualizado |
|---|---|---|---|---|---|
| 1 | `docs/tasks/T-902/PRD.md` | Criar | PRD com auditoria estática + evidência da prova empírica | ✅ Concluído | 2026-07-02 |
| 2 | `docs/tasks/T-902/PLAN.md` | Criar | Plano de execução desta task | ✅ Concluído | 2026-07-02 |

> Task de **verificação** (RNF-H-008) — não altera código nem manifests; sem mudanças fora de
> `docs/tasks/T-902/`. `state.md` atualizado ao final.

## Passos de implementação

1. Auditoria estática: `grep` por referências a "horus" nos `pom.xml`/fontes dos 4 serviços de
   domínio/SAGA — confirmar ausência de client REST/dependência de runtime.
2. Prova empírica: `docker compose up -d postgres-prontuario` (infra mínima, Horus **nunca**
   iniciado), build + run de `prontuario-service` (JAR Quarkus, perfil dev), health check,
   criar+ler+validar via `curl`.
3. Confirmar ausência do Horus (`docker ps` + `ps aux | grep horus`) antes/durante/depois.
4. Desmontar o ambiente (`kill` do processo, `docker compose down`).
5. Registrar evidência (comandos + saída real) no `PRD.md`.

## Verificação / testes

- [x] `services/prontuario` builda (`./mvnw -pl services/prontuario -am -DskipTests package`).
- [x] `GET /q/health/ready` → `200 UP` sem Horus em execução.
- [x] `POST /prontuarios` → `201 Created`; `GET /prontuarios/{id}` → `200` com os dados
      persistidos; `POST` inválido → `400` (validação intacta).
- [x] `docker ps` / `ps aux | grep horus` vazios durante todo o teste.

## Registro de alterações

| Data | Arquivo | Mudança |
|---|---|---|
| `2026-07-02` | `docs/tasks/T-902/*` | PRD (auditoria + prova empírica) e plano criados; task de verificação, sem mudança de código |
