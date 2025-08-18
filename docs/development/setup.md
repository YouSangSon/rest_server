# 개발 환경 설정 가이드

## 📋 개요

이 문서는 REST Server 프로젝트의 개발 환경을 설정하는 방법을 단계별로 설명합니다. 로컬 개발 환경에서 프로젝트를 실행하고 개발할 수 있도록 필요한 모든 설정을 포함합니다.

## 🎯 사전 요구사항

### 1. 필수 소프트웨어

- **Java Development Kit (JDK)**: 21 이상
- **Gradle**: 8.0 이상 (Gradle Wrapper 사용 시 자동 설치)
- **Docker**: 20.0 이상 (선택사항, 로컬 데이터베이스용)
- **Git**: 2.30 이상

### 2. 권장 개발 도구

- **IDE**: IntelliJ IDEA, VS Code, Eclipse
- **API 테스트**: Postman, Insomnia, 또는 HTTPie
- **데이터베이스 클라이언트**: DBeaver, pgAdmin

## 🚀 빠른 시작

### 1. 프로젝트 클론

```bash
# 프로젝트 저장소 클론
git clone <repository-url>
cd rest_server

# 프로젝트 구조 확인
ls -la
```

### 2. 환경 설정 파일 준비

```bash
# 환경 설정 디렉토리 생성
mkdir -p env

# 예시 환경 파일 복사
cp src/main/resources/application-dev.yml.example src/main/resources/application-dev.yml
cp src/main/resources/application-prod.yml.example src/main/resources/application-prod.yml

# 개발 환경 설정 파일 생성
cat > env/dev.env << EOF
# 개발 환경 설정
SPRING_PROFILES_ACTIVE=dev
APP_PORT=8080

# 데이터베이스 설정
DB_URL=jdbc:postgresql://localhost:5432/rest_dev
DB_USERNAME=postgres
DB_PASSWORD=postgres
DB_POOL_SIZE=5

# 로깅 설정
LOG_LEVEL=INFO
APP_LOG_LEVEL=DEBUG
JPA_SHOW_SQL=true
HIBERNATE_FORMAT_SQL=true

# CORS 설정
CORS_ALLOWED_ORIGINS=*
CORS_ALLOWED_METHODS=GET,POST,PUT,DELETE,OPTIONS
CORS_ALLOWED_HEADERS=*

# JVM 설정
JVM_XMS=512m
JVM_XMX=1g
JVM_MAX_RAM_PERCENTAGE=75
EOF
```

### 3. 데이터베이스 설정

#### PostgreSQL 로컬 설치 (권장)

```bash
# Docker를 사용한 PostgreSQL 실행
docker run --name postgres-rest -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=rest_dev -p 5432:5432 -d postgres:15

# 데이터베이스 연결 확인
docker exec -it postgres-rest psql -U postgres -d rest_dev
```

#### 또는 기존 PostgreSQL 사용

```bash
# PostgreSQL에 데이터베이스 생성
psql -U postgres -h localhost
CREATE DATABASE rest_dev;
CREATE USER rest_user WITH PASSWORD 'rest_password';
GRANT ALL PRIVILEGES ON DATABASE rest_dev TO rest_user;
\q
```

### 4. 애플리케이션 실행

```bash
# 의존성 다운로드 및 빌드
./gradlew build

# 개발 환경으로 실행
./gradlew bootRun

# 또는 특정 프로파일로 실행
SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun
```

## 🔧 상세 설정 가이드

### 1. Java 환경 설정

#### JDK 21 설치 (macOS)

```bash
# Homebrew를 사용한 설치
brew install openjdk@21

# 환경 변수 설정
echo 'export JAVA_HOME=/opt/homebrew/opt/openjdk@21' >> ~/.zshrc
echo 'export PATH=$JAVA_HOME/bin:$PATH' >> ~/.zshrc
source ~/.zshrc

# Java 버전 확인
java -version
javac -version
```

#### JDK 21 설치 (Ubuntu/Debian)

```bash
# 패키지 업데이트
sudo apt update

# OpenJDK 21 설치
sudo apt install openjdk-21-jdk

# 환경 변수 설정
echo 'export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64' >> ~/.bashrc
echo 'export PATH=$JAVA_HOME/bin:$PATH' >> ~/.bashrc
source ~/.bashrc

# Java 버전 확인
java -version
javac -version
```

#### JDK 21 설치 (Windows)

1. [Oracle JDK 21](https://www.oracle.com/java/technologies/downloads/#java21) 또는 [OpenJDK 21](https://adoptium.net/) 다운로드
2. 설치 프로그램 실행
3. 환경 변수 설정:
   - `JAVA_HOME`: JDK 설치 경로 (예: `C:\Program Files\Java\jdk-21`)
   - `PATH`: `%JAVA_HOME%\bin` 추가

### 2. Gradle 설정

#### Gradle Wrapper 사용 (권장)

```bash
# Gradle Wrapper 권한 설정
chmod +x gradlew
chmod +x gradlew.bat

# Gradle 버전 확인
./gradlew --version

# 프로젝트 빌드
./gradlew build

# 테스트 실행
./gradlew test

# 애플리케이션 실행
./gradlew bootRun
```

#### 로컬 Gradle 설치

```bash
# Gradle 설치 (macOS)
brew install gradle

# Gradle 설치 (Ubuntu/Debian)
sudo apt install gradle

# Gradle 버전 확인
gradle --version
```

### 3. IDE 설정

#### IntelliJ IDEA 설정

1. **프로젝트 열기**
   - `File` → `Open` → 프로젝트 루트 디렉토리 선택

2. **JDK 설정**
   - `File` → `Project Structure` → `Project`
   - `Project SDK`: JDK 21 선택
   - `Project language level`: 21 선택

3. **Gradle 설정**
   - `File` → `Settings` → `Build, Execution, Deployment` → `Build Tools` → `Gradle`
   - `Gradle JVM`: JDK 21 선택
   - `Build and run using`: Gradle 선택

4. **Kotlin 플러그인**
   - `File` → `Settings` → `Plugins`
   - `Kotlin` 플러그인 설치 및 활성화

#### VS Code 설정

1. **확장 프로그램 설치**
   - `Extension Pack for Java`
   - `Kotlin`
   - `Spring Boot Extension Pack`

2. **Java 설정**
   - `Ctrl+Shift+P` → `Java: Configure Java Runtime`
   - JDK 21 경로 설정

3. **Gradle 설정**
   - `Ctrl+Shift+P` → `Gradle: Refresh Gradle Project`

### 4. 데이터베이스 설정

#### application-dev.yml 설정

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/rest_dev
    username: postgres
    password: postgres
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 5
      minimum-idle: 2
      idle-timeout: 30000
      connection-timeout: 10000
      max-lifetime: 2000000
  
  jpa:
    hibernate:
      ddl-auto: create-drop  # 개발 환경에서는 create-drop 사용
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.PostgreSQLDialect

logging:
  level:
    root: INFO
    yousang.rest: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

### 5. 환경 변수 설정

#### 개발 환경 변수

```bash
# ~/.zshrc 또는 ~/.bashrc에 추가
export SPRING_PROFILES_ACTIVE=dev
export DB_URL=jdbc:postgresql://localhost:5432/rest_dev
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
export LOG_LEVEL=DEBUG
export APP_LOG_LEVEL=DEBUG
export JPA_SHOW_SQL=true
export HIBERNATE_FORMAT_SQL=true
export CORS_ALLOWED_ORIGINS=*
```

## 🧪 테스트 환경 설정

### 1. 단위 테스트 실행

```bash
# 전체 테스트 실행
./gradlew test

# 특정 테스트 클래스 실행
./gradlew test --tests LottoServiceTest

# 테스트 결과 확인
./gradlew test --info
```

### 2. 통합 테스트 실행

```bash
# 통합 테스트 실행 (데이터베이스 필요)
./gradlew integrationTest

# 테스트 커버리지 확인
./gradlew test jacocoTestReport
```

### 3. API 테스트

#### HTTPie를 사용한 테스트

```bash
# HTTPie 설치
pip install httpie

# 로또 정보 조회
http GET localhost:8080/api/v1/lotto

# 로또 정보 생성
http POST localhost:8080/api/v1/lotto \
  drwNo:=1001 \
  drwNoDate=2024-01-01 \
  drwtNo1:=1 \
  drwtNo2:=2 \
  drwtNo3:=3 \
  drwtNo4:=4 \
  drwtNo5:=5 \
  drwtNo6:=6 \
  bnusNo:=7
```

#### Postman을 사용한 테스트

1. **Postman 설치**: [postman.com](https://www.postman.com/downloads/)
2. **컬렉션 생성**: REST Server API 컬렉션 생성
3. **환경 변수 설정**:
   - `baseUrl`: `http://localhost:8080/api/v1`
   - `port`: `8080`

## 🔍 디버깅 설정

### 1. 로그 레벨 설정

```yaml
# application-dev.yml
logging:
  level:
    root: INFO
    yousang.rest: DEBUG
    org.springframework.web: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
    org.springframework.security: DEBUG
```

### 2. IDE 디버깅

#### IntelliJ IDEA 디버깅

1. **디버그 구성 생성**
   - `Run` → `Edit Configurations`
   - `+` → `Spring Boot`
   - `Main class`: `yousang.rest.RestApplicationKt`
   - `VM options`: `-Dspring.profiles.active=dev`

2. **브레이크포인트 설정**
   - 코드 라인 번호 옆 클릭하여 브레이크포인트 설정
   - `Debug` 버튼으로 디버그 모드 실행

#### VS Code 디버깅

1. **launch.json 생성**
   ```json
   {
     "version": "0.2.0",
     "configurations": [
       {
         "type": "java",
         "name": "Debug Spring Boot",
         "request": "launch",
         "mainClass": "yousang.rest.RestApplicationKt",
         "projectName": "rest_server",
         "args": "--spring.profiles.active=dev"
       }
     ]
   }
   ```

### 3. 원격 디버깅

```bash
# JVM 옵션에 디버그 포트 추가
./gradlew bootRun --args='--spring.profiles.active=dev -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005'
```

## 🚨 문제 해결

### 1. 일반적인 문제들

#### 포트 충돌
```bash
# 포트 사용 확인
lsof -i :8080

# 프로세스 종료
kill -9 <PID>
```

#### 데이터베이스 연결 실패
```bash
# PostgreSQL 상태 확인
sudo systemctl status postgresql

# 서비스 시작
sudo systemctl start postgresql

# 연결 테스트
psql -h localhost -U postgres -d rest_dev
```

#### 메모리 부족
```bash
# JVM 메모리 설정 확인
./gradlew bootRun --args='-Xms256m -Xmx512m'

# 환경 변수로 설정
export GRADLE_OPTS="-Xmx512m"
```

### 2. 로그 확인

```bash
# 애플리케이션 로그 확인
tail -f logs/application.log

# Spring Boot 로그 확인
./gradlew bootRun --info

# 특정 로그 레벨로 실행
./gradlew bootRun --args='--logging.level.yousang.rest=DEBUG'
```

### 3. 의존성 문제

```bash
# Gradle 캐시 정리
./gradlew clean build --refresh-dependencies

# 의존성 트리 확인
./gradlew dependencies

# 특정 의존성 확인
./gradlew dependencies --configuration compileClasspath
```

## 📚 추가 리소스

### 1. 공식 문서
- [Spring Boot Reference](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Kotlin Documentation](https://kotlinlang.org/docs/home.html)
- [Exposed ORM Documentation](https://github.com/JetBrains/Exposed)

### 2. 유용한 도구
- [Spring Initializr](https://start.spring.io/)
- [Kotlin Playground](https://play.kotlinlang.org/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)

### 3. 커뮤니티
- [Spring Community](https://spring.io/community)
- [Kotlin Community](https://kotlinlang.org/community/)
- [Stack Overflow](https://stackoverflow.com/questions/tagged/spring-boot+kotlin)

---

**문서 버전**: v1.0.0  
**마지막 업데이트**: 2024-12-19  
**작성자**: Development Team
