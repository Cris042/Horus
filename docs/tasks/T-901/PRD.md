# PRD — T-901: TLS onde exigido; revisão de segredos/credenciais

| Campo | Valor |
|---|---|
| **Task** | `T-901` |
| **Fase do roadmap** | Fase 9 — Endurecimento e aceite |
| **Status** | `Em progresso` |
| **Branch** | `task/T-901-tls-secrets-review` |
| **Depende de** | `T-802` |
| **Requisitos atendidos** | `RNF-017` |
| **ADRs relacionados** | `ADR-0002` (LB como entrada única), `ADR-0012` (Docker/K8s) |
| **Data** | 2026-07-02 |

## Objetivo

Permitir **TLS na borda** (ponto único de entrada, ADR-0002) tanto no `docker-compose` de dev
quanto no Kubernetes, e **auditar** como credenciais/segredos são manuseados hoje no repositório
— sem introduzir infraestrutura de certificados real (fora do alcance de um cluster de
desenvolvimento), apenas a **capacidade** e a **documentação** (RNF-017: "permitir TLS onde
exigido", não mandatório em todo lugar).

## Escopo (o que entra)

- **Compose:** `deploy/lb/nginx.conf` ganha um `server` HTTPS (443) opcional — ativado apenas
  quando os arquivos de certificado existem no volume montado — reaproveitando o roteamento via
  `include` de um snippet compartilhado com o `server` HTTP (80) existente. Documentação de como
  gerar um certificado autoassinado para dev e como montar um real em produção.
- **Kubernetes:** novo `deploy/k8s/apps/ingress.yaml` — o cluster **não tinha nenhum ponto de
  entrada externo** (Services eram só `ClusterIP`); o `Ingress` cobre esse vazio e roteia pelos
  mesmos prefixos do NGINX de compose, com `tls:` referenciando um Secret `horus-tls-secret`
  (não commitado — via cert-manager ou `kubectl create secret tls`).
- **Revisão de credenciais** (auditoria, ver seção abaixo) documentada neste PRD e refletida no
  `deploy/k8s/README.md`.

## Fora do escopo

- Emissão/gestão real de certificados (cert-manager como dependência de cluster, ACME, etc.) —
  documentado como opção, não implementado.
- TLS **interno** (serviço-a-serviço, Postgres, RabbitMQ) — tráfego dentro do namespace/rede
  do compose é tratado como fronteira de confiança (sem service mesh nesta versão); os *knobs*
  (`sslmode` do JDBC, `amqps`) ficam documentados como endurecimento futuro, não ativados por
  padrão (não há CA/cert de banco/fila configurado em dev).
- Ativar o Load Balancer no `docker-compose.yml`/K8s como serviço containerizado — gap
  pré-existente de T-801 (Dockerfiles construídos, mas o `load-balancer` nunca foi adicionado
  ao compose nem ganhou Deployment em K8s); registrado em `state.md` como pendência separada,
  não faz parte do escopo de TLS/segredos desta task.

## Premissas e dependências

- `T-802` (manifests K8s / Kustomize base) já existe e é a base sobre a qual o `Ingress` é
  adicionado.
- Sem cluster real disponível nesta sessão — validação é por parser YAML + `nginx -t`, como nas
  tasks anteriores de K8s (T-802/T-803/T-804).

## Revisão de credenciais/segredos (auditoria)

| Item | Situação | Veredito |
|---|---|---|
| `deploy/docker-compose.yml` (senhas de Postgres/RabbitMQ dev) | Valores fixos, mas **não-secretos** (uso local, comentário explícito "nunca usar em prod") | ✅ OK |
| `deploy/k8s/secrets.example.yaml` | Marcado como exemplo; instrui copiar para `secrets.yaml` (gitignored) ou criar via `kubectl create secret` | ✅ OK |
| `deploy/k8s/.gitignore` | Ignora `secrets.yaml` (valores reais) | ✅ OK |
| `application.properties` dos serviços | Credenciais de **dev** (`*_svc`/`*_pw`) hardcoded, mas o perfil `%prod` usa o mesmo usuário/senha — **não há indireção por env var em prod** | ⚠️ Ver nota |
| `ANTHROPIC_API_KEY` (Horus/CI) | Vazio por padrão (StubLlmEngine); em CI via GitHub Actions Secret; em K8s via `horus-ai-secret` | ✅ OK |
| Chamadas ao Anthropic API | HTTPS por padrão (SDK/`langchain4j-anthropic`, endpoint `api.anthropic.com`) | ✅ OK (TLS já exigido pelo provedor) |
| `.gitignore` raiz | Ignora `.env`, `.env.*` | ✅ OK |

**Nota (`application.properties`):** o perfil `%prod` dos 4 serviços de domínio usa
`quarkus.datasource.username`/`password` fixos no arquivo (iguais ao dev), em vez de ler de
variável de ambiente. Em Kubernetes isso é **sobrescrito** pelo Secret `horus-db-secret`
(`envFrom.secretRef` nos Deployments — ver `deploy/k8s/apps/*.yaml`), então o valor real em
cluster vem do Secret, não do arquivo — mas o arquivo em si não deveria conter uma senha ainda
que "de dev", por clareza de que é só um placeholder. Ajuste de código (extrair para
`%prod.quarkus.datasource.password=${DB_PASSWORD}`) fica fora do escopo desta task (seria
mudança nos módulos Java, não em `deploy/`) — registrado como observação para T-902+ ou uma
task de hardening dedicada, não bloqueia o critério de aceite (nenhuma credencial **real**
está commitada).

## Critérios de aceite

- [ ] `deploy/lb/nginx.conf` aceita HTTPS (443) quando há certificado montado, sem quebrar o
      fluxo HTTP (80) existente; `nginx -t` válido.
- [ ] `deploy/k8s/apps/ingress.yaml` existe, referencia TLS via Secret, roteia os mesmos
      prefixos do NGINX de compose; YAML válido; incluído no `kustomization.yaml`.
- [ ] `deploy/k8s/README.md` e `deploy/lb/README.md` documentam como habilitar TLS (dev via
      autoassinado, prod via cert real/cert-manager).
- [ ] Auditoria de credenciais registrada neste PRD (tabela acima); nenhuma credencial real
      commitada confirmada.

## Riscos

| Risco | Mitigação |
|---|---|
| TLS mal configurado passar despercebido sem cluster real | Validação via `nginx -t` + parser YAML; ativação real fica para T-405 (E2E) |
| Confundir "permitir TLS" com "TLS obrigatório em produção" | Documentar explicitamente que é opt-in nesta fase (RNF-017 diz "permitir", não "exigir sempre") |

## Referências

- [`../../PRD.md`](../../PRD.md) — PRD global (RNF-017)
- [`./PLAN.md`](./PLAN.md) — plano de execução desta task
- `docs/adr/ADR-0002-load-balancer-sem-api-gateway.md`
- `docs/adr/ADR-0012-docker-kubernetes.md`
