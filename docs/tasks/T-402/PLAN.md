# Plano de Execução — T-402: Instrumentação OTel da borda (FastAPI/Locust + LB)

> ⚠️ **Regra R2 ([WORKFLOW](../../WORKFLOW.md)).** Sempre que **criar ou modificar** um arquivo desta tabela, atualize **na mesma hora** o seu `Status` e `Atualizado`, e registre a mudança em *Registro de alterações*.

| Campo | Valor |
|---|---|
| **Task** | `T-402` |
| **Branch** | `task/T-402-otel-loadtest-edge` |
| **PRD** | [`./PRD.md`](./PRD.md) |
| **Progresso** | `6/6` arquivos |

## Arquivos a criar/modificar

| # | Arquivo | Ação | Propósito | Status | Atualizado |
|---|---|---|---|---|---|
| 1 | `loadtest/app/telemetry.py` | Criar | `TracerProvider` + recurso canônico + OTLP | ✅ Concluído | 2026-06-28 |
| 2 | `loadtest/app/main.py` | Modificar | Instrumentar FastAPI (gated por env) | ✅ Concluído | 2026-06-28 |
| 3 | `loadtest/app/locustfile.py` | Modificar | `events.init`: telemetria + injeção de `traceparent` no `requests` | ✅ Concluído | 2026-06-28 |
| 4 | `loadtest/pyproject.toml` | Modificar | Deps OTel (sdk, otlp-http, instrumentation-fastapi/-requests) | ✅ Concluído | 2026-06-28 |
| 5 | `loadtest/tests/test_telemetry.py` | Criar | Testes offline (recurso, span de servidor, W3C) | ✅ Concluído | 2026-06-28 |
| 6 | `deploy/lb/nginx.conf` | Modificar | Access log JSON com `traceparent` | ✅ Concluído | 2026-06-28 |
| 7 | `loadtest/tests/test_scenarios.py` | Modificar | `FakeProc.returncode` p/ o watcher de término | ✅ Concluído | 2026-06-28 |
| 8 | `loadtest/README.md` | Modificar | Seção de telemetria/OTel | ✅ Concluído | 2026-06-28 |

**Legenda:** ⬜ Pendente · 🟡 Em progresso · ✅ Concluído

## Passos de implementação

1. `telemetry.py`: builder de `TracerProvider` (recurso canônico) + `configure_telemetry` idempotente.
2. Wire FastAPI (gated `HORUS_OTEL_ENABLED`) e Locust (`events.init` + `RequestsInstrumentor`).
3. LB: `log_format` JSON com `traceparent`. Testes offline com exporter em memória.

## Verificação / testes

- [x] `pytest -q` verde (21/21) localmente.
- [x] Deps OTel instaladas via `.[dev]`; job de CI `build-loadtest` cobre os testes.

## Registro de alterações

| Data | Arquivo | Mudança |
|---|---|---|
| `2026-06-28` | `app/telemetry.py` | Provider + recurso canônico + OTLP |
| `2026-06-28` | `app/main.py`, `app/locustfile.py` | Wire de instrumentação (FastAPI + borda Locust) |
| `2026-06-28` | `pyproject.toml` | Deps OTel |
| `2026-06-28` | `deploy/lb/nginx.conf` | Access log JSON com `traceparent` |
| `2026-06-28` | `tests/test_telemetry.py`, `tests/test_scenarios.py` | Testes OTel + fix do `FakeProc` |
