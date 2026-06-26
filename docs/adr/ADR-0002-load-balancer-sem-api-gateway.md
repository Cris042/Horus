# ADR-0002 — Load Balancer como ponto de entrada, sem API Gateway

- **Status:** Aceito
- **Data:** 2026-06-26
- **Origem:** Herdado do documento-fonte (DA-001, DA-002)
- **Requisitos:** RF-005, RNF-006

## Contexto

A solução precisa de um ponto de entrada que distribua a carga gerada pelo Locust entre as instâncias dos microsserviços. Autenticação/autorização e roteamento avançado de um API Gateway não fazem parte do escopo desta versão do caso de estudo.

## Decisão

Usar um **Load Balancer** (**NGINX** ou **Traefik**) como **ponto único de entrada**. Nesta versão **não há API Gateway**: o Load Balancer apenas recebe o tráfego e o distribui entre as instâncias dos serviços de domínio.

## Consequências

**Positivas**
- Entrada simples, alinhada ao tamanho do estudo.
- Menos componentes para instrumentar e operar.

**Negativas**
- Sem autenticação/autorização na borda dos serviços de domínio (aceitável no estudo; o **Horus** possui RBAC próprio para o painel — ver ADR-0008 e `ROLES.md`).
- Roteamento/políticas avançadas ficam fora do escopo.
