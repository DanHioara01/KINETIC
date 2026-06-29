#!/bin/bash
# Simplified entry point for Docker container

set -e

# Create logs directory
mkdir -p logs

# Copy database from shared volume if available
if [ -f "/app/kinetic.db" ]; then
    echo "Loading external database from /app/kinetic.db"
    cp /app/kinetic.db shared-data/db/kinetic.db
fi

# Set permissions
chown -R node:node .
chmod -R 755 shared-data

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