# Horus — Makefile de conveniência
#
# Fase 0: `up`/`down` sobem a infraestrutura local de desenvolvimento (T-003,
# `deploy/docker-compose.yml`): Postgres ×3, RabbitMQ, OTel Collector, Jaeger, Loki, Prometheus.
# `build` passa a compilar cada componente a partir das Fases 1+.

COMPOSE := docker compose -f deploy/docker-compose.yml

IMAGE_TAG ?= dev

.DEFAULT_GOAL := help
.PHONY: help up down ps logs restart clean build docker-images docker-quarkus docker-worker docker-loadtest package up-apps down-apps e2e

help: ## Lista os alvos disponíveis
	@echo "Horus — alvos disponíveis:"
	@echo "  make up      — sobe a infraestrutura local (Postgres x3, RabbitMQ, Collector, Jaeger, Loki, Prometheus)"
	@echo "  make down    — derruba a infraestrutura local"
	@echo "  make ps      — status dos contêineres"
	@echo "  make logs    — segue os logs de todos os serviços"
	@echo "  make restart — reinicia a infraestrutura"
	@echo "  make clean   — derruba e REMOVE volumes (apaga os dados dos bancos)"
	@echo "  make build   — compila os componentes (placeholder até as Fases 1+)"
	@echo "  make up-apps — empacota e sobe infra + aplicações + LB (profile apps, T-1002)"
	@echo "  make e2e     — teste ponta a ponta contra o stack no ar (scripts/e2e.sh)"
	@echo ""
	@echo "UIs: Jaeger http://localhost:16686 · RabbitMQ http://localhost:15672 · Prometheus http://localhost:9090"

up: ## Sobe a infraestrutura local em background
	$(COMPOSE) up -d

down: ## Derruba a infraestrutura local (mantém volumes)
	$(COMPOSE) down

ps: ## Status dos contêineres
	$(COMPOSE) ps

logs: ## Segue os logs de todos os serviços
	$(COMPOSE) logs -f

restart: ## Reinicia a infraestrutura
	$(COMPOSE) restart

package: ## Empacota os 5 módulos Quarkus (target/quarkus-app) para as imagens do profile apps
	./mvnw -B -q -DskipTests package

up-apps: package ## Sobe infra + aplicações + Load Balancer (http://localhost:8088) e Horus (:8080)
	$(COMPOSE) --profile apps up -d --build --wait

down-apps: ## Derruba infra + aplicações
	$(COMPOSE) --profile apps down

e2e: ## Teste ponta a ponta contra o stack no ar (make up-apps antes)
	scripts/e2e.sh

clean: ## Derruba e REMOVE volumes (apaga dados dos bancos)
	$(COMPOSE) --profile apps down -v

build: ## Compila os componentes (no-op por enquanto)
	@echo "[build] placeholder — nenhum componente compilável ainda; ver ROADMAP Fases 1+."

# ---------------------------------------------------------------- Imagens Docker (T-801)
docker-images: docker-quarkus docker-worker docker-loadtest ## Constrói TODAS as imagens (5 Quarkus + worker + loadtest)

docker-quarkus: ## Imagens JVM dos 5 módulos Quarkus (build a partir da raiz = reator Maven)
	docker build -f horus/src/main/docker/Dockerfile.jvm -t horus/horus:$(IMAGE_TAG) .
	docker build -f services/prontuario/src/main/docker/Dockerfile.jvm -t horus/prontuario:$(IMAGE_TAG) .
	docker build -f services/payment/src/main/docker/Dockerfile.jvm -t horus/payment:$(IMAGE_TAG) .
	docker build -f services/invoice/src/main/docker/Dockerfile.jvm -t horus/invoice:$(IMAGE_TAG) .
	docker build -f services/saga-orchestrator/src/main/docker/Dockerfile.jvm -t horus/saga-orchestrator:$(IMAGE_TAG) .

docker-worker: ## Imagem do worker Rust de relatório/e-mail
	docker build -t horus/report-worker:$(IMAGE_TAG) worker/

docker-loadtest: ## Imagem da API de teste de carga (FastAPI)
	docker build -t horus/loadtest:$(IMAGE_TAG) loadtest/
