# PRD — T-104: Invoice Service (domínio)

| Campo | Valor |
|---|---|
| **Task** | `T-104` |
| **Fase do roadmap** | Fase 1 — Microsserviços de domínio |
| **Status** | `Em revisão (PR)` |
| **Branch** | `task/T-104-invoice-domain` |
| **PR** | [#10](https://github.com/mclovin137/Horus/pull/10) |
| **Depende de** | `T-101` (scaffold) |
| **Requisitos atendidos** | RF-016, RF-017, RF-018, RF-019, RF-020, RF-027 |
| **ADRs relacionados** | ADR-0002 (banco por serviço), ADR-0003 (Quarkus), ADR-0005 (Flyway) |
| **Data** | 2026-06-27 |

## Objetivo

Implementar o **domínio do Invoice Service** sobre o scaffold de T-101: receber solicitação de emissão (RF-016), criar/gerar NF simulada (RF-017), registrar resultado sucesso/falha (RF-018), consultar/listar (RF-019) e reprocessar notas com erro (RF-020), persistindo **somente em `invoice_db`** (RF-027). Conclui os três serviços de domínio da Fase 1.

## Escopo (o que entra)

- **Entidade Panache `NotaFiscal`** (valor, referência, número, status, motivo de falha, tentativas, timestamps) + enum `StatusNota` (`PENDENTE`→`EMITIDA`/`FALHA`).
- **Migração Flyway `V1`** reescrita (schema completo + sequência `nota_fiscal_seq`, alinhada ao id do Panache — lição de T-102/103).
- **Serviço** `NotaFiscalService`: emissão simulada (gera número ou registra falha via knob `simularFalha`), listagem com filtro por status, reprocessamento (só `FALHA` → emite; senão 409).
- **REST** (JSON + Bean Validation): `POST /notas`, `GET /notas/{id}`, `GET /notas?status=`, `POST /notas/{id}/reprocessar`.
- **Erros:** 404 (não encontrada), 409 (reprocessar não-falha), 400 (validação).
- **Teste de fluxo `@QuarkusTest`** (sucesso, falha+reprocesso, filtro por status, 400).

## Fora do escopo

- **Recebimento via RabbitMQ** (o pedido real chega por mensageria) → T-301/Fase 3; aqui o gatilho é REST.
- **Prontuário/Payment** (T-102/T-103). **SAGA** (T-107).
- **Migrações consolidadas/isolamento** (T-105/T-106), **OTel/PII** (T-401/T-406), **RBAC**.

## Premissas e dependências

- Scaffold T-101 (módulo `invoice-service`); testes via Dev Services (CI com Docker).
- Entidade Panache usa id por **sequência** — a migração cria `nota_fiscal_seq` (não IDENTITY).

## Critérios de aceite

- [ ] Emitir NF simulada com sucesso (número, `EMITIDA`) e com falha (`FALHA`, motivo) — RF-016..018.
- [ ] Consultar e listar (filtrando por status) — RF-019.
- [ ] Reprocessar nota em falha → emite; reprocessar não-falha → 409 — RF-020.
- [ ] Persistência só em `invoice_db` (RF-027); schema versionado por Flyway.
- [ ] Build verde; `PLAN.md` 100% ✅ e `state.md` atualizado.

## Riscos

| Risco | Mitigação |
|---|---|
| Emissão "simulada" sem critério de falha realista | Knob `simularFalha` no pedido (não persistido) para exercitar RF-018/020; integração real (mensageria/erros) em fases posteriores. |
| Número de NF não-único | Gerado a partir do id (sequência) — `NF-%08d`; unicidade garantida pela sequência. |

## Referências

- [`../../PRD.md`](../../PRD.md), [`../../ROADMAP.md`](../../ROADMAP.md) — RF-016..020, RF-027
- ADR-0002, ADR-0003, ADR-0005
- [`./PLAN.md`](./PLAN.md)
