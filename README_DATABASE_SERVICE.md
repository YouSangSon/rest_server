# Database Service 통합 가이드

## 🎯 개요

이 프로젝트는 [database-service](https://github.com/YouSangSon/database-service)를 통합하여 **엔터프라이즈급 데이터 관리**를 구현합니다.

### Database Service란?

Go 기반의 통합 데이터베이스 서비스로, **6개의 데이터베이스**(MongoDB, PostgreSQL, MySQL, Cassandra, Elasticsearch, Vitess)를 **단일 REST API**로 제공합니다.

## 🚀 주요 이점

### 1. **멀티 데이터베이스 지원**
```kotlin
// MongoDB로 뉴스 저장
databaseServiceClient.create(
    collection = "news_articles",
    document = newsArticle,
    databaseType = "mongodb"
)

// PostgreSQL로 주문 저장
databaseServiceClient.create(
    collection = "orders",
    document = order,
    databaseType = "postgres"
)

// Elasticsearch로 전체 텍스트 검색
databaseServiceClient.search(
    collection = "news_articles",
    searchQuery = "Bitcoin rally",
    databaseType = "elasticsearch"
)
```

### 2. **통합 API**
- 36개의 REST 엔드포인트
- 모든 DB에 동일한 인터페이스
- CRUD + 검색 + 집계 + 트랜잭션

### 3. **고급 기능**
- ✅ Optimistic Locking (버전 관리)
- ✅ Bulk Operations (대량 작업)
- ✅ Atomic Operations (원자적 작업)
- ✅ Full-text Search
- ✅ Index Management
- ✅ CDC (Change Data Capture) via Kafka
- ✅ Dynamic Credentials (HashiCorp Vault)

### 4. **관측성 & 모니터링**
- OpenTelemetry + Jaeger (분산 트레이싱)
- Prometheus + Grafana (메트릭)
- 구조화된 로깅 (Zap)
- 100+ AlertManager 규칙

## 📦 설치 및 실행

### 1. Database Service 실행

```bash
# Repository 클론
git clone https://github.com/YouSangSon/database-service.git
cd database-service

# Docker Compose로 전체 스택 실행
docker-compose up -d

# 서비스 확인
curl http://localhost:8080/health
```

**포함 서비스:**
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

### 2. Trading Bot 설정

**환경 변수:**
```bash
export DATABASE_SERVICE_URL=http://localhost:8080
export DATABASE_SERVICE_ENABLED=true
```

**application.yml:**
```yaml
database-service:
  url: http://localhost:8080
  enabled: true
```

### 3. 확인

```bash
# Trading Bot 실행
./gradlew bootRun

# 로그 확인
tail -f logs/spring.log | grep "Database Service"
```

## 🔧 사용법

### DatabaseServiceClient 직접 사용

```kotlin
@Autowired
lateinit var databaseServiceClient: DatabaseServiceClient

// Create
val newsArticle = NewsArticle(...)
val response = databaseServiceClient.create(
    collection = "news_articles",
    document = newsArticle,
    databaseType = DatabaseServiceClient.DB_MONGODB
)

// Read
val article = databaseServiceClient.findById(
    collection = "news_articles",
    id = "12345",
    databaseType = DatabaseServiceClient.DB_MONGODB,
    responseType = NewsArticle::class.java
)

// Update
databaseServiceClient.update(
    collection = "news_articles",
    id = "12345",
    updates = mapOf(
        "sentiment" to "POSITIVE",
        "sentimentScore" to 0.85
    ),
    databaseType = DatabaseServiceClient.DB_MONGODB
)

// Delete
databaseServiceClient.delete(
    collection = "news_articles",
    id = "12345",
    databaseType = DatabaseServiceClient.DB_MONGODB
)
```

### Repository를 통한 사용 (권장)

```kotlin
@Autowired
lateinit var newsRepository: NewsRepositoryPort

// 내부적으로 Database Service 사용
val article = newsRepository.save(newsArticle)
val articles = newsRepository.findByPublishedAtBetween(from, to)
```

## 🎯 고급 기능

### 1. Optimistic Locking

**동시성 충돌 방지:**
```kotlin
// Version과 함께 업데이트
databaseServiceClient.update(
    collection = "orders",
    id = "ORDER123",
    updates = mapOf("status" to "FILLED"),
    version = 5  // 버전이 일치해야만 업데이트 성공
)
```

### 2. Bulk Operations

**대량 삽입:**
```kotlin
val articles = listOf(article1, article2, article3, ...)

val result = databaseServiceClient.bulkInsert(
    collection = "news_articles",
    documents = articles
)

println("Inserted: ${result.data?.insertedCount}")
```

**대량 업데이트:**
```kotlin
val result = databaseServiceClient.updateMany(
    collection = "orders",
    filter = mapOf("status" to "PENDING"),
    updates = mapOf("status" to "CANCELLED")
)

println("Updated: ${result.data?.modifiedCount}")
```

### 3. Atomic Operations

**Find and Update (원자적):**
```kotlin
val result = databaseServiceClient.findAndUpdate(
    collection = "portfolios",
    filter = mapOf("userId" to 1, "symbol" to "BTC/USDT"),
    updates = mapOf(
        "quantity" to 1.5,
        "avgBuyPrice" to 43000.0
    ),
    returnNew = true,  // 업데이트 후 문서 반환
    upsert = true      // 없으면 생성
)
```

### 4. Full-text Search

```kotlin
val results = databaseServiceClient.search(
    collection = "news_articles",
    searchQuery = "Bitcoin ETF approval",
    fields = listOf("title", "content"),
    limit = 50
)

results.data?.forEach { article ->
    println(article.title)
}
```

### 5. Index Management

```kotlin
// 인덱스 생성 (성능 향상)
databaseServiceClient.createIndex(
    collection = "orders",
    keys = mapOf(
        "userId" to 1,
        "createdAt" to -1  // 내림차순
    ),
    unique = false,
    name = "idx_user_created"
)

// 인덱스 목록 조회
val indexes = databaseServiceClient.listIndexes("orders")
```

### 6. Aggregation Pipeline

**복잡한 집계 쿼리:**
```kotlin
// 사용자별 총 주문 금액 계산
val pipeline = listOf(
    mapOf("\$match" to mapOf("status" to "FILLED")),
    mapOf("\$group" to mapOf(
        "_id" to "\$userId",
        "totalAmount" to mapOf("\$sum" to "\$quantity")
    ))
)

// Raw query로 실행
```

## 🔄 CDC (Change Data Capture)

Database Service는 **Kafka CDC**를 지원하여 데이터 변경사항을 실시간 스트리밍합니다.

### CDC 활성화

**database-service docker-compose.yml:**
```yaml
services:
  database-service:
    environment:
      - CDC_ENABLED=true
      - KAFKA_BROKERS=kafka:9092
```

### 변경 이벤트 수신

**Kotlin (Kafka Consumer):**
```kotlin
@KafkaListener(topics = ["db.news_articles.changes"])
fun handleNewsChanges(event: ChangeEvent) {
    when (event.operation) {
        "INSERT" -> println("New article: ${event.document}")
        "UPDATE" -> println("Updated article: ${event.documentKey}")
        "DELETE" -> println("Deleted article: ${event.documentKey}")
    }
}
```

**활용 사례:**
- 실시간 검색 인덱스 업데이트 (Elasticsearch)
- 캐시 무효화 (Redis)
- 이벤트 기반 알림
- 감사 로그 (Audit Trail)

## 📊 모니터링

### Grafana 대시보드

**URL:** http://localhost:3000

**주요 메트릭:**
- Database connection pool
- Query latency (P50, P95, P99)
- Error rate
- Request throughput
- Cache hit ratio

### Jaeger 분산 트레이싱

**URL:** http://localhost:16686

**추적 가능:**
- API 요청 → Database Service → MongoDB/PostgreSQL
- 전체 트랜잭션 흐름
- 병목 구간 식별

### Prometheus Metrics

**URL:** http://localhost:9090

```promql
# Database Service 요청 속도
rate(http_requests_total{service="database-service"}[5m])

# 에러율
rate(http_requests_total{service="database-service",status=~"5.."}[5m])

# 레이턴시
histogram_quantile(0.95, rate(http_request_duration_seconds_bucket[5m]))
```

## 🔐 보안

### HashiCorp Vault 통합

**동적 자격 증명:**
```bash
# Vault에서 DB 자격 증명 자동 로테이션
export VAULT_ENABLED=true
export VAULT_ADDR=http://localhost:8200

# Database Service가 자동으로 Vault에서 credentials 가져옴
```

**장점:**
- 하드코딩된 비밀번호 제거
- 자동 자격 증명 로테이션
- 접근 감사 로그

## 🎯 Database Service vs Direct Access

| 측면 | Direct Access | Database Service |
|------|---------------|------------------|
| **코드 복잡도** | 높음 (DB별 다른 코드) | 낮음 (통일된 API) |
| **DB 전환** | 어려움 (리팩토링 필요) | 쉬움 (헤더만 변경) |
| **모니터링** | 개별 설정 필요 | 통합 제공 |
| **트레이싱** | 수동 구현 | 자동 제공 |
| **CDC** | 직접 구현 | 기본 제공 |
| **보안** | 수동 관리 | Vault 통합 |
| **성능** | 직접 연결 (빠름) | 1 hop 추가 (약간 느림) |

**권장 사용:**
- **개발/테스트**: Database Service (빠른 프로토타이핑)
- **프로덕션**: Database Service (운영 편의성)
- **초고성능 필요 시**: Direct Access + Database Service 혼용

## 🚀 실전 예제

### 시나리오: 뉴스 기사 수집 및 분석

```kotlin
// 1. 뉴스 수집
val articles = newsApiAdapter.fetchNews(keywords)

// 2. Database Service로 저장 (MongoDB)
val saved = databaseServiceClient.bulkInsert(
    collection = "news_articles",
    documents = articles,
    databaseType = DatabaseServiceClient.DB_MONGODB
)

// 3. ML 감성 분석
articles.forEach { article ->
    val sentiment = mlServiceClient.analyzeSentiment(article.content)

    // 4. 업데이트 (Optimistic Lock)
    databaseServiceClient.update(
        collection = "news_articles",
        id = article.id.toString(),
        updates = mapOf(
            "sentiment" to sentiment.sentimentType.name,
            "sentimentScore" to sentiment.score
        ),
        databaseType = DatabaseServiceClient.DB_MONGODB
    )
}

// 5. Elasticsearch로 전체 텍스트 검색 인덱싱
databaseServiceClient.create(
    collection = "news_articles_search",
    document = articles,
    databaseType = "elasticsearch"
)

// 6. 검색
val results = databaseServiceClient.search(
    collection = "news_articles_search",
    searchQuery = "Bitcoin price prediction",
    databaseType = "elasticsearch"
)
```

## 🐛 트러블슈팅

### Database Service 연결 실패

```kotlin
// Health Check
val isHealthy = databaseServiceClient.healthCheck()
if (!isHealthy) {
    println("Database Service is not responding!")
}
```

### 버전 충돌 (Optimistic Lock)

```kotlin
try {
    databaseServiceClient.update(..., version = 5)
} catch (e: Exception) {
    // 409 Conflict - 다른 프로세스가 먼저 업데이트함
    // 최신 버전 다시 가져와서 재시도
}
```

### 성능 최적화

```kotlin
// Bad: 1000번 개별 요청
articles.forEach { databaseServiceClient.create(...) }

// Good: 1번 bulk 요청
databaseServiceClient.bulkInsert(articles)
```

## 📚 추가 자료

- [Database Service Repository](https://github.com/YouSangSon/database-service)
- [REST API Specification](https://github.com/YouSangSon/database-service/blob/main/docs/REST_API_SPECIFICATION.md)
- [gRPC Documentation](https://github.com/YouSangSon/database-service/blob/main/docs/GRPC_USAGE.md)
- [Monitoring Guide](https://github.com/YouSangSon/database-service/blob/main/docs/MONITORING.md)

---

**Database Service 통합으로 데이터 관리가 엔터프라이즈급으로 업그레이드되었습니다! 🚀**
