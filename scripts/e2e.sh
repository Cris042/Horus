#!/usr/bin/env bash
# Horus — teste ponta a ponta contra o stack real (T-1002).
#
# Pré-requisito: stack no ar com as aplicações (`make up-apps`). O script entra pelo Load
# Balancer (como a carga real), injeta um `traceparent` conhecido em cada fluxo e verifica que
# o Horus enxerga o ciclo de vida completo desses traces nos backends reais:
#   1. SAGA feliz (pagar → emitir NF → relatório via RabbitMQ → worker)
#   2. SAGA com falha simulada na NF → compensação (estorno)
# e, para cada uma, as APIs do Horus: SAGA, queries SQL, correlação (mensageria/worker),
# busca por janela (T-1001) e resumo de estado.
#
# Uso: scripts/e2e.sh   (variáveis: LB_URL, HORUS_URL, E2E_TIMEOUT em segundos)
set -euo pipefail

LB_URL="${LB_URL:-http://localhost:8088}"
HORUS_URL="${HORUS_URL:-http://localhost:8080}"
TIMEOUT="${E2E_TIMEOUT:-120}"

FAILURES=0
pass() { printf '  \033[32m✔\033[0m %s\n' "$1"; }
fail() { printf '  \033[31m✘\033[0m %s\n' "$1"; FAILURES=$((FAILURES + 1)); }

# Avalia uma expressão Python sobre o JSON de stdin (`d` = documento); imprime o resultado.
jget() { python3 -c "import json,sys; d=json.load(sys.stdin); print($1)"; }

wait_for() { # wait_for <descrição> <url>
  local deadline=$((SECONDS + TIMEOUT))
  until curl -fsS -o /dev/null "$2"; do
    if ((SECONDS > deadline)); then
      echo "timeout esperando $1 ($2)" >&2
      exit 1
    fi
    sleep 2
  done
  pass "$1 no ar"
}

# Repete <check> até passar ou estourar o timeout — o pipeline OTLP→Collector→Jaeger/Loki
# é assíncrono (batch), então o trace aparece alguns segundos depois da request.
eventually() { # eventually <descrição> <comando...>
  local desc="$1"; shift
  local deadline=$((SECONDS + TIMEOUT))
  until "$@" >/dev/null 2>&1; do
    if ((SECONDS > deadline)); then
      fail "$desc"
      return 0
    fi
    sleep 3
  done
  pass "$desc"
}

new_trace_id() { python3 -c "import secrets; print(secrets.token_hex(16))"; }
traceparent() { echo "00-$1-$(python3 -c 'import secrets; print(secrets.token_hex(8))')-01"; }

post() { # post <traceId> <path> <json>
  curl -fsS -X POST "$LB_URL$2" -H 'Content-Type: application/json' \
    -H "traceparent: $(traceparent "$1")" -d "$3"
}

echo "== Aguardando o stack"
wait_for "Load Balancer" "$LB_URL/healthz"
wait_for "Horus" "$HORUS_URL/q/health/ready"

echo "== Gerando tráfego pelo Load Balancer"
SETUP_TRACE=$(new_trace_id)
CARTEIRA=$(post "$SETUP_TRACE" /carteiras '{"titularId":"e2e-'"$SETUP_TRACE"'","saldoInicial":1000.00}' | jget 'd["id"]')
pass "carteira $CARTEIRA criada"

OK_TRACE=$(new_trace_id)
OK_STATUS=$(post "$OK_TRACE" /sagas/pagar-e-emitir "{\"carteiraId\":$CARTEIRA,\"valor\":42.50,\"simularFalhaNota\":false}" | jget 'd["status"]')
[[ "$OK_STATUS" == "CONCLUIDA" ]] && pass "SAGA feliz CONCLUIDA (trace $OK_TRACE)" || fail "SAGA feliz terminou $OK_STATUS"

KO_TRACE=$(new_trace_id)
KO_STATUS=$(post "$KO_TRACE" /sagas/pagar-e-emitir "{\"carteiraId\":$CARTEIRA,\"valor\":10.00,\"simularFalhaNota\":true}" | jget 'd["status"]')
[[ "$KO_STATUS" == "COMPENSADA" ]] && pass "SAGA com falha COMPENSADA (trace $KO_TRACE)" || fail "SAGA com falha terminou $KO_STATUS"

echo "== Verificando o Horus sobre os backends reais"
h() { curl -fsS "$HORUS_URL$1"; }

check_saga_completed() { h "/horus/lifecycle/saga/$OK_TRACE" | jget 'd["outcome"]=="completed" and d["stepCount"]>=2' | grep -qx True; }
check_saga_compensated() { h "/horus/lifecycle/saga/$KO_TRACE" | jget 'd["outcome"]=="compensated" and d["compensationCount"]>=1' | grep -qx True; }
check_queries() { h "/horus/lifecycle/queries/$OK_TRACE" | jget 'd["queryCount"]>0' | grep -qx True; }
check_services() { h "/horus/correlation/trace/$OK_TRACE" | jget '{s["serviceName"] for s in d["services"]} >= {"saga-orchestrator","payment-service","invoice-service"}' | grep -qx True; }
check_worker() { h "/horus/correlation/trace/$OK_TRACE" | jget 'd["messagingInvolved"] and d["workerInvolved"]' | grep -qx True; }
check_request_lifecycle() { h "/horus/lifecycle/requests/$KO_TRACE" | jget 'len(d["spans"])>0' | grep -qx True; }
check_search() { h "/horus/traces?lookback=15m&service=saga-orchestrator&limit=200" | jget "{'$OK_TRACE','$KO_TRACE'} <= {t['traceId'] for t in d['traces']}" | grep -qx True; }
check_search_errors() { h "/horus/traces?lookback=15m&error=true&limit=200" | jget "'$KO_TRACE' in {t['traceId'] for t in d['traces']}" | grep -qx True; }
check_state_summary() { h "/horus/ai/summary/state?lookback=15m" | jget '"window" in d["signals"] and len(d["summary"])>0' | grep -qx True; }

eventually "SAGA feliz visualizada (passos, desfecho completed)" check_saga_completed
eventually "SAGA compensada visualizada (compensação correlacionada)" check_saga_compensated
eventually "queries SQL correlacionadas ao trace" check_queries
eventually "serviços saga/payment/invoice no mesmo trace" check_services
eventually "HTTP→RabbitMQ→worker no mesmo trace" check_worker
eventually "ciclo de vida da request (waterfall)" check_request_lifecycle
eventually "busca por janela encontra os dois traces (T-1001)" check_search
eventually "busca por janela com erro encontra a SAGA compensada" check_search_errors
eventually "resumo de estado da janela" check_state_summary

echo
if ((FAILURES > 0)); then
  echo "E2E: $FAILURES verificação(ões) falharam"
  exit 1
fi
echo "E2E: todas as verificações passaram"
