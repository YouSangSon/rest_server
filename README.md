# REST Server

Kotlin + Spring Boot 3 + WebFlux 기반의 REST API 서버입니다.

## 🚀 주요 기능

- **Kotlin + Spring Boot 3**: 최신 Spring Boot 프레임워크와 Kotlin 언어 사용
- **Spring WebFlux**: 비동기 비차단 I/O 기반의 반응형 웹 애플리케이션
- **PostgreSQL**: 프로덕션급 관계형 데이터베이스 지원
- **OAuth2 소셜 로그인**: Google, GitHub, Kakao 소셜 로그인 지원
- **Swagger/OpenAPI**: 자동 API 문서화 및 테스트 인터페이스
- **Exposed ORM**: Kotlin 전용 ORM 프레임워크
- **gRPC**: 마이크로서비스 간 통신을 위한 gRPC 지원
- **WebSocket**: 실시간 양방향 통신
- **Redis**: 캐싱 및 세션 저장소
- **Docker**: 컨테이너화 및 배포 지원

## 🏗️ 아키텍처

- **헥사고날 아키텍처**: 도메인 중심의 계층 분리
- **포트와 어댑터 패턴**: 의존성 역전 원칙 적용
- **반응형 프로그래밍**: WebFlux와 Kotlin Coroutines 활용
- **마이크로서비스 준비**: gRPC, WebSocket, Kafka 지원

## 📋 요구사항

- **JDK**: 21 이상
- **Kotlin**: 1.9.x 이상
- **Gradle**: 8.x 이상
- **PostgreSQL**: 15 이상
- **Redis**: 7.x 이상
- **Docker**: 20.x 이상 (선택사항)

## 🚀 빠른 시작

### 1. 프로젝트 클론

```bash
git clone https://github.com/YourUsername/rest_server.git
cd rest_server
```

### 2. 환경 설정

```bash
# 개발 환경 설정 파일 복사
cp env/dev.env.example env/dev.env

# 환경 변수 설정 (OAuth2 클라이언트 정보 등)
# Google, GitHub, Kakao OAuth2 앱 등록 후 클라이언트 ID/시크릿 설정
```

### 3. Docker Compose로 인프라 실행

```bash
# PostgreSQL, Redis, pgAdmin 실행
docker-compose up -d postgres redis pgadmin

# 데이터베이스 준비 대기 (약 30초)
sleep 30
```

### 4. 애플리케이션 실행

```bash
# Gradle로 실행
./gradlew bootRun

# 또는 Docker로 실행
docker-compose up rest-server
```

### 5. 접속 확인

- **애플리케이션**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API 문서**: http://localhost:8080/api-docs
- **pgAdmin**: http://localhost:5050 (admin@example.com / admin)
- **PostgreSQL**: localhost:5432
- **Redis**: localhost:6379

## 🔐 OAuth2 소셜 로그인 설정

### Google OAuth2
1. [Google Cloud Console](https://console.cloud.google.com/)에서 프로젝트 생성
2. OAuth 2.0 클라이언트 ID 생성
3. `env/dev.env`에 클라이언트 ID/시크릿 설정

### GitHub OAuth2
1. [GitHub Developer Settings](https://github.com/settings/developers)에서 OAuth App 생성
2. `env/dev.env`에 클라이언트 ID/시크릿 설정

### Kakao OAuth2
1. [Kakao Developers](https://developers.kakao.com/)에서 애플리케이션 생성
2. `env/dev.env`에 클라이언트 ID/시크릿 설정

## 📚 API 사용법

### 인증 API

```bash
# 현재 사용자 정보 조회
curl -H "Authorization: Bearer {token}" \
  http://localhost:8080/api/v1/auth/me

# 지원하는 OAuth2 제공자 목록
curl http://localhost:8080/api/v1/auth/providers

# 사용자 프로필 수정
curl -X PUT -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{"username": "새로운이름"}' \
  http://localhost:8080/api/v1/auth/profile

# 로그아웃
curl -X POST -H "Authorization: Bearer {token}" \
  http://localhost:8080/api/v1/auth/logout
```

### 로또 API

```bash
# 로또 당첨 정보 조회
curl http://localhost:8080/api/v1/lotto/1001

# 로또 당첨 정보 생성
curl -X POST -H "Content-Type: application/json" \
  -d '{"drwNo": 1003, "drwNoDate": "2024-01-15", ...}' \
  http://localhost:8080/api/v1/lotto
```

## 🧪 테스트

```bash
# 단위 테스트
./gradlew test

# 통합 테스트
./gradlew integrationTest

# 전체 테스트
./gradlew check
```

## 📊 모니터링

- **Health Check**: `/actuator/health`
- **Metrics**: `/actuator/metrics`
- **Prometheus**: `/actuator/prometheus`

## 🐳 Docker

```bash
# 이미지 빌드
docker build -t rest-server .

# 컨테이너 실행
docker run -p 8080:8080 rest-server

# 전체 스택 실행
docker-compose up -d
```

## 📁 프로젝트 구조

```
src/
├── main/
│   ├── kotlin/yousang/rest/
│   │   ├── application/          # 애플리케이션 서비스
│   │   ├── config/               # 설정 클래스
│   │   ├── domain/               # 도메인 모델
│   │   ├── infra/                # 인프라 구현
│   │   └── interfaces/           # API 인터페이스
│   └── resources/
│       ├── application.yml       # 애플리케이션 설정
│       └── db/                   # 데이터베이스 스크립트
├── test/                         # 테스트 코드
└── proto/                        # gRPC 프로토콜 정의
```

## 🔧 개발 환경 설정

### IntelliJ IDEA
1. Kotlin 플러그인 설치
2. Spring Boot 플러그인 설치
3. 프로젝트 임포트 후 Gradle 동기화

### VS Code
1. Kotlin, Spring Boot Extension Pack 설치
2. 프로젝트 폴더 열기

## 📝 문서

자세한 문서는 [docs/](docs/) 디렉토리를 참조하세요:

- [시스템 아키텍처](docs/architecture/system-overview.md)
- [API 엔드포인트](docs/api/endpoints.md)
- [개발 가이드](docs/development/setup.md)
- [배포 가이드](docs/deployment/infrastructure.md)

## 🤝 기여하기

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다. 자세한 내용은 [LICENSE](LICENSE) 파일을 참조하세요.

## 📞 지원

- **이슈**: [GitHub Issues](https://github.com/YourUsername/rest_server/issues)
- **문서**: [docs/](docs/) 디렉토리
- **이메일**: dev@example.com

---

**마지막 업데이트**: 2024-12-19  
**버전**: v1.1.0
