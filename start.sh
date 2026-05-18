#!/usr/bin/env bash
# =============================================================================
# InvestAgg — Full Project Startup Script
# =============================================================================
# Usage:
#   ./start.sh            — run everything (default: Android emulator)
#   ./start.sh ios        — run with iOS simulator
#   ./start.sh backend    — start only backend + DB
#   ./start.sh db         — start only Docker containers (Postgres + pgAdmin)
#   ./start.sh stop       — stop all Docker containers
# =============================================================================

set -euo pipefail

# ─── Colours ──────────────────────────────────────────────────────────────────
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m'

log()  { echo -e "${BLUE}[INFO]${NC}  $*"; }
ok()   { echo -e "${GREEN}[OK]${NC}    $*"; }
warn() { echo -e "${YELLOW}[WARN]${NC}  $*"; }
err()  { echo -e "${RED}[ERROR]${NC} $*" >&2; }
step() { echo -e "\n${BOLD}${CYAN}━━━ $* ━━━${NC}"; }

# ─── Paths ────────────────────────────────────────────────────────────────────
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$SCRIPT_DIR/backend"
MOBILE_DIR="$SCRIPT_DIR/mobile"

# ─── Mode ─────────────────────────────────────────────────────────────────────
MODE="${1:-all}"
PLATFORM="android"
[ "$MODE" = "ios" ] && PLATFORM="ios" && MODE="all"

# ─── Helper: check command exists ─────────────────────────────────────────────
need() {
  if ! command -v "$1" &>/dev/null; then
    err "Required command not found: $1"
    err "Please install it and re-run."
    exit 1
  fi
}

# =============================================================================
# STEP 1 — Docker (Postgres + pgAdmin)
# =============================================================================
start_db() {
  step "Starting Docker services (Postgres + pgAdmin)"

  need docker

  if ! docker info &>/dev/null; then
    err "Docker is not running. Start Docker Desktop and re-run."
    exit 1
  fi

  cd "$SCRIPT_DIR"
  docker compose up -d

  log "Waiting for Postgres to be healthy..."
  local attempts=0
  until docker compose exec -T postgres pg_isready -U investagg -d investagg &>/dev/null; do
    attempts=$((attempts + 1))
    if [ "$attempts" -ge 30 ]; then
      err "Postgres did not become healthy after 30 attempts. Check: docker compose logs postgres"
      exit 1
    fi
    sleep 2
  done

  ok "Postgres is ready at localhost:5432"
  ok "pgAdmin is available at http://localhost:5050  (admin@investagg.local / admin)"
}

# =============================================================================
# STEP 2 — Backend (Spring Boot)
# =============================================================================
start_backend() {
  step "Building & starting Spring Boot backend"

  need java

  JAVA_VER=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d. -f1)
  if [ "${JAVA_VER:-0}" -lt 17 ]; then
    err "Java 17+ is required. Found: Java ${JAVA_VER:-unknown}"
    exit 1
  fi

  cd "$BACKEND_DIR"

  log "Running Gradle build..."
  ./gradlew build -x test --quiet

  ok "Build successful"

  log "Starting backend (logs → backend/app.log)..."
  nohup ./gradlew bootRun --args='--spring.profiles.active=dev' \
    > "$BACKEND_DIR/app.log" 2>&1 &
  BACKEND_PID=$!
  echo $BACKEND_PID > "$BACKEND_DIR/.pid"

  log "Waiting for backend to be ready (PID=$BACKEND_PID)..."
  local attempts=0
  until curl -sf http://localhost:8080/api/v1/actuator/health &>/dev/null \
     || grep -q "Started BackendApplication" "$BACKEND_DIR/app.log" 2>/dev/null; do
    attempts=$((attempts + 1))
    if [ "$attempts" -ge 60 ]; then
      warn "Backend may not be ready yet — check logs: tail -f backend/app.log"
      break
    fi
    sleep 2
  done

  ok "Backend running at http://localhost:8080/api/v1"
  ok "Swagger UI: http://localhost:8080/api/v1/swagger-ui.html"
}

# =============================================================================
# STEP 3 — Mobile (React Native)
# =============================================================================
start_mobile() {
  step "Starting React Native mobile app ($PLATFORM)"

  need node
  need npm

  cd "$MOBILE_DIR"

  if [ ! -d "node_modules" ]; then
    log "Installing npm dependencies..."
    npm install --silent
    ok "Dependencies installed"
  else
    log "node_modules already present — skipping npm install"
  fi

  if [ "$PLATFORM" = "ios" ]; then
    need pod
    log "Installing CocoaPods..."
    cd ios && pod install --silent && cd ..
    ok "Pods installed"

    log "Starting Metro bundler in background..."
    npx react-native start --reset-cache &>/dev/null &
    sleep 3

    log "Building and launching on iOS simulator..."
    npx react-native run-ios

  else
    # Android
    if [ -z "${ANDROID_HOME:-}" ]; then
      warn "ANDROID_HOME is not set."
      warn "Make sure an Android emulator is running (AVD Manager in Android Studio)"
      warn "or a physical device is connected via ADB."
    fi

    log "Starting Metro bundler in background..."
    npx react-native start --reset-cache &>/dev/null &
    sleep 3

    log "Building and launching on Android..."
    npx react-native run-android
  fi

  ok "Mobile app launched"
}

# =============================================================================
# STOP
# =============================================================================
stop_all() {
  step "Stopping all services"

  cd "$SCRIPT_DIR"

  if [ -f "$BACKEND_DIR/.pid" ]; then
    PID=$(cat "$BACKEND_DIR/.pid")
    if kill -0 "$PID" 2>/dev/null; then
      log "Stopping backend (PID=$PID)..."
      kill "$PID"
      ok "Backend stopped"
    fi
    rm -f "$BACKEND_DIR/.pid"
  fi

  log "Stopping Docker containers..."
  docker compose down
  ok "Docker containers stopped"
}

# =============================================================================
# MAIN
# =============================================================================
echo -e "\n${BOLD}InvestAgg — Startup Script${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

case "$MODE" in
  stop)
    stop_all
    ;;
  db)
    start_db
    echo -e "\n${GREEN}${BOLD}✓ Database ready.${NC}"
    ;;
  backend)
    start_db
    start_backend
    echo -e "\n${GREEN}${BOLD}✓ Backend is up.${NC}"
    echo "  API:     http://localhost:8080/api/v1"
    echo "  Swagger: http://localhost:8080/api/v1/swagger-ui.html"
    echo "  Logs:    tail -f backend/app.log"
    ;;
  all)
    start_db
    start_backend
    start_mobile
    echo -e "\n${GREEN}${BOLD}✓ All services running.${NC}"
    echo "  API:     http://localhost:8080/api/v1"
    echo "  Swagger: http://localhost:8080/api/v1/swagger-ui.html"
    echo "  pgAdmin: http://localhost:5050"
    echo "  Logs:    tail -f backend/app.log"
    ;;
  *)
    err "Unknown mode: $MODE"
    echo "Usage: ./start.sh [all|ios|backend|db|stop]"
    exit 1
    ;;
esac
