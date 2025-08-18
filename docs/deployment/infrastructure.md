# 배포 및 인프라 구성 가이드

## 📋 개요

이 문서는 REST Server 프로젝트의 배포 및 인프라 구성에 대한 상세한 가이드를 제공합니다. 개발 환경부터 프로덕션 환경까지 다양한 배포 시나리오를 다루며, 컨테이너화, 오케스트레이션, 모니터링 등의 내용을 포함합니다.

## 🏗️ 인프라 아키텍처

### 1. 전체 인프라 구조

```
┌─────────────────────────────────────────────────────────────────┐
│                        클라이언트 (Clients)                      │
│  - 웹 브라우저                                                │
│  - 모바일 앱                                                  │
│  - API 클라이언트                                             │
└─────────────────────┬───────────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────────┐
│                    로드 밸런서 (Load Balancer)                   │
│  - Nginx / HAProxy / AWS ALB                                   │
│  - SSL/TLS 종료                                                │
│  - 헬스 체크                                                   │
└─────────────────────┬───────────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────────┐
│                  API Gateway / Ingress                          │
│  - Spring Cloud Gateway                                        │
│  - Kubernetes Ingress                                          │
│  - 라우팅 및 인증                                              │
└─────────────────────┬───────────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────────┐
│                    애플리케이션 서버 (App Servers)               │
│  - REST Server 인스턴스 (여러 개)                              │
│  - Spring Boot 애플리케이션                                    │
│  - JVM 기반 실행 환경                                          │
└─────────────────────┬───────────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────────┐
│                      데이터 계층 (Data Layer)                    │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐            │
│  │ PostgreSQL  │  │ Redis       │  │ File        │            │
│  │ (Primary)   │  │ Cache       │  │ Storage     │            │
│  └─────────────┘  └─────────────┘  └─────────────┘            │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐            │
│  │ PostgreSQL  │  │ Monitoring  │  │ Log         │            │
│  │ (Replica)   │  │ Stack       │  │ Aggregation │            │
│  └─────────────┘  └─────────────┘  └─────────────┘            │
└─────────────────────────────────────────────────────────────────┘
```

### 2. 환경별 구성

#### 개발 환경 (Development)
- **서버**: 로컬 개발 머신 또는 개발 서버
- **데이터베이스**: 로컬 PostgreSQL 또는 Docker
- **스케일**: 단일 인스턴스
- **모니터링**: 기본 로깅

#### 스테이징 환경 (Staging)
- **서버**: 클라우드 VM 또는 컨테이너
- **데이터베이스**: 전용 PostgreSQL 인스턴스
- **스케일**: 2-3 인스턴스
- **모니터링**: 기본 모니터링 + 로그 집계

#### 프로덕션 환경 (Production)
- **서버**: 클라우드 인스턴스 또는 Kubernetes
- **데이터베이스**: 고가용성 PostgreSQL 클러스터
- **스케일**: 자동 스케일링 (5-20 인스턴스)
- **모니터링**: 전체 모니터링 스택 + 알림

## 🐳 컨테이너화

### 1. Docker 이미지 빌드

#### Jib을 사용한 이미지 빌드

```bash
# 환경 변수 설정
export DOCKER_REGISTRY_URL=your-registry.com
export DB_URL=jdbc:postgresql://prod-db:5432/rest_prod
export DB_USERNAME=rest_user
export DB_PASSWORD=secure_password

# 이미지 빌드 및 푸시
./gradlew jib

# 특정 태그로 빌드
./gradlew jib -Djib.to.tags=v1.0.0,latest
```

#### Dockerfile을 사용한 이미지 빌드

```dockerfile
# Dockerfile
FROM eclipse-temurin:21-jre-alpine

# 메타데이터
LABEL maintainer="Development Team"
LABEL version="1.0.0"
LABEL description="REST Server Application"

# 작업 디렉토리 설정
WORKDIR /app

# 애플리케이션 JAR 파일 복사
COPY build/libs/rest_server-*.jar app.jar

# 포트 노출
EXPOSE 8080

# JVM 옵션 설정
ENV JAVA_OPTS="-Xms512m -Xmx1g -XX:+UseContainerSupport -XX:MaxRAMPercentage=75"

# 헬스 체크
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# 애플리케이션 실행
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

#### Docker Compose 설정

```yaml
# docker-compose.yml
version: '3.8'

services:
  rest-server:
    image: your-registry.com/rest-server:latest
    container_name: rest-server
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DB_URL=jdbc:postgresql://postgres:5432/rest_prod
      - DB_USERNAME=rest_user
      - DB_PASSWORD=secure_password
      - LOG_LEVEL=INFO
    depends_on:
      - postgres
      - redis
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "wget", "--no-verbose", "--tries=1", "--spider", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 40s

  postgres:
    image: postgres:15-alpine
    container_name: postgres
    environment:
      - POSTGRES_DB=rest_prod
      - POSTGRES_USER=rest_user
      - POSTGRES_PASSWORD=secure_password
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./init-scripts:/docker-entrypoint-initdb.d
    ports:
      - "5432:5432"
    restart: unless-stopped

  redis:
    image: redis:7-alpine
    container_name: redis
    ports:
      - "6379:6379"
    restart: unless-stopped

volumes:
  postgres_data:
```

### 2. 컨테이너 최적화

#### 멀티 스테이지 빌드

```dockerfile
# 멀티 스테이지 Dockerfile
FROM gradle:8.5-jdk21 AS builder

WORKDIR /app
COPY . .
RUN gradle build --no-daemon

FROM eclipse-temurin:21-jre-alpine AS runtime

RUN apk add --no-cache curl

WORKDIR /app
COPY --from=builder /app/build/libs/rest_server-*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### 보안 최적화

```dockerfile
# 보안 강화된 Dockerfile
FROM eclipse-temurin:21-jre-alpine

# 비루트 사용자 생성
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# 필요한 패키지만 설치
RUN apk add --no-cache curl

WORKDIR /app

# 애플리케이션 파일 복사
COPY --chown=appuser:appgroup build/libs/rest_server-*.jar app.jar

# 비루트 사용자로 전환
USER appuser

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

## ☸️ Kubernetes 배포

### 1. 기본 배포 매니페스트

#### Deployment

```yaml
# k8s/deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: rest-server
  labels:
    app: rest-server
    version: v1.0.0
spec:
  replicas: 3
  selector:
    matchLabels:
      app: rest-server
  template:
    metadata:
      labels:
        app: rest-server
        version: v1.0.0
    spec:
      containers:
      - name: rest-server
        image: your-registry.com/rest-server:v1.0.0
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: DB_URL
          valueFrom:
            secretKeyRef:
              name: db-secret
              key: url
        - name: DB_USERNAME
          valueFrom:
            secretKeyRef:
              name: db-secret
              key: username
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: db-secret
              key: password
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        securityContext:
          runAsNonRoot: true
          runAsUser: 1001
          allowPrivilegeEscalation: false
          readOnlyRootFilesystem: true
```

#### Service

```yaml
# k8s/service.yaml
apiVersion: v1
kind: Service
metadata:
  name: rest-server-service
  labels:
    app: rest-server
spec:
  type: ClusterIP
  ports:
  - port: 80
    targetPort: 8080
    protocol: TCP
  selector:
    app: rest-server
```

#### Ingress

```yaml
# k8s/ingress.yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: rest-server-ingress
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /
    nginx.ingress.kubernetes.io/ssl-redirect: "true"
    cert-manager.io/cluster-issuer: "letsencrypt-prod"
spec:
  tls:
  - hosts:
    - api.yourdomain.com
    secretName: rest-server-tls
  rules:
  - host: api.yourdomain.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: rest-server-service
            port:
              number: 80
```

#### ConfigMap 및 Secret

```yaml
# k8s/configmap.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: rest-server-config
data:
  application.yml: |
    spring:
      application:
        name: rest
      webflux:
        base-path: /api/v1
      datasource:
        driver-class-name: org.postgresql.Driver
        hikari:
          maximum-pool-size: 20
          minimum-idle: 5
    logging:
      level:
        root: INFO
        yousang.rest: INFO
    server:
      port: 8080
      compression:
        enabled: true
        mime-types: application/json,application/xml
        min-response-size: 1KB

---
# k8s/secret.yaml
apiVersion: v1
kind: Secret
metadata:
  name: db-secret
type: Opaque
data:
  url: amRiYzpwb3N0Z3Jlc3FsOi8vcG9zdGdyZXMtc2VydmljZTU0MzIvcmVzdF9wcm9k
  username: cmVzdF91c2Vy
  password: c2VjdXJlX3Bhc3N3b3Jk
```

### 2. 고급 Kubernetes 기능

#### Horizontal Pod Autoscaler

```yaml
# k8s/hpa.yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: rest-server-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: rest-server
  minReplicas: 3
  maxReplicas: 20
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
  behavior:
    scaleDown:
      stabilizationWindowSeconds: 300
      policies:
      - type: Percent
        value: 10
        periodSeconds: 60
    scaleUp:
      stabilizationWindowSeconds: 60
      policies:
      - type: Percent
        value: 100
        periodSeconds: 15
```

#### Pod Disruption Budget

```yaml
# k8s/pdb.yaml
apiVersion: policy/v1
kind: PodDisruptionBudget
metadata:
  name: rest-server-pdb
spec:
  minAvailable: 2
  selector:
    matchLabels:
      app: rest-server
```

## 🚀 배포 자동화

### 1. CI/CD 파이프라인

#### GitHub Actions

```yaml
# .github/workflows/deploy.yml
name: Deploy to Production

on:
  push:
    tags:
      - 'v*'

jobs:
  build-and-deploy:
    runs-on: ubuntu-latest
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v4
      
    - name: Set up JDK 21
      uses: actions/setup-java@v4
      with:
        java-version: '21'
        distribution: 'temurin'
        
    - name: Build with Gradle
      run: ./gradlew build
      
    - name: Build Docker image
      run: |
        docker build -t your-registry.com/rest-server:${{ github.ref_name }} .
        docker push your-registry.com/rest-server:${{ github.ref_name }}
        
    - name: Deploy to Kubernetes
      run: |
        kubectl set image deployment/rest-server rest-server=your-registry.com/rest-server:${{ github.ref_name }}
        kubectl rollout status deployment/rest-server
        
    - name: Health check
      run: |
        kubectl rollout status deployment/rest-server
        curl -f http://api.yourdomain.com/actuator/health
```

#### GitLab CI/CD

```yaml
# .gitlab-ci.yml
stages:
  - build
  - test
  - deploy

variables:
  DOCKER_REGISTRY: your-registry.com
  IMAGE_NAME: rest-server

build:
  stage: build
  image: gradle:8.5-jdk21
  script:
    - ./gradlew build
  artifacts:
    paths:
      - build/libs/*.jar
    expire_in: 1 week

test:
  stage: test
  image: gradle:8.5-jdk21
  script:
    - ./gradlew test
    - ./gradlew integrationTest

deploy:
  stage: deploy
  image: bitnami/kubectl:latest
  script:
    - kubectl config use-context $KUBE_CONTEXT
    - kubectl set image deployment/rest-server rest-server=$DOCKER_REGISTRY/$IMAGE_NAME:$CI_COMMIT_TAG
    - kubectl rollout status deployment/rest-server
  only:
    - tags
  environment:
    name: production
    url: https://api.yourdomain.com
```

### 2. 배포 전략

#### Rolling Update

```bash
# 롤링 업데이트 실행
kubectl rollout restart deployment/rest-server

# 업데이트 상태 확인
kubectl rollout status deployment/rest-server

# 업데이트 이력 확인
kubectl rollout history deployment/rest-server
```

#### Blue-Green Deployment

```yaml
# k8s/blue-green-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: rest-server-blue
  labels:
    app: rest-server
    version: blue
spec:
  replicas: 3
  selector:
    matchLabels:
      app: rest-server
      version: blue
  template:
    metadata:
      labels:
        app: rest-server
        version: blue
    spec:
      containers:
      - name: rest-server
        image: your-registry.com/rest-server:v1.0.0
        # ... 기타 설정

---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: rest-server-green
  labels:
    app: rest-server
    version: green
spec:
  replicas: 0  # 초기에는 0개
  selector:
    matchLabels:
      app: rest-server
      version: green
  template:
    metadata:
      labels:
        app: rest-server
        version: green
    spec:
      containers:
      - name: rest-server
        image: your-registry.com/rest-server:v1.1.0
        # ... 기타 설정
```

#### Canary Deployment

```yaml
# k8s/canary-deployment.yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: rest-server-canary
  annotations:
    nginx.ingress.kubernetes.io/canary: "true"
    nginx.ingress.kubernetes.io/canary-weight: "10"  # 10% 트래픽
spec:
  rules:
  - host: api.yourdomain.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: rest-server-canary-service
            port:
              number: 80
```

## 📊 모니터링 및 로깅

### 1. 모니터링 스택

#### Prometheus + Grafana

```yaml
# k8s/monitoring.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: prometheus-config
data:
  prometheus.yml: |
    global:
      scrape_interval: 15s
    scrape_configs:
    - job_name: 'rest-server'
      static_configs:
      - targets: ['rest-server-service:8080']
      metrics_path: /actuator/prometheus
      scrape_interval: 5s

---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: prometheus
spec:
  replicas: 1
  selector:
    matchLabels:
      app: prometheus
  template:
    metadata:
      labels:
        app: prometheus
    spec:
      containers:
      - name: prometheus
        image: prom/prometheus:latest
        ports:
        - containerPort: 9090
        volumeMounts:
        - name: config
          mountPath: /etc/prometheus
      volumes:
      - name: config
        configMap:
          name: prometheus-config
```

#### Spring Boot Actuator 설정

```yaml
# application-prod.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when-authorized
      show-components: always
    metrics:
      enabled: true
  metrics:
    export:
      prometheus:
        enabled: true
    tags:
      application: ${spring.application.name}
      environment: ${spring.profiles.active}
  health:
    redis:
      enabled: true
    db:
      enabled: true
    disk:
      enabled: true
```

### 2. 로그 집계

#### ELK Stack (Elasticsearch, Logstash, Kibana)

```yaml
# k8s/logging.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: elasticsearch
spec:
  replicas: 3
  selector:
    matchLabels:
      app: elasticsearch
  template:
    metadata:
      labels:
        app: elasticsearch
    spec:
      containers:
      - name: elasticsearch
        image: docker.elastic.co/elasticsearch/elasticsearch:8.11.0
        env:
        - name: discovery.type
          value: single-node
        - name: xpack.security.enabled
          value: "false"
        ports:
        - containerPort: 9200
        resources:
          requests:
            memory: "1Gi"
            cpu: "500m"
          limits:
            memory: "2Gi"
            cpu: "1000m"

---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: kibana
spec:
  replicas: 1
  selector:
    matchLabels:
      app: kibana
  template:
    metadata:
      labels:
        app: kibana
    spec:
      containers:
      - name: kibana
        image: docker.elastic.co/kibana/kibana:8.11.0
        env:
        - name: ELASTICSEARCH_HOSTS
          value: "http://elasticsearch:9200"
        ports:
        - containerPort: 5601
```

## 🔒 보안 설정

### 1. 네트워크 보안

#### Network Policies

```yaml
# k8s/network-policy.yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: rest-server-network-policy
spec:
  podSelector:
    matchLabels:
      app: rest-server
  policyTypes:
  - Ingress
  - Egress
  ingress:
  - from:
    - namespaceSelector:
        matchLabels:
          name: ingress-nginx
    ports:
    - protocol: TCP
      port: 8080
  egress:
  - to:
    - namespaceSelector:
        matchLabels:
          name: database
    ports:
    - protocol: TCP
      port: 5432
  - to:
    - namespaceSelector:
        matchLabels:
          name: monitoring
    ports:
    - protocol: TCP
      port: 9090
```

#### Pod Security Standards

```yaml
# k8s/pod-security.yaml
apiVersion: v1
kind: Pod
metadata:
  name: rest-server-secure
spec:
  securityContext:
    runAsNonRoot: true
    runAsUser: 1001
    fsGroup: 1001
  containers:
  - name: rest-server
    image: your-registry.com/rest-server:v1.0.0
    securityContext:
      allowPrivilegeEscalation: false
      readOnlyRootFilesystem: true
      capabilities:
        drop:
        - ALL
    volumeMounts:
    - name: tmp
      mountPath: /tmp
    - name: logs
      mountPath: /app/logs
  volumes:
  - name: tmp
    emptyDir: {}
  - name: logs
    emptyDir: {}
```

### 2. 시크릿 관리

#### External Secrets Operator

```yaml
# k8s/external-secret.yaml
apiVersion: external-secrets.io/v1beta1
kind: ExternalSecret
metadata:
  name: db-secret
spec:
  refreshInterval: 1h
  secretStoreRef:
    name: vault-backend
    kind: SecretStore
  target:
    name: db-secret
    type: Opaque
  data:
  - secretKey: url
    remoteRef:
      key: rest-server/db
      property: url
  - secretKey: username
    remoteRef:
      key: rest-server/db
      property: username
  - secretKey: password
    remoteRef:
      key: rest-server/db
      property: password
```

## 📈 성능 최적화

### 1. JVM 튜닝

#### 프로덕션 JVM 옵션

```bash
# JVM 옵션
JAVA_OPTS="
  -server
  -Xms2g
  -Xmx4g
  -XX:+UseG1GC
  -XX:MaxGCPauseMillis=200
  -XX:+UnlockExperimentalVMOptions
  -XX:+UseContainerSupport
  -XX:MaxRAMPercentage=75
  -XX:+HeapDumpOnOutOfMemoryError
  -XX:HeapDumpPath=/tmp
  -XX:+UseGCLogFileRotation
  -XX:NumberOfGCLogFiles=5
  -XX:GCLogFileSize=100M
  -Djava.security.egd=file:/dev/./urandom
  -Dspring.profiles.active=prod
"
```

#### Kubernetes 리소스 설정

```yaml
# 리소스 요청 및 제한
resources:
  requests:
    memory: "1Gi"
    cpu: "500m"
  limits:
    memory: "4Gi"
    cpu: "2000m"
```

### 2. 데이터베이스 최적화

#### PostgreSQL 설정

```sql
-- postgresql.conf 최적화
shared_buffers = 256MB
effective_cache_size = 1GB
maintenance_work_mem = 64MB
checkpoint_completion_target = 0.9
wal_buffers = 16MB
default_statistics_target = 100
random_page_cost = 1.1
effective_io_concurrency = 200
work_mem = 4MB
min_wal_size = 1GB
max_wal_size = 4GB
max_worker_processes = 8
max_parallel_workers_per_gather = 4
max_parallel_workers = 8
max_parallel_maintenance_workers = 4
```

## 🚨 장애 대응

### 1. 자동 복구

#### Pod Restart Policy

```yaml
# 재시작 정책
spec:
  restartPolicy: Always
  containers:
  - name: rest-server
    image: your-registry.com/rest-server:v1.0.0
    livenessProbe:
      httpGet:
        path: /actuator/health
        port: 8080
      initialDelaySeconds: 60
      periodSeconds: 30
      failureThreshold: 3
    readinessProbe:
      httpGet:
        path: /actuator/health/readiness
        port: 8080
      initialDelaySeconds: 30
      periodSeconds: 10
      failureThreshold: 3
```

#### Circuit Breaker 패턴

```kotlin
// Circuit Breaker 구현
@Service
class LottoServiceWithCircuitBreaker(
    private val lottoRepository: LottoRepository
) : LottoService {
    
    private val circuitBreaker = CircuitBreaker.builder()
        .failureRateThreshold(50.0f)
        .waitDurationInOpenState(Duration.ofSeconds(60))
        .ringBufferSizeInHalfOpenState(2)
        .ringBufferSizeInClosedState(10)
        .build()
    
    override suspend fun findById(id: Long): LottoEntity? {
        return circuitBreaker.executeSupplier {
            lottoRepository.findById(id)
        }
    }
}
```

### 2. 백업 및 복구

#### 데이터베이스 백업

```bash
#!/bin/bash
# backup-db.sh

BACKUP_DIR="/backup/postgresql"
DATE=$(date +%Y%m%d_%H%M%S)
DB_NAME="rest_prod"
DB_USER="rest_user"
DB_HOST="postgres-service"

# 백업 디렉토리 생성
mkdir -p $BACKUP_DIR

# PostgreSQL 백업
pg_dump -h $DB_HOST -U $DB_USER -d $DB_NAME > $BACKUP_DIR/backup_$DATE.sql

# 압축
gzip $BACKUP_DIR/backup_$DATE.sql

# 30일 이상 된 백업 삭제
find $BACKUP_DIR -name "backup_*.sql.gz" -mtime +30 -delete

echo "Database backup completed: backup_$DATE.sql.gz"
```

#### 애플리케이션 백업

```yaml
# k8s/backup-job.yaml
apiVersion: batch/v1
kind: CronJob
metadata:
  name: app-backup
spec:
  schedule: "0 2 * * *"  # 매일 새벽 2시
  jobTemplate:
    spec:
      template:
        spec:
          containers:
          - name: backup
            image: bitnami/kubectl:latest
            command:
            - /bin/bash
            - -c
            - |
              kubectl get deployment rest-server -o yaml > /backup/deployment_$(date +%Y%m%d).yaml
              kubectl get configmap rest-server-config -o yaml > /backup/configmap_$(date +%Y%m%d).yaml
          volumes:
          - name: backup-volume
            persistentVolumeClaim:
              claimName: backup-pvc
          restartPolicy: OnFailure
```

---

**문서 버전**: v1.0.0  
**마지막 업데이트**: 2024-12-19  
**작성자**: Development Team
