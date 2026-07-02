# deploy/k8s — Manifests Kubernetes (T-802, RF-033)

Workloads de **aplicação** do Horus para Kubernetes, organizados como base Kustomize.

## Conteúdo

| Arquivo | O quê |
|---|---|
| `namespace.yaml` | Namespace `horus` |
| `config.yaml` | `ConfigMap horus-config` (endpoints OTLP/backends, `HORUS_ENV`) |
| `secrets.example.yaml` | **Exemplo** de Secrets (credenciais de banco por serviço + `ANTHROPIC_API_KEY`) |
| `apps/*.yaml` | `Deployment` + `Service` de cada executável |
| `apps/ingress.yaml` | `Ingress` (ponto único de entrada externo, TLS opcional — T-901) |
| `autoscaling/*-hpa.yaml` | `HorizontalPodAutoscaler` por componente (T-803) |
| `kustomization.yaml` | Agrega tudo + transformer de tag de imagem |

### Escala horizontal (T-803, RNF-005)

Cada componente escalável tem um HPA (CPU) — escala **independente** por componente:

| Componente | min → max | alvo CPU |
|---|---|---|
| `prontuario-service` / `payment-service` / `invoice-service` | 2 → 6 | 70% |
| `saga-orchestrator` | 2 → 5 | 70% |
| `horus` | 1 → 3 | 75% |
| `report-worker` | 1 → 4 | 75% |

Os Deployments geridos por HPA **não** fixam `replicas` (evita conflito Kustomize×HPA); o
`loadtest` (controlador) mantém réplica fixa. Requer **metrics-server** no cluster. Escala
por métricas customizadas (ex.: fila RabbitMQ via KEDA) é evolução futura.

### Topologia (ADR-0012 / T-804)

- **domain:** `prontuario-service` (8081), `payment-service` (8082), `invoice-service` (8083)
- **orchestrator:** `saga-orchestrator` (8084)
- **platform:** `horus` (8080) — só consome os backends, **separado** do domínio
- **worker:** `report-worker` — consumidor RabbitMQ, sem porta/Service
- **loadtest:** `loadtest` (8000)

As apps Quarkus rodam no profile **prod** (default da imagem empacotada), onde os endpoints
já apontam para os Services do cluster (`postgres-*`, `otel-collector`, `payment-service`…).

#### Separação platform × domain (ADR-0012 / T-804)

`horus` e `report-worker` têm `nodeAffinity` **preferida** pelo pool `platform`; os serviços
de domínio + saga preferem o pool `domain`. A preferência é *soft* (não bloqueia em cluster
single-node sem labels). Para separar de fato, rotule os nós:

```bash
kubectl label node <no-plataforma> horus.io/tier=platform
kubectl label node <no-dominio>   horus.io/tier=domain
```

Assim o Horus (observador) e o worker escalam/operam isolados da carga de domínio,
reforçando a não intrusividade (RNF-H-008).

### Entrada externa e TLS (T-901, RNF-017 / ADR-0002)

Antes desta task os Services eram só `ClusterIP` — sem nenhum ponto de entrada externo em
K8s. `apps/ingress.yaml` cobre isso: um `Ingress` (`ingressClassName: nginx`, mesma tecnologia
do LB de compose por ADR-0002) espelhando o roteamento por prefixo de
`deploy/lb/locations.conf` (`/prontuarios`, `/consultas`, `/carteiras`, `/pagamentos`,
`/notas`, `/sagas`) para os Services de domínio/saga.

TLS via `spec.tls`, referenciando um Secret `kubernetes.io/tls` chamado `horus-tls-secret`
(**não** commitado). Crie-o antes de aplicar a base:

```bash
kubectl -n horus create secret tls horus-tls-secret --cert=tls.crt --key=tls.key
# ou, com cert-manager instalado no cluster, emita via um Certificate/Issuer apontando
# para o mesmo Secret name.
```

Sem o Secret, o `Ingress` fica pendente de TLS (o roteamento HTTP simples via
`ingressClassName` ainda funciona se o controller permitir; para produção, sempre forneça o
certificado). O `host` (`horus.local`) é um placeholder — troque por um domínio real no seu
DNS/`/etc/hosts`.

### Segurança e credenciais (T-901, RNF-017 — auditoria)

- `secrets.example.yaml` é só exemplo (valores de dev não-secretos); `secrets.yaml` (real) é
  gitignorado (`deploy/k8s/.gitignore`) — nunca commitar segredos reais.
- `ANTHROPIC_API_KEY` vem do Secret `horus-ai-secret` (`optional: true` no Deployment do
  Horus); vazio por padrão → `StubLlmEngine`.
- Tráfego **interno** (serviço-a-serviço, Postgres, RabbitMQ) roda em texto claro dentro do
  namespace — tratado como fronteira de confiança nesta versão (sem service mesh); TLS interno
  fica para um endurecimento futuro (ex.: `sslmode=require` no JDBC, `amqps` no RabbitMQ), não
  ativado por padrão pois exigiria material de CA que não existe nesta base.
- Chamadas do Horus ao Anthropic API já são HTTPS (endpoint do provedor).

## Pré-requisitos

A **infraestrutura** (Postgres ×4, RabbitMQ, OTel Collector, Jaeger, Loki, Prometheus) deve
existir no cluster com os **nomes de Service** esperados (`postgres-prontuario`, `rabbitmq`,
`otel-collector`, `jaeger`, `loki`, `prometheus`). Em dev, suba-a com o compose (`make up`);
um overlay de infra para K8s pode ser adicionado em fatia seguinte.

## Aplicar

```bash
# 1) Construa e disponibilize as imagens no cluster (ex.: kind load / registry):
make docker-images                       # tags :dev

# 2) Crie os Secrets reais (NÃO use o exemplo em produção):
cp deploy/k8s/secrets.example.yaml deploy/k8s/secrets.yaml   # edite os valores

# 3) Aplique a base (requer kubectl >= 1.27 com kustomize embutido):
kubectl apply -k deploy/k8s/

# Trocar a tag das imagens sem editar manifestos:
cd deploy/k8s && kustomize edit set image horus/horus=horus/horus:0.1.0
```

> `secrets.yaml` (valores reais) é ignorado pelo git (`deploy/k8s/.gitignore`). Em produção,
> prefira sealed-secrets / external-secrets a Secrets em texto.

## Fora de escopo (próximas fatias)

- Overlay de **infraestrutura** K8s (bancos/mensageria/observabilidade) — hoje pré-requisito.
- **Escala horizontal** por componente (HPA) — T-803.
- Emissão/gestão automatizada de certificados (cert-manager como dependência de cluster) — o
  `Ingress` (T-901) só referencia o Secret TLS, não o provisiona.
- NetworkPolicies, PodDisruptionBudgets, Helm chart empacotado.
