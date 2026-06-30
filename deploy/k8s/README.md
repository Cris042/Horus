# deploy/k8s — Manifests Kubernetes (T-802, RF-033)

Workloads de **aplicação** do Horus para Kubernetes, organizados como base Kustomize.

## Conteúdo

| Arquivo | O quê |
|---|---|
| `namespace.yaml` | Namespace `horus` |
| `config.yaml` | `ConfigMap horus-config` (endpoints OTLP/backends, `HORUS_ENV`) |
| `secrets.example.yaml` | **Exemplo** de Secrets (credenciais de banco por serviço + `ANTHROPIC_API_KEY`) |
| `apps/*.yaml` | `Deployment` + `Service` de cada executável |
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
- Ingress/Gateway, NetworkPolicies, PodDisruptionBudgets, Helm chart empacotado.
