# Enterprise REST Server

**Kotlin + Spring Boot 3 기반의 엔터프라이즈급 REST API 서버**

Clean Architecture, Domain-Driven Design (DDD), Test-Driven Development (TDD) 원칙을 따르는 대규모 프로젝트용 REST 서버입니다.

**주요 시스템:**
- 🤖 **자동 투자 봇 시스템** - 주식/코인 자동 매매 및 ML 기반 전략
- 📱 **투자 중심 SNS 플랫폼** - 포트폴리오 공유 및 투자 아이디어 소셜 네트워크

[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.20-purple.svg)](https://kotlinlang.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

[![CI](https://github.com/YouSangSon/rest_server/workflows/CI%20-%20Build%20and%20Test/badge.svg)](https://github.com/YouSangSon/rest_server/actions/workflows/ci.yml)
[![CodeQL](https://github.com/YouSangSon/rest_server/workflows/CodeQL%20Security%20Analysis/badge.svg)](https://github.com/YouSangSon/rest_server/actions/workflows/codeql.yml)
[![Docker](https://img.shields.io/badge/Docker-Hub-blue.svg)](https://hub.docker.com/r/yousangson/rest-server)

## 🌟 주요 기능

### 🤖 자동 투자 봇 시스템
- ✅ **다중 거래소 지원** (Upbit, Binance, KIS 증권)
- ✅ **ML 기반 투자 전략** - 가격 예측 및 신호 생성
- ✅ **백테스팅 엔진** - 전략 검증 및 성과 분석
- ✅ **고급 기술 지표** (MACD, RSI, Bollinger Bands, Stochastic)
- ✅ **리스크 관리** - 손절/익절, 포지션 사이징
- ✅ **실시간 시장 데이터** - WebSocket 스트리밍

### 📱 투자 중심 SNS 플랫폼
- ✅ **포트폴리오 공유** - 실시간 수익률 추적
- ✅ **투자 아이디어 게시** - 종목 분석 및 전략 공유
- ✅ **소셜 기능** - 팔로우, 좋아요, 댓글, 북마크
- ✅ **실시간 채팅** - 1:1 대화 및 투자 토론
- ✅ **가격 알림** - 관심 종목 워치리스트
- ✅ **24시간 스토리** - 빠른 시장 업데이트

### 🔐 인증 & 보안
- ✅ **JWT 기반 인증 시스템**
- ✅ **OAuth2 소셜 로그인** (Google, Naver, Kakao)
- ✅ **Spring Security** 통합
- ✅ **Role 기반 권한 관리** (USER, ADMIN, MODERATOR)
- ✅ **BCrypt 비밀번호 암호화**

### 📊 데이터 저장소
- ✅ **PostgreSQL** - 관계형 데이터 (Users, Portfolios, Holdings, Trades)
  - Spring Data JPA로 직접 연결
  - HikariCP 커넥션 풀링
- ✅ **MongoDB** - 비정형 데이터 (Posts, Messages, Notifications, Stories)
  - Spring Data MongoDB로 직접 연결
  - 자동 인덱싱 및 샤딩 지원
- ✅ **Redis** - 캐싱, 세션, 실시간 데이터
  - Spring Data Redis 통합
- ✅ **JPA/Hibernate** + **Exposed ORM** + **Spring Data**

### 📨 메시징 & 이벤트
- ✅ **Apache Kafka** - 이벤트 스트리밍
- ✅ **Producer/Consumer** 패턴
- ✅ **Event-Driven Architecture**

### 📚 API 문서화
- ✅ **Swagger/OpenAPI 3.0** (SpringDoc)
- ✅ **Interactive API Testing UI**
- ✅ **자동 문서 생성**

### 📈 모니터링 & 로깅
- ✅ **Spring Actuator** - 헬스체크, 메트릭
- ✅ **Prometheus Metrics** - 시계열 데이터
- ✅ **Audit Logging** (MongoDB)
- ✅ **구조화된 로깅**

### 🚀 성능 & 확장성
- ✅ **Redis 캐싱**
- ✅ **Rate Limiting** (Bucket4j) - DDoS 방지
- ✅ **Virtual Threads** (Java 21)
- ✅ **Connection Pooling** (HikariCP)

### 🐳 DevOps
- ✅ **Docker & Docker Compose**
- ✅ **Multi-stage Dockerfile**
- ✅ **Health Checks**
- ✅ **Auto-restart Policies**

## 🚀 빠른 시작

### 전체 스택 실행 (권장)

```bash
# 전체 서비스 실행
docker-compose up -d

# 로그 확인
docker-compose logs -f rest-server

# 접속 확인
curl http://localhost:8080/actuator/health
```

### OAuth2 설정

소셜 로그인을 사용하려면 [README_OAUTH2_SETUP.md](README_OAUTH2_SETUP.md) 참조

## 🔄 CI/CD

### GitHub Actions

프로젝트는 완전 자동화된 CI/CD 파이프라인을 제공합니다:

**Workflows:**
- ✅ **CI**: 자동 빌드, 테스트, Docker 이미지 생성
- ✅ **CD**: 스테이징/프로덕션 자동 배포
- ✅ **PR Validation**: 코드 리뷰, 품질 검사, 보안 스캔
- ✅ **CodeQL**: 주간 보안 취약점 분석
- ✅ **Release**: 자동 릴리즈 노트 생성 및 배포

**Features:**
- 🔄 자동 빌드 & 테스트 (PostgreSQL, MongoDB, Redis 포함)
- 🐳 멀티 플랫폼 Docker 이미지 빌드 (amd64, arm64)
- 🔒 보안 스캔 (Trivy, OWASP, CodeQL)
- 📊 코드 품질 분석 (Detekt, SonarCloud)
- 🚀 Blue-Green 무중단 배포
- 📦 자동 의존성 업데이트 (Dependabot)

**배포 전략:**
```bash
# 개발 → 스테이징 (자동)
git push origin develop

# 프로덕션 릴리즈 (태그)
git tag -a v1.0.0 -m "Release 1.0.0"
git push origin v1.0.0
```

자세한 내용: [CI/CD 가이드](docs/DEPLOYMENT_GUIDE.md#-cicd-pipeline-github-actions)

## 📚 주요 API

### 자동 투자 봇 API
```bash
# 전략 관리
GET    /api/v1/strategies
POST   /api/v1/strategies
PUT    /api/v1/strategies/{id}

# 주문 실행
POST   /api/v1/orders
GET    /api/v1/orders/{id}
DELETE /api/v1/orders/{id}

# 백테스팅
POST   /api/v1/backtest/run
GET    /api/v1/backtest/{id}/results

# 포트폴리오 조회
GET    /api/v1/portfolio/balances
GET    /api/v1/portfolio/performance
```

### SNS 플랫폼 API
```bash
# 인증
POST   /api/v1/sns/auth/register
POST   /api/v1/sns/auth/login
POST   /api/v1/sns/auth/refresh

# 소셜 기능
GET    /api/v1/sns/posts (Feed)
POST   /api/v1/sns/posts
POST   /api/v1/sns/posts/{id}/like
POST   /api/v1/sns/users/{id}/follow

# 투자 포트폴리오
GET    /api/v1/sns/portfolios
POST   /api/v1/sns/portfolios
GET    /api/v1/sns/portfolios/{id}/analytics
POST   /api/v1/sns/portfolios/{id}/holdings

# 실시간 채팅
GET    /api/v1/sns/conversations
POST   /api/v1/sns/messages
```

### 기타
```bash
# OAuth2 소셜 로그인
http://localhost:8080/oauth2/authorization/google
http://localhost:8080/oauth2/authorization/naver
http://localhost:8080/oauth2/authorization/kakao

# Swagger UI
http://localhost:8080/swagger-ui.html
```

## 📊 기술 스택

- **Kotlin 2.2.20** + **Spring Boot 3.5.6** + **Java 21**
- **PostgreSQL** + **MongoDB** + **Redis** + **Kafka**
- **JWT** + **OAuth2** + **Spring Security**
- **Docker** + **Prometheus** + **Actuator**

## 📖 문서

### 시스템 아키텍처
- [완전한 시스템 아키텍처](docs/COMPLETE_ARCHITECTURE.md) - 전체 시스템 개요 및 설계
- [데이터베이스 스키마](docs/DATABASE_SCHEMA.md) - PostgreSQL/MongoDB 스키마 상세 문서
- [배포 가이드](docs/DEPLOYMENT_GUIDE.md) - Docker, 프로덕션 배포, 모니터링

### API 문서
- [SNS API 문서](docs/SNS_API_DOCUMENTATION.md) - SNS 플랫폼 API 완전 가이드 (37+ 엔드포인트)
- [Trading Bot API](docs/TRADING_BOT_API.md) - 자동 투자 봇 API 가이드
- [Swagger UI](http://localhost:8080/swagger-ui.html) - 인터랙티브 API 테스트

### 기술 구현
- [SNS Repository Adapters](docs/SNS_REPOSITORY_ADAPTERS.md) - 17개 Repository Adapter 구현 가이드
- [Database Service Integration](docs/DATABASE_SERVICE_INTEGRATION.md) - DB 서비스 통합 완료 보고서

### 설정 가이드
- [OAuth2 설정 가이드](README_OAUTH2_SETUP.md) - 소셜 로그인 설정
- [엔터프라이즈 가이드](README_ENTERPRISE.md) - 대규모 운영 가이드

---

**Version**: v3.0.0 | **Author**: YouSang Son
