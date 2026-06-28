# Plano de Execução — T-303: Worker Rust de relatórios

> ⚠️ **Regra R2 ([WORKFLOW](../../WORKFLOW.md)).** Atualize `Status`/`Atualizado` e o *Registro de alterações* ao criar/modificar cada arquivo.

| Campo | Valor |
|---|---|
| **Task** | `T-303` |
| **Branch** | `task/T-303-rust-worker` |
| **PRD** | [`./PRD.md`](./PRD.md) |
| **Progresso** | `9/9` arquivos |

## Arquivos a criar/modificar

| # | Arquivo | Ação | Propósito | Status | Atualizado |
|---|---|---|---|---|---|
| 1 | `worker/Cargo.toml` | Criar | Crate + deps (tokio, lapin, lettre, serde, tracing) | ✅ Concluído | 2026-06-28 |
| 2 | `worker/src/message.rs` | Criar | `RelatorioMensagem` (JSON, espelha T-301) | ✅ Concluído | 2026-06-28 |
| 3 | `worker/src/report.rs` | Criar | Geração do relatório (texto) | ✅ Concluído | 2026-06-28 |
| 4 | `worker/src/email.rs` | Criar | Porta `EmailSender` + SMTP (lettre) + stub de log | ✅ Concluído | 2026-06-28 |
| 5 | `worker/src/consumer.rs` | Criar | Orquestração idempotente por `id` (testável) | ✅ Concluído | 2026-06-28 |
| 6 | `worker/src/config.rs` | Criar | Config via ambiente (AMQP/SMTP/e-mail) | ✅ Concluído | 2026-06-28 |
| 7 | `worker/src/main.rs` | Criar | Conexão AMQP, declare/bind, loop de consumo, ack/nack | ✅ Concluído | 2026-06-28 |
| 8 | `worker/.gitignore` | Criar | Ignorar `/target` | ✅ Concluído | 2026-06-28 |
| 9 | `.github/workflows/ci.yml` | Modificar | Job `build-worker` (fmt + clippy + test) | ✅ Concluído | 2026-06-28 |
| 10 | `worker/README.md` | Modificar | Marcar T-303 + instruções | ✅ Concluído | 2026-06-28 |

**Legenda:** ⬜ Pendente · 🟡 Em progresso · ✅ Concluído

## Passos de implementação

1. Crate Cargo + módulos: `message`/`report`/`email`/`consumer`/`config` (lógica pura, testada).
2. `main.rs`: lapin (executor/reactor tokio), declara exchange/fila/binding, loop ack/nack.
3. Job de CI Rust; documentar instalação de `build-essential` para verificação local.

## Verificação / testes

- [ ] `cargo fmt --check`, `cargo clippy -D warnings`, `cargo test` verdes no CI `build-worker`.
- [x] Lógica pura coberta (parse, relatório, idempotência, e-mail) por testes unitários.
- ⚠️ Local bloqueado por ausência de `gcc` (linker C); validação no CI.

## Registro de alterações

| Data | Arquivo | Mudança |
|---|---|---|
| `2026-06-28` | `worker/**` | Worker Rust criado (consumo, relatório, e-mail, idempotência) |
| `2026-06-28` | `.github/workflows/ci.yml` | Job `build-worker` |
