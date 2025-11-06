# 🚀 Quick Start Guide

이 가이드는 REST Server를 최대한 빠르게 실행하는 방법을 안내합니다.

## 📋 목차

1. [환경 설정](#1-환경-설정)
2. [OAuth2 설정](#2-oauth2-설정)
3. [서비스 실행](#3-서비스-실행)
4. [테스트](#4-테스트)

---

## 전체 플로우

```mermaid
graph TB
    A[시작] --> B[환경 변수 설정]
    B --> C{OAuth2<br/>사용?}
    C -->|Yes| D[OAuth2 Credentials 발급]
    C -->|No| E[Docker Compose 실행]
    D --> E
    E --> F[서비스 헬스체크]
    F --> G{정상?}
    G -->|Yes| H[API 테스트]
    G -->|No| I[로그 확인]
    I --> E
    H --> J[완료!]

    style A fill:#e1f5fe
    style J fill:#c8e6c9
    style I fill:#ffcdd2
```

---

## 1. 환경 설정

### Step 1-1: 환경 변수 설정 스크립트 실행

```bash
# 스크립트 실행 (.env 파일 생성)
./setup-oauth2-env.sh
```

이 스크립트는:
- `.env` 파일 템플릿 생성
- 환경 변수 확인 및 검증
- 현재 설정 상태 표시

### Step 1-2: .env 파일 편집

OAuth2를 사용하지 않는 경우 이 단계를 건너뛸 수 있습니다.

```bash
# .env 파일 편집
nano .env  # 또는 vi, code 등 선호하는 에디터 사용
```

---

## 2. OAuth2 설정

### OAuth2 플로우

```mermaid
sequenceDiagram
    participant User
    participant Frontend
    participant RestServer
    participant OAuth2Provider
    participant Database

    User->>Frontend: 소셜 로그인 클릭
    Frontend->>RestServer: /oauth2/authorization/{provider}
    RestServer->>OAuth2Provider: 인증 요청
    OAuth2Provider->>User: 로그인 페이지
    User->>OAuth2Provider: 로그인 & 동의
    OAuth2Provider->>RestServer: Authorization Code
    RestServer->>OAuth2Provider: Access Token 요청
    OAuth2Provider->>RestServer: Access Token
    RestServer->>OAuth2Provider: 사용자 정보 요청
    OAuth2Provider->>RestServer: 사용자 정보
    RestServer->>Database: 사용자 저장/조회
    Database->>RestServer: 사용자 데이터
    RestServer->>RestServer: JWT 생성
    RestServer->>Frontend: Redirect with JWT
    Frontend->>User: 로그인 완료
```

### 2-1. Google OAuth2 설정

**필수 URL**: https://console.cloud.google.com/

```mermaid
graph LR
    A[Google Cloud Console] --> B[프로젝트 생성]
    B --> C[OAuth 동의 화면]
    C --> D[OAuth 2.0 클라이언트 ID]
    D --> E[Redirect URI 설정]
    E --> F[Credentials 복사]
    F --> G[.env 파일에 추가]

    style A fill:#4285f4,color:#fff
    style G fill:#c8e6c9
```

**Redirect URI**:
```
http://localhost:8080/login/oauth2/code/google
```

**Scopes**:
- `email`
- `profile`

### 2-2. Naver OAuth2 설정

**필수 URL**: https://developers.naver.com/

```mermaid
graph LR
    A[Naver Developers] --> B[애플리케이션 등록]
    B --> C[네이버 로그인 API]
    C --> D[Callback URL 설정]
    D --> E[Credentials 복사]
    E --> F[.env 파일에 추가]

    style A fill:#00c73c,color:#fff
    style F fill:#c8e6c9
```

**Callback URL**:
```
http://localhost:8080/login/oauth2/code/naver
```

**제공 정보**:
- 회원이름
- 이메일 주소

### 2-3. Kakao OAuth2 설정

**필수 URL**: https://developers.kakao.com/

```mermaid
graph LR
    A[Kakao Developers] --> B[애플리케이션 추가]
    B --> C[Web 플랫폼 등록]
    C --> D[카카오 로그인 활성화]
    D --> E[Redirect URI 등록]
    E --> F[동의항목 설정]
    F --> G[Credentials 복사]
    G --> H[.env 파일에 추가]

    style A fill:#fee500
    style H fill:#c8e6c9
```

**Redirect URI**:
```
http://localhost:8080/login/oauth2/code/kakao
```

**동의항목**:
- 닉네임 (필수)
- 이메일 (필수)

---

## 3. 서비스 실행

### 아키텍처 개요

```mermaid
graph TB
    subgraph "Frontend"
        FE[React/Vue/Angular]
    end

    subgraph "Backend - Port 8080"
        API[REST API Server]
    end

    subgraph "Databases"
        PG[(PostgreSQL<br/>5432)]
        MG[(MongoDB<br/>27017)]
        RD[(Redis<br/>6379)]
    end

    subgraph "Messaging"
        ZK[Zookeeper<br/>2181]
        KF[Kafka<br/>9092]
        KUI[Kafka UI<br/>8090]
    end

    subgraph "Monitoring"
        PGA[pgAdmin<br/>5050]
        ACT[Actuator<br/>/actuator]
        SW[Swagger<br/>/swagger-ui]
    end

    FE -->|HTTP| API
    API --> PG
    API --> MG
    API --> RD
    API --> KF
    KF --> ZK

    style API fill:#4caf50,color:#fff
    style PG fill:#336791,color:#fff
    style MG fill:#47a248,color:#fff
    style RD fill:#dc382d,color:#fff
    style KF fill:#231f20,color:#fff
```

### 3-1. Docker Compose 실행

```bash
# 전체 스택 실행
docker-compose up -d

# 로그 확인
docker-compose logs -f rest-server
```

### 3-2. 서비스 상태 확인

```bash
# Health Check
curl http://localhost:8080/actuator/health

# 예상 출력:
# {"status":"UP"}
```

### 3-3. 각 서비스 접속 확인

| 서비스 | URL | 설명 |
|--------|-----|------|
| 🌐 REST API | http://localhost:8080 | 메인 API |
| 📚 Swagger UI | http://localhost:8080/swagger-ui.html | API 문서 |
| 🔍 Actuator | http://localhost:8080/actuator | 모니터링 |
| 📊 Kafka UI | http://localhost:8090 | Kafka 모니터링 |
| 🗄️ pgAdmin | http://localhost:5050 | PostgreSQL 관리 |

---

## 4. 테스트

### 테스트 플로우

```mermaid
graph TB
    A[시작] --> B[회원가입 테스트]
    B --> C[로그인 테스트]
    C --> D[JWT 토큰 확인]
    D --> E[인증 API 테스트]
    E --> F[OAuth2 테스트]
    F --> G[Kafka 이벤트 확인]
    G --> H[MongoDB Audit Log 확인]
    H --> I[완료]

    style A fill:#e1f5fe
    style I fill:#c8e6c9
```

### 4-1. 회원가입 테스트

```bash
curl -X POST http://localhost:8080/api/v1/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123"
  }'
```

**예상 응답**:
```json
{
  "id": 1,
  "username": "testuser",
  "email": "test@example.com",
  "roles": ["USER"],
  "enabled": true,
  "createdAt": "2025-11-06T12:00:00",
  "updatedAt": "2025-11-06T12:00:00"
}
```

### 4-2. 로그인 테스트

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

**예상 응답**:
```json
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

### 4-3. 인증 API 테스트

```bash
# 토큰 저장
TOKEN="your-access-token-here"

# 사용자 정보 조회
curl -X GET http://localhost:8080/api/v1/users/1 \
  -H "Authorization: Bearer $TOKEN"
```

### 4-4. OAuth2 로그인 테스트

브라우저에서 접속:

```
# Google
http://localhost:8080/oauth2/authorization/google

# Naver
http://localhost:8080/oauth2/authorization/naver

# Kakao
http://localhost:8080/oauth2/authorization/kakao
```

**OAuth2 로그인 후 플로우**:

```mermaid
graph LR
    A[OAuth2 로그인] --> B[권한 동의]
    B --> C[Redirect with Code]
    C --> D[토큰 발급]
    D --> E[사용자 정보 조회]
    E --> F{기존 사용자?}
    F -->|Yes| G[로그인]
    F -->|No| H[회원가입]
    H --> G
    G --> I[JWT 발급]
    I --> J[Frontend Redirect]

    style A fill:#4285f4,color:#fff
    style J fill:#c8e6c9
```

### 4-5. Kafka 이벤트 확인

**Kafka UI 접속**: http://localhost:8090

확인할 토픽:
- `user-events` - 사용자 이벤트
- `audit-events` - 감사 로그
- `notifications` - 알림

### 4-6. MongoDB Audit Log 확인

```bash
# MongoDB 접속
docker exec -it rest-mongodb mongosh

# 데이터베이스 선택
use rest_server

# Audit Log 확인
db.audit_logs.find().pretty()

# 특정 사용자의 로그 확인
db.audit_logs.find({username: "testuser"}).pretty()

# OAuth2 로그인 이벤트만 확인
db.audit_logs.find({eventType: "OAUTH2_LOGIN"}).pretty()
```

---

## 🎯 주요 명령어 정리

```bash
# 환경 설정
source setup-oauth2-env.sh

# 서비스 시작
docker-compose up -d

# 서비스 중지
docker-compose down

# 로그 확인
docker-compose logs -f rest-server

# 전체 재시작
docker-compose restart

# 볼륨 포함 전체 삭제
docker-compose down -v

# 특정 서비스만 재시작
docker-compose restart rest-server
```

---

## 🐛 문제 해결

### 포트 충돌

```mermaid
graph TB
    A[포트 사용 중 에러] --> B{어떤 포트?}
    B -->|8080| C[REST Server]
    B -->|5432| D[PostgreSQL]
    B -->|27017| E[MongoDB]
    B -->|9092| F[Kafka]

    C --> G[docker-compose.yml에서<br/>포트 변경]
    D --> G
    E --> G
    F --> G

    G --> H[다시 실행]

    style A fill:#ffcdd2
    style H fill:#c8e6c9
```

**해결 방법**:
```bash
# 포트 사용 확인
lsof -i :8080
netstat -tuln | grep 8080

# 프로세스 종료
kill -9 <PID>
```

### 서비스 시작 실패

```bash
# 로그 확인
docker-compose logs rest-server

# 의존성 서비스 확인
docker-compose ps

# 헬스체크 확인
docker-compose exec rest-server wget --spider http://localhost:8080/actuator/health
```

### OAuth2 로그인 실패

**체크리스트**:
- [ ] Client ID/Secret이 정확한가?
- [ ] Redirect URI가 정확히 일치하는가?
- [ ] 환경 변수가 제대로 로드되었는가?
- [ ] 제공자 콘솔에서 API가 활성화되었는가?

---

## 📚 추가 자료

- [OAuth2 상세 설정 가이드](README_OAUTH2_SETUP.md)
- [엔터프라이즈 가이드](README_ENTERPRISE.md)
- [API 문서](http://localhost:8080/swagger-ui.html)

---

**Last Updated**: 2025-11-06
**Version**: v3.0.0
