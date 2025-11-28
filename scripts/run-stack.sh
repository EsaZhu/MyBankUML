#!/usr/bin/env bash
set -euo pipefail

BACKEND_PORT=${BACKEND_PORT:-8081}
REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

cleanup() {
  if [[ -n "${BACKEND_PID:-}" ]]; then
    kill "$BACKEND_PID" >/dev/null 2>&1 || true
  fi
}
trap cleanup EXIT

echo "Starting backend on port ${BACKEND_PORT}..."
(
  cd "$REPO_ROOT"
  mvn -Dmaven.repo.local=.m2repo -Dexec.mainClass=application.Main -Dexec.args="${BACKEND_PORT}" exec:java
) > /tmp/mybank_backend.log 2>&1 &
BACKEND_PID=$!

sleep 2
echo "Backend pid ${BACKEND_PID} (logs: /tmp/mybank_backend.log)"

echo "Starting frontend with API base http://localhost:${BACKEND_PORT}/api ..."
cd "$REPO_ROOT/front"
REACT_APP_API_BASE="http://localhost:${BACKEND_PORT}/api" npm start
