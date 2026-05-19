#!/usr/bin/env bash
# =============================================================================
# InvestAgg — Stop Script
# Останавливает всё, что запустил start.sh
# =============================================================================

set -uo pipefail

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
BOLD='\033[1m'
NC='\033[0m'

log()  { echo -e "${BLUE}[INFO]${NC}  $*"; }
ok()   { echo -e "${GREEN}[OK]${NC}    $*"; }
warn() { echo -e "${YELLOW}[WARN]${NC}  $*"; }

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$SCRIPT_DIR/backend"

echo -e "\n${BOLD}InvestAgg — Stopping all services${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# ─── 1. Backend (Spring Boot) ─────────────────────────────────────────────────
if [ -f "$BACKEND_DIR/.pid" ]; then
  PID=$(cat "$BACKEND_DIR/.pid")
  if kill -0 "$PID" 2>/dev/null; then
    log "Stopping backend (PID=$PID)..."
    kill "$PID"
    # Wait up to 10s for graceful shutdown
    for i in $(seq 1 10); do
      kill -0 "$PID" 2>/dev/null || break
      sleep 1
    done
    # Force-kill if still alive
    if kill -0 "$PID" 2>/dev/null; then
      warn "Graceful shutdown timed out, force-killing..."
      kill -9 "$PID" 2>/dev/null || true
    fi
    ok "Backend stopped"
  else
    warn "Backend PID=$PID is not running (already stopped?)"
  fi
  rm -f "$BACKEND_DIR/.pid"
else
  warn "No backend/.pid file found — backend may not have been started by start.sh"
fi

# ─── 2. Gradle daemon (может держать порты после остановки bootRun) ───────────
GRADLE_PIDS=$(pgrep -f "GradleDaemon\|gradle-launcher" 2>/dev/null || true)
if [ -n "$GRADLE_PIDS" ]; then
  log "Stopping Gradle daemons..."
  cd "$BACKEND_DIR" && ./gradlew --stop --quiet 2>/dev/null || true
  ok "Gradle daemons stopped"
fi

# ─── 3. Metro bundler ─────────────────────────────────────────────────────────
METRO_PIDS=$(pgrep -f "react-native start\|metro.*8082\|metro.*8081" 2>/dev/null || true)
if [ -n "$METRO_PIDS" ]; then
  log "Stopping Metro bundler (PIDs: $METRO_PIDS)..."
  echo "$METRO_PIDS" | xargs kill 2>/dev/null || true
  ok "Metro bundler stopped"
else
  warn "Metro bundler not found (already stopped?)"
fi

# ─── 4. Docker (Postgres + pgAdmin) ───────────────────────────────────────────
if command -v docker &>/dev/null && docker info &>/dev/null 2>&1; then
  cd "$SCRIPT_DIR"
  RUNNING=$(docker compose ps --services --filter "status=running" 2>/dev/null || true)
  if [ -n "$RUNNING" ]; then
    log "Stopping Docker containers..."
    docker compose down
    ok "Docker containers stopped"
  else
    warn "No running Docker containers found"
  fi
else
  warn "Docker not available or not running — skipping"
fi

echo -e "\n${GREEN}${BOLD}✓ All services stopped.${NC}\n"
