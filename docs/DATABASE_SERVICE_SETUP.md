# Database Service 설치 및 설정 가이드

## 📋 목차

1. [개요](#개요)
2. [설치 방법](#설치-방법)
3. [설정](#설정)
4. [테스트](#테스트)
5. [문제 해결](#문제-해결)

## 개요

이 프로젝트는 [database-service](https://github.com/YouSangSon/database-service)를 통합하여 엔터프라이즈급 데이터 관리를 구현합니다.

### Database Service를 사용하는 이유

**장점:**
- ✅ **6개 데이터베이스** 통합 지원 (MongoDB, PostgreSQL, MySQL, Cassandra, Elasticsearch, Vitess)
- ✅ **통일된 REST API** - 모든 DB에 동일한 인터페이스 제공
- ✅ **고급 기능** - Optimistic Locking, Bulk Operations, CDC, Full-text Search
- ✅ **관측성** - OpenTelemetry/Jaeger 트레이싱, Prometheus/Grafana 메트릭
- ✅ **보안** - HashiCorp Vault 통합

**단점:**
- ⚠️ 1 hop 추가로 인한 약간의 레이턴시 증가
- ⚠️ 추가 서비스 운영 필요

### 아키텍처

```
Trading Bot (REST Server)
    ↓ REST API
Database Service
    ↓ Direct Connection
MongoDB / PostgreSQL / MySQL / Cassandra / Elasticsearch / Vitess
```

## 설치 방법

### Option 1: Docker Compose로 통합 실행 (권장)

**Step 1: Database Service 클론**

```bash
cd /home/user
git clone https://github.com/YouSangSon/database-service.git
cd database-service
```

**Step 2: Database Service Docker Compose 실행**

```bash
# 전체 스택 실행 (MongoDB, PostgreSQL, Kafka, Redis, etc.)
docker-compose up -d

# 서비스 상태 확인
docker-compose ps
```

**포함된 서비스:**
- Database Service API (8080)
- MongoDB (27017)
- PostgreSQL (5432)
- MySQL (3306)
- Cassandra (9042)
- Elasticsearch (9200)
- Vitess (15991)
- Redis (6379)
- Kafka (9092)
- HashiCorp Vault (8200)
- Jaeger (16686)
- Grafana (3000)
- Prometheus (9090)

**Step 3: Trading Bot 설정**

```bash
cd /home/user/rest_server

# .env 파일 생성
cp .env.example .env

# .env 파일 수정
nano .env
```

**.env 설정:**
```bash
DATABASE_SERVICE_ENABLED=true
DATABASE_SERVICE_URL=http://localhost:8080

# 나머지 API 키 설정...
TELEGRAM_BOT_TOKEN=your-token
NEWSAPI_KEY=your-key
```

**Step 4: Trading Bot 실행**

```bash
# Gradle로 실행
./gradlew bootRun

# 또는 Docker Compose로 실행
docker-compose up -d rest-server
```

### Option 2: Database Service만 단독 실행

Trading Bot의 docker-compose.yml에서 database-service 주석 해제:

```yaml
# docker-compose.yml 수정
# database-service 섹션의 주석 해제
```

```bash
# 전체 스택 실행
docker-compose up -d
```

### Option 3: 외부 Database Service 사용

이미 실행 중인 Database Service가 있다면:

```bash
# .env 파일 설정
DATABASE_SERVICE_ENABLED=true
DATABASE_SERVICE_URL=http://your-database-service-host:8080
```

## 설정

### application.yml 설정

```yaml
# Database Service 활성화
database-service:
  url: ${DATABASE_SERVICE_URL:http://localhost:8080}
  enabled: ${DATABASE_SERVICE_ENABLED:true}
  timeout: ${DATABASE_SERVICE_TIMEOUT:30000}
```

### 환경 변수

| 변수 | 기본값 | 설명 |
|------|--------|------|
| `DATABASE_SERVICE_ENABLED` | `true` | Database Service 사용 여부 |
| `DATABASE_SERVICE_URL` | `http://localhost:8080` | Database Service URL |
| `DATABASE_SERVICE_TIMEOUT` | `30000` | 타임아웃 (ms) |

### Database Service 비활성화 (Direct Access)

Database Service를 사용하지 않고 직접 연결하려면:

```bash
DATABASE_SERVICE_ENABLED=false
```

이 경우 다음 Repository가 활성화됩니다:
- `NewsRepositoryMongoAdapter` (MongoDB 직접 연결)
- `OrderRepositoryJpaAdapter` (PostgreSQL JPA 직접 연결)

## 테스트

### 1. Database Service Health Check

```bash
# Database Service 응답 확인
curl http://localhost:8080/health

# 예상 응답:
# {"status":"UP","timestamp":"2025-11-13T..."}
```

### 2. Trading Bot Health Check

```bash
# Trading Bot 응답 확인
curl http://localhost:8080/actuator/health

# Database Service 연결 확인
curl http://localhost:8080/actuator/health/databaseService
```

### 3. 뉴스 API 테스트

```bash
# 뉴스 수집 트리거 (Database Service를 통해 MongoDB에 저장)
curl -X POST http://localhost:8080/api/news/collect \
  -H "Content-Type: application/json" \
  -d '{
    "keywords": ["Bitcoin", "Ethereum"],
    "limit": 10
  }'

# 저장된 뉴스 조회
curl http://localhost:8080/api/news/latest?limit=5
```

### 4. 주문 API 테스트

```bash
# 주문 생성 (Database Service를 통해 PostgreSQL에 저장)
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "userId": 1,
    "symbol": "BTC/USDT",
    "exchange": "BINANCE",
    "type": "LIMIT",
    "side": "BUY",
    "quantity": 0.001,
    "price": 43000
  }'

# 주문 조회
curl http://localhost:8080/api/orders/1
```

### 5. Database Service 직접 테스트

```bash
# MongoDB에 직접 데이터 생성
curl -X POST http://localhost:8080/api/v1/create \
  -H "Content-Type: application/json" \
  -H "X-Database-Type: mongodb" \
  -d '{
    "collection": "test_collection",
    "document": {
      "name": "Test Document",
      "value": 123,
      "timestamp": "2025-11-13T10:00:00Z"
    }
  }'

# PostgreSQL에 직접 데이터 생성
curl -X POST http://localhost:8080/api/v1/create \
  -H "Content-Type: application/json" \
  -H "X-Database-Type: postgres" \
  -d '{
    "collection": "test_table",
    "document": {
      "name": "Test Row",
      "value": 456
    }
  }'
```

## 문제 해결

### ❌ Database Service 연결 실패

**증상:**
```
Failed to connect to database-service: Connection refused
```

**해결:**
```bash
# Database Service 실행 확인
docker ps | grep database-service

# Database Service 로그 확인
docker logs rest-database-service

# Database Service 재시작
cd /home/user/database-service
docker-compose restart database-service
```

### ❌ MongoDB/PostgreSQL 연결 실패

**증상:**
```
Database Service returned error: Failed to connect to MongoDB
```

**해결:**
```bash
# MongoDB 실행 확인
docker ps | grep mongodb

# PostgreSQL 실행 확인
docker ps | grep postgres

# Database Service 환경 변수 확인
docker exec rest-database-service env | grep MONGO
docker exec rest-database-service env | grep POSTGRES
```

### ❌ 성능 문제

**증상:**
- 요청이 느림 (> 1초)

**해결:**

1. **Bulk Operations 사용:**
```kotlin
// Bad: 100번 개별 요청
articles.forEach { newsRepository.save(it) }

// Good: 1번 bulk 요청
newsRepository.saveAll(articles)
```

2. **인덱스 생성:**
```bash
# Database Service를 통해 인덱스 생성
curl -X POST http://localhost:8080/api/v1/create-index \
  -H "Content-Type: application/json" \
  -H "X-Database-Type: mongodb" \
  -d '{
    "collection": "news_articles",
    "keys": {"publishedAt": -1, "sentiment": 1},
    "unique": false,
    "name": "idx_published_sentiment"
  }'
```

3. **커넥션 풀 증가:**
```yaml
# database-service docker-compose.yml
environment:
  - DB_POOL_SIZE=50
  - DB_MAX_IDLE=20
```

### ❌ Optimistic Lock 충돌

**증상:**
```
409 Conflict: Version mismatch
```

**해결:**
```kotlin
// 재시도 로직 추가
fun saveOrderWithRetry(order: Order, maxRetries: Int = 3): Order {
    var attempt = 0
    while (attempt < maxRetries) {
        try {
            return orderRepository.save(order)
        } catch (e: OptimisticLockException) {
            attempt++
            if (attempt >= maxRetries) throw e

            // 최신 버전 다시 가져오기
            val latest = orderRepository.findByOrderId(order.orderId)
            order.version = latest?.version ?: 0
        }
    }
}
```

### ❌ CDC 이벤트가 전송되지 않음

**증상:**
- Kafka에서 변경 이벤트가 수신되지 않음

**해결:**
```bash
# CDC 활성화 확인
docker exec rest-database-service env | grep CDC_ENABLED

# Kafka 토픽 확인
docker exec -it rest-kafka kafka-topics --list --bootstrap-server localhost:9092

# Kafka 메시지 확인
docker exec -it rest-kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic db.news_articles.changes \
  --from-beginning
```

## 모니터링

### Grafana 대시보드

**URL:** http://localhost:3000

**기본 로그인:**
- Username: `admin`
- Password: `admin`

**주요 메트릭:**
- Database connection pool usage
- Query latency (P50, P95, P99)
- Request throughput (req/s)
- Error rate

### Jaeger 분산 트레이싱

**URL:** http://localhost:16686

**추적 가능한 흐름:**
```
Trading Bot → Database Service → MongoDB
Trading Bot → Database Service → PostgreSQL
Kafka Event → Database Service → CDC
```

### Prometheus Metrics

**URL:** http://localhost:9090

**유용한 쿼리:**
```promql
# Database Service 요청 속도
rate(http_requests_total{service="database-service"}[5m])

# 에러율
rate(http_requests_total{service="database-service",status=~"5.."}[5m])

# P95 레이턴시
histogram_quantile(0.95, rate(http_request_duration_seconds_bucket[5m]))

# Connection pool usage
database_connection_pool_active / database_connection_pool_max
```

## 추가 자료

- [Database Service GitHub](https://github.com/YouSangSon/database-service)
- [REST API 명세서](https://github.com/YouSangSon/database-service/blob/main/docs/REST_API_SPECIFICATION.md)
- [gRPC 사용법](https://github.com/YouSangSon/database-service/blob/main/docs/GRPC_USAGE.md)
- [모니터링 가이드](https://github.com/YouSangSon/database-service/blob/main/docs/MONITORING.md)
- [Trading Bot Database Service 통합 가이드](../README_DATABASE_SERVICE.md)

---

**Database Service 통합 완료! 🎉**
