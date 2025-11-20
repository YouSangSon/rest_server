# Multi-stage build for optimized Kubernetes deployment
# Optimized for Java 21 Virtual Threads and production workloads

# Stage 1: Build
FROM gradle:8.5-jdk21-alpine AS builder
WORKDIR /app

# Copy gradle files first for better caching
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle
COPY gradlew ./

# Download dependencies (this layer will be cached)
RUN gradle dependencies --no-daemon --quiet || true

# Copy source code
COPY src ./src

# Build the application (skip tests - run in CI/CD)
RUN gradle bootJar --no-daemon --quiet -x test && \
    java -Djarmode=layertools -jar build/libs/*.jar list

# Stage 2: Runtime (Production)
FROM eclipse-temurin:25-jre-alpine

# Install required tools for K8s
RUN apk add --no-cache \
    curl \
    tini \
    && rm -rf /var/cache/apk/*

# Create non-root user (UID 1000 for K8s compatibility)
RUN addgroup -g 1000 appuser && \
    adduser -D -u 1000 -G appuser appuser

WORKDIR /app

# Copy JAR from builder
COPY --from=builder --chown=appuser:appuser /app/build/libs/*.jar app.jar

# Create directories
RUN mkdir -p /app/logs /app/tmp && \
    chown -R appuser:appuser /app

# Switch to non-root user
USER appuser:appuser

# Expose port
EXPOSE 8080

# Health check (K8s will use liveness/readiness probes)
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health/liveness || exit 1

# Use tini for proper signal handling (Graceful shutdown)
ENTRYPOINT ["/sbin/tini", "--"]

# Optimized JVM settings for K8s + Virtual Threads
CMD ["java", \
     # GC Settings
     "-XX:+UseG1GC", \
     "-XX:MaxGCPauseMillis=200", \
     "-XX:ParallelGCThreads=2", \
     "-XX:ConcGCThreads=1", \
     # Memory Settings (K8s aware)
     "-XX:+UseContainerSupport", \
     "-XX:InitialRAMPercentage=70.0", \
     "-XX:MaxRAMPercentage=80.0", \
     # Heap Dump on OOM
     "-XX:+HeapDumpOnOutOfMemoryError", \
     "-XX:HeapDumpPath=/app/logs/heap-dump.hprof", \
     # Performance
     "-XX:+OptimizeStringConcat", \
     "-XX:+UseStringDeduplication", \
     # Security
     "-Djava.security.egd=file:/dev/./urandom", \
     # Spring Profile
     "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:prod}", \
     # Logging
     "-Dlogging.file.path=/app/logs", \
     # Run JAR
     "-jar", "app.jar"]
