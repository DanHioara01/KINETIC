#!/bin/bash
# Structured Logging Setup for Kinetic Backend

LOG_DIR="./logs"
RUN_ID=$(date +%Y%m%d_%H%M%S)
STRUCTURED_LOG_FILE="$LOG_DIR/structured.log"
ROTATED_LOG_FILE="$LOG_DIR/structured-$RUN_ID.log"

# Create log directory
mkdir -p "$LOG_DIR"

# Function to log structured entries
log_entry() {
    local level=$1
    local component=$2
    local message=$3
    local timestamp=$(date -u +"%Y-%m-%dT%H:%M:%S.000Z")
    local trace_id="$(head -c 16 /dev/urandom | xxd -p)"

    local structured_entry="{\"timestamp\": \"$timestamp\", \"level\": \"$level\", \"component\": \"$component\", \"trace_id\": \"$trace_id\", \"message\": \"$message\"}"

    # Log to structured file
    echo "$structured_entry" >> "$STRUCTURED_LOG_FILE"
    echo "$structured_entry"
}

# Function to log application metrics
log_metrics() {
    local component=$1
    local duration=$2
    local memory_usage=$3
    local cpu_usage=$4

    local metrics_entry="{\"timestamp\": \"$(date -u +'%Y-%m-%dT%H:%M:%S.000Z')\", \"component\": \"$component\", \"metrics\": {\"duration_ms\": $duration, \"memory_mb\": $memory_usage, \"cpu_percent\": $cpu_usage}}"

    echo "$metrics_entry" >> "$ROTATED_LOG_FILE"
}

# Setup log rotation
rotate_logs() {
    if [ -f "$STRUCTURED_LOG_FILE" ]; then
        mv "$STRUCTURED_LOG_FILE" "$ROTATED_LOG_FILE"
        touch "$STRUCTURED_LOG_FILE"
    fi
}

# Initialize logging
rotate_logs

# Export functions for use by Node.js application
export -f log_entry
export -f log_metrics
export -f STRUCTURED_LOG_FILE
export -f ROTATED_LOG_FILE

# Display logging info
echo "Structured logging initialized"
echo "Log file: $STRUCTURED_LOG_FILE"
echo "Rotation backup: $ROTATED_LOG_FILE"
