# REST Server

Spring Boot 기반 REST API 서버

## 환경 변수 관리

이 프로젝트는 모든 설정을 `.env` 파일을 통해 관리합니다. 이 방식은 민감한 정보가 소스 코드에 포함되지 않도록 하고, 다양한 환경에서 쉽게 구성할 수 있도록 합니다.

### 1. .env 파일 설정하기

```bash
# .env.example 파일을 복사하여 사용
cp .env.example .env

# 편집기로 실제 값 입력
vi .env   # 또는 선호하는 텍스트 편집기 사용
```

`.env` 파일에는 다음과 같은 설정이 포함됩니다:

```properties
# Docker 레지스트리 설정
DOCKER_REGISTRY_URL=your-registry-url:port

# 데이터베이스 연결 설정
DB_URL=jdbc:postgresql://your-db-host:5432/your-db
DB_USERNAME=your-username
DB_PASSWORD=your-password

# Spring Boot 설정
SPRING_PROFILES_ACTIVE=prod
APP_PORT=8080

# 기타 애플리케이션 설정
CORS_ALLOWED_ORIGINS=https://example.com
```

### 2. 환경 변수 적용 범위

설정된 환경 변수는 다음 컴포넌트에 적용됩니다:

1. **Spring Boot 애플리케이션**: `application.yml`과 프로파일별 설정 파일에서 `${변수명}` 형식으로 사용
2. **build.gradle.kts**: 빌드 스크립트에서 Docker 이미지 빌드 설정
3. **build.sh**: Docker 이미지 빌드 및 배포 스크립트

### 3. 애플리케이션 설정 파일 준비

```bash
# 기본 설정 파일
cp src/main/resources/application.yml.example src/main/resources/application.yml

# 환경별 설정 파일
cp src/main/resources/application-dev.yml.example src/main/resources/application-dev.yml
cp src/main/resources/application-prod.yml.example src/main/resources/application-prod.yml
```

## 빌드 및 배포

### 이미지 빌드 및 푸시

```bash
./build.sh
```

이 스크립트는:
1. `.env` 파일에서 모든 설정을 로드
2. 레지스트리에서 기존 이미지 태그 조회
3. 버전 입력 요청 (Enter 키를 누르면 'latest' 사용)
4. 지정한 태그로 이미지 빌드 및 푸시
5. 선택적으로 Kubernetes 배포 재시작

### Gradle로 직접 빌드

```bash
# 환경 변수 정보 확인
./gradlew printEnv

# 이미지 빌드 및 푸시
./gradlew jib
```

### 로컬에서 실행

```bash
# .env에 설정된 프로파일로 실행
./gradlew bootRun

# 특정 프로파일로 실행
./gradlew bootRun --args='--spring.profiles.active=dev'
```

## 환경 변수 목록

| 변수명 | 설명 | 기본값 |
|--------|------|--------|
| `DOCKER_REGISTRY_URL` | Docker 레지스트리 주소 | `localhost:5000` |
| `DB_URL` | 데이터베이스 연결 URL | `jdbc:postgresql://localhost:5432/postgres` |
| `DB_USERNAME` | 데이터베이스 사용자 이름 | `postgres` |
| `DB_PASSWORD` | 데이터베이스 암호 | `postgres` |
| `DB_POOL_SIZE` | 데이터베이스 연결 풀 크기 | 개발: `5`, 운영: `20` |
| `SPRING_PROFILES_ACTIVE` | 활성 Spring 프로파일 | `prod` |
| `APP_PORT` | 애플리케이션 포트 | `8080` |
| `LOG_LEVEL` | 기본 로그 레벨 | 개발: `INFO`, 운영: `WARN` |
| `LOG_FILE_PATH` | 로그 파일 경로 | `/var/log/rest-server/application.log` |
| `CORS_ALLOWED_ORIGINS` | CORS 허용 출처 | 개발: `*`, 운영: `https://example.com` |
| `JVM_XMS` | JVM 최소 힙 크기 | `512m` |
| `JVM_XMX` | JVM 최대 힙 크기 | `1g` |

## 기술 스택

- 언어: Kotlin
- 프레임워크: Spring Boot 3.2.4
- 데이터베이스: PostgreSQL, Exposed ORM
- 컨테이너화: Jib
- 배포: Kubernetes

## Recent Updates

The project has recently undergone significant refactoring to improve performance, stability, and maintainability:

- **Transaction Management**: Resolved nested transaction issues that caused "Connection is closed" errors
- **Code Organization**: Enhanced separation of concerns in DTO, Service, and Repository layers
- **Error Handling**: Improved exception handling for greater resilience during batch operations
- **Performance**: Optimized database connection usage and transaction boundaries

For detailed information about the refactoring changes, please see [refactoring.md](refactoring.md).

## Project Architecture

This project implements a layered hexagonal architecture with the following structure:

```
src/main/kotlin/yousang/rest/
├── application/        # Application services implementing use cases
├── config/             # Configuration classes for the application
├── domain/             # Domain models and business logic
├── infra/              # Infrastructure implementations (repositories, external services)
├── interfaces/         # Controllers and request/response DTOs
├── shared/             # Shared utilities and cross-cutting concerns
│   ├── exception/      # Exception handling
│   ├── log/            # Advanced logging utilities
│   └── Constants.kt    # Application constants
└── RestApplication.kt  # Application entry point
```

## Technology Stack

- **Language**: Kotlin 2.1.10
- **Framework**: Spring Boot 3.4.3
- **Database**:
  - PostgreSQL (Production)
  - H2 (Development)
  - Exposed ORM (SQL DSL & DAO)
  - Spring Data JPA
  - Redis
- **Security**: Spring Security, OAuth2
- **API Documentation**: OpenAPI (Springdoc)
- **Validation**: Spring Validation
- **Testing**: JUnit 5, Spring Test
- **Logging**: Logback with Logstash encoder
- **Additional Features**:
  - WebFlux for reactive programming
  - Kotlin Coroutines
  - Kafka integration
  - WebSockets
  - gRPC support
  - AOP capabilities

## Logging System

The project includes a comprehensive logging system with advanced features:

- **Structured Logging**: JSON-formatted logs with contextual information
- **Colored Logging**: Terminal-friendly colored log output
- **Performance Monitoring**: Execution time tracking for operations
- **Visual Logging**: ASCII art-based visual representation for important logs
- **AOP-based Logging**: Automatic logging of method invocations
- **Request Context**: Correlation IDs for request tracing
- **Extensive Log Extensions**: Convenient logging utility functions

## Getting Started

### Prerequisites

- JDK 21
- Gradle
- Docker (optional, for containerized databases)

### Development Setup

1. Clone the repository
2. Copy the example configuration files:
   ```bash
   cp src/main/resources/application.yml.example src/main/resources/application.yml
   cp src/main/resources/application-dev.yml.example src/main/resources/application-dev.yml
   ```
3. Configure the database connection directly in `application.yml` and `application-dev.yml`:
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://your_host:your_port/your_db
       username: your_username
       password: your_password
       driver-class-name: org.postgresql.Driver
   ```

### Configuration

Database and other configuration values should be configured directly in the application YAML files.

Example `application.yml` configuration:
```yaml
spring:
  application:
    name: rest
  datasource:
    url: jdbc:postgresql://your_host:your_port/your_db
    username: your_username
    password: your_password
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      idle-timeout: 30000
      connection-timeout: 10000
      max-lifetime: 2000000
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.PostgreSQLDialect

server:
  port: 8080
```

### Security Considerations

When deploying or sharing this application:

1. Never commit sensitive information (API keys, passwords, etc.) to version control
2. Ensure `application.yml` and `

## 환경 설정 관리

이 프로젝트는 두 가지 방식으로 설정을 관리합니다:

1. **민감한 정보**: 'env' 디렉토리의 환경 파일에서 관리
2. **구조화된 설정**: `application.yml` 파일에서 관리

### 프로필 기반 설정

Spring의 프로필 기능을 활용하여 환경별 설정을 관리합니다:

- **개발 환경**: `env/dev.env` + `application-dev.yml`
- **운영 환경**: `env/prod.env` + `application-prod.yml`

### 설정 파일 구조

- `env/dev.env`: 개발 환경의 민감한 정보와 환경별 설정값
- `env/prod.env`: 운영 환경의 민감한 정보와 환경별 설정값
- `env/example.env`: 환경 설정 예시 파일
- `application.yml`: 공통 구조화 설정
- `application-dev.yml`: 개발 환경 전용 구조화 설정
- `application-prod.yml`: 운영 환경 전용 구조화 설정

### 민감한 설정 관리

이 프로젝트에서는 민감한 설정 정보를 `env` 디렉토리에 저장하고, 이를 별도의 프라이빗 Git 저장소로 관리합니다:

1. **env 디렉토리 초기화**:
   ```bash
   # 메인 저장소 클론 후
   git clone https://your-private-repo/rest-server-env.git env
   ```

2. **새로운 환경에서 설정**:
   ```bash
   # env 디렉토리가 존재하지 않는 경우
   mkdir -p env
   cp env-example/example.env env/dev.env
   # 필요한 설정 수정
   ```

3. **env 디렉토리를 별도 저장소로 관리**:
   ```bash
   cd env
   git init
   git remote add origin https://your-private-repo/rest-server-env.git
   git add .
   git commit -m "Add environment files"
   git push -u origin main
   ```

### 사용 방법

1. `env/example.env`를 복사하여 `env/dev.env` 파일 생성
2. 개발 환경에서는 `env/dev.env` 파일만 수정하여 사용
3. 운영 환경에서는 `env/prod.env` 파일 생성 및 설정
4. 환경 변수 `SPRING_PROFILES_ACTIVE`를 통해 프로필 지정 (기본값: `dev`)

```bash
# 개발 환경으로 실행
SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun

# 운영 환경으로 실행
SPRING_PROFILES_ACTIVE=prod ./gradlew bootRun
```

### 주의사항

- `env` 디렉토리는 메인 저장소의 `.gitignore`에 포함되어 있으며, 별도의 프라이빗 저장소로 관리됩니다.
- 민감한 정보는 항상 `env/*.env` 파일에 저장하고, 환경 변수를 통해 로드합니다.
- 구조화된 설정은 `application-{profile}.yml` 파일에 저장합니다.