#!/bin/bash
# Simplified entry point for Docker container

set -e

# Create logs directory
mkdir -p logs

# Copy database from shared volume if available
mkdir -p shared-data/db
if [ -f "/app/kinetic.db" ]; then
    echo "Loading external database from /app/kinetic.db"
    cp /app/kinetic.db shared-data/db/kinetic.db
fi

# Set permissions — do this only when running as root (the Dockerfile already
# chowns at build time; as USER node these would fail with EPERM and exit 1).
if [ "$(id -u)" = "0" ]; then
    chown -R node:node .
    chmod -R 755 shared-data
fi

# User-friendly startup message
echo "========================================"
echo "KINETIC BACKEND - Docker Deployment"
echo "========================================"
echo "Port: 4242"
echo "Health Check: http://localhost:4242/health"
echo "Logs: ./logs/server.log"
echo "========================================"
echo "Starting server..."
echo "========================================"

# Start the actual server using the Node.js process
exec node index.js
