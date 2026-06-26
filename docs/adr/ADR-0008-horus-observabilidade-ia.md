# ADR-0008 — Horus: plataforma de observabilidade com IA (supersede "sem dashboard")

- **Status:** Aceito
- **Data:** 2026-06-26
- **Origem:** Novo (foco principal do projeto)
- **Supersede:** a restrição "sem dashboard customizado / somente Jaeger UI" do documento-fonte (RNF-018, DA-008 parcial)
- **Requisitos:** RF-H-001..RF-H-015, RNF-H-001..RNF-H-010

## Contexto

O documento-fonte definiu observabilidade mínima: traces no **Jaeger UI** e **nenhum dashboard próprio**. Na prática, isso obriga o operador a reconstruir mentalmente cada fluxo, não dá visão de ciclo de vida das **queries**, deixa os **logs de erro** dispersos e não oferece **síntese** alguma.

O **foco principal deste projeto** é exatamente o oposto: uma estrutura de observabilidade **automatizada com IA** que permita ver o fluxo de vida completo de cada request e query, reunir todos os logs de erro e usar IA para **resumir** o que está acontecendo. Isso exige um produto próprio.

## Decisão

Construir o **Horus**, uma plataforma de observabilidade aumentada por IA, como produto principal do repositório. O Horus:

1. **Ingere** telemetria padrão (OTel/OTLP) de todos os componentes.
2. **Correlaciona** o ciclo de vida de request e de query (ADR-0009).
3. **Agrega** todos os logs de erro vinculados ao trace.
4. Oferece uma **camada de IA** (Claude) para resumo, explicação de trace, RCA, anomalias e consulta em linguagem natural (ADR-0011).
5. Expõe **API + painel próprios** (RF-H-012).

Esta decisão **revisa conscientemente** a restrição "sem dashboard" do documento-fonte: o Horus **é** o dashboard, e ele é o objetivo do trabalho. O **Jaeger UI é mantido** para inspeção de traces crus, complementando (não competindo com) o Horus.

A observabilidade é **passiva e não intrusiva**: o Horus lê telemetria, nunca está no caminho crítico dos serviços de domínio (RNF-H-008).

## Consequências

**Positivas**
- Atende ao objetivo central do projeto (visão de ciclo de vida + IA).
- Centraliza request, query, logs e métricas em um só lugar, com síntese automática.

**Negativas**
- Aumenta o escopo além do documento-fonte (assumido explicitamente).
- Introduz dependência de um provedor de IA e de backends de logs/métricas (ADR-0010/0011).
- Exige governança de custo e de privacidade da IA (RNF-H-002/003/006).
