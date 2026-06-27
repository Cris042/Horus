# Horus — Makefile de conveniência
# Alvos placeholder na Fase 0 (T-001). Serão preenchidos conforme o roadmap:
#   - `up`/`down` ganham conteúdo real em T-003 (docker-compose de desenvolvimento).
#   - `build` passa a compilar cada componente a partir das Fases 1+.

.DEFAULT_GOAL := help
.PHONY: help up down build

help: ## Lista os alvos disponíveis
	@echo "Horus — alvos disponíveis:"
	@echo "  make up     — sobe a infraestrutura local (placeholder até T-003)"
	@echo "  make down   — derruba a infraestrutura local (placeholder até T-003)"
	@echo "  make build  — compila os componentes (placeholder até as Fases 1+)"

up: ## Sobe a infraestrutura local (Postgres x3, RabbitMQ, Collector, Jaeger, Loki, Prometheus)
	@echo "[up] placeholder — será implementado em T-003 (deploy/docker-compose)."

down: ## Derruba a infraestrutura local
	@echo "[down] placeholder — será implementado em T-003 (deploy/docker-compose)."

build: ## Compila os componentes (no-op por enquanto)
	@echo "[build] placeholder — nenhum componente compilável ainda; ver ROADMAP Fases 1+."
