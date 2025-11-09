# REST Server v2 — 엔터프라이즈 아키텍처 문서

## 📋 목차
1. [시스템 개요](#시스템-개요)
2. [아키텍처 원칙](#아키텍처-원칙)
3. [시스템 아키텍처](#시스템-아키텍처)
4. [프로젝트 구조](#프로젝트-구조)
5. [보안 아키텍처](#보안-아키텍처)
6. [데이터 아키텍처](#데이터-아키텍처)
7. [이벤트 기반 아키텍처](#이벤트-기반-아키텍처)
8. [성능 최적화](#성능-최적화)
9. [배포 아키텍처](#배포-아키텍처)
10. [관측성 스택](#관측성-스택)
11. [CI/CD 파이프라인](#cicd-파이프라인)
12. [확장 패턴](#확장-패턴)
13. [재해 복구](#재해-복구)
14. [품질 속성](#품질-속성)

---

## 시스템 개요

### C4 컨텍스트 다이어그램

```mermaid
graph TB
    subgraph "외부 사용자"
        User[웹/모바일 사용자]
        Admin[관리자]
    end

    subgraph "외부 시스템"
        Google[Google OAuth2]
        Naver[Naver OAuth2]
        Kakao[Kakao OAuth2]
        LottoAPI[로또 번호 추첨기]
    end

    subgraph "REST Server 시스템"
        API[REST API Server<br/>Java 21 Virtual Threads<br/>Spring Boot 3.2]
    end

    subgraph "데이터 저장소"
        PG[(PostgreSQL<br/>관계형 데이터)]
        Mongo[(MongoDB<br/>문서형 데이터)]
        Redis[(Redis<br/>캐시/세션)]
        Kafka[Kafka<br/>이벤트 스트림]
    end

    User -->|HTTPS| API
    Admin -->|HTTPS| API
    API -->|인증| Google
    API -->|인증| Naver
    API -->|인증| Kakao
    API -->|추첨 결과 조회| LottoAPI
    API -->|트랜잭션| PG
    API -->|문서 저장| Mongo
    API -->|캐싱| Redis
    API -->|이벤트 발행| Kafka

    style API fill:#4A90E2,stroke:#2E5C8A,color:#fff
    style PG fill:#336791,stroke:#23527C,color:#fff
    style Mongo fill:#47A248,stroke:#2F6B30,color:#fff
    style Redis fill:#DC382D,stroke:#9A2620,color:#fff
    style Kafka fill:#231F20,stroke:#000,color:#fff
```

### 주요 특징

- **Java 21 Virtual Threads**: 10,000+ 동시 연결 처리
- **Clean Architecture**: Hexagonal Architecture / Ports & Adapters 패턴
- **Domain-Driven Design**: 순수 도메인 모델 중심 설계
- **Test-Driven Development**: 단위/통합/슬라이스 테스트
- **OAuth2 소셜 로그인**: Google, Naver, Kakao 지원
- **Polyglot Persistence**: PostgreSQL, MongoDB, Redis, Kafka
- **Event-Driven Architecture**: Kafka 기반 이벤트 스트리밍
- **Kubernetes 배포**: 3-50 Pod 오토스케일링
- **Comprehensive APIs**: 사용자 관리, 로또/연금복권 APIs

---

## 아키텍처 원칙

### 1. 의존성 규칙 (Dependency Rule)

```mermaid
graph LR
    subgraph "외부 레이어"
        Infrastructure[Infrastructure<br/>Frameworks & Drivers]
        Adapters[Interface Adapters<br/>Controllers, Gateways]
    end

    subgraph "내부 레이어"
        Application[Application Business Rules<br/>Use Cases]
        Domain[Enterprise Business Rules<br/>Entities]
    end

    Infrastructure -->|의존| Adapters
    Adapters -->|의존| Application
    Application -->|의존| Domain
    Domain -.->|의존 금지| Application
    Domain -.->|의존 금지| Adapters
    Domain -.->|의존 금지| Infrastructure

    style Domain fill:#E74C3C,stroke:#C0392B,color:#fff
    style Application fill:#3498DB,stroke:#2980B9,color:#fff
    style Adapters fill:#2ECC71,stroke:#27AE60,color:#fff
    style Infrastructure fill:#95A5A6,stroke:#7F8C8D,color:#fff
```

### 2. Hexagonal Architecture

```mermaid
graph TB
    subgraph "Primary Adapters (Inbound)"
        REST[REST Controllers]
        GraphQL[GraphQL Resolvers]
        Messaging[Kafka Consumers]
    end

    subgraph "Application Core"
        Ports[Ports<br/>Interfaces]
        UseCases[Use Cases<br/>Application Services]
        DomainModels[Domain Models<br/>Business Logic]
    end

    subgraph "Secondary Adapters (Outbound)"
        JPA[JPA Repositories]
        MongoRepo[MongoDB Repositories]
        RedisCache[Redis Cache]
        KafkaProducer[Kafka Producers]
        External[External APIs]
    end

    REST --> Ports
    GraphQL --> Ports
    Messaging --> Ports
    Ports --> UseCases
    UseCases --> DomainModels
    UseCases --> Ports
    Ports -.implements.- JPA
    Ports -.implements.- MongoRepo
    Ports -.implements.- RedisCache
    Ports -.implements.- KafkaProducer
    Ports -.implements.- External

    style DomainModels fill:#E74C3C,stroke:#C0392B,color:#fff
    style UseCases fill:#3498DB,stroke:#2980B9,color:#fff
    style Ports fill:#F39C12,stroke:#E67E22,color:#fff
    style REST fill:#2ECC71,stroke:#27AE60,color:#fff
    style JPA fill:#95A5A6,stroke:#7F8C8D,color:#fff
```

---

## 프로젝트 구조

```mermaid
graph LR
    subgraph "src/main/kotlin"
        Domain[domain/<br/>model/<br/>- User<br/>- LottoTicket<br/>- PensionLotteryTicket]

        Application[application/<br/>ports/in/<br/>- Use Case Interfaces<br/>ports/out/<br/>- Repository Ports<br/>service/<br/>- Service Implementations]

        Adapter[adapter/<br/>in/web/<br/>- REST Controllers<br/>out/persistence/<br/>- JPA/MongoDB/Redis<br/>out/messaging/<br/>- Kafka Producers]

        Config[config/<br/>- Security Config<br/>- Database Config<br/>- Kafka Config<br/>- Redis Config]
    end

    Domain --> Application
    Application --> Adapter
    Application --> Config
    Adapter --> Config

    style Domain fill:#E74C3C,stroke:#C0392B,color:#fff
    style Application fill:#3498DB,stroke:#2980B9,color:#fff
    style Adapter fill:#2ECC71,stroke:#27AE60,color:#fff
    style Config fill:#F39C12,stroke:#E67E22,color:#fff
```

### 디렉토리 구조

```
src/main/kotlin/yousang/rest_server/
├── domain/                           # 도메인 레이어 (순수 비즈니스 로직)
│   └── model/
│       ├── User.kt                   # 사용자 도메인 모델
│       ├── LottoTicket.kt           # 로또 티켓 도메인 모델
│       ├── PensionLotteryTicket.kt  # 연금복권 티켓 도메인 모델
│       ├── LottoDrawResult.kt       # 로또 추첨 결과
│       └── PensionLotteryDrawResult.kt
│
├── application/                      # 애플리케이션 레이어 (유스케이스)
│   ├── ports/
│   │   ├── in/                      # Inbound Ports (Use Cases)
│   │   │   ├── CreateUserUseCase.kt
│   │   │   ├── GenerateLottoNumbersUseCase.kt
│   │   │   ├── GeneratePensionLotteryNumbersUseCase.kt
│   │   │   ├── CheckLotteryWinningUseCase.kt
│   │   │   ├── GetLotteryTicketsUseCase.kt
│   │   │   └── GetLotteryDrawResultsUseCase.kt
│   │   └── out/                     # Outbound Ports (Repository/Gateway Interfaces)
│   │       ├── UserRepositoryPort.kt
│   │       ├── LottoTicketRepositoryPort.kt
│   │       ├── PensionLotteryTicketRepositoryPort.kt
│   │       ├── LottoDrawResultRepositoryPort.kt
│   │       ├── PensionLotteryDrawResultRepositoryPort.kt
│   │       ├── UserCachePort.kt
│   │       └── EventPublisherPort.kt
│   └── service/                     # Service Implementations
│       ├── UserService.kt
│       └── LotteryService.kt
│
├── adapter/                          # 어댑터 레이어 (인프라/프레임워크)
│   ├── in/
│   │   └── web/                     # REST Controllers
│   │       ├── UserController.kt
│   │       ├── AuthController.kt
│   │       ├── LottoController.kt
│   │       └── PensionLotteryController.kt
│   ├── out/
│   │   ├── persistence/
│   │   │   ├── jpa/                # PostgreSQL JPA
│   │   │   │   ├── UserJpaEntity.kt
│   │   │   │   ├── UserJpaRepository.kt
│   │   │   │   ├── UserRepositoryAdapter.kt
│   │   │   │   ├── LottoTicketJpaEntity.kt
│   │   │   │   ├── LottoTicketJpaRepository.kt
│   │   │   │   └── LottoTicketRepositoryAdapter.kt
│   │   │   ├── mongodb/            # MongoDB
│   │   │   │   ├── UserMongoDocument.kt
│   │   │   │   └── UserMongoRepository.kt
│   │   │   └── redis/              # Redis Cache
│   │   │       └── RedisCacheAdapter.kt
│   │   └── messaging/
│   │       └── kafka/               # Kafka Event Publisher
│   │           └── KafkaEventPublisher.kt
│   └── security/
│       └── oauth2/
│           ├── CustomOAuth2UserService.kt
│           ├── OAuth2AuthenticationSuccessHandler.kt
│           └── OAuth2AuthenticationFailureHandler.kt
│
└── config/                           # 설정 클래스
    ├── SecurityConfig.kt
    ├── JpaConfig.kt
    ├── MongoConfig.kt
    ├── RedisConfig.kt
    ├── KafkaConfig.kt
    └── WebConfig.kt
```

---

## 보안 아키텍처

### 다층 보안 모델

```mermaid
graph TD
    Client[클라이언트] -->|1. HTTPS| WAF[AWS WAF<br/>SQL Injection<br/>XSS 방어]
    WAF -->|2. 인증| ALB[AWS ALB<br/>SSL/TLS 종료]
    ALB -->|3. 토큰 검증| Ingress[K8s Ingress<br/>Rate Limiting]
    Ingress -->|4. JWT 검증| Security[Spring Security<br/>OAuth2 Resource Server]
    Security -->|5. 권한 확인| Authorization[Method Security<br/>@PreAuthorize]
    Authorization -->|6. 데이터 접근| DataAccess[Row Level Security<br/>Audit Logging]

    style WAF fill:#E74C3C,stroke:#C0392B,color:#fff
    style ALB fill:#E67E22,stroke:#D35400,color:#fff
    style Ingress fill:#F39C12,stroke:#E67E22,color:#fff
    style Security fill:#2ECC71,stroke:#27AE60,color:#fff
    style Authorization fill:#3498DB,stroke:#2980B9,color:#fff
    style DataAccess fill:#9B59B6,stroke:#8E44AD,color:#fff
```

### OAuth2 인증 플로우

```mermaid
sequenceDiagram
    autonumber
    participant User as 사용자
    participant Client as REST API
    participant OAuth2 as OAuth2 Provider<br/>(Google/Naver/Kakao)
    participant DB as PostgreSQL
    participant Redis as Redis Cache

    User->>Client: 1. 소셜 로그인 요청
    Client->>OAuth2: 2. Authorization Code 요청
    OAuth2->>User: 3. 로그인 페이지 표시
    User->>OAuth2: 4. 인증 정보 입력
    OAuth2->>Client: 5. Authorization Code 반환
    Client->>OAuth2: 6. Access Token 요청
    OAuth2->>Client: 7. Access Token 발급
    Client->>OAuth2: 8. 사용자 정보 요청
    OAuth2->>Client: 9. 사용자 정보 반환
    Client->>DB: 10. 사용자 조회/생성
    DB->>Client: 11. User Entity
    Client->>Client: 12. JWT 토큰 생성
    Client->>Redis: 13. Refresh Token 저장
    Client->>User: 14. JWT + Refresh Token 반환

    Note over User,Redis: 이후 요청은 JWT Bearer Token으로 인증
```

### 보안 기능

- **OAuth2 소셜 로그인**: Google, Naver, Kakao
- **JWT 토큰 기반 인증**: Access Token (15분) + Refresh Token (7일)
- **Spring Security**: Method-level @PreAuthorize 권한 검증
- **Rate Limiting**: Redis 기반 API 호출 제한 (100 req/min per user)
- **CORS 설정**: 허용된 도메인만 접근 가능
- **HTTPS Only**: TLS 1.2+ 강제
- **Password Encryption**: BCrypt (strength 12)
- **SQL Injection 방어**: Prepared Statements (JPA)
- **XSS 방어**: Content Security Policy, X-XSS-Protection
- **CSRF 방어**: Double Submit Cookie Pattern
- **Audit Logging**: 모든 인증/인가 이벤트 기록

---

## 데이터 아키텍처

### Polyglot Persistence 전략

```mermaid
graph TB
    subgraph "데이터 소스"
        PG[(PostgreSQL<br/>OLTP)]
        Mongo[(MongoDB<br/>문서형)]
        Redis[(Redis<br/>캐시)]
        Kafka[Kafka<br/>이벤트 스트림]
    end

    subgraph "데이터 유형"
        PG -->|사용자 정보<br/>로또 티켓<br/>추첨 결과| Transactional[트랜잭션 데이터]
        Mongo -->|사용자 활동 로그<br/>분석 데이터<br/>감사 로그| Document[문서형 데이터]
        Redis -->|세션<br/>JWT 토큰<br/>API 응답| Cache[캐시 데이터]
        Kafka -->|사용자 이벤트<br/>시스템 이벤트<br/>통계 이벤트| Stream[이벤트 스트림]
    end

    style PG fill:#336791,stroke:#23527C,color:#fff
    style Mongo fill:#47A248,stroke:#2F6B30,color:#fff
    style Redis fill:#DC382D,stroke:#9A2620,color:#fff
    style Kafka fill:#231F20,stroke:#000,color:#fff
```

### 데이터 플로우

```mermaid
flowchart TD
    A[API Request] --> B{캐시 존재?}
    B -->|Yes| C[Redis에서 반환]
    B -->|No| D[DB 조회]
    D --> E[PostgreSQL]
    D --> F[MongoDB]
    E --> G[캐시 저장]
    F --> G
    G --> H[Redis에 저장]
    H --> I[응답 반환]
    C --> I

    D --> J[이벤트 발행]
    J --> K[Kafka]
    K --> L[다른 서비스/분석]

    style B fill:#F39C12,stroke:#E67E22,color:#fff
    style E fill:#336791,stroke:#23527C,color:#fff
    style F fill:#47A248,stroke:#2F6B30,color:#fff
    style H fill:#DC382D,stroke:#9A2620,color:#fff
    style K fill:#231F20,stroke:#000,color:#fff
```

### 데이터베이스 설계

**PostgreSQL 스키마**:
- **users**: 사용자 기본 정보 (id, email, name, oauth2_provider)
- **lotto_tickets**: 로또 티켓 (id, user_id, draw_number, numbers, purchase_date)
- **pension_lottery_tickets**: 연금복권 티켓 (id, user_id, draw_number, group, number)
- **lotto_draw_results**: 로또 추첨 결과 (draw_number, winning_numbers, bonus_number, draw_date)
- **pension_lottery_draw_results**: 연금복권 추첨 결과

**MongoDB 컬렉션**:
- **user_activities**: 사용자 활동 로그 (user_id, action, timestamp, metadata)
- **audit_logs**: 감사 로그 (user_id, action, resource, timestamp)
- **analytics**: 분석 데이터 (user_id, event_type, data)

**Redis 키 패턴**:
- **session:{sessionId}**: 세션 데이터 (TTL: 30분)
- **user:{userId}**: 사용자 정보 캐시 (TTL: 15분)
- **lottery:draw:{drawNumber}**: 추첨 결과 캐시 (TTL: 24시간)
- **ratelimit:{userId}**: Rate Limiting 카운터 (TTL: 1분)

---

## 이벤트 기반 아키텍처

### Event Sourcing 패턴

```mermaid
graph LR
    A[API Request] --> B[Command Handler]
    B --> C[Domain Model]
    C --> D[Event]
    D --> E[Event Store<br/>PostgreSQL]
    D --> F[Event Bus<br/>Kafka]
    F --> G[Consumer 1<br/>통계 서비스]
    F --> H[Consumer 2<br/>알림 서비스]
    F --> I[Consumer 3<br/>분석 서비스]

    style C fill:#E74C3C,stroke:#C0392B,color:#fff
    style F fill:#231F20,stroke:#000,color:#fff
```

### Kafka Topics

```mermaid
mindmap
  root((Kafka Topics))
    사용자 이벤트
      user.created
      user.updated
      user.deleted
      user.login
      user.logout
    로또 이벤트
      lotto.ticket.created
      lotto.winning.checked
      lotto.draw.result.published
    연금복권 이벤트
      pension.ticket.created
      pension.winning.checked
      pension.draw.result.published
    시스템 이벤트
      system.error
      system.alert
      system.metric
```

### 이벤트 처리 플로우

```mermaid
sequenceDiagram
    autonumber
    participant API as REST API
    participant Service as Application Service
    participant Domain as Domain Model
    participant Repo as Repository
    participant Event as Event Publisher
    participant Kafka as Kafka
    participant Consumer as Event Consumer

    API->>Service: POST /api/v1/lottery/lotto/generate
    Service->>Domain: LottoTicket.generateRandom()
    Domain->>Service: LottoTicket
    Service->>Repo: save(ticket)
    Repo->>Service: saved ticket
    Service->>Event: publish(LottoTicketCreatedEvent)
    Event->>Kafka: send to lotto.ticket.created
    Kafka->>Consumer: consume event
    Consumer->>Consumer: 통계 업데이트 / 알림 발송
    Service->>API: LottoTicketResponse
```

---

## 성능 최적화

### Virtual Threads & 연결 풀 최적화

```mermaid
graph TD
    subgraph "Java 21 Virtual Threads"
        VT[10,000+ Virtual Threads<br/>Lightweight<br/>Platform Thread 매핑]
    end

    subgraph "HikariCP Connection Pool"
        CP[Maximum Pool Size: 20<br/>Minimum Idle: 5<br/>Connection Timeout: 30s]
    end

    subgraph "Tomcat Configuration"
        TC[Max Threads: 1000<br/>Min Spare: 100<br/>Accept Count: 1000]
    end

    VT --> CP
    VT --> TC
    CP --> DB[(PostgreSQL)]
    TC --> VT

    style VT fill:#4A90E2,stroke:#2E5C8A,color:#fff
    style CP fill:#2ECC71,stroke:#27AE60,color:#fff
    style TC fill:#E67E22,stroke:#D35400,color:#fff
```

### 캐싱 전략

```mermaid
graph TD
    A[API Request] --> B{L1 Cache<br/>Caffeine}
    B -->|Hit| C[Return from L1]
    B -->|Miss| D{L2 Cache<br/>Redis}
    D -->|Hit| E[Store to L1]
    E --> C
    D -->|Miss| F[Database Query]
    F --> G[Store to L2]
    G --> E

    H[Write Operation] --> I[Update Database]
    I --> J[Invalidate L1]
    J --> K[Invalidate L2]

    style B fill:#F39C12,stroke:#E67E22,color:#fff
    style D fill:#DC382D,stroke:#9A2620,color:#fff
    style F fill:#336791,stroke:#23527C,color:#fff
```

### 성능 최적화 기법

1. **Virtual Threads**: Java 21의 경량 스레드로 10,000+ 동시 연결 처리
2. **HikariCP**: 최적화된 JDBC 연결 풀 (maximum-pool-size: 20)
3. **Hibernate 최적화**:
   - `hibernate.jdbc.batch_size=50`: 배치 쓰기
   - `hibernate.order_inserts=true`: 삽입 순서 최적화
   - `hibernate.order_updates=true`: 업데이트 순서 최적화
4. **Redis 캐싱**:
   - L1 Cache: Caffeine (로컬 메모리)
   - L2 Cache: Redis (분산 캐시)
   - TTL: 사용자 정보(15분), 추첨 결과(24시간)
5. **데이터베이스 인덱싱**:
   - B-Tree Index: PK, FK, 검색 컬럼
   - Composite Index: 복합 조건 쿼리
6. **응답 압축**: Gzip (>1KB 응답)
7. **비동기 처리**: @Async, CompletableFuture
8. **읽기 전용 트랜잭션**: @Transactional(readOnly=true)

---

## 배포 아키텍처

### Kubernetes Multi-Pod 배포

```mermaid
graph TB
    subgraph "AWS Cloud"
        subgraph "ALB"
            LB[Application Load Balancer<br/>SSL/TLS, WAF]
        end

        subgraph "EKS Cluster"
            subgraph "rest-server Namespace"
                subgraph "Deployment"
                    Pod1[Pod 1<br/>Virtual Threads<br/>500m-2 CPU<br/>1-2Gi Memory]
                    Pod2[Pod 2<br/>Virtual Threads<br/>500m-2 CPU<br/>1-2Gi Memory]
                    Pod3[Pod 3<br/>Virtual Threads<br/>500m-2 CPU<br/>1-2Gi Memory]
                    PodN[Pod N<br/>3-50 replicas<br/>HPA Auto Scaling]
                end

                Service[Kubernetes Service<br/>ClusterIP]
                Ingress[Kubernetes Ingress<br/>ALB Controller]
            end

            HPA[Horizontal Pod Autoscaler<br/>CPU 70%, Memory 80%]
            CA[Cluster Autoscaler<br/>Node Auto Provisioning]
        end

        subgraph "Data Layer"
            RDS[(RDS PostgreSQL<br/>Multi-AZ)]
            DocumentDB[(DocumentDB<br/>MongoDB-compatible)]
            ElastiCache[(ElastiCache<br/>Redis)]
            MSK[MSK<br/>Managed Kafka]
        end
    end

    LB -->|Route| Ingress
    Ingress --> Service
    Service --> Pod1 & Pod2 & Pod3 & PodN

    HPA -->|Scale| Pod1 & Pod2 & Pod3 & PodN
    CA -->|Provision Nodes| EKS

    Pod1 & Pod2 & Pod3 & PodN --> RDS
    Pod1 & Pod2 & Pod3 & PodN --> DocumentDB
    Pod1 & Pod2 & Pod3 & PodN --> ElastiCache
    Pod1 & Pod2 & Pod3 & PodN --> MSK

    style LB fill:#FF9900,stroke:#E68A00,color:#fff
    style Pod1 fill:#4A90E2,stroke:#2E5C8A,color:#fff
    style Pod2 fill:#4A90E2,stroke:#2E5C8A,color:#fff
    style Pod3 fill:#4A90E2,stroke:#2E5C8A,color:#fff
    style PodN fill:#4A90E2,stroke:#2E5C8A,color:#fff
    style RDS fill:#336791,stroke:#23527C,color:#fff
    style DocumentDB fill:#47A248,stroke:#2F6B30,color:#fff
    style ElastiCache fill:#DC382D,stroke:#9A2620,color:#fff
    style MSK fill:#231F20,stroke:#000,color:#fff
```

### 배포 전략

- **Rolling Update**: maxSurge=2, maxUnavailable=1
- **Health Checks**:
  - Liveness Probe: `/actuator/health/liveness` (initialDelay=60s, period=10s)
  - Readiness Probe: `/actuator/health/readiness` (initialDelay=30s, period=5s)
  - Startup Probe: `/actuator/health/startup` (failureThreshold=30, period=10s)
- **Pod Disruption Budget**: minAvailable=2 (고가용성 보장)
- **Pod Anti-Affinity**: preferredDuringSchedulingIgnoredDuringExecution (노드 분산)
- **Resource Requests/Limits**:
  - Requests: cpu=500m, memory=1Gi
  - Limits: cpu=2000m, memory=2Gi

---

## 관측성 스택

### Monitoring Architecture

```mermaid
graph TB
    subgraph "Application"
        App[REST Server Pods]
        Actuator[Spring Boot Actuator<br/>/actuator/prometheus]
    end

    subgraph "Metrics Collection"
        Prometheus[Prometheus<br/>Time-series DB]
        ServiceMonitor[ServiceMonitor<br/>Prometheus Operator]
    end

    subgraph "Visualization"
        Grafana[Grafana<br/>대시보드]
    end

    subgraph "Alerting"
        AlertManager[AlertManager<br/>알림 관리]
        Slack[Slack]
        Email[Email]
        PagerDuty[PagerDuty]
    end

    subgraph "Logging"
        App2[REST Server Pods]
        Fluentd[Fluentd<br/>Log Aggregator]
        S3[(S3<br/>Log Storage)]
        CloudWatch[CloudWatch Logs]
    end

    App --> Actuator
    Actuator -->|Scrape| ServiceMonitor
    ServiceMonitor --> Prometheus
    Prometheus --> Grafana
    Prometheus --> AlertManager
    AlertManager --> Slack
    AlertManager --> Email
    AlertManager --> PagerDuty

    App2 -->|stdout/stderr| Fluentd
    Fluentd --> S3
    Fluentd --> CloudWatch

    style Prometheus fill:#E6522C,stroke:#C43C1F,color:#fff
    style Grafana fill:#F46800,stroke:#D35400,color:#fff
    style Fluentd fill:#0E83C8,stroke:#0A5F91,color:#fff
```

### 주요 메트릭

```mermaid
mindmap
  root((모니터링 메트릭))
    애플리케이션
      JVM Heap/Non-Heap Memory
      GC Pause Time
      Thread Count
      CPU Usage
      HTTP Request Rate
      HTTP Error Rate
      Response Time p50/p95/p99
    비즈니스
      로또 티켓 생성 수
      당첨 확인 요청 수
      사용자 가입 수
      OAuth2 로그인 성공/실패 수
    인프라
      Pod CPU/Memory Usage
      Node CPU/Memory Usage
      Disk I/O
      Network I/O
    데이터베이스
      PostgreSQL Connections
      Query Execution Time
      Slow Queries
      MongoDB Operations
      Redis Hit/Miss Rate
      Kafka Lag
```

### Alert Rules

- **HighErrorRate**: HTTP 5xx > 5% for 5분
- **HighMemoryUsage**: Memory > 90% for 5분
- **PodNotReady**: Pod not ready for 5분
- **DatabaseConnectionPoolExhausted**: Active connections > 95%
- **KafkaConsumerLag**: Lag > 1000 messages for 10분

---

## CI/CD 파이프라인

```mermaid
graph LR
    A[Git Push] --> B[GitHub Actions]
    B --> C{테스트}
    C -->|Pass| D[Build JAR]
    C -->|Fail| Z[알림 & 중단]
    D --> E[Build Docker Image]
    E --> F[Push to ECR]
    F --> G[Image Scan]
    G --> H{취약점?}
    H -->|Critical| Z
    H -->|Low/Medium| I[Deploy to Dev]
    I --> J[Integration Tests]
    J -->|Pass| K{Manual Approval}
    K -->|Approved| L[Deploy to Prod]
    L --> M[Health Check]
    M -->|Success| N[Complete]
    M -->|Fail| O[Rollback]

    style C fill:#F39C12,stroke:#E67E22,color:#fff
    style K fill:#E74C3C,stroke:#C0392B,color:#fff
    style M fill:#2ECC71,stroke:#27AE60,color:#fff
    style Z fill:#E74C3C,stroke:#C0392B,color:#fff
```

### CI/CD 단계

1. **코드 품질 검증**:
   - Ktlint (코드 스타일)
   - Detekt (정적 분석)
   - JaCoCo (코드 커버리지 > 80%)

2. **테스트**:
   - 단위 테스트 (JUnit 5)
   - 통합 테스트 (Testcontainers)
   - API 테스트 (MockMvc)

3. **빌드**:
   - Gradle bootJar (최적화 빌드)
   - Multi-stage Docker build

4. **보안 스캔**:
   - ECR Image Scan (취약점 검사)
   - Trivy (컨테이너 스캔)

5. **배포**:
   - Dev: 자동 배포
   - Prod: Manual approval 후 배포
   - Blue-Green Deployment

6. **검증**:
   - Health Check (Actuator)
   - Smoke Tests
   - Rollback on failure

---

## 확장 패턴

### Horizontal Scaling (Auto Scaling)

```mermaid
graph TD
    A[Metrics Collection] --> B{CPU > 70%<br/>또는<br/>Memory > 80%?}
    B -->|Yes| C[HPA Trigger]
    C --> D[Scale Up Pods]
    D --> E[새로운 Pod 시작]
    E --> F[Readiness Probe 통과?]
    F -->|Yes| G[Service에 추가]
    F -->|No| H[재시도 또는 실패]
    G --> I[트래픽 분산]

    B -->|No| J{CPU < 50%<br/>그리고<br/>Memory < 60%?}
    J -->|Yes| K[Scale Down 고려]
    K --> L[Stabilization Window<br/>5분 대기]
    L --> M[여전히 저부하?]
    M -->|Yes| N[Pod 종료]
    M -->|No| A
    N --> O[Graceful Shutdown<br/>30초 대기]
    O --> P[Service에서 제거]

    style C fill:#E74C3C,stroke:#C0392B,color:#fff
    style G fill:#2ECC71,stroke:#27AE60,color:#fff
    style N fill:#F39C12,stroke:#E67E22,color:#fff
```

### Cluster Autoscaler

```mermaid
sequenceDiagram
    autonumber
    participant HPA as Horizontal Pod Autoscaler
    participant K8s as Kubernetes Scheduler
    participant CA as Cluster Autoscaler
    participant AWS as AWS Auto Scaling Group

    HPA->>K8s: Scale up to 30 pods (현재 노드 용량 부족)
    K8s->>K8s: Pod를 Pending 상태로 유지
    K8s->>CA: 리소스 부족 알림
    CA->>AWS: 새 노드 요청
    AWS->>CA: 노드 프로비저닝 시작
    CA->>K8s: 노드 추가됨
    K8s->>K8s: Pending Pod를 새 노드에 스케줄
    K8s->>K8s: Pod 시작 및 Readiness Probe
```

### 확장 한계

- **Pod 수**: 3 (최소) ~ 50 (최대)
- **노드 수**: 2 (최소) ~ 20 (최대)
- **CPU**: Pod당 최대 2 vCPU
- **Memory**: Pod당 최대 2Gi
- **Database Connections**: HikariCP max pool size 20

---

## 재해 복구

### Disaster Recovery 전략

```mermaid
stateDiagram-v2
    [*] --> Normal: 정상 운영
    Normal --> Degraded: 장애 발생

    Degraded --> PodFailure: Pod 장애
    Degraded --> NodeFailure: Node 장애
    Degraded --> AZFailure: AZ 장애
    Degraded --> DatabaseFailure: DB 장애

    PodFailure --> AutoRestart: Liveness Probe 실패
    AutoRestart --> Normal: Pod 재시작 성공

    NodeFailure --> Reschedule: Node Not Ready
    Reschedule --> Normal: 다른 노드에 재스케줄

    AZFailure --> MultiAZ: AZ 전체 장애
    MultiAZ --> Normal: 다른 AZ로 트래픽 전환

    DatabaseFailure --> Failover: RDS Failover
    Failover --> Normal: Standby로 전환 (< 60초)

    Degraded --> ManualIntervention: 자동 복구 실패
    ManualIntervention --> Rollback: 이전 버전으로 롤백
    Rollback --> Normal: 롤백 완료
```

### RTO/RPO 목표

- **RTO (Recovery Time Objective)**: 5분 이내
- **RPO (Recovery Point Objective)**: 1분 이내 (거의 0에 가까움)

### 백업 전략

- **Database**: 자동 백업 (매일), 스냅샷 보관 (30일)
- **Configuration**: Git 버전 관리
- **Docker Images**: ECR 이미지 보관 (최근 10개 버전)
- **Logs**: S3 장기 보관 (90일)

---

## 품질 속성

```mermaid
mindmap
  root((품질 속성))
    성능
      Response Time
        p50 < 100ms
        p95 < 500ms
        p99 < 1000ms
      Throughput
        10,000+ req/sec
      Concurrency
        10,000+ connections
    확장성
      Horizontal Scaling
        3-50 pods
      Vertical Scaling
        500m-2 CPU per pod
      Database Scaling
        Read Replicas
    가용성
      Uptime: 99.9%
      Multi-AZ
      Auto Healing
      Zero Downtime Deployment
    보안
      OAuth2 Authentication
      JWT Authorization
      Rate Limiting
      Encryption at Rest/Transit
    유지보수성
      Clean Architecture
      Test Coverage > 80%
      Monitoring & Alerting
      Comprehensive Documentation
    관측성
      Metrics
        Prometheus
      Logs
        CloudWatch
      Traces
        Distributed Tracing
      Dashboards
        Grafana
```

---

## 기술 스택 요약

| 카테고리 | 기술 |
|---------|------|
| **Language** | Kotlin 1.9 |
| **Runtime** | Java 21 (Virtual Threads) |
| **Framework** | Spring Boot 3.2 |
| **Security** | Spring Security, OAuth2, JWT |
| **Database** | PostgreSQL (JPA/Hibernate) |
| **Document DB** | MongoDB |
| **Cache** | Redis, Caffeine |
| **Message Broker** | Apache Kafka |
| **Container** | Docker, Kubernetes |
| **Cloud** | AWS (EKS, RDS, DocumentDB, ElastiCache, MSK) |
| **Monitoring** | Prometheus, Grafana |
| **Logging** | Logback, Fluentd, CloudWatch |
| **Testing** | JUnit 5, Mockito, Testcontainers |
| **Build** | Gradle 8.5 |
| **CI/CD** | GitHub Actions, ArgoCD |

---

## API 엔드포인트 요약

### 사용자 관리
- `POST /api/v1/users` - 사용자 생성
- `GET /api/v1/users/{id}` - 사용자 조회
- `PUT /api/v1/users/{id}` - 사용자 수정
- `DELETE /api/v1/users/{id}` - 사용자 삭제

### 인증
- `POST /api/v1/auth/login` - 로그인
- `POST /api/v1/auth/logout` - 로그아웃
- `POST /api/v1/auth/refresh` - 토큰 갱신
- `GET /oauth2/authorization/{provider}` - OAuth2 로그인 (Google/Naver/Kakao)

### 로또 6/45
- `POST /api/v1/lottery/lotto/generate/auto` - 자동 번호 생성
- `POST /api/v1/lottery/lotto/generate/manual` - 수동 번호 입력
- `GET /api/v1/lottery/lotto/tickets` - 내 티켓 목록 조회
- `GET /api/v1/lottery/lotto/tickets/{id}` - 티켓 상세 조회
- `POST /api/v1/lottery/lotto/tickets/{id}/check-winning` - 당첨 확인
- `GET /api/v1/lottery/lotto/draws/{drawNumber}` - 추첨 결과 조회

### 연금복권
- `POST /api/v1/lottery/pension/generate/auto` - 자동 번호 생성
- `POST /api/v1/lottery/pension/generate/manual` - 수동 번호 입력
- `GET /api/v1/lottery/pension/tickets` - 내 티켓 목록 조회
- `GET /api/v1/lottery/pension/tickets/{id}` - 티켓 상세 조회
- `POST /api/v1/lottery/pension/tickets/{id}/check-winning` - 당첨 확인
- `GET /api/v1/lottery/pension/draws/{drawNumber}` - 추첨 결과 조회

### 모니터링
- `GET /actuator/health` - Health Check
- `GET /actuator/health/liveness` - Liveness Probe
- `GET /actuator/health/readiness` - Readiness Probe
- `GET /actuator/prometheus` - Prometheus Metrics

---

## 다음 단계

1. **성능 최적화**: JVM Tuning, Query Optimization
2. **추가 기능**: WebSocket 실시간 알림, GraphQL API
3. **AI/ML 통합**: 번호 패턴 분석, 당첨 예측 (참고용)
4. **모바일 앱**: React Native / Flutter 앱 개발
5. **글로벌 확장**: Multi-region 배포, CDN 통합

---

**작성일**: 2025-11-08
**버전**: v2.0.0
**작성자**: REST Server Team
