#!/usr/bin/env bash
set -euo pipefail

ENV_FILE="/mnt/c/Users/mclov/IdeaProjects/Horus/.env"

if [[ -f "$ENV_FILE" ]]; then
  set -a
  # shellcheck disable=SC1090
  source "$ENV_FILE"
  set +a
fi

exec npx -y @upstash/context7-mcp
