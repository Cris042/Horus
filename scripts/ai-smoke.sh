#!/usr/bin/env bash
# Horus — smoke da IA real (T-1003): uma chamada curta por camada (FAST/BALANCED/DEEP) contra a
# API Anthropic, pelo endpoint /horus/ai/complete de um Horus em modo live.
#
# Pré-requisito: Horus rodando com HORUS_AI_ENABLED=true e ANTHROPIC_API_KEY (ex.: o job
# `ai-live` do CI). Custo: 3 respostas curtas. Uso: scripts/ai-smoke.sh [HORUS_URL]
set -euo pipefail
HORUS_URL="${1:-${HORUS_URL:-http://localhost:8080}}"

mode=$(curl -fsS "$HORUS_URL/horus/ai/health" | python3 -c 'import json,sys; print(json.load(sys.stdin)["mode"])')
if [[ "$mode" != "live" ]]; then
  echo "Horus não está em modo live (mode=$mode) — defina HORUS_AI_ENABLED=true e ANTHROPIC_API_KEY" >&2
  exit 1
fi

failures=0
for tier in FAST BALANCED DEEP; do
  resp=$(curl -fsS -X POST "$HORUS_URL/horus/ai/complete" -H 'Content-Type: application/json' \
    -d "{\"system\":\"Responda em uma palavra.\",\"prompt\":\"Diga apenas: ok\",\"tier\":\"$tier\"}") || {
    echo "✘ $tier: chamada falhou"; failures=$((failures + 1)); continue; }
  if echo "$resp" | python3 -c 'import json,sys; d=json.load(sys.stdin); sys.exit(0 if d["live"] and d["text"].strip() else 1)'; then
    echo "✔ $tier → $(echo "$resp" | python3 -c 'import json,sys; print(json.load(sys.stdin)["modelId"])')"
  else
    echo "✘ $tier: resposta inesperada: $resp"; failures=$((failures + 1))
  fi
done
exit "$failures"
