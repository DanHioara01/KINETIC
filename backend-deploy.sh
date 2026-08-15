#!/bin/bash
# Enhanced Backend Deployment Script with Structured Logging

set -e

# ==============================================================================
# CONFIG
# ==============================================================================

LOG_DIR="./logs"
LOG_FILE="$LOG_DIR/server.log"
PID_FILE="$LOG_DIR/server.pid"
MAX_LOG_SIZE=10M

# PostgreSQL connection string. Override with DATABASE_URL env var
# (e.g. the connection string from Render managed Postgres or Supabase).
DATABASE_URL="${DATABASE_URL:-postgres://postgres:postgres@localhost:5432/kinetic}"

# ==============================================================================
# FUNCTIONS
# ==============================================================================

log() {
    local level=$1
    local message=$2
    local timestamp=$(date '+%Y-%m-%d %H:%M:%S')
    echo "[$timestamp] [$level] $message" | tee -a "$LOG_FILE"
}

log_structured() {
    local level=$1
    local message=$2
    local timestamp=$(date '+%Y-%m-%d %H:%M:%S')
    echo "{\"timestamp\": \"$timestamp\", \"level\": \"$level\", \"message\": \"$message\"}" | tee -a "$LOG_FILE"
}

check_database() {
    if [ -z "$DATABASE_URL" ]; then
        log "ERROR" "DATABASE_URL is not set"
        return 1
    fi
    local db_host=$(echo "$DATABASE_URL" | sed -E 's|^[a-z]+://[^@]*@?([^:/]+).*|\1|')
    log "INFO" "Using PostgreSQL at $db_host (set DATABASE_URL to override)"
}

start_server() {
    log "INFO" "Starting Kinetic Backend Server on port 4242"
    log "INFO" "Node.js version: $(node --version)"
    log "INFO" "npm version: $(npm --version)"
    
    export NODE_ENV=production
    export DATABASE_URL="$DATABASE_URL"
    
    node index.js >> "$LOG_FILE" 2>&1 &
    local server_pid=$!
    
    echo $server_pid > "$PID_FILE"
    log "INFO" "Server started with PID: $server_pid"
    
    sleep 3
    if kill -0 $server_pid 2>/dev/null; then
        log "INFO" "Server is running successfully"
        return 0
    else
        log "ERROR" "Server failed to start"
        return 1
    fi
}

stop_server() {
    if [ -f "$PID_FILE" ]; then
        local pid=$(cat "$PID_FILE")
        if kill -0 $pid 2>/dev/null; then
            log "INFO" "Stopping server with PID: $pid"
            kill $pid
            local timeout=30
            local count=0
            
            while kill -0 $pid 2>/dev/null && [ $count -lt $timeout ]; do
                sleep 1
                count=$((count + 1))
            done
            
            if kill -0 $pid 2>/dev/null; then
                log "WARN" "Server did not stop gracefully, forcing termination"
                kill -9 $pid
            else
                log "INFO" "Server stopped successfully"
            fi
        fi
        rm -f "$PID_FILE"
    fi
}