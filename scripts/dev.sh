#!/usr/bin/env bash
set -e

echo "============================================================"
echo " Marabagam Platform Local Development Script (Bash)"
echo "============================================================"

# Ensure .env exists
if [ ! -f .env ]; then
    echo "Creating .env from .env.example..."
    cp .env.example .env
fi

# Function to display usage
usage() {
    echo "Usage: $0 [docker|native|stop|status]"
    echo "  docker : Build and start all services via Docker Compose (Recommended)"
    echo "  native : Run services natively in background"
    echo "  stop   : Stop running Docker Compose containers"
    echo "  status : Check status of Docker Compose services"
    exit 1
}

MODE=${1:-docker}

case "$MODE" in
    docker)
        echo "Starting Marabagam environment with Docker Compose..."
        docker compose down --remove-orphans 2>/dev/null || true
        docker compose build
        docker compose up -d
        echo "Waiting for services to initialize..."
        sleep 5
        docker compose ps
        echo ""
        echo "Frontend URL:   http://localhost:5173"
        echo "Backend Health: http://localhost:8080/api/v1/health"
        echo "AI Service:     http://localhost:8000/health"
        ;;
    stop)
        echo "Stopping Marabagam Docker containers..."
        docker compose down
        ;;
    status)
        docker compose ps
        ;;
    *)
        usage
        ;;
esac
