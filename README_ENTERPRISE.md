# Enterprise REST Server

엔터프라이즈급 대규모 프로젝트를 위한 REST API 서버입니다. Clean Architecture, Domain-Driven Design (DDD), Test-Driven Development (TDD) 원칙을 따릅니다.

## 🏗️ 아키텍처

### Clean Architecture 레이어

```
┌─────────────────────────────────────────────────────┐
│                   Adapter Layer                      │
│  ┌──────────────┐              ┌─────────────────┐  │
│  │   Web API    │              │  Persistence    │  │
│  │ (Controllers)│              │ (JPA/Exposed)   │  │
│  └──────────────┘              └─────────────────┘  │
├─────────────────────────────────────────────────────┤
│              Application Layer                       │
│  ┌──────────────┐              ┌─────────────────┐  │
│  │  Use Cases   │              │    Services     │  │
│  │   (Ports)    │              │                 │  │
│  └──────────────┘              └─────────────────┘  │
├─────────────────────────────────────────────────────┤
│                Domain Layer                          │
│  ┌──────────────┐              ┌─────────────────┐  │
│  │    Models    │              │   Exceptions    │  │
│  │ (Pure Kotlin)│              │                 │  │
│  └──────────────┘              └─────────────────┘  │
└─────────────────────────────────────────────────────┘
```

### 주요 설계 원칙

- **Domain-Driven Design (DDD)**: 비즈니스 로직을 도메인 모델에 집중
- **Test-Driven Development (TDD)**: 테스트 우선 개발
- **SOLID 원칙**: 유지보수 가능한 코드 작성
- **Dependency Inversion**: 의존성 역전을 통한 유연한 구조
- **Hexagonal Architecture**: 포트와 어댑터 패턴

## 🚀 주요 기능

### 보안 & 인증
- ✅ JWT 기반 인증 시스템
- ✅ Spring Security 통합
- ✅ Role 기반 권한 관리 (USER, ADMIN, MODERATOR)
- ✅ Password Encoding (BCrypt)
- ✅ Access Token & Refresh Token

### API 문서화
- ✅ Swagger/OpenAPI 3.0 (SpringDoc)
- ✅ 자동 API 문서 생성
- ✅ Interactive API Testing UI
- ✅ 엔드포인트: `/swagger-ui.html`, `/api-docs`

### 데이터 관리
- ✅ PostgreSQL (프로덕션 DB)
- ✅ JPA/Hibernate (ORM)
- ✅ JetBrains Exposed (DSL 쿼리)
- ✅ 트랜잭션 관리
- ✅ 연결 풀링 (HikariCP)

### 캐싱 & 성능
- ✅ Redis 캐싱
- ✅ Spring Cache Abstraction
- ✅ Method-level Caching
- ✅ Rate Limiting (Bucket4j)
- ✅ 요청 제한: 분당 100회 (설정 가능)

### 모니터링 & 로깅
- ✅ Spring Actuator
- ✅ Prometheus Metrics
- ✅ Health Checks
- ✅ 구조화된 로깅
- ✅ 환경별 로그 레벨 설정

### 에러 처리
- ✅ 글로벌 예외 핸들러
- ✅ 커스텀 도메인 예외
- ✅ 표준화된 에러 응답
- ✅ Validation 에러 처리

### 기타
- ✅ CORS 설정
- ✅ Bean Validation
- ✅ Virtual Threads (Java 21)
- ✅ Docker & Docker Compose
- ✅ Multi-stage Dockerfile

## 📋 기술 스택

### 핵심 기술
- **Language**: Kotlin 2.2.20
- **Framework**: Spring Boot 3.5.6
- **Java**: JDK 21 (Virtual Threads)
- **Build Tool**: Gradle 8.14.3

### 데이터베이스
- **PostgreSQL**: 15-alpine
- **Redis**: 7-alpine
- **ORM**: JPA/Hibernate, Exposed

### 보안
- **Spring Security**: 인증/인가
- **JWT**: JSON Web Tokens (JJWT 0.12.3)

### 모니터링
- **Actuator**: 헬스체크, 메트릭
- **Prometheus**: 메트릭 수집
- **Micrometer**: 메트릭 라이브러리

### 문서화 & 테스트
- **SpringDoc**: OpenAPI 3.0
- **JUnit 5**: 테스트 프레임워크
- **Mockito-Kotlin**: 모킹
- **H2**: 테스트용 인메모리 DB

## 🚀 빠른 시작

### 전제 조건
- JDK 21 이상
- Docker & Docker Compose (선택사항)
- PostgreSQL 15+ (로컬 실행 시)
- Redis 7+ (캐싱 사용 시)

### 1. 프로젝트 클론

```bash
git clone <repository-url>
cd rest_server
```

### 2. 환경 변수 설정

```bash
# JWT Secret (최소 256비트)
export JWT_SECRET="your-very-secure-secret-key-that-is-at-least-256-bits-long"

# Database
export DB_URL="jdbc:postgresql://localhost:5432/rest_dev"
export DB_USERNAME="postgres"
export DB_PASSWORD="postgres"

# Redis (선택)
export REDIS_HOST="localhost"
export REDIS_PORT="6379"

# CORS
export CORS_ALLOWED_ORIGINS="http://localhost:3000,http://localhost:8080"

# Rate Limiting
export RATE_LIMIT_ENABLED="true"
export RATE_LIMIT_RPM="100"
```

### 3. Docker Compose로 실행 (권장)

```bash
# 전체 스택 실행 (PostgreSQL + Redis + Application)
docker-compose up -d

# 로그 확인
docker-compose logs -f rest-server

# 중지
docker-compose down
```

### 4. 로컬 개발 환경 실행

```bash
# PostgreSQL & Redis 실행
docker-compose up -d postgres redis

# 애플리케이션 실행
./gradlew bootRun

# 또는
gradle bootRun
```

### 5. 테스트 실행

```bash
# 모든 테스트 실행
./gradlew test

# 특정 테스트 실행
./gradlew test --tests "UserServiceTest"

# 테스트 커버리지 리포트
./gradlew jacocoTestReport
```

## 📚 API 엔드포인트

### 인증 (Authentication)

```bash
# 회원가입
POST /api/v1/users/register
{
  "username": "testuser",
  "email": "test@example.com",
  "password": "password123"
}

# 로그인
POST /api/v1/auth/login
{
  "username": "testuser",
  "password": "password123"
}

# 응답
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
  "user": {
    "id": 1,
    "username": "testuser",
    "email": "test@example.com",
    "roles": ["USER"],
    "enabled": true
  }
}
```

### 사용자 관리 (User Management)

```bash
# 사용자 조회 (인증 필요)
GET /api/v1/users/{id}
Authorization: Bearer {accessToken}

# 모든 사용자 조회 (ADMIN만)
GET /api/v1/users
Authorization: Bearer {accessToken}

# 사용자 정보 수정 (인증 필요)
PUT /api/v1/users/{id}
Authorization: Bearer {accessToken}
{
  "username": "newusername",
  "email": "newemail@example.com"
}

# 사용자 삭제 (ADMIN만)
DELETE /api/v1/users/{id}
Authorization: Bearer {accessToken}
```

### 기타 엔드포인트

```bash
# Greeting
GET /api/v1/greetings?name=Junie

# DB 시간 조회
GET /api/v1/db/time

# Lotto
POST /api/v1/lotto
GET /api/v1/lotto/{id}
GET /api/v1/lotto
GET /api/v1/lotto/numbers?firstDrwNo=1&lastDrwNo=10
```

### 모니터링

```bash
# Health Check
GET /actuator/health

# Metrics
GET /actuator/metrics

# Prometheus Metrics
GET /actuator/prometheus

# 환경 정보
GET /actuator/env

# 로거 설정
GET /actuator/loggers
```

### API 문서

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs
- **OpenAPI YAML**: http://localhost:8080/api-docs.yaml

## 🧪 테스트 전략

### 테스트 피라미드

```
        ┌─────────────┐
        │   E2E Tests │  ← 소수
        ├─────────────┤
        │Integration  │  ← 중간
        │    Tests    │
        ├─────────────┤
        │   Unit      │  ← 다수
        │   Tests     │
        └─────────────┘
```

### 테스트 유형

1. **Unit Tests**: 도메인 로직 테스트
   - `UserTest`: User 도메인 모델 테스트
   - `UserServiceTest`: 비즈니스 로직 테스트

2. **Integration Tests**: 레이어 간 통합 테스트
   - Controller + Service + Repository
   - Database 통합

3. **API Tests**: REST API 엔드포인트 테스트
   - HTTP 요청/응답 테스트
   - 인증/인가 테스트

## 🐳 Docker

### 이미지 빌드

```bash
docker build -t rest-server:latest .
```

### 컨테이너 실행

```bash
docker run -p 8080:8080 \
  -e DB_URL="jdbc:postgresql://host.docker.internal:5432/rest_dev" \
  -e JWT_SECRET="your-secret-key" \
  rest-server:latest
```

### Docker Compose

```bash
# 전체 스택 실행
docker-compose up -d

# 특정 서비스만 실행
docker-compose up -d postgres redis

# 로그 확인
docker-compose logs -f rest-server

# 스케일링
docker-compose up -d --scale rest-server=3

# 중지 및 삭제
docker-compose down -v
```

## 📁 프로젝트 구조

```
rest_server/
├── src/
│   ├── main/
│   │   ├── kotlin/yousang/rest_server/
│   │   │   ├── adapter/
│   │   │   │   ├── in/
│   │   │   │   │   └── web/
│   │   │   │   │       ├── controllers/       # REST Controllers
│   │   │   │   │       └── exception/         # Global Exception Handler
│   │   │   │   └── out/
│   │   │   │       └── persistence/           # DB Adapters
│   │   │   ├── application/
│   │   │   │   ├── ports/
│   │   │   │   │   ├── in/                   # Use Case Interfaces
│   │   │   │   │   └── out/                  # Repository Ports
│   │   │   │   └── service/                  # Service Implementations
│   │   │   ├── domain/
│   │   │   │   ├── model/                    # Domain Models
│   │   │   │   └── exception/                # Domain Exceptions
│   │   │   ├── config/                       # Configuration
│   │   │   │   ├── security/                 # Security Config
│   │   │   │   └── interceptor/              # Interceptors
│   │   │   └── RestServerApplication.kt
│   │   └── resources/
│   │       ├── application.yml               # Application Config
│   │       └── logback-spring.xml           # Logging Config
│   └── test/
│       └── kotlin/yousang/rest_server/
│           ├── domain/                       # Domain Tests
│           ├── application/                  # Service Tests
│           └── adapter/                      # Controller Tests
├── build.gradle.kts                          # Gradle Build Script
├── docker-compose.yml                        # Docker Compose Config
├── Dockerfile                                # Multi-stage Dockerfile
└── README.md                                 # This file
```

## 🔧 설정

### application.yml 주요 설정

```yaml
# JWT
jwt:
  secret: ${JWT_SECRET}
  expiration: 86400000  # 24 hours
  refresh-expiration: 604800000  # 7 days

# CORS
cors:
  allowed-origins: ${CORS_ALLOWED_ORIGINS}
  allowed-methods: GET,POST,PUT,DELETE,PATCH,OPTIONS
  allow-credentials: true

# Rate Limiting
rate-limit:
  enabled: ${RATE_LIMIT_ENABLED:true}
  requests-per-minute: ${RATE_LIMIT_RPM:100}

# Database
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5

# Redis
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}

# Actuator
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
```

## 🔐 보안 고려사항

1. **JWT Secret**: 프로덕션에서는 안전한 256비트 이상의 시크릿 사용
2. **Password**: BCrypt로 인코딩
3. **HTTPS**: 프로덕션에서는 HTTPS 사용 필수
4. **환경변수**: 민감한 정보는 환경변수로 관리
5. **Rate Limiting**: DDoS 방지를 위한 요청 제한
6. **CORS**: 허용된 오리진만 접근 가능

## 📈 성능 최적화

1. **Virtual Threads**: Java 21의 Virtual Threads 사용
2. **Connection Pooling**: HikariCP로 DB 연결 풀링
3. **Redis Caching**: 자주 조회되는 데이터 캐싱
4. **Rate Limiting**: 시스템 과부하 방지
5. **Lazy Loading**: JPA 지연 로딩
6. **Database Indexing**: 적절한 인덱스 설정

## 🚦 모니터링

### Prometheus Metrics

```bash
# Prometheus 서버 설정 (prometheus.yml)
scrape_configs:
  - job_name: 'rest-server'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['localhost:8080']
```

### Grafana Dashboard

- JVM Metrics
- HTTP Request Metrics
- Database Connection Pool
- Cache Hit Ratio
- Error Rates

## 📝 개발 가이드

### 새로운 도메인 추가 (TDD + DDD)

1. **도메인 모델 작성** (`domain/model/`)
2. **도메인 테스트 작성** (`test/.../domain/`)
3. **Use Case 인터페이스 정의** (`application/ports/in/`)
4. **Repository Port 정의** (`application/ports/out/`)
5. **서비스 테스트 작성** (`test/.../application/`)
6. **서비스 구현** (`application/service/`)
7. **Repository 어댑터 구현** (`adapter/out/persistence/`)
8. **Controller 작성** (`adapter/in/web/`)
9. **통합 테스트 작성**

### 코드 스타일

- Kotlin 코딩 컨벤션 준수
- SOLID 원칙 적용
- Clean Code 작성
- 의미있는 변수/함수명 사용
- 적절한 주석 작성

## 🐛 트러블슈팅

### 일반적인 문제

1. **DB 연결 실패**
   ```bash
   # PostgreSQL 실행 확인
   docker-compose ps postgres

   # 연결 테스트
   psql -h localhost -U postgres -d rest_dev
   ```

2. **Redis 연결 실패**
   ```bash
   # Redis 실행 확인
   docker-compose ps redis

   # 연결 테스트
   redis-cli -h localhost ping
   ```

3. **JWT 토큰 검증 실패**
   - JWT_SECRET 환경변수 확인
   - 토큰 만료 시간 확인

## 🤝 기여하기

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 라이선스

MIT License

## 📞 지원

- **Issues**: GitHub Issues
- **Documentation**: `/docs` 디렉토리
- **Email**: dev@example.com

---

**Version**: v2.0.0
**Last Updated**: 2025-11-06
**Author**: YouSang Son
