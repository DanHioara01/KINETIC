#!/bin/bash
# CI/CD Configuration and Build Automation Script

set -e

# ==============================================================================
# CI/CD CONFIGURATION
# ==============================================================================

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
VERSION="$(date +%Y.%m.%d-%H%M%S)"
IMAGE_NAME="kinetic-backend"
REGISTRY_URL="${REGISTRY_URL:-localhost:5000}"

# ==============================================================================
# BUILD AND DEPLOY FUNCTIONS
# ==============================================================================

log_info() {
    echo -e "${GREEN}[INFO]${NC} $(date '+%Y-%m-%d %H:%M:%S') - $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $(date '+%Y-%m-%d %H:%M:%S') - $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $(date '+%Y-%m-%d %H:%M:%S') - $1"
}

# ==============================================================================
# BUILD PHASE
# ==============================================================================

build_image() {
    log_info "Building Docker image for $IMAGE_NAME:$VERSION"
    
    local build_args=""
    if [ "$CI" = "true" ]; then
        build_args="$build_args --build-arg NODE_ENV=production"
    fi
    
    docker build $build_args -f Dockerfile.backend -t "$IMAGE_NAME:$VERSION" ./backend
    
    if [ $? -eq 0 ]; then
        log_info "Successfully built image: $IMAGE_NAME:$VERSION"
    else
        log_error "Failed to build image"
        exit 1
    fi
}

# ==============================================================================
# TEST PHASE
# ==============================================================================

test_backend() {
    log_info "Testing backend functionality"
    
    # Wait for container to be ready
    local max_attempts=30
    local attempt=1
    
    while [ $attempt -le $max_attempts ]; do
        if curl -f http://localhost:4242/health > /dev/null 2>&1; then
            log_info "Backend health check passed"
            return 0
        fi
        
        log_warn "Health check attempt $attempt/$max_attempts failed"
        sleep 2
        attempt=$((attempt + 1))
    done
    
    log_error "Backend health check failed after $max_attempts attempts"
    return 1
}

# ==============================================================================
# DEPLOY PHASE
# ==============================================================================

deploy_to_registry() {
    log_info "Deploying image to registry"
    
    # Tag for registry
    docker tag "$IMAGE_NAME:$VERSION" "$REGISTRY_URL/$IMAGE_NAME:$VERSION"
    
    # Push to registry
    docker push "$REGISTRY_URL/$IMAGE_NAME:$VERSION"
    
    if [ $? -eq 0 ]; then
        log_info "Successfully deployed to registry"
    else
        log_error "Failed to deploy to registry"
        exit 1
    fi
}

# ==============================================================================
# CLEANUP PHASE
# ==============================================================================

cleanup() {
    log_info "Cleaning up build artifacts"
    
    # Remove local images
    docker rmi "$IMAGE_NAME:$VERSION" 2>/dev/null || true
    docker rmi "$REGISTRY_URL/$IMAGE_NAME:$VERSION" 2>/dev/null || true
    
    log_info "Cleanup completed"
}

# ==============================================================================
# MONITORING SETUP
# ==============================================================================

setup_monitoring() {
    log_info "Setting up monitoring infrastructure"
    
    # Create monitoring directories
    mkdir -p monitoring/logs
    mkdir -p monitoring/metrics
    mkdir -p monitoring/alerts
    
    # Generate systemd service for monitoring
    cat > monitoring/kinetic-backend-monitor.service << EOF
[Unit]
Description=Kinetic Backend Monitoring Service
After=docker.service
Requires=docker.service

[Service]
Type=oneshot
ExecStart=/bin/bash -c 'curl -f http://localhost:4242/health || systemctl restart kinetic-backend.service'
User=node

[Install]
WantedBy=multi-user.target
EOF
    
    log_info "Monitoring setup completed"
}

# ==============================================================================
# MAIN EXECUTION
# ==============================================================================

main() {
    log_info "Starting CI/CD pipeline for Kinetic Backend"
    log_info "Version: $VERSION"
    
    # Build phase
    build_image
    
    # Test phase
    test_backend
    
    # Setup monitoring
    setup_monitoring
    
    # Deploy if not in test mode
    if [ "$CI" = "true" ] || [ "$CI_DEPLOY" = "true" ]; then
        deploy_to_registry
    fi
    
    log_info "CI/CD pipeline completed successfully"
}

# ==============================================================================
# ERROR HANDLING
# ==============================================================================

trap 'log_error "CI/CD pipeline failed"; exit 1' ERR

main "$@"
