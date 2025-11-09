# OAuth2 소셜 로그인 설정 가이드

이 문서는 Google, Naver, Kakao OAuth2 소셜 로그인 설정 방법을 안내합니다.

## 📋 목차

- [Google OAuth2 설정](#google-oauth2-설정)
- [Naver OAuth2 설정](#naver-oauth2-설정)
- [Kakao OAuth2 설정](#kakao-oauth2-설정)
- [환경 변수 설정](#환경-변수-설정)
- [테스트 방법](#테스트-방법)

---

## Google OAuth2 설정

### 1. Google Cloud Console 접속

1. [Google Cloud Console](https://console.cloud.google.com/) 접속
2. 새 프로젝트 생성 또는 기존 프로젝트 선택

### 2. OAuth 동의 화면 구성

1. 좌측 메뉴에서 `API 및 서비스` > `OAuth 동의 화면` 선택
2. User Type 선택:
   - **External** (테스트용)
   - **Internal** (조직 내부용, Google Workspace 필요)
3. 앱 정보 입력:
   - 앱 이름
   - 사용자 지원 이메일
   - 개발자 연락처 정보
4. 범위 추가:
   - `email`
   - `profile`

### 3. OAuth 2.0 클라이언트 ID 만들기

1. `사용자 인증 정보` > `사용자 인증 정보 만들기` > `OAuth 클라이언트 ID` 선택
2. 애플리케이션 유형: **웹 애플리케이션**
3. 승인된 리디렉션 URI 추가:
   ```
   http://localhost:8080/login/oauth2/code/google
   https://your-domain.com/login/oauth2/code/google
   ```
4. 생성 후 **클라이언트 ID**와 **클라이언트 보안 비밀** 복사

### 4. 환경 변수 설정

```bash
export GOOGLE_CLIENT_ID="your-google-client-id"
export GOOGLE_CLIENT_SECRET="your-google-client-secret"
```

---

## Naver OAuth2 설정

### 1. Naver Developers 접속

1. [Naver Developers](https://developers.naver.com/apps/#/register) 접속
2. 네이버 계정으로 로그인

### 2. 애플리케이션 등록

1. `Application` > `애플리케이션 등록` 클릭
2. 애플리케이션 정보 입력:
   - 애플리케이션 이름
   - 사용 API: **네이버 로그인**
3. 로그인 오픈 API 서비스 환경 설정:
   - 서비스 URL: `http://localhost:8080`
   - Callback URL: `http://localhost:8080/login/oauth2/code/naver`

### 3. 제공 정보 선택

- 회원이름
- 이메일 주소

### 4. 클라이언트 정보 확인

등록 후 **Client ID**와 **Client Secret** 확인

### 5. 환경 변수 설정

```bash
export NAVER_CLIENT_ID="your-naver-client-id"
export NAVER_CLIENT_SECRET="your-naver-client-secret"
```

---

## Kakao OAuth2 설정

### 1. Kakao Developers 접속

1. [Kakao Developers](https://developers.kakao.com/) 접속
2. 카카오 계정으로 로그인

### 2. 애플리케이션 추가

1. `내 애플리케이션` > `애플리케이션 추가하기` 클릭
2. 앱 이름, 사업자명 입력
3. 앱 생성 완료

### 3. 플랫폼 설정

1. 생성한 앱 선택 > `플랫폼` 메뉴
2. `Web 플랫폼 등록` 클릭
3. 사이트 도메인: `http://localhost:8080`

### 4. 카카오 로그인 활성화

1. `제품 설정` > `카카오 로그인` 메뉴
2. `카카오 로그인 활성화` ON
3. `Redirect URI` 등록:
   ```
   http://localhost:8080/login/oauth2/code/kakao
   ```

### 5. 동의항목 설정

1. `제품 설정` > `카카오 로그인` > `동의항목` 메뉴
2. 필요한 항목 설정:
   - 닉네임 (필수)
   - 이메일 (필수)

### 6. 클라이언트 정보 확인

1. `앱 설정` > `앱 키` 메뉴
2. **REST API 키** 확인 (Client ID로 사용)
3. `제품 설정` > `카카오 로그인` > `보안` 메뉴
4. **Client Secret** 생성 및 확인

### 7. 환경 변수 설정

```bash
export KAKAO_CLIENT_ID="your-kakao-rest-api-key"
export KAKAO_CLIENT_SECRET="your-kakao-client-secret"
```

---

## 환경 변수 설정

### Docker Compose 사용 시

`.env` 파일 생성:

```env
# Google OAuth2
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret

# Naver OAuth2
NAVER_CLIENT_ID=your-naver-client-id
NAVER_CLIENT_SECRET=your-naver-client-secret

# Kakao OAuth2
KAKAO_CLIENT_ID=your-kakao-rest-api-key
KAKAO_CLIENT_SECRET=your-kakao-client-secret
```

그리고 Docker Compose 실행:

```bash
docker-compose up -d
```

### 로컬 실행 시

환경 변수 설정 후 애플리케이션 실행:

```bash
# 환경 변수 설정
export GOOGLE_CLIENT_ID="your-google-client-id"
export GOOGLE_CLIENT_SECRET="your-google-client-secret"
export NAVER_CLIENT_ID="your-naver-client-id"
export NAVER_CLIENT_SECRET="your-naver-client-secret"
export KAKAO_CLIENT_ID="your-kakao-rest-api-key"
export KAKAO_CLIENT_SECRET="your-kakao-client-secret"

# 애플리케이션 실행
./gradlew bootRun
```

---

## 테스트 방법

### 1. OAuth2 로그인 URL 접속

각 제공자별 로그인 URL:

```
# Google
http://localhost:8080/oauth2/authorization/google

# Naver
http://localhost:8080/oauth2/authorization/naver

# Kakao
http://localhost:8080/oauth2/authorization/kakao
```

### 2. 로그인 플로우

1. 위 URL로 접속
2. 각 제공자의 로그인 페이지로 리디렉션
3. 로그인 후 권한 동의
4. 리디렉션 URI로 돌아오면서 JWT 토큰 발급

### 3. 프론트엔드 연동 예시

```html
<!-- 소셜 로그인 버튼 -->
<a href="http://localhost:8080/oauth2/authorization/google">
  <img src="/images/google-login.png" alt="Google 로그인" />
</a>

<a href="http://localhost:8080/oauth2/authorization/naver">
  <img src="/images/naver-login.png" alt="Naver 로그인" />
</a>

<a href="http://localhost:8080/oauth2/authorization/kakao">
  <img src="/images/kakao-login.png" alt="Kakao 로그인" />
</a>
```

### 4. 성공 후 처리

OAuth2 로그인 성공 시 다음과 같은 형식으로 리디렉션됩니다:

```
http://your-frontend.com?token=<access_token>&refreshToken=<refresh_token>
```

프론트엔드에서 토큰을 추출하여 로컬 스토리지에 저장:

```javascript
// URL에서 토큰 추출
const urlParams = new URLSearchParams(window.location.search);
const accessToken = urlParams.get('token');
const refreshToken = urlParams.get('refreshToken');

// 로컬 스토리지에 저장
if (accessToken) {
  localStorage.setItem('accessToken', accessToken);
  localStorage.setItem('refreshToken', refreshToken);

  // 홈 페이지로 리디렉션
  window.location.href = '/';
}
```

### 5. API 요청 시 토큰 사용

```javascript
// API 요청 예시
fetch('http://localhost:8080/api/v1/users/me', {
  headers: {
    'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
  }
})
.then(response => response.json())
.then(data => console.log(data));
```

---

## 🔒 보안 고려사항

1. **Client Secret 보안**
   - Client Secret은 절대 프론트엔드에 노출하지 않기
   - 환경 변수나 시크릿 관리 서비스 사용

2. **HTTPS 사용**
   - 프로덕션에서는 반드시 HTTPS 사용
   - HTTP는 개발 환경에서만 사용

3. **Redirect URI 화이트리스트**
   - 각 제공자 콘솔에서 허용된 Redirect URI만 등록
   - 와일드카드 사용 자제

4. **토큰 저장**
   - Access Token은 짧은 만료 시간 설정 (1시간 ~ 24시간)
   - Refresh Token으로 갱신 로직 구현
   - XSS 공격 방지를 위해 HttpOnly 쿠키 사용 권장

---

## 📚 추가 자료

- [Google OAuth2 문서](https://developers.google.com/identity/protocols/oauth2)
- [Naver 로그인 API 문서](https://developers.naver.com/docs/login/api/)
- [Kakao 로그인 문서](https://developers.kakao.com/docs/latest/ko/kakaologin/common)
- [Spring Security OAuth2 문서](https://docs.spring.io/spring-security/reference/servlet/oauth2/index.html)

---

## 🐛 문제 해결

### 401 Unauthorized

- Client ID/Secret이 정확한지 확인
- 환경 변수가 올바르게 설정되었는지 확인

### Redirect URI mismatch

- OAuth2 제공자 콘솔에 등록된 Redirect URI와 정확히 일치하는지 확인
- 프로토콜(http/https), 포트, 경로 모두 확인

### Email not found

- OAuth2 제공자에서 email 스코프 권한이 허용되었는지 확인
- 사용자가 이메일 제공에 동의했는지 확인

---

**Last Updated**: 2025-11-06
**Version**: v2.0.0
