# PRD — T-902: Verificação de não intrusividade (RNF-H-008)

| Campo | Valor |
|---|---|
| **Task** | `T-902` |
| **Fase do roadmap** | Fase 9 — Endurecimento e aceite |
| **Status** | `Entregue` |
| **Branch** | `task/T-902-non-intrusiveness` |
| **Depende de** | `T-804` |
| **Requisitos atendidos** | `RNF-H-008` |
| **ADRs relacionados** | `ADR-0012` (Docker/K8s — separação de topologia) |
| **Data** | 2026-07-02 |

## Objetivo

Verificar — arquiteturalmente e empiricamente — que **derrubar o Horus/IA não afeta os
serviços de domínio** (RNF-H-008: "observabilidade é passiva; falha do Horus/IA não afeta os
serviços de domínio").

## Escopo

- **Auditoria estática**: confirmar que nenhum serviço de domínio/SAGA depende do Horus em
  tempo de execução (sem REST client, sem chamada HTTP, sem dependência Maven além do parent
  POM de build).
- **Prova empírica**: subir um serviço de domínio real (Postgres via compose + JAR Quarkus) com
  o Horus **totalmente ausente** (nenhum processo, nenhum container) e exercitar o fluxo
  completo de escrita+leitura+validação, confirmando que funciona normalmente.
- Nota sobre exportação OTel (não bloqueante) como reforço da não-intrusividade mesmo se o
  Collector/backends estiverem fora do ar (relacionado, mas T-903 é quem mede overhead/carga).

## Fora de escopo

- Medir overhead de instrumentação sob carga (T-903).
- Repetir a prova empírica para os 4 serviços de domínio/SAGA individualmente — o padrão de
  código é uniforme (cada serviço tem seu próprio `DataSource`, nenhum tem cliente REST para o
  Horus) e a auditoria estática já cobre todos; `prontuario-service` foi usado como caso
  representativo real (rodar os 4 + orquestrador simultaneamente não agrega evidência nova).

## Auditoria estática (evidência)

```
$ grep -n "horus" services/prontuario/pom.xml
9:        <artifactId>horus-parent</artifactId>
```

Único hit em cada `pom.xml` de serviço de domínio/SAGA: a coordenada do **parent POM**
(`org.example:horus-parent`), que é só agregação de build Maven — não gera dependência de
runtime nem client HTTP.

```
$ grep -rniE "horus" services/{prontuario,payment,invoice,saga-orchestrator}/src \
    | grep -viE "org.example|package |import org"
```

Resultado: só ocorrências em `Dockerfile.jvm` (nome da imagem/usuário do container, ex.
`horus/prontuario:dev`, `useradd -r -g horus horus`) e em `application.properties`
(`${HORUS_ENV:dev}` — nome de uma variável de ambiente de rotulagem, não um endpoint; e
`rabbitmq-username=horus`/`rabbitmq-password=horus` no invoice — credencial do RabbitMQ que
por coincidência se chama "horus", não o serviço Horus). **Nenhuma** ocorrência de client REST,
URL ou host/porta apontando para o Horus (porta `8080`) em nenhum dos 4 serviços de domínio +
SAGA.

**Conclusão da auditoria:** o Horus só **consome** dados dos backends (Jaeger/Loki/Prometheus,
via `T-501` query adapters) — os serviços de domínio nunca chamam o Horus. A dependência é
unidirecional (Horus → backends de observabilidade), nunca (domínio → Horus). Isso já é
garantido pelo desenho (ADR-0008/0012), e a auditoria confirma que o código não introduziu
nenhum acoplamento acidental.

**Exportação OTel (reforço, não o núcleo da verificação):** os serviços usam o exportador OTLP
padrão da extensão Quarkus OTel (`quarkus.otel.exporter.otlp.endpoint`), que usa um
`BatchSpanProcessor` assíncrono — falhas de exportação (Collector fora do ar) são registradas em
log, não propagadas à thread da requisição. Isso é relevante porque, mesmo que o Horus dependa
indiretamente do Collector para observar, uma falha na cadeia de observabilidade não bloqueia o
tráfego de domínio. (Medir isso sob carga real é T-903.)

## Prova empírica (evidência)

Ambiente: `prontuario-service` (representativo dos 4 serviços de domínio/SAGA — mesmo padrão de
código) + Postgres via `docker compose -f deploy/docker-compose.yml up -d postgres-prontuario`.
**Nenhum** processo/container do Horus em execução durante todo o teste (confirmado via
`docker ps` / `ps aux | grep horus` antes, durante e depois).

```
$ docker ps --format "{{.Names}}"
horus-dev-postgres-prontuario-1
$ ps aux | grep -i horus | grep -v grep
(nenhuma saída — nenhum processo do Horus)

$ curl -s http://localhost:8081/q/health/ready
{"status":"UP","checks":[{"name":"Database connections health check","status":"UP", ...}]}

$ curl -s -i -X POST http://localhost:8081/prontuarios -H "Content-Type: application/json" \
    -d '{"pacienteId":"paciente-t902-smoke"}'
HTTP/1.1 201 Created
Location: http://localhost:8081/prontuarios/1
{"criadoEm":"2026-07-02T01:08:54.354257Z","id":1,"pacienteId":"paciente-t902-smoke"}

$ curl -s -w "\nHTTP %{http_code}\n" http://localhost:8081/prontuarios/1
{"criadoEm":"2026-07-02T01:08:54.354257Z","id":1,"pacienteId":"paciente-t902-smoke"}
HTTP 200

$ curl -s -o /dev/null -w "HTTP %{http_code}\n" -X POST http://localhost:8081/prontuarios \
    -H "Content-Type: application/json" -d '{"pacienteId":""}'
HTTP 400   # validação (Bean Validation) segue funcionando normalmente
```

**Conclusão:** criação, leitura e validação do domínio funcionam **integralmente** com o Horus
totalmente ausente — não apenas "derrubado" a meio de operação, mas nunca sequer iniciado.
Ambiente de teste desmontado ao final (`kill` do processo Quarkus + `docker compose down`).

## Critérios de aceite

- [x] Auditoria estática confirma zero dependência de runtime dos 4 serviços de
      domínio/SAGA sobre o Horus.
- [x] Prova empírica: serviço de domínio real funciona (criar/ler/validar) com o Horus
      totalmente ausente.
- [x] Evidência (comandos + saída) registrada neste PRD.
- [x] Nenhuma mudança de código necessária — a não intrusividade já é uma propriedade do
      desenho existente (ADR-0008/0012); esta task **verifica**, não implementa.

## Riscos

| Risco | Mitigação |
|---|---|
| Auditoria estática não pega acoplamento dinâmico (ex. reflection, service discovery) | Nenhum dos serviços usa service discovery dinâmico nesta versão (nomes fixos via `application.properties`); baixo risco |
| Overhead de exportação OTel sob carga real não medido aqui | Fica para T-903 |

## Referências

- [`../../PRD.md`](../../PRD.md) — PRD global (RNF-H-008)
- [`./PLAN.md`](./PLAN.md) — plano de execução desta task
- `docs/adr/ADR-0012-docker-kubernetes.md`
