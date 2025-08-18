# 시스템 아키텍처 개요

## 🏗️ 아키텍처 개요

REST Server는 **헥사고날 아키텍처(Hexagonal Architecture)** 패턴을 기반으로 설계된 Spring Boot 3 기반의 REST API 서버입니다. 이 아키텍처는 비즈니스 로직과 외부 의존성을 분리하여 유지보수성과 테스트 가능성을 높입니다.

## 🎯 아키텍처 원칙

### 1. 계층 분리 (Layered Separation)
- **인터페이스 계층**: 외부 요청/응답 처리
- **애플리케이션 계층**: 비즈니스 유스케이스 구현
- **도메인 계층**: 핵심 비즈니스 로직 및 엔티티
- **인프라 계층**: 데이터베이스, 외부 서비스 연동

### 2. 의존성 역전 (Dependency Inversion)
- 고수준 모듈이 저수준 모듈에 의존하지 않음
- 추상화를 통한 느슨한 결합 구현
- 테스트 시 모킹이 용이한 구조

### 3. 단일 책임 원칙 (Single Responsibility)
- 각 클래스는 하나의 명확한 책임만 가짐
- 관심사의 분리로 코드 가독성 향상

## 🏛️ 시스템 구조

```
┌─────────────────────────────────────────────────────────────┐
│                    외부 시스템 (External Systems)            │
│  - 클라이언트 애플리케이션                                   │
│  - 외부 API 서비스                                          │
│  - 메시징 시스템 (Kafka)                                    │
└─────────────────────┬───────────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────────┐
│                    인터페이스 계층 (Interface Layer)          │
│  - REST Controllers                                        │
│  - gRPC Services                                           │
│  - WebSocket Handlers                                      │
│  - API Response DTOs                                       │
└─────────────────────┬───────────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────────┐
│                  애플리케이션 계층 (Application Layer)        │
│  - Use Case Services                                       │
│  - Application Services                                    │
│  - Command/Query Handlers                                  │
└─────────────────────┬───────────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────────┐
│                     도메인 계층 (Domain Layer)                │
│  - Domain Entities                                         │
│  - Domain Services                                         │
│  - Repository Interfaces                                   │
│  - Business Logic                                          │
└─────────────────────┬───────────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────────┐
│                   인프라 계층 (Infrastructure Layer)         │
│  - Repository Implementations                              │
│  - Database Connections                                    │
│  - External Service Clients                                │
│  - Message Brokers                                         │
└─────────────────────┬───────────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────────┐
│                    데이터 계층 (Data Layer)                  │
│  - PostgreSQL Database                                     │
│  - Redis Cache                                             │
│  - File Storage                                            │
└─────────────────────────────────────────────────────────────┘
```

## 📁 프로젝트 구조

```
src/main/kotlin/yousang/rest/
├── RestApplication.kt              # 애플리케이션 진입점
├── application/                    # 애플리케이션 서비스
│   └── lotto/                     # 로또 도메인 유스케이스
│       ├── LottoUseCase.kt        # 로또 비즈니스 로직
│       └── AnnuityLottoUseCase.kt # 연금 로또 비즈니스 로직
├── config/                         # 설정 클래스들
│   ├── ApplicationStartupConfig.kt # 애플리케이션 시작 설정
│   ├── DataConfig.kt              # 데이터베이스 설정
│   ├── SecurityConfig.kt          # 보안 설정
│   ├── WebConfig.kt               # 웹 설정
│   └── ...                        # 기타 설정들
├── domain/                         # 도메인 모델
│   └── lotto/                     # 로또 도메인
│       ├── LottoEntity.kt         # 로또 엔티티
│       ├── LottoService.kt        # 로또 서비스 인터페이스
│       └── LottoRepository.kt     # 로또 리포지토리 인터페이스
├── infra/                          # 인프라 구현
│   └── lotto/                     # 로또 인프라
│       └── LottoRepositoryImpl.kt # 로또 리포지토리 구현
├── interfaces/                     # 외부 인터페이스
│   ├── ApiResponse.kt             # API 응답 표준
│   ├── BaseController.kt          # 기본 컨트롤러
│   └── lotto/                     # 로또 API
│       ├── LottoController.kt     # 로또 REST 컨트롤러
│       └── LottoDto.kt            # 로또 데이터 전송 객체
└── shared/                         # 공통 유틸리티
    ├── Constants.kt                # 상수 정의
    ├── exception/                  # 예외 처리
    ├── log/                        # 로깅 유틸리티
    └── util/                       # 기타 유틸리티
```

## 🔧 기술 스택

### 핵심 프레임워크
- **Spring Boot 3.4.4**: 메인 애플리케이션 프레임워크
- **Kotlin 2.1.20**: 주 개발 언어
- **Spring WebFlux**: 리액티브 웹 프레임워크
- **Spring Security**: 보안 및 인증

### 데이터베이스 및 ORM
- **PostgreSQL**: 주 데이터베이스
- **Exposed ORM**: Kotlin 전용 ORM
- **HikariCP**: 커넥션 풀
- **Redis**: 캐싱 및 세션 저장소

### 통신 및 프로토콜
- **gRPC**: 마이크로서비스 간 통신
- **WebSocket**: 실시간 양방향 통신
- **Kafka**: 메시지 브로커링
- **HTTP/2**: 고성능 HTTP 프로토콜

### 개발 도구 및 라이브러리
- **Gradle**: 빌드 도구
- **JUnit 5**: 테스트 프레임워크
- **MockK**: Kotlin 전용 모킹 라이브러리
- **SpringDoc**: OpenAPI 문서화

## 🚀 아키텍처 특징

### 1. 리액티브 프로그래밍
- **WebFlux**: 논블로킹 I/O 지원
- **Kotlin Coroutines**: 비동기 프로그래밍
- **Project Reactor**: 리액티브 스트림 구현

### 2. 마이크로서비스 준비
- **gRPC**: 서비스 간 통신 표준화
- **API Gateway**: 단일 진입점 제공
- **Service Discovery**: 서비스 발견 메커니즘

### 3. 확장 가능한 설계
- **수평 확장**: 스테이트리스 아키텍처
- **로드 밸런싱**: 다중 인스턴스 지원
- **데이터베이스 샤딩**: 대용량 데이터 처리

### 4. 보안 및 모니터링
- **OAuth2**: 표준 인증 프로토콜
- **Actuator**: 애플리케이션 모니터링
- **Structured Logging**: 체계적인 로깅

## 🔄 데이터 흐름

### 1. 요청 처리 흐름
```
Client Request → Controller → Use Case → Domain Service → Repository → Database
     ↓
Response ← Controller ← Use Case ← Domain Service ← Repository ← Database
```

### 2. 비즈니스 로직 처리
```
Input Validation → Business Rules → Data Transformation → Persistence → Response
```

### 3. 예외 처리 흐름
```
Exception → Global Exception Handler → Logging → Structured Response
```

## 📊 성능 특성

### 1. 응답 시간
- **일반 API**: < 100ms
- **데이터베이스 조회**: < 50ms
- **복잡한 비즈니스 로직**: < 200ms

### 2. 처리량
- **동시 사용자**: 1000+ (기본 설정)
- **초당 요청**: 1000+ (기본 설정)
- **데이터베이스 연결**: 20 (기본 풀 크기)

### 3. 확장성
- **수평 확장**: 무제한 (스테이트리스)
- **수직 확장**: JVM 힙 메모리 조정 가능
- **데이터베이스**: 읽기 전용 복제본 지원

## 🔮 향후 발전 방향

### 1. 단기 목표 (3-6개월)
- **API 버전 관리**: v1, v2 등 체계적 버전 관리
- **캐싱 전략**: Redis를 활용한 성능 최적화
- **모니터링 강화**: Prometheus + Grafana 연동

### 2. 중기 목표 (6-12개월)
- **마이크로서비스 분리**: 도메인별 서비스 분리
- **이벤트 소싱**: CQRS 패턴 도입
- **CI/CD 파이프라인**: 자동화된 배포 프로세스

### 3. 장기 목표 (1년 이상)
- **클라우드 네이티브**: Kubernetes 기반 운영
- **서버리스**: FaaS 아키텍처 고려
- **AI/ML 통합**: 머신러닝 모델 서빙

---

**문서 버전**: v1.0.0  
**마지막 업데이트**: 2024-12-19  
**작성자**: Development Team
