# Horus — Makefile de conveniência
#
# Fase 0: `up`/`down` sobem a infraestrutura local de desenvolvimento (T-003,
# `deploy/docker-compose.yml`): Postgres ×3, RabbitMQ, OTel Collector, Jaeger, Loki, Prometheus.
# `build` passa a compilar cada componente a partir das Fases 1+.

COMPOSE := docker compose -f deploy/docker-compose.yml

.DEFAULT_GOAL := help
.PHONY: help up down ps logs restart clean build

help: ## Lista os alvos disponíveis
	@echo "Horus — alvos disponíveis:"
	@echo "  make up      — sobe a infraestrutura local (Postgres x3, RabbitMQ, Collector, Jaeger, Loki, Prometheus)"
	@echo "  make down    — derruba a infraestrutura local"
	@echo "  make ps      — status dos contêineres"
	@echo "  make logs    — segue os logs de todos os serviços"
	@echo "  make restart — reinicia a infraestrutura"
	@echo "  make clean   — derruba e REMOVE volumes (apaga os dados dos bancos)"
	@echo "  make build   — compila os componentes (placeholder até as Fases 1+)"
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

clean: ## Derruba e REMOVE volumes (apaga dados dos bancos)
	$(COMPOSE) down -v

build: ## Compila os componentes (no-op por enquanto)
	@echo "[build] placeholder — nenhum componente compilável ainda; ver ROADMAP Fases 1+."
