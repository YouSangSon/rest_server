# Enterprise REST Server

**Kotlin + Spring Boot 3 기반의 엔터프라이즈급 REST API 서버**

Clean Architecture, Domain-Driven Design (DDD), Test-Driven Development (TDD) 원칙을 따르는 대규모 프로젝트용 REST 서버입니다.

[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.20-purple.svg)](https://kotlinlang.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

## 🌟 주요 기능

### 🔐 인증 & 보안
- ✅ **JWT 기반 인증 시스템**
- ✅ **OAuth2 소셜 로그인** (Google, Naver, Kakao)
- ✅ **Spring Security** 통합
- ✅ **Role 기반 권한 관리** (USER, ADMIN, MODERATOR)
- ✅ **BCrypt 비밀번호 암호화**

### 📊 데이터 저장소
- ✅ **PostgreSQL** - 관계형 데이터 (User, 트랜잭션 데이터)
- ✅ **MongoDB** - 비정형 데이터 (Audit Logs, 이벤트)
- ✅ **Redis** - 캐싱 및 세션 저장
- ✅ **JPA/Hibernate** + **Exposed ORM**

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

## 📚 주요 API

```bash
# 회원가입
POST /api/v1/users/register

# 로그인
POST /api/v1/auth/login

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

- [OAuth2 설정 가이드](README_OAUTH2_SETUP.md)
- [엔터프라이즈 가이드](README_ENTERPRISE.md)
- [API 문서](http://localhost:8080/swagger-ui.html)

---

**Version**: v3.0.0 | **Author**: YouSang Son
