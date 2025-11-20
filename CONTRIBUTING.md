# Contributing to REST Server

감사합니다! 이 프로젝트에 기여해주셔서 감사합니다. 이 가이드는 기여 과정을 쉽게 만들기 위한 것입니다.

## 📋 목차

- [개발 환경 설정](#개발-환경-설정)
- [브랜치 전략](#브랜치-전략)
- [커밋 컨벤션](#커밋-컨벤션)
- [Pull Request 프로세스](#pull-request-프로세스)
- [코드 스타일](#코드-스타일)
- [테스트 가이드](#테스트-가이드)
- [CI/CD 파이프라인](#cicd-파이프라인)

## 🛠️ 개발 환경 설정

### 필수 요구사항

- **JDK 21** (Temurin 권장)
- **Kotlin 2.2.20+**
- **Gradle 8.x**
- **Docker & Docker Compose**
- **Git**

### 로컬 설정

```bash
# 1. Fork 및 Clone
git clone https://github.com/YOUR_USERNAME/rest_server.git
cd rest_server

# 2. Upstream 추가
git remote add upstream https://github.com/YouSangSon/rest_server.git

# 3. 의존성 설치 및 빌드
./gradlew build

# 4. 인프라 서비스 시작
docker-compose up -d postgres mongodb redis

# 5. 애플리케이션 실행
./gradlew bootRun
```

### IDE 설정

**IntelliJ IDEA (권장):**
1. File → Open → 프로젝트 디렉토리 선택
2. Gradle 프로젝트로 import
3. Kotlin 플러그인 활성화
4. Code Style: Settings → Editor → Code Style → Import Scheme → `kotlin-style.xml`

## 🌿 브랜치 전략

프로젝트는 Git Flow 브랜치 전략을 사용합니다:

### 주요 브랜치

- **`main`**: 프로덕션 릴리즈 브랜치
  - 항상 안정적이고 배포 가능한 상태 유지
  - Direct push 금지 (PR만 허용)

- **`develop`**: 개발 통합 브랜치
  - 다음 릴리즈를 위한 feature 통합
  - 스테이징 환경에 자동 배포

### 보조 브랜치

- **`feature/*`**: 새로운 기능 개발
  ```bash
  git checkout -b feature/add-user-authentication develop
  ```

- **`fix/*`**: 버그 수정
  ```bash
  git checkout -b fix/login-validation-bug develop
  ```

- **`hotfix/*`**: 프로덕션 긴급 수정
  ```bash
  git checkout -b hotfix/critical-security-fix main
  ```

- **`release/*`**: 릴리즈 준비
  ```bash
  git checkout -b release/v1.2.0 develop
  ```

### 브랜치 네이밍 규칙

```
feature/[issue-number]-brief-description
fix/[issue-number]-brief-description
hotfix/critical-bug-description
release/v[major].[minor].[patch]
```

**예시:**
- `feature/123-add-sns-api`
- `fix/456-null-pointer-exception`
- `hotfix/security-vulnerability`
- `release/v1.2.0`

## 📝 커밋 컨벤션

프로젝트는 [Conventional Commits](https://www.conventionalcommits.org/) 스펙을 따릅니다.

### 커밋 메시지 형식

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Type

- **feat**: 새로운 기능
- **fix**: 버그 수정
- **docs**: 문서 변경
- **style**: 코드 스타일 변경 (formatting, 세미콜론 등)
- **refactor**: 리팩토링
- **perf**: 성능 개선
- **test**: 테스트 추가/수정
- **build**: 빌드 시스템 변경 (Gradle, dependencies)
- **ci**: CI/CD 설정 변경
- **chore**: 기타 변경사항
- **revert**: 커밋 되돌리기

### 예시

```bash
# Good ✅
feat(sns): add investment portfolio sharing feature

Implemented portfolio sharing with privacy controls.
Users can now share their portfolios publicly or with followers only.

Closes #123

# Good ✅
fix(auth): resolve JWT token expiration issue

Fixed bug where refresh tokens were expiring prematurely.
Updated token validation logic to check expiration correctly.

# Good ✅
docs: update API documentation for SNS endpoints

# Bad ❌
update files
fixed bug
WIP
```

### Breaking Changes

Breaking change가 있는 경우 footer에 `BREAKING CHANGE:` 추가:

```
feat(api)!: change user authentication endpoint

BREAKING CHANGE: The /api/v1/login endpoint has been moved to /api/v1/auth/login.
Update your API clients accordingly.
```

## 🔄 Pull Request 프로세스

### PR 생성 전 체크리스트

- [ ] 최신 `develop` 브랜치와 동기화
- [ ] 모든 테스트 통과 (`./gradlew test`)
- [ ] 빌드 성공 (`./gradlew build`)
- [ ] 코드 스타일 준수 (`./gradlew detekt`)
- [ ] 문서 업데이트 (필요시)
- [ ] 커밋 메시지가 컨벤션 준수

### PR 생성

```bash
# 1. 최신 변경사항 pull
git checkout develop
git pull upstream develop

# 2. feature 브랜치 생성
git checkout -b feature/my-new-feature develop

# 3. 변경사항 commit
git add .
git commit -m "feat: add amazing new feature"

# 4. push
git push origin feature/my-new-feature

# 5. GitHub에서 PR 생성
# https://github.com/YouSangSon/rest_server/pulls
```

### PR 제목 형식

PR 제목은 커밋 컨벤션을 따라야 합니다:

```
feat: add user authentication
fix: resolve database connection issue
docs: update deployment guide
```

### PR 템플릿

```markdown
## 📝 변경사항

<!-- 무엇을 변경했는지 설명 -->

## 🎯 목적

<!-- 왜 이 변경이 필요한지 설명 -->
Closes #[issue-number]

## 🧪 테스트

<!-- 어떻게 테스트했는지 설명 -->
- [ ] Unit tests 추가
- [ ] Integration tests 추가
- [ ] 수동 테스트 완료

## 📸 스크린샷 (UI 변경인 경우)

<!-- 스크린샷 추가 -->

## ✅ 체크리스트

- [ ] 코드가 컨벤션을 따름
- [ ] 테스트 추가/업데이트
- [ ] 문서 업데이트
- [ ] Breaking changes 없음 (있다면 설명)
```

### 자동화된 검사

PR 생성 시 자동으로 실행되는 검사:

1. **Build & Test** - 빌드 및 테스트 통과 확인
2. **Code Quality** - Detekt, SonarCloud 분석
3. **Security Scan** - 보안 취약점 검사
4. **Test Coverage** - 코드 커버리지 확인 (>50%)
5. **Dependency Review** - 의존성 검토
6. **Documentation Check** - 문서 업데이트 확인

### 리뷰 프로세스

1. **자동 검사 통과**: 모든 CI 검사가 통과해야 함
2. **코드 리뷰**: 최소 1명의 승인 필요
3. **변경사항 반영**: 리뷰 피드백 반영
4. **최종 승인**: Maintainer의 최종 승인
5. **Merge**: Squash and merge 방식 사용

## 🎨 코드 스타일

### Kotlin 코드 스타일

프로젝트는 [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)을 따릅니다.

**주요 규칙:**

```kotlin
// Good ✅
class UserService(
    private val userRepository: UserRepository,
    private val emailService: EmailService
) {
    fun createUser(email: String, username: String): User {
        // Validate input
        require(email.isNotBlank()) { "Email cannot be blank" }
        require(username.isNotBlank()) { "Username cannot be blank" }

        // Create user
        val user = User(email = email, username = username)
        return userRepository.save(user)
    }
}

// Bad ❌
class userService(val userRepository: UserRepository, val emailService: EmailService) {
    fun CreateUser(email:String,username:String):User {
        val user = User(email, username)
        return userRepository.save(user)
    }
}
```

### 네이밍 컨벤션

- **클래스**: PascalCase (`UserService`, `PostController`)
- **함수/변수**: camelCase (`createUser`, `findById`)
- **상수**: UPPER_SNAKE_CASE (`MAX_RETRY_COUNT`)
- **패키지**: lowercase (`yousang.rest.domain.user`)

### 코드 검사

```bash
# Detekt 실행
./gradlew detekt

# 자동 포맷팅
./gradlew ktlintFormat
```

## 🧪 테스트 가이드

### 테스트 전략

프로젝트는 테스트 피라미드를 따릅니다:

```
        /\
       /E2E\          (적음)
      /------\
     /  통합   \       (중간)
    /----------\
   /   단위 테스트  \   (많음)
  /----------------\
```

### Unit Tests

**위치:** `src/test/kotlin/`

```kotlin
@Test
fun `should create user with valid data`() {
    // Given
    val email = "test@example.com"
    val username = "testuser"

    // When
    val user = userService.createUser(email, username)

    // Then
    assertThat(user.email).isEqualTo(email)
    assertThat(user.username).isEqualTo(username)
}

@Test
fun `should throw exception when email is blank`() {
    // When & Then
    assertThrows<IllegalArgumentException> {
        userService.createUser("", "testuser")
    }
}
```

### Integration Tests

**위치:** `src/integrationTest/kotlin/`

```kotlin
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `should register new user`() {
        mockMvc.perform(
            post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"email": "test@example.com", "password": "password123"}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.email").value("test@example.com"))
    }
}
```

### 테스트 실행

```bash
# 모든 테스트
./gradlew test

# 특정 테스트만
./gradlew test --tests UserServiceTest

# 통합 테스트
./gradlew integrationTest

# 테스트 커버리지
./gradlew test jacocoTestReport
open build/reports/jacoco/test/html/index.html
```

### 테스트 커버리지 목표

- **전체**: 최소 50%
- **새로운 코드**: 최소 60%
- **비즈니스 로직**: 최소 80%

## 🚀 CI/CD 파이프라인

### GitHub Actions Workflows

프로젝트는 5개의 주요 workflow를 사용합니다:

#### 1. CI Workflow (`.github/workflows/ci.yml`)

**트리거:**
- Push to `main`, `develop`, `feature/*`, `claude/*`
- Pull requests to `main`, `develop`

**실행 내용:**
- Build & Test (PostgreSQL, MongoDB, Redis 포함)
- Docker 이미지 빌드 (multi-platform)
- 코드 품질 분석 (Detekt, SonarCloud)
- 보안 스캔 (Trivy, OWASP)

**소요 시간:** ~15분

#### 2. CD Workflow (`.github/workflows/cd.yml`)

**트리거:**
- Push to `main` (프로덕션 배포)
- Tags `v*.*.*` (릴리즈)
- Manual workflow dispatch

**배포 전략:**
- Blue-Green deployment
- Docker Swarm rolling update
- 자동 롤백 (실패 시)

**소요 시간:** ~10분

#### 3. PR Validation (`.github/workflows/pr.yml`)

**트리거:**
- Pull request opened/updated

**검사 항목:**
- PR 제목 컨벤션 검사
- 코드 품질 분석
- 테스트 커버리지 (>50%)
- 보안 스캔 (secrets, vulnerabilities)
- 문서 업데이트 확인

**결과:**
- 자동 코멘트 (커버리지, 품질 리포트)
- 모든 검사 통과 시 "Ready for review" 코멘트

#### 4. CodeQL (`.github/workflows/codeql.yml`)

**트리거:**
- Push to `main`, `develop`
- 주간 스케줄 (월요일 6 AM)

**분석:**
- 보안 취약점 자동 탐지
- GitHub Security 탭에 결과 업로드

#### 5. Release (`.github/workflows/release.yml`)

**트리거:**
- Git tags `v*.*.*`

**자동 생성:**
- GitHub Release with changelog
- Docker 이미지 (versioned tags)
- JAR artifacts with checksums

### 로컬에서 CI 실행

```bash
# Act로 로컬 CI 시뮬레이션 (Docker 필요)
# Install: https://github.com/nektos/act
act -j build-and-test

# 특정 workflow 실행
act -W .github/workflows/ci.yml
```

### CI/CD 모니터링

```bash
# GitHub CLI로 workflow 상태 확인
gh run list --workflow=ci.yml

# 특정 run 로그 보기
gh run view <run-id> --log

# 실패한 job 재실행
gh run rerun <run-id> --failed
```

## 🐛 버그 리포트

버그를 발견하셨나요? [GitHub Issues](https://github.com/YouSangSon/rest_server/issues)에 리포트해주세요.

**템플릿:**
```markdown
## 🐛 버그 설명
<!-- 버그에 대한 명확하고 간결한 설명 -->

## 📝 재현 방법
1. '...' 페이지로 이동
2. '...' 클릭
3. 스크롤 다운
4. 오류 발생

## 💡 예상 동작
<!-- 어떻게 동작해야 하는지 설명 -->

## 📸 스크린샷
<!-- 있다면 추가 -->

## 🖥️ 환경
- OS: [e.g., Ubuntu 22.04]
- Java Version: [e.g., 21]
- Docker Version: [e.g., 24.0.5]
```

## 💡 기능 제안

새로운 기능을 제안하고 싶으신가요? [GitHub Discussions](https://github.com/YouSangSon/rest_server/discussions)에서 논의해주세요.

## 📞 연락처

질문이나 도움이 필요하신가요?

- **GitHub Issues**: 버그 리포트, 기능 제안
- **GitHub Discussions**: 일반 질문, 아이디어 공유
- **Email**: [your-email@example.com]

## 📜 라이선스

이 프로젝트에 기여함으로써, 당신의 기여가 MIT 라이선스 하에 배포되는 것에 동의합니다.

---

**감사합니다! 🙏**

모든 기여자들의 노력이 이 프로젝트를 더 나은 것으로 만듭니다.
