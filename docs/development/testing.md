# 테스트 가이드

## 📋 개요

이 문서는 REST Server 프로젝트의 테스트 전략과 가이드라인을 제공합니다. 단위 테스트부터 통합 테스트, E2E 테스트까지 다양한 테스트 레벨을 다루며, 테스트 작성 방법과 실행 방법을 상세히 설명합니다.

## 🎯 테스트 전략

### 1. 테스트 피라미드

```
        /\
       /  \     E2E Tests (End-to-End)
      /____\        (5-10%)
     /      \
    /        \   Integration Tests
   /__________\      (15-20%)
  /            \
 /              \  Unit Tests
/________________\    (70-80%)
```

### 2. 테스트 목표

- **품질 보증**: 버그 조기 발견 및 수정
- **리팩토링 안전성**: 코드 변경 시 기존 기능 보장
- **문서화**: 코드 동작 방식 이해
- **설계 개선**: 테스트하기 어려운 코드 구조 개선

### 3. 테스트 원칙

- **FIRST 원칙**:
  - **F**ast: 빠른 실행
  - **I**solated: 독립적 실행
  - **R**epeatable: 반복 가능
  - **S**elf-validating: 자동 검증
  - **T**imely: 적시에 작성

## 🧪 테스트 종류

### 1. 단위 테스트 (Unit Tests)

#### 특징
- 개별 함수/메서드 단위로 테스트
- 외부 의존성 모킹
- 빠른 실행 속도
- 높은 격리성

#### 테스트 대상
```kotlin
// 서비스 클래스
@Service
class LottoServiceImpl(
    private val lottoRepository: LottoRepository,
    private val lottoValidator: LottoValidator
) : LottoService {
    
    override suspend fun createLotto(lotto: LottoEntity): LottoEntity {
        lottoValidator.validate(lotto)
        return lottoRepository.save(lotto)
    }
}

// 테스트 코드
@ExtendWith(MockKExtension::class)
class LottoServiceImplTest {
    
    @MockK
    private lateinit var lottoRepository: LottoRepository
    
    @MockK
    private lateinit var lottoValidator: LottoValidator
    
    private lateinit var lottoService: LottoServiceImpl
    
    @BeforeEach
    fun setUp() {
        lottoService = LottoServiceImpl(lottoRepository, lottoValidator)
    }
    
    @Test
    fun `로또 생성 시 유효한 데이터로 성공해야 한다`() = runTest {
        // Given
        val lotto = createValidLottoEntity()
        every { lottoValidator.validate(lotto) } just Runs
        every { lottoRepository.save(lotto) } returns lotto
        
        // When
        val result = lottoService.createLotto(lotto)
        
        // Then
        assertThat(result).isEqualTo(lotto)
        verify { lottoValidator.validate(lotto) }
        verify { lottoRepository.save(lotto) }
    }
    
    @Test
    fun `로또 생성 시 유효하지 않은 데이터로 실패해야 한다`() = runTest {
        // Given
        val invalidLotto = createInvalidLottoEntity()
        every { lottoValidator.validate(invalidLotto) } throws IllegalArgumentException("유효하지 않은 데이터")
        
        // When & Then
        assertThatThrownBy { lottoService.createLotto(invalidLotto) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("유효하지 않은 데이터")
        
        verify(exactly = 0) { lottoRepository.save(any()) }
    }
}
```

### 2. 통합 테스트 (Integration Tests)

#### 특징
- 여러 컴포넌트 간 상호작용 테스트
- 실제 데이터베이스 사용
- Spring 컨텍스트 로드
- 중간 속도 실행

#### 테스트 대상
```kotlin
@SpringBootTest
@Transactional
@TestPropertySource(properties = [
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver"
])
class LottoIntegrationTest {
    
    @Autowired
    private lateinit var lottoService: LottoService
    
    @Autowired
    private lateinit var lottoRepository: LottoRepository
    
    @Test
    fun `로또 정보 생성 및 조회 통합 테스트`() = runTest {
        // Given
        val lottoDto = createValidLottoDto()
        
        // When
        val savedLotto = lottoService.createLotto(lottoDto.toEntity())
        val retrievedLotto = lottoService.findById(savedLotto.id!!)
        
        // Then
        assertThat(retrievedLotto).isNotNull()
        assertThat(retrievedLotto!!.drwNo).isEqualTo(lottoDto.drwNo)
        assertThat(retrievedLotto.drwtNo1).isEqualTo(lottoDto.drwtNo1)
    }
    
    @Test
    fun `로또 정보 수정 통합 테스트`() = runTest {
        // Given
        val originalLotto = lottoService.createLotto(createValidLottoDto().toEntity())
        val updateDto = createUpdateLottoDto(originalLotto.id!!)
        
        // When
        val updatedLotto = lottoService.updateLotto(originalLotto.id!!, updateDto.toEntity())
        
        // Then
        assertThat(updatedLotto.drwtNo1).isEqualTo(updateDto.drwtNo1)
        assertThat(updatedLotto.drwtNo2).isEqualTo(updateDto.drwtNo2)
    }
}
```

### 3. API 테스트 (API Tests)

#### 특징
- HTTP 엔드포인트 테스트
- 실제 웹 서버 실행
- JSON 요청/응답 검증
- 비즈니스 로직 검증

#### 테스트 대상
```kotlin
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class LottoApiTest {
    
    @Autowired
    private lateinit var testRestTemplate: TestRestTemplate
    
    @LocalServerPort
    private var port: Int = 0
    
    @Test
    fun `GET /api/v1/lotto/{id} - 존재하는 로또 정보 조회`() {
        // Given
        val lottoId = 1L
        
        // When
        val response = testRestTemplate.getForEntity(
            "http://localhost:$port/api/v1/lotto/$lottoId",
            ApiResponse::class.java
        )
        
        // Then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isNotNull()
        assertThat(response.body!!.statusCode).isEqualTo(200)
        assertThat(response.body!!.data).isNotNull()
    }
    
    @Test
    fun `POST /api/v1/lotto - 새로운 로또 정보 생성`() {
        // Given
        val lottoDto = createValidLottoDto()
        
        // When
        val response = testRestTemplate.postForEntity(
            "http://localhost:$port/api/v1/lotto",
            lottoDto,
            ApiResponse::class.java
        )
        
        // Then
        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(response.body).isNotNull()
        assertThat(response.body!!.statusCode).isEqualTo(201)
        assertThat(response.body!!.message).contains("성공적으로 생성되었습니다")
    }
    
    @Test
    fun `POST /api/v1/lotto - 유효하지 않은 데이터로 생성 실패`() {
        // Given
        val invalidLottoDto = createInvalidLottoDto()
        
        // When
        val response = testRestTemplate.postForEntity(
            "http://localhost:$port/api/v1/lotto",
            invalidLottoDto,
            ApiResponse::class.java
        )
        
        // Then
        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(response.body).isNotNull()
        assertThat(response.body!!.statusCode).isEqualTo(400)
    }
}
```

### 4. E2E 테스트 (End-to-End Tests)

#### 특징
- 전체 시스템 흐름 테스트
- 실제 브라우저/클라이언트 사용
- 느린 실행 속도
- 높은 신뢰성

#### 테스트 대상
```kotlin
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class LottoE2ETest {
    
    @Autowired
    private lateinit var webTestClient: WebTestClient
    
    @Test
    fun `로또 정보 전체 생명주기 E2E 테스트`() {
        // 1. 로또 정보 생성
        val lottoDto = createValidLottoDto()
        val createResponse = webTestClient.post()
            .uri("/api/v1/lotto")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(lottoDto)
            .exchange()
            .expectStatus().isCreated
            .expectBody(ApiResponse::class.java)
            .returnResult()
            .responseBody
        
        assertThat(createResponse).isNotNull()
        assertThat(createResponse!!.statusCode).isEqualTo(201)
        
        // 2. 생성된 로또 정보 조회
        val lottoId = (createResponse.data as Map<*, *>)["id"] as Long
        val getResponse = webTestClient.get()
            .uri("/api/v1/lotto/$lottoId")
            .exchange()
            .expectStatus().isOk
            .expectBody(ApiResponse::class.java)
            .returnResult()
            .responseBody
        
        assertThat(getResponse).isNotNull()
        assertThat(getResponse!!.statusCode).isEqualTo(200)
        
        // 3. 로또 정보 수정
        val updateDto = createUpdateLottoDto(lottoId)
        val updateResponse = webTestClient.put()
            .uri("/api/v1/lotto/$lottoId")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(updateDto)
            .exchange()
            .expectStatus().isOk
            .expectBody(ApiResponse::class.java)
            .returnResult()
            .responseBody
        
        assertThat(updateResponse).isNotNull()
        assertThat(updateResponse!!.statusCode).isEqualTo(200)
        
        // 4. 수정된 로또 정보 확인
        val finalResponse = webTestClient.get()
            .uri("/api/v1/lotto/$lottoId")
            .exchange()
            .expectStatus().isOk
            .expectBody(ApiResponse::class.java)
            .returnResult()
            .responseBody
        
        assertThat(finalResponse).isNotNull()
        val finalData = finalResponse!!.data as Map<*, *>
        assertThat(finalData["drwtNo1"]).isEqualTo(updateDto.drwtNo1)
        
        // 5. 로또 정보 삭제
        webTestClient.delete()
            .uri("/api/v1/lotto/$lottoId")
            .exchange()
            .expectStatus().isOk
        
        // 6. 삭제된 로또 정보 확인
        webTestClient.get()
            .uri("/api/v1/lotto/$lottoId")
            .exchange()
            .expectStatus().isNotFound
    }
}
```

## 🛠️ 테스트 도구 및 설정

### 1. 테스트 의존성

```kotlin
// build.gradle.kts
dependencies {
    // 테스트 프레임워크
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    
    // Spring Boot 테스트
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    
    // MockK (Kotlin 전용 모킹)
    testImplementation("io.mockk:mockk")
    testImplementation("com.ninja-squad:springmockk")
    
    // AssertJ (가독성 높은 assertion)
    testImplementation("org.assertj:assertj-core")
    
    // Testcontainers (통합 테스트용)
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    
    // H2 데이터베이스 (테스트용)
    testRuntimeOnly("com.h2database:h2")
    
    // Kotlin Coroutines 테스트
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
}
```

### 2. 테스트 설정

#### application-test.yml
```yaml
# src/test/resources/application-test.yml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.H2Dialect
  
  test:
    database:
      replace: any

logging:
  level:
    yousang.rest: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE

server:
  port: 0  # 랜덤 포트 사용
```

#### 테스트 설정 클래스
```kotlin
@TestConfiguration
class TestConfig {
    
    @Bean
    fun testRestTemplate(): TestRestTemplate {
        return TestRestTemplate()
    }
    
    @Bean
    fun webTestClient(webApplicationContext: WebApplicationContext): WebTestClient {
        return WebTestClient.bindToApplicationContext(webApplicationContext).build()
    }
}
```

### 3. 테스트 유틸리티

#### 테스트 데이터 생성
```kotlin
object TestDataFactory {
    
    fun createValidLottoDto(
        id: Long? = null,
        drwNo: Int = 1001,
        drwNoDate: LocalDate = LocalDate.now()
    ): LottoDto {
        return LottoDto(
            id = id,
            drwNo = drwNo,
            drwNoDate = drwNoDate,
            drwtNo1 = 1,
            drwtNo2 = 2,
            drwtNo3 = 3,
            drwtNo4 = 4,
            drwtNo5 = 5,
            drwtNo6 = 6,
            bnusNo = 7,
            firstPrzwnerCo = 10,
            firstAccumamnt = 1000000000L,
            firstWinamnt = 100000000L,
            totSellamnt = 10000000000L
        )
    }
    
    fun createInvalidLottoDto(): LottoDto {
        return createValidLottoDto(drwNo = -1)
    }
    
    fun createUpdateLottoDto(id: Long): LottoDto {
        return createValidLottoDto(
            id = id,
            drwtNo1 = 10,
            drwtNo2 = 20
        )
    }
    
    fun createValidLottoEntity(): LottoEntity {
        return LottoEntity.new {
            drwNo = 1001
            drwNoDate = LocalDate.now()
            drwtNo1 = 1
            drwtNo2 = 2
            drwtNo3 = 3
            drwtNo4 = 4
            drwtNo5 = 5
            drwtNo6 = 6
            bnusNo = 7
            firstPrzwnerCo = 10
            firstAccumamnt = 1000000000L
            firstWinamnt = 100000000L
            totSellamnt = 10000000000L
        }
    }
}
```

#### 테스트 헬퍼
```kotlin
abstract class BaseTest {
    
    protected fun <T> runTest(block: suspend () -> T): T {
        return runBlocking { block() }
    }
    
    protected fun createRandomString(length: Int = 10): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..length)
            .map { chars.random() }
            .joinToString("")
    }
    
    protected fun createRandomInt(min: Int = 1, max: Int = 100): Int {
        return (min..max).random()
    }
    
    protected fun createRandomLong(min: Long = 1L, max: Long = 1000000L): Long {
        return (min..max).random()
    }
}
```

## 🚀 테스트 실행

### 1. 명령어 실행

```bash
# 전체 테스트 실행
./gradlew test

# 특정 테스트 클래스 실행
./gradlew test --tests LottoServiceTest

# 특정 테스트 메서드 실행
./gradlew test --tests LottoServiceTest.createLotto

# 통합 테스트 실행
./gradlew integrationTest

# 테스트 커버리지 확인
./gradlew test jacocoTestReport

# 테스트 결과 상세 출력
./gradlew test --info

# 테스트 실패 시 중단하지 않고 계속 실행
./gradlew test --continue
```

### 2. IDE에서 실행

#### IntelliJ IDEA
1. **테스트 실행**: 테스트 메서드 옆의 실행 버튼 클릭
2. **테스트 디버깅**: 디버그 버튼으로 브레이크포인트 설정
3. **테스트 결과**: Run 창에서 결과 확인
4. **테스트 커버리지**: Run → Run with Coverage

#### VS Code
1. **테스트 탐색기**: Java Test Explorer 확장 사용
2. **테스트 실행**: 테스트 메서드 옆의 실행 버튼 클릭
3. **테스트 디버깅**: 디버그 구성에서 테스트 설정

### 3. CI/CD 파이프라인

```yaml
# .github/workflows/test.yml
name: Test

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v4
    
    - name: Set up JDK 21
      uses: actions/setup-java@v4
      with:
        java-version: '21'
        distribution: 'temurin'
    
    - name: Run tests
      run: ./gradlew test
    
    - name: Run integration tests
      run: ./gradlew integrationTest
    
    - name: Generate test coverage report
      run: ./gradlew jacocoTestReport
    
    - name: Upload coverage to Codecov
      uses: codecov/codecov-action@v3
      with:
        file: ./build/reports/jacoco/test/jacocoTestReport.xml
```

## 📊 테스트 커버리지

### 1. JaCoCo 설정

```kotlin
// build.gradle.kts
plugins {
    id("jacoco")
}

jacoco {
    toolVersion = "0.8.11"
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "0.80".toBigDecimal()
            }
        }
        rule {
            limit {
                counter = "BRANCH"
                value = "COVEREDRATIO"
                minimum = "0.70".toBigDecimal()
            }
        }
    }
}
```

### 2. 커버리지 목표

```kotlin
// 테스트 커버리지 목표
// Line Coverage: 80% 이상
// Branch Coverage: 70% 이상
// Function Coverage: 90% 이상
// Class Coverage: 85% 이상
```

## 🔍 테스트 디버깅

### 1. 로그 설정

```yaml
# application-test.yml
logging:
  level:
    yousang.rest: DEBUG
    org.springframework.test: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
    org.springframework.transaction: DEBUG
```

### 2. 테스트 실행 옵션

```bash
# 상세 로그와 함께 테스트 실행
./gradlew test --info --debug

# 특정 테스트만 실행하여 디버깅
./gradlew test --tests LottoServiceTest.createLotto --info

# 테스트 실패 시 스택 트레이스 출력
./gradlew test --stacktrace
```

### 3. IDE 디버깅

```kotlin
// 테스트에 브레이크포인트 설정
@Test
fun `로또 생성 테스트`() {
    val lotto = createValidLottoEntity() // 브레이크포인트 설정
    
    // 디버거로 변수 값 확인
    val result = lottoService.createLotto(lotto)
    
    assertThat(result).isNotNull()
}
```

## 📈 테스트 성능 최적화

### 1. 테스트 병렬 실행

```kotlin
// build.gradle.kts
tasks.test {
    useJUnitPlatform()
    maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).takeIf { it > 0 } ?: 1
}
```

### 2. 테스트 그룹화

```kotlin
// 빠른 테스트
@Test
@Tag("fast")
fun `빠른 단위 테스트`() {
    // ...
}

// 느린 테스트
@Test
@Tag("slow")
fun `느린 통합 테스트`() {
    // ...
}

// 실행 시 그룹 선택
./gradlew test --tests "*" --include-tag "fast"
```

### 3. 테스트 데이터 최적화

```kotlin
// 테스트 데이터 공유
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LottoServiceTest {
    
    private lateinit var sharedLottoData: List<LottoDto>
    
    @BeforeAll
    fun setUpSharedData() {
        sharedLottoData = (1..100).map { createValidLottoDto(drwNo = it) }
    }
    
    @Test
    fun `대량 데이터 처리 테스트`() {
        val results = sharedLottoData.map { lottoService.createLotto(it.toEntity()) }
        assertThat(results).hasSize(100)
    }
}
```

---

**문서 버전**: v1.0.0  
**마지막 업데이트**: 2024-12-19  
**작성자**: Development Team
