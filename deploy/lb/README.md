# Load Balancer (NGINX) — entrada do sistema observado (T-201)

| Campo | Valor |
|---|---|
| **Requisitos** | RF-005 (balancear), RNF-006 (distribuir entre instâncias) |
| **Contrato** | T-005 §2 / RF-029 — o LB **repassa** `traceparent` e **não encerra** o trace |
| **Config** | [`nginx.conf`](./nginx.conf) |

## Papel

Ponto **único de entrada**: recebe as chamadas (do load test / clientes) e as roteia, por **prefixo de caminho**, aos serviços de domínio e ao orquestrador de SAGA. Quando um serviço tem **réplicas**, o DNS do orquestrador retorna múltiplos endereços e o NGINX distribui entre eles (RNF-006).

## Roteamento

| Prefixo | Serviço (upstream) | Porta |
|---|---|---|
| `/prontuarios`, `/consultas` | `prontuario-service` | 8081 |
| `/carteiras`, `/pagamentos` | `payment-service` | 8082 |
| `/notas` | `invoice-service` | 8083 |
| `/sagas` | `saga-orchestrator` | 8084 |
| `/healthz` | (o próprio LB) | — |

## Observabilidade (crítico)

O LB **encaminha** o W3C `traceparent`/`tracestate` aos upstreams (NGINX repassa os headers do cliente por padrão; o `nginx.conf` não os remove) e **não inicia/encerra** um trace próprio. Assim o trace começa no cliente (load test, T-202/T-402) e segue intacto pelos serviços — correlação ponta a ponta no mesmo `trace_id` (RF-029, contrato §2/§7).

## Execução

Os nomes resolvem em **tempo de requisição** (`resolver 127.0.0.11` + variável no `proxy_pass`): o LB **sobe mesmo se um serviço estiver fora** (responde 502 por requisição até o serviço voltar), e os serviços podem reiniciar/escalar **sem recarregar** o NGINX. Os nomes (`prontuario-service` etc.) são os da topologia **containerizada**.

O LB **ainda não** entra no `docker-compose` de dev (que hoje sobe **só infra**; os serviços rodam via `quarkus:dev` no host, fora da rede do compose). **Será ativado em T-801** (imagens Docker dos serviços + rede `horus` comum). Esboço de serviço para o compose:

```yaml
load-balancer:
  image: nginx:1.27-alpine
  volumes:
    - ./lb/nginx.conf:/etc/nginx/nginx.conf:ro
  ports: ["8088:80"]
  networks: [horus]   # mesma rede dos serviços containerizados
```

> **Sintaxe validada** (T-201): `docker run --rm -v "$PWD/deploy/lb/nginx.conf:/etc/nginx/nginx.conf:ro" nginx:1.27-alpine nginx -t` → *syntax is ok / test is successful*.

## Tasks

`T-201` (esta config) · `T-801` (containerizar serviços + ativar o LB no compose/K8s) · `T-202`/`T-402` (load test atravessa o LB).
