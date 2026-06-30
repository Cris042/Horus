# PRD — T-703: Alertas (e-mail/webhook) com resumo de IA

| Campo | Valor |
|---|---|
| **Task** | `T-703` |
| **Fase** | Fase 7 — Painel e alertas do Horus |
| **Requisitos** | RF-H-013 |
| **Dependências** | T-606 (anomalias/erros), T-601 (IA) |
| **Branch** | `task/T-703-alerts` |

## Objetivo

Emitir **alertas** (e-mail/webhook) em anomalias ou erros críticos, cada um
acompanhado de um **resumo em linguagem natural gerado por IA** (RF-H-013). O Horus
recebe um gatilho de alerta (título, severidade, contexto — tipicamente vindo do
Anomaly Detector / Error Clusterer da T-606), gera o resumo e **dispara para todos os
canais habilitados**.

## Escopo

- `alert/AlertModel` — DTOs: `AlertSeverity`, `AlertRequest`, `Alert`, `ChannelResult`,
  `AlertResult`.
- `alert/AlertChannel` — porta de canal (`name`, `enabled`, `dispatch`).
- Canais:
  - `LogAlertChannel` — sempre habilitado; registra o alerta no log (default seguro,
    espelha o stub de e-mail do worker).
  - `WebhookAlertChannel` — habilitado quando `horus.alert.webhook.url` definido;
    faz `POST` JSON best-effort (timeout curto, nunca lança).
  - `EmailAlertChannel` — habilitado quando `horus.alert.email.to` definido; stub que
    registra a intenção de envio (integração SMTP fica para fatia seguinte).
- `alert/AlertService` — monta o resumo de IA (camada FAST) e faz fan-out aos canais
  habilitados, retornando o resultado por canal.
- `api/HorusAlertResource` — `POST /horus/alerts` (levanta um alerta) e
  `GET /horus/alerts/channels` (lista canais e estado habilitado).
- Config em `application.properties` (canais desligados por padrão, exceto log).
- Teste `@QuarkusTest` com `StubLlmEngine` (resumo placeholder) verificando o fan-out.

## Fora de escopo

- Integração SMTP real (e-mail) — `quarkus-mailer` fica para fatia seguinte; o canal de
  e-mail é stub gated por config.
- Disparo automático a partir do T-606 / agendamento — esta fatia entrega o mecanismo de
  alerta + API; o gatilho automático e a deduplicação entram depois.
- Persistência/histórico de alertas.

## Critérios de aceitação

1. `POST /horus/alerts` gera um alerta com **resumo de IA** e faz fan-out aos canais
   habilitados, retornando o resultado por canal.
2. Canal de **log** sempre dispara; webhook/e-mail só quando configurados.
3. `GET /horus/alerts/channels` lista os canais e seu estado.
4. Funciona com `StubLlmEngine` (sem chave) — `live=false`, resumo placeholder.
5. `./mvnw -pl horus test` verde.
