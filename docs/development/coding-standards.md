# 코딩 표준 및 가이드라인

## 📋 개요

이 문서는 REST Server 프로젝트의 코딩 표준과 가이드라인을 정의합니다. 일관된 코드 품질을 유지하고, 팀 협업을 원활하게 하며, 유지보수성을 높이기 위한 규칙들을 포함합니다.

## 🎯 코딩 원칙

### 1. SOLID 원칙 준수
- **S**: 단일 책임 원칙 (Single Responsibility Principle)
- **O**: 개방-폐쇄 원칙 (Open-Closed Principle)
- **L**: 리스코프 치환 원칙 (Liskov Substitution Principle)
- **I**: 인터페이스 분리 원칙 (Interface Segregation Principle)
- **D**: 의존성 역전 원칙 (Dependency Inversion Principle)

### 2. DRY 원칙 (Don't Repeat Yourself)
- 중복 코드 제거
- 공통 로직 추상화
- 유틸리티 클래스 활용

### 3. KISS 원칙 (Keep It Simple, Stupid)
- 복잡한 로직 단순화
- 이해하기 쉬운 코드 작성
- 과도한 추상화 지양

### 4. YAGNI 원칙 (You Aren't Gonna Need It)
- 현재 필요하지 않은 기능 구현 지양
- 미래 요구사항에 대한 과도한 설계 방지

## 🏗️ 아키텍처 가이드라인

### 1. 계층 분리
```kotlin
// ✅ 올바른 구조
@RestController
class LottoController(private val lottoUseCase: LottoUseCase) {
    @GetMapping("/lotto/{id}")
    suspend fun getLotto(@PathVariable id: Long): ResponseEntity<ApiResponse<LottoDto>> {
        val lotto = lottoUseCase.getLottoById(id)
        return ResponseEntity.ok(ApiResponse(
            statusCode = HttpStatus.OK.value(),
            message = "로또 정보가 성공적으로 조회되었습니다.",
            data = lotto
        ))
    }
}

// ❌ 잘못된 구조 - 컨트롤러에서 직접 리포지토리 접근
@RestController
class LottoController(private val lottoRepository: LottoRepository) {
    @GetMapping("/lotto/{id}")
    suspend fun getLotto(@PathVariable id: Long): ResponseEntity<ApiResponse<LottoDto>> {
        val lotto = lottoRepository.findById(id) // 직접 접근 금지
        // ...
    }
}
```

### 2. 의존성 주입
```kotlin
// ✅ 생성자 주입 (권장)
@Service
class LottoServiceImpl(
    private val lottoRepository: LottoRepository,
    private val lottoValidator: LottoValidator
) : LottoService {
    // ...
}

// ❌ 필드 주입 (지양)
@Service
class LottoServiceImpl : LottoService {
    @Autowired
    private lateinit var lottoRepository: LottoRepository
    // ...
}
```

## 📝 Kotlin 코딩 스타일

### 1. 네이밍 컨벤션

#### 클래스 및 인터페이스
```kotlin
// ✅ 올바른 네이밍
class LottoService
interface LottoRepository
abstract class BaseService
data class LottoDto

// ❌ 잘못된 네이밍
class lottoService
interface lotto_repository
class Base
data class lotto
```

#### 함수 및 변수
```kotlin
// ✅ 올바른 네이밍
fun getLottoById(id: Long): LottoEntity?
fun createLotto(lotto: LottoDto): LottoEntity
val lottoList: List<LottoEntity>
var currentUser: User?

// ❌ 잘못된 네이밍
fun GetLottoById(id: Long): LottoEntity?
fun create_lotto(lotto: LottoDto): LottoEntity
val lotto_list: List<LottoEntity>
var CurrentUser: User?
```

#### 상수
```kotlin
// ✅ 올바른 네이밍
const val MAX_LOTTO_NUMBER = 45
const val MIN_LOTTO_NUMBER = 1
const val API_VERSION = "v1"

// ❌ 잘못된 네이밍
const val maxLottoNumber = 45
const val min_lotto_number = 1
const val apiVersion = "v1"
```

### 2. 함수 작성 가이드라인

#### 함수 시그니처
```kotlin
// ✅ 명확한 함수 시그니처
suspend fun findLottoByDrwNo(drwNo: Int): LottoEntity?
suspend fun createLotto(lotto: LottoDto): LottoEntity
fun validateLottoNumbers(numbers: List<Int>): Boolean

// ❌ 모호한 함수 시그니처
fun get(data: Any): Any?
fun process(input: String): String
fun validate(data: Any): Boolean
```

#### 함수 본문
```kotlin
// ✅ 간결하고 명확한 함수
suspend fun findLottoByDrwNo(drwNo: Int): LottoEntity? {
    require(drwNo > 0) { "회차 번호는 1 이상이어야 합니다." }
    
    return try {
        lottoRepository.findByDrwNo(drwNo)
    } catch (e: Exception) {
        logger.error("로또 정보 조회 실패: drwNo=$drwNo", e)
        null
    }
}

// ❌ 복잡하고 이해하기 어려운 함수
suspend fun findLottoByDrwNo(drwNo: Int): LottoEntity? {
    if (drwNo <= 0) return null
    try {
        val result = lottoRepository.findByDrwNo(drwNo)
        if (result != null) {
            return result
        } else {
            return null
        }
    } catch (e: Exception) {
        logger.error("Error: $e")
        return null
    }
}
```

### 3. 데이터 클래스 활용
```kotlin
// ✅ 데이터 클래스 활용
data class LottoDto(
    val id: Long? = null,
    val drwNo: Int,
    val drwNoDate: LocalDate,
    val drwtNo1: Int,
    val drwtNo2: Int,
    val drwtNo3: Int,
    val drwtNo4: Int,
    val drwtNo5: Int,
    val drwtNo6: Int,
    val bnusNo: Int,
    val firstPrzwnerCo: Int,
    val firstAccumamnt: Long,
    val firstWinamnt: Long,
    val totSellamnt: Long,
    val returnValue: String = ""
) {
    init {
        require(drwNo > 0) { "회차 번호는 1 이상이어야 합니다." }
        require(drwtNo1 in 1..45) { "당첨번호는 1-45 범위여야 합니다." }
        // ... 기타 검증
    }
}

// ❌ 일반 클래스 사용
class LottoDto {
    var id: Long? = null
    var drwNo: Int = 0
    var drwNoDate: LocalDate? = null
    // ... getter/setter 메서드들
}
```

## 🔒 보안 가이드라인

### 1. 입력 검증
```kotlin
// ✅ 입력 검증
@PostMapping("/lotto")
suspend fun createLotto(@Valid @RequestBody dto: LottoDto): ResponseEntity<ApiResponse<LottoDto>> {
    // Bean Validation으로 기본 검증
    // 추가 비즈니스 로직 검증
    validateLottoBusinessRules(dto)
    
    val entity = lottoService.createLotto(dto.toEntity())
    return ResponseEntity.ok(ApiResponse(
        statusCode = HttpStatus.CREATED.value(),
        message = "로또 정보가 성공적으로 생성되었습니다.",
        data = entity.toDto()
    ))
}

// ❌ 검증 없음
@PostMapping("/lotto")
suspend fun createLotto(@RequestBody dto: LottoDto): ResponseEntity<ApiResponse<LottoDto>> {
    val entity = lottoService.createLotto(dto.toEntity()) // 검증 없음
    // ...
}
```

### 2. SQL 인젝션 방지
```kotlin
// ✅ Exposed ORM 사용 (안전함)
override suspend fun findByDrwNo(drwNo: Int): LottoEntity? {
    return transaction {
        LottoEntity.find { LottoTable.drwNo eq drwNo }.firstOrNull()
    }
}

// ❌ 문자열 연결 (위험함)
override suspend fun findByDrwNo(drwNo: Int): LottoEntity? {
    return transaction {
        exec("SELECT * FROM lotto WHERE drwNo = $drwNo") { // SQL 인젝션 위험
            // ...
        }
    }
}
```

### 3. 민감한 정보 보호
```kotlin
// ✅ 로그에서 민감한 정보 제거
logger.info("사용자 로그인 시도: userId=${user.id}, timestamp=${LocalDateTime.now()}")
// 비밀번호나 개인정보는 로그에 기록하지 않음

// ❌ 민감한 정보 로깅
logger.info("사용자 로그인: userId=${user.id}, password=${user.password}") // 위험!
```

## 🧪 테스트 가이드라인

### 1. 테스트 구조
```kotlin
// ✅ 테스트 구조
@SpringBootTest
@Transactional
class LottoServiceTest {
    
    @Autowired
    private lateinit var lottoService: LottoService
    
    @MockK
    private lateinit var lottoRepository: LottoRepository
    
    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
    }
    
    @Test
    fun `로또 정보 생성 시 유효한 데이터로 성공해야 한다`() {
        // Given
        val dto = createValidLottoDto()
        every { lottoRepository.save(any()) } returns createMockLottoEntity()
        
        // When
        val result = lottoService.createLotto(dto.toEntity())
        
        // Then
        assertThat(result).isNotNull()
        assertThat(result.drwNo).isEqualTo(dto.drwNo)
        verify { lottoRepository.save(any()) }
    }
    
    @Test
    fun `로또 정보 생성 시 유효하지 않은 데이터로 실패해야 한다`() {
        // Given
        val invalidDto = createInvalidLottoDto()
        
        // When & Then
        assertThatThrownBy { lottoService.createLotto(invalidDto.toEntity()) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("회차 번호는 1 이상이어야 합니다.")
    }
}
```

### 2. 테스트 명명 규칙
```kotlin
// ✅ 테스트 메서드 명명
@Test
fun `사용자가 존재할 때 로그인이 성공해야 한다`()

@Test
fun `존재하지 않는 사용자로 로그인 시도 시 예외가 발생해야 한다`()

@Test
fun `빈 문자열로 검색 시 빈 리스트를 반환해야 한다`()

// ❌ 테스트 메서드 명명
@Test
fun test1()

@Test
fun testLogin()

@Test
fun testSearch()
```

## 📚 문서화 가이드라인

### 1. 코드 주석
```kotlin
/**
 * 로또 정보를 생성합니다.
 * 
 * @param lotto 생성할 로또 정보
 * @return 생성된 로또 엔티티
 * @throws IllegalArgumentException 로또 데이터가 유효하지 않은 경우
 * @throws DataAccessException 데이터베이스 접근 실패 시
 */
suspend fun createLotto(lotto: LottoEntity): LottoEntity {
    // 비즈니스 로직 검증
    validateLottoData(lotto)
    
    // 데이터베이스 저장
    return lottoRepository.save(lotto)
}

// 간단한 주석
// 회차 번호 중복 검사
val existingLotto = lottoRepository.findByDrwNo(lotto.drwNo)
if (existingLotto != null) {
    throw IllegalArgumentException("이미 존재하는 회차 번호입니다: ${lotto.drwNo}")
}
```

### 2. README 문서
```markdown
# LottoService

로또 정보를 관리하는 서비스 클래스입니다.

## 주요 기능

- 로또 정보 생성
- 로또 정보 조회
- 로또 정보 수정
- 로또 정보 삭제

## 사용 예시

```kotlin
@Service
class LottoServiceImpl(
    private val lottoRepository: LottoRepository
) : LottoService {
    
    override suspend fun findById(id: Long): LottoEntity? {
        return lottoRepository.findById(id)
    }
}
```

## 의존성

- `LottoRepository`: 로또 데이터 접근
- `LottoValidator`: 로또 데이터 검증

## 테스트

```bash
./gradlew test --tests LottoServiceTest
```
```

## 🔧 도구 및 설정

### 1. IDE 설정
```kotlin
// IntelliJ IDEA 설정
// File → Settings → Editor → Code Style → Kotlin

// 들여쓰기: 4 spaces
// 최대 줄 길이: 120
// 연속 줄 들여쓰기: 8 spaces
// 가져오기 정렬: 알파벳 순
```

### 2. Gradle 설정
```kotlin
// build.gradle.kts
tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "21"
        // 추가 컴파일러 옵션
        freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
        freeCompilerArgs += "-Xopt-in=kotlin.ExperimentalCoroutinesApi"
    }
}

// 코드 품질 도구
plugins {
    id("org.jlleitschuh.gradle.ktlint") version "11.6.1"
    id("io.gitlab.arturbosch.detekt") version "1.23.4"
}

ktlint {
    android.set(false)
    verbose.set(true)
    filter {
        exclude { element -> element.file.path.contains("build/") }
    }
}

detekt {
    config = files("$projectDir/config/detekt/detekt.yml")
    reports {
        html.enabled = true
        xml.enabled = true
        txt.enabled = false
        sarif.enabled = true
    }
}
```

### 3. ktlint 설정
```yaml
# .editorconfig
[*.{kt,kts}]
# ktlint 규칙
ktlint_code_style = official
ktlint_ignore_back_ticked_identifier = true
ktlint_standard_trailing-comma-on-call-site = disabled
ktlint_standard_trailing-comma-on-declaration-site = disabled
ktlint_standard_no-wildcard-imports = disabled
ktlint_standard_filename = disabled
```

## 🚨 코드 리뷰 체크리스트

### 1. 기능적 측면
- [ ] 요구사항을 정확히 구현했는가?
- [ ] 예외 처리가 적절한가?
- [ ] 입력 검증이 충분한가?
- [ ] 보안 취약점이 없는가?

### 2. 코드 품질
- [ ] SOLID 원칙을 준수하는가?
- [ ] 중복 코드가 없는가?
- [ ] 함수가 단일 책임을 가지는가?
- [ ] 변수명과 함수명이 명확한가?

### 3. 성능 및 확장성
- [ ] 불필요한 객체 생성이 없는가?
- [ ] 적절한 데이터 구조를 사용하는가?
- [ ] 비동기 처리가 적절한가?
- [ ] 확장 가능한 설계인가?

### 4. 테스트
- [ ] 단위 테스트가 충분한가?
- [ ] 테스트 케이스가 명확한가?
- [ ] 모킹이 적절한가?
- [ ] 테스트 커버리지가 충분한가?

## 📈 코드 품질 개선

### 1. 정적 분석 도구
```bash
# ktlint 실행
./gradlew ktlintCheck

# ktlint 자동 수정
./gradlew ktlintFormat

# Detekt 실행
./gradlew detekt

# 테스트 커버리지 확인
./gradlew test jacocoTestReport
```

### 2. 코드 품질 메트릭
```kotlin
// 코드 복잡도 측정
// Cyclomatic Complexity < 10
// Cognitive Complexity < 15
// Lines of Code < 50 (함수당)

// 테스트 커버리지 목표
// Line Coverage > 80%
// Branch Coverage > 70%
// Function Coverage > 90%
```

---

**문서 버전**: v1.0.0  
**마지막 업데이트**: 2024-12-19  
**작성자**: Development Team
