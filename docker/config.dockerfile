# Docker build configuration for backend
# Multi-stage build for production optimization

# ==============================================
# BASE STAGE
# ==============================================
FROM node:20-alpine AS base

WORKDIR /app

# Install system dependencies
RUN apk add --no-cache --virtual .build-deps \
    curl \
    jq \
    && rm -rf /var/cache/apk/*

# Copy package files
COPY backend/package.json backend/package-lock.json* ./

# ==============================================
# DEPENDENCY STAGE
# ==============================================
FROM base AS dependencies

# Install dependencies with production mode
RUN npm ci --production --ignore-scripts

# ==============================================
# BUILDER STAGE
# ==============================================
FROM dependencies AS builder

# Copy source code
COPY backend/ ./

# Cache busting optimization
RUN find . -name "*.js" -type f -exec touch {} \;

# ==============================================
# PRODUCTION STAGE
# ==============================================
FROM node:20-alpine AS production

WORKDIR /app

# Install runtime dependencies only
RUN apk add --no-cache --virtual .run-deps \
    curl \
    jq \
    && rm -rf /var/cache/apk/*

# Copy compiled application
COPY --from=builder /app/ ./

# Create logs and data directories
RUN mkdir -p logs shared-data

# Set proper permissions
RUN chown -R node:node . && chmod -R 755 .

# Health check configuration
COPY docker/health.json ./

# Application configuration
ENV NODE_ENV=production
ENV PORT=4242
EXPOSE 4242

# Health check endpoint
HEALTHCHECK --interval=30s --timeout=10s --start-period=10s --retries=3 \
    CMD curl -f http://localhost:4242/health || exit 1

# Start application
USER node
CMD ["node", "index.js"]

# ==============================================
# DEVELOPMENT STAGE
# ==============================================
FROM dependencies AS development

WORKDIR /app

# Copy all source code
COPY backend/ ./

# Install development dependencies
RUN npm install --ignore-scripts

# Environment
ENV NODE_ENV=development
ENV PORT=4242
EXPOSE 4242

# Development command with auto-restart
CMD ["npm", "run", "dev"]