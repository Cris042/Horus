# ADR-0010 — Pipeline unificado de traces, logs e métricas

- **Status:** Aceito
- **Data:** 2026-06-26
- **Origem:** Novo (estende ADR-0007)
- **Requisitos:** RF-H-014, RF-031, RNF-H-005, RNF-H-007, RNF-H-009

## Contexto

A IA do Horus precisa de **três sinais** correlacionados — traces, logs e métricas — mas o documento-fonte só previa traces (Jaeger). É preciso um pipeline único de coleta e backends consultáveis programaticamente pelo Horus.

## Decisão

Adotar um pipeline centrado no **OpenTelemetry Collector** recebendo **OTLP** de todos os componentes e distribuindo para backends especializados:

| Sinal | Backend | Papel |
|---|---|---|
| **Traces** | **Jaeger** (mantido por exigência do fonte) — opcionalmente **Grafana Tempo** ao lado | Caminho/duração das requests e queries |
| **Logs** | **Loki** | Logs de erro correlacionados por `trace_id` |
| **Métricas** | **Prometheus** | Taxa de erro, latência, throughput, saúde |

Regras:
- Todos os componentes exportam **somente via OTLP** para o Collector; trocar de backend não muda os serviços (RNF-H-009).
- O **Horus consome os backends** por suas APIs de consulta para montar contexto à IA e ao painel.
- **Retenção configurável** por sinal (RNF-H-005).
- O **Collector** centraliza amostragem, redação adicional e roteamento.

> Decisão de manter Jaeger **e** avaliar Tempo permanece como questão aberta do PRD; ambos são alimentados pelo mesmo OTLP, então a escolha não afeta a instrumentação.

## Consequências

**Positivas**
- Um só ponto de coleta; backends substituíveis sem tocar nos serviços.
- IA e painel têm acesso uniforme aos três sinais.

**Negativas**
- Mais componentes de infraestrutura para operar (Collector, Loki, Prometheus, Jaeger/Tempo).
- O Collector vira ponto de atenção de escalabilidade sob carga (RNF-H-007).
