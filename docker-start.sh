#!/bin/bash

set -e

timestamp=$(date '+%Y-%m-%d %H:%M:%S')
echo "======================================="
echo "Kinetic Backend Startup at $timestamp"
echo "======================================="
echo "Port: 4242"
echo "Node.js Version: $(node --version)"
echo "npm Version: $(npm --version)"
echo "Logs: ./logs/server.log"
echo "Shared Data: ./shared-data/db"
echo "Health Check: http://localhost:4242/health"
echo "======================================="
echo "Server starting..."

echo $! > ./logs/server.pid

# Keep running
echo "Backend is running. Press Ctrl+C to stop."
while true; do
    sleep 60
done