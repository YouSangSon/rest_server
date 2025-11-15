# Deployment Guide

Complete guide for deploying the REST Server (Trading Bot + SNS API) to various environments.

**Last Updated:** 2025-11-14
**Version:** 1.0.0

---

## 📋 Table of Contents

1. [Prerequisites](#prerequisites)
2. [Local Development](#local-development)
3. [Docker Deployment](#docker-deployment)
4. [Production Deployment](#production-deployment)
5. [Database Service Setup](#database-service-setup)
6. [Environment Configuration](#environment-configuration)
7. [Health Checks & Monitoring](#health-checks--monitoring)
8. [Troubleshooting](#troubleshooting)

---

## ✅ Prerequisites

### Required Software

| Software | Version | Purpose |
|----------|---------|---------|
| JDK | 21+ | Java runtime |
| Kotlin | 1.9+ | Programming language |
| Gradle | 8.x | Build tool |
| Docker | 24.x+ | Containerization |
| Docker Compose | 2.x+ | Multi-container orchestration |
| PostgreSQL | 15+ | Relational database |
| MongoDB | 7+ | Document database |
| Redis | 7+ | Cache |
| Kafka | 7.5+ | Event streaming |

### Hardware Requirements

**Minimum (Development):**
- CPU: 4 cores
- RAM: 8 GB
- Disk: 50 GB

**Recommended (Production):**
- CPU: 8+ cores
- RAM: 16+ GB
- Disk: 200+ GB SSD

---

## 🖥️ Local Development

### Step 1: Clone Repository

```bash
git clone https://github.com/YouSangSon/rest_server.git
cd rest_server
```

### Step 2: Setup Environment Variables

```bash
# Copy example environment file
cp .env.example .env

# Edit environment variables
nano .env
```

**Required Variables:**
```bash
# JWT Secret (256-bit minimum)
JWT_SECRET=your-very-secure-secret-key-that-is-at-least-256-bits-long

# Database Service
DATABASE_SERVICE_URL=http://localhost:8080
DATABASE_SERVICE_ENABLED=true

# Trading APIs (optional)
NEWSAPI_KEY=your-newsapi-key
BINANCE_API_KEY=your-binance-key
BINANCE_SECRET_KEY=your-binance-secret

# Notifications (optional)
TELEGRAM_BOT_TOKEN=your-telegram-token
SLACK_WEBHOOK_URL=your-slack-webhook
```

### Step 3: Start Infrastructure Services

```bash
# Start PostgreSQL, MongoDB, Redis, Kafka
docker-compose up -d postgres mongodb redis kafka zookeeper

# Check services are running
docker-compose ps
```

### Step 4: Run Application

**Option A: Using Gradle**
```bash
./gradlew bootRun
```

**Option B: Using IntelliJ IDEA**
1. Open project in IntelliJ
2. Wait for Gradle sync
3. Run `RestServerApplication.kt`

### Step 5: Verify

```bash
# Health check
curl http://localhost:8080/actuator/health

# Expected response:
# {"status":"UP"}

# API documentation
open http://localhost:8080/swagger-ui.html
```

---

## 🐳 Docker Deployment

### Option 1: Docker Compose (All Services)

**Start Everything:**
```bash
# Build and start all services
docker-compose up -d

# View logs
docker-compose logs -f rest-server

# Stop all services
docker-compose down
```

**Services Included:**
- PostgreSQL
- MongoDB
- Redis
- Kafka + Zookeeper
- ML Service (Python)
- REST Server
- pgAdmin (database admin)
- Kafka UI (Kafka admin)

### Option 2: Docker Compose (Selective Services)

```bash
# Only infrastructure
docker-compose up -d postgres mongodb redis kafka zookeeper

# Run REST Server locally
./gradlew bootRun
```

### Option 3: Custom Docker Build

**Build Docker Image:**
```bash
# Build JAR
./gradlew clean build

# Build Docker image
docker build -t rest-server:latest .

# Run container
docker run -d \
  --name rest-server \
  -p 8080:8080 \
  -e DATABASE_SERVICE_URL=http://database-service:8080 \
  -e JWT_SECRET=your-secret \
  --network rest-network \
  rest-server:latest
```

**Dockerfile:**
```dockerfile
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## 🚀 Production Deployment

### Architecture Overview

```
┌─────────────────────────────────────┐
│     Load Balancer (Nginx)            │
│     SSL Termination                  │
└──────────────┬──────────────────────┘
               │
       ┌───────┴────────┐
       ↓                ↓
┌─────────────┐  ┌─────────────┐
│ REST Server │  │ REST Server │
│  Instance 1 │  │  Instance 2 │
│  (Docker)   │  │  (Docker)   │
└──────┬──────┘  └──────┬───────┘
       │                │
       └────────┬───────┘
                ↓
┌─────────────────────────────────┐
│      Database Service            │
│    (Separate Server/Cluster)    │
└──────────┬──────────────────────┘
           │
   ┌───────┴────────┐
   ↓                ↓
┌─────────┐    ┌──────────┐
│PostgreSQL│    │ MongoDB  │
│(Primary +│    │(Replica  │
│ Replica) │    │  Set)    │
└─────────┘    └──────────┘
```

### Step 1: Prepare Production Environment

**Update Environment Variables:**
```bash
# Production .env file
SPRING_PROFILES_ACTIVE=prod
APP_PORT=8080

# Security
JWT_SECRET=production-256-bit-secret-change-this
CORS_ALLOWED_ORIGINS=https://yourdomain.com

# Database Service (external)
DATABASE_SERVICE_URL=https://db-service.yourdomain.com
DATABASE_SERVICE_ENABLED=true

# Database direct connections (backup)
DB_URL=jdbc:postgresql://prod-postgres:5432/rest_prod
DB_USERNAME=prod_user
DB_PASSWORD=secure_password

MONGODB_URI=mongodb://prod-mongo:27017/rest_server

# Redis
REDIS_HOST=prod-redis
REDIS_PORT=6379
REDIS_PASSWORD=redis_password

# Kafka
KAFKA_BOOTSTRAP_SERVERS=prod-kafka:9092

# External APIs
NEWSAPI_KEY=production-key
BINANCE_API_KEY=production-key
BINANCE_SECRET_KEY=production-secret

# Logging
LOG_LEVEL=INFO
APP_LOG_LEVEL=INFO
```

### Step 2: Build Production Image

```bash
# Build optimized JAR
./gradlew clean build -Pprod

# Build Docker image with version tag
docker build -t rest-server:2.0.0 .
docker tag rest-server:2.0.0 rest-server:latest

# Push to registry (if using)
docker push yourdomain/rest-server:2.0.0
```

### Step 3: Deploy with Docker Swarm

**Initialize Swarm:**
```bash
docker swarm init
```

**Create Stack File (`docker-stack.yml`):**
```yaml
version: '3.8'

services:
  rest-server:
    image: rest-server:2.0.0
    deploy:
      replicas: 2
      resources:
        limits:
          cpus: '2'
          memory: 4G
        reservations:
          cpus: '1'
          memory: 2G
      restart_policy:
        condition: on-failure
        delay: 5s
        max_attempts: 3
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DATABASE_SERVICE_URL=http://database-service:8080
    networks:
      - app-network
    healthcheck:
      test: ["CMD", "wget", "--quiet", "--tries=1", "--spider", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3

networks:
  app-network:
    driver: overlay
```

**Deploy Stack:**
```bash
docker stack deploy -c docker-stack.yml rest-server-stack

# Check services
docker service ls

# View logs
docker service logs -f rest-server-stack_rest-server
```

### Step 4: Setup Nginx Load Balancer

**Install Nginx:**
```bash
sudo apt update
sudo apt install nginx
```

**Configure Nginx (`/etc/nginx/sites-available/rest-server`):**
```nginx
upstream rest_server {
    least_conn;
    server localhost:8080 weight=1;
    server localhost:8081 weight=1;
}

server {
    listen 80;
    server_name api.yourdomain.com;

    # Redirect to HTTPS
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name api.yourdomain.com;

    # SSL certificates
    ssl_certificate /etc/letsencrypt/live/api.yourdomain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/api.yourdomain.com/privkey.pem;

    # SSL configuration
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_prefer_server_ciphers on;
    ssl_ciphers ECDHE-ECDSA-AES128-GCM-SHA256:ECDHE-RSA-AES128-GCM-SHA256;

    # Security headers
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;

    # Rate limiting
    limit_req_zone $binary_remote_addr zone=api_limit:10m rate=10r/s;
    limit_req zone=api_limit burst=20 nodelay;

    location / {
        proxy_pass http://rest_server;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        # Timeouts
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;

        # WebSocket support
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }

    # Health check endpoint
    location /actuator/health {
        proxy_pass http://rest_server;
        access_log off;
    }
}
```

**Enable Site:**
```bash
sudo ln -s /etc/nginx/sites-available/rest-server /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx
```

### Step 5: Setup SSL with Let's Encrypt

```bash
# Install certbot
sudo apt install certbot python3-certbot-nginx

# Obtain certificate
sudo certbot --nginx -d api.yourdomain.com

# Auto-renewal
sudo certbot renew --dry-run
```

---

## 🗄️ Database Service Setup

### Option 1: Use External Database Service

**Clone and Setup:**
```bash
# Clone Database Service
cd /opt
git clone https://github.com/YouSangSon/database-service.git
cd database-service

# Configure
cp .env.example .env
nano .env

# Start
docker-compose up -d
```

**Configure REST Server:**
```bash
DATABASE_SERVICE_URL=http://localhost:8080
DATABASE_SERVICE_ENABLED=true
```

### Option 2: Direct Database Connections

**Disable Database Service:**
```bash
DATABASE_SERVICE_ENABLED=false
```

**Configure Direct Connections:**
```bash
# PostgreSQL
DB_URL=jdbc:postgresql://prod-postgres:5432/rest_prod
DB_USERNAME=prod_user
DB_PASSWORD=secure_password

# MongoDB
MONGODB_URI=mongodb://prod-mongo:27017/rest_server
```

---

## ⚙️ Environment Configuration

### Configuration Profiles

**Development (`application-dev.yml`):**
```yaml
logging:
  level:
    yousang.rest: DEBUG
    org.springframework: INFO

spring:
  datasource:
    hikari:
      maximum-pool-size: 10
```

**Production (`application-prod.yml`):**
```yaml
logging:
  level:
    yousang.rest: INFO
    org.springframework: WARN

spring:
  datasource:
    hikari:
      maximum-pool-size: 50
      minimum-idle: 20
      connection-timeout: 30000

server:
  compression:
    enabled: true
```

### Secrets Management

**Option 1: Environment Variables**
```bash
export JWT_SECRET=$(openssl rand -base64 64)
```

**Option 2: Docker Secrets**
```bash
# Create secret
echo "my-secret-value" | docker secret create jwt_secret -

# Use in docker-compose
services:
  rest-server:
    secrets:
      - jwt_secret
    environment:
      - JWT_SECRET_FILE=/run/secrets/jwt_secret

secrets:
  jwt_secret:
    external: true
```

**Option 3: HashiCorp Vault** (via Database Service)
```yaml
vault:
  enabled: true
  url: http://vault:8200
  token: ${VAULT_TOKEN}
```

---

## 📊 Health Checks & Monitoring

### Health Check Endpoints

```bash
# Application health
curl https://api.yourdomain.com/actuator/health

# Database connectivity
curl https://api.yourdomain.com/actuator/health/db

# Detailed health (authenticated)
curl -H "Authorization: Bearer $TOKEN" \
  https://api.yourdomain.com/actuator/health?details=true
```

### Prometheus Metrics

**Configure Prometheus (`prometheus.yml`):**
```yaml
scrape_configs:
  - job_name: 'rest-server'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['rest-server:8080']
```

**Start Prometheus:**
```bash
docker run -d \
  -p 9090:9090 \
  -v $(pwd)/prometheus.yml:/etc/prometheus/prometheus.yml \
  prom/prometheus
```

### Grafana Dashboards

**Start Grafana:**
```bash
docker run -d \
  -p 3000:3000 \
  --name=grafana \
  grafana/grafana
```

**Import Dashboards:**
1. Access http://localhost:3000
2. Login (admin/admin)
3. Import dashboard ID: 4701 (JVM Micrometer)

### Log Aggregation

**Using ELK Stack:**
```yaml
# docker-compose.yml
services:
  elasticsearch:
    image: elasticsearch:8.x

  logstash:
    image: logstash:8.x

  kibana:
    image: kibana:8.x
    ports:
      - "5601:5601"
```

**Configure Logback:**
```xml
<appender name="LOGSTASH" class="net.logstash.logback.appender.LogstashTcpSocketAppender">
    <destination>logstash:5000</destination>
</appender>
```

---

## 🔧 Troubleshooting

### Common Issues

#### Issue 1: Application Won't Start

**Symptom:**
```
Error: Port 8080 already in use
```

**Solution:**
```bash
# Find process using port
lsof -i :8080

# Kill process
kill -9 <PID>

# Or change port
export APP_PORT=8081
```

#### Issue 2: Database Connection Failed

**Symptom:**
```
Unable to connect to PostgreSQL
```

**Solution:**
```bash
# Check database is running
docker ps | grep postgres

# Check connection
psql -h localhost -U postgres -d rest_dev

# Check environment variables
env | grep DB_
```

#### Issue 3: Out of Memory

**Symptom:**
```
java.lang.OutOfMemoryError: Java heap space
```

**Solution:**
```bash
# Increase heap size
export JAVA_OPTS="-Xms2g -Xmx4g"

# Or in Dockerfile
ENV JAVA_OPTS="-Xms2g -Xmx4g"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

#### Issue 4: Slow API Responses

**Symptom:**
Response time > 1 second

**Solution:**
1. Check database indexes:
```sql
-- PostgreSQL
EXPLAIN ANALYZE SELECT * FROM sns_users WHERE email = 'test@example.com';
```

2. Enable query logging:
```yaml
logging:
  level:
    org.hibernate.SQL: DEBUG
```

3. Monitor with Actuator:
```bash
curl http://localhost:8080/actuator/metrics/http.server.requests
```

### Health Check Script

```bash
#!/bin/bash

# health_check.sh

URL="http://localhost:8080/actuator/health"
TIMEOUT=5

response=$(curl -s -o /dev/null -w "%{http_code}" --max-time $TIMEOUT $URL)

if [ $response -eq 200 ]; then
    echo "✅ Application is healthy"
    exit 0
else
    echo "❌ Application is unhealthy (HTTP $response)"
    exit 1
fi
```

---

## 📝 Deployment Checklist

### Pre-Deployment

- [ ] All tests passing (`./gradlew test`)
- [ ] Build successful (`./gradlew build`)
- [ ] Environment variables configured
- [ ] Database migrations applied
- [ ] SSL certificates installed
- [ ] Secrets configured
- [ ] Monitoring setup complete

### Deployment

- [ ] Backup current database
- [ ] Deploy new version
- [ ] Run smoke tests
- [ ] Monitor error rates
- [ ] Check health endpoints
- [ ] Verify external integrations

### Post-Deployment

- [ ] Monitor logs for errors
- [ ] Check performance metrics
- [ ] Verify user functionality
- [ ] Update documentation
- [ ] Notify team
- [ ] Plan rollback if needed

---

## 🔄 Rollback Procedure

### Docker Swarm Rollback

```bash
# Rollback to previous version
docker service rollback rest-server-stack_rest-server

# Or deploy specific version
docker service update \
  --image rest-server:1.9.0 \
  rest-server-stack_rest-server
```

### Manual Rollback

```bash
# Stop current version
docker-compose down

# Checkout previous version
git checkout v1.9.0

# Rebuild and start
./gradlew build
docker-compose up -d
```

---

## 📚 Additional Resources

- [Complete Architecture](./COMPLETE_ARCHITECTURE.md)
- [Database Service Setup](./DATABASE_SERVICE_SETUP.md)
- [Developer Guide](./DEVELOPER_GUIDE.md)
- [API Documentation](./SNS_API_DOCUMENTATION.md)

---

**Last Updated:** 2025-11-14
**Version:** 1.0.0
