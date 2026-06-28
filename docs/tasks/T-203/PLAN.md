# Plano de Execução — T-203: Cenários Locust (geração de carga)

> ⚠️ **Regra R2 ([WORKFLOW](../../WORKFLOW.md)).** Sempre que **criar ou modificar** um arquivo desta tabela, atualize **na mesma hora** o seu `Status` e `Atualizado`, e registre a mudança em *Registro de alterações*. Arquivo não previsto que precise ser tocado deve ser **adicionado** à tabela.

| Campo | Valor |
|---|---|
| **Task** | `T-203` |
| **Branch** | `task/T-203-locust-scenarios` |
| **PRD** | [`./PRD.md`](./PRD.md) |
| **Progresso** | `5/5` arquivos |

## Arquivos a criar/modificar

| # | Arquivo | Ação | Propósito | Status | Atualizado |
|---|---|---|---|---|---|
| 1 | `loadtest/app/locustfile.py` | Criar | Cenários `HttpUser` por domínio + SAGA (RF-004) | ✅ Concluído | 2026-06-28 |
| 2 | `loadtest/app/runner.py` | Criar | `LocustRunner` (porta `LoadRunner`) + `build_command` | ✅ Concluído | 2026-06-28 |
| 3 | `loadtest/app/main.py` | Modificar | Seleção de runner por ambiente (`HORUS_LOADTEST_RUNNER`) | ✅ Concluído | 2026-06-28 |
| 4 | `loadtest/pyproject.toml` | Modificar | `locust` nas deps de dev (importável nos testes) | ✅ Concluído | 2026-06-28 |
| 5 | `loadtest/tests/test_scenarios.py` | Criar | Testes offline (cenários, comando, ciclo de vida) | ✅ Concluído | 2026-06-28 |
| 6 | `loadtest/README.md` | Modificar | Marcar T-203 + instruções de geração de carga | ✅ Concluído | 2026-06-28 |

**Legenda:** ⬜ Pendente · 🟡 Em progresso · ✅ Concluído

## Passos de implementação

1. `locustfile.py`: usuários por domínio + SAGA com fluxos encadeados e fração de falhas.
2. `runner.py`: `build_command` + `LocustRunner` (start/stop do processo Locust headless).
3. `main.py`: escolher runner por env (padrão no-op); testes offline com `monkeypatch`.

## Verificação / testes

- [x] `pytest -q` verde (T-202 + T-203) localmente.
- [x] Job de CI `build-loadtest` instala `.[dev]` (inclui Locust) e roda os testes.

## Registro de alterações

| Data | Arquivo | Mudança |
|---|---|---|
| `2026-06-28` | `app/locustfile.py` | Cenários Locust por domínio + SAGA |
| `2026-06-28` | `app/runner.py` | `LocustRunner` + `build_command` |
| `2026-06-28` | `app/main.py` | Seleção de runner por ambiente |
| `2026-06-28` | `pyproject.toml` | `locust` em dev |
| `2026-06-28` | `tests/test_scenarios.py` | Testes offline dos cenários e do runner |
| `2026-06-28` | `README.md` | T-203 marcada + instruções |
