# Plano de Execução — T-202: API FastAPI de controle de teste de carga

> ⚠️ **Regra R2 ([WORKFLOW](../../WORKFLOW.md)).** Sempre que **criar ou modificar** um arquivo desta tabela, atualize **na mesma hora** o seu `Status` e `Atualizado`, e registre a mudança em *Registro de alterações*. Arquivo não previsto que precise ser tocado deve ser **adicionado** à tabela.

| Campo | Valor |
|---|---|
| **Task** | `T-202` |
| **Branch** | `task/T-202-fastapi-control-api` |
| **PRD** | [`./PRD.md`](./PRD.md) |
| **Progresso** | `9/9` arquivos |

## Arquivos a criar/modificar

| # | Arquivo | Ação | Propósito | Status | Atualizado |
|---|---|---|---|---|---|
| 1 | `loadtest/pyproject.toml` | Criar | Projeto/deps (FastAPI/Uvicorn/Pydantic; dev: pytest/httpx) | ✅ Concluído | 2026-06-28 |
| 2 | `loadtest/app/__init__.py` | Criar | Pacote da API | ✅ Concluído | 2026-06-28 |
| 3 | `loadtest/app/models.py` | Criar | Modelos Pydantic + enum de status | ✅ Concluído | 2026-06-28 |
| 4 | `loadtest/app/manager.py` | Criar | Controlador de ciclo de vida + porta `LoadRunner` | ✅ Concluído | 2026-06-28 |
| 5 | `loadtest/app/main.py` | Criar | App FastAPI + endpoints RF-001..003 | ✅ Concluído | 2026-06-28 |
| 6 | `loadtest/tests/__init__.py` | Criar | Pacote de testes | ✅ Concluído | 2026-06-28 |
| 7 | `loadtest/tests/test_api.py` | Criar | Testes da API (pytest + TestClient) | ✅ Concluído | 2026-06-28 |
| 8 | `loadtest/.gitignore` | Criar | Ignorar `.venv`, caches, lock | ✅ Concluído | 2026-06-28 |
| 9 | `loadtest/README.md` | Modificar | Marcar T-202 + instruções de dev | ✅ Concluído | 2026-06-28 |
| 10 | `.github/workflows/ci.yml` | Modificar | Job `build-loadtest` (uv + pytest) | ✅ Concluído | 2026-06-28 |

**Legenda:** ⬜ Pendente · 🟡 Em progresso · ✅ Concluído

## Passos de implementação

1. Modelos Pydantic + enum de status; `LoadTestManager` thread-safe com porta `LoadRunner` (no-op).
2. App FastAPI com os 5 endpoints; erros mapeados para 404/409/422.
3. `pyproject.toml` (uv) + testes pytest; job de CI Python.

## Verificação / testes

- [x] `uv pip install -e ".[dev]"` + `pytest -q` verde (10/10) localmente.
- [x] Job `build-loadtest` adicionado ao `ci.yml` (uv + pytest no PR).

## Registro de alterações

| Data | Arquivo | Mudança |
|---|---|---|
| `2026-06-28` | `app/*`, `tests/*`, `pyproject.toml`, `.gitignore` | API de controle (RF-001..003) + testes criados |
| `2026-06-28` | `README.md` | T-202 marcada + instruções de dev |
| `2026-06-28` | `.github/workflows/ci.yml` | Job `build-loadtest` (uv + pytest) |
