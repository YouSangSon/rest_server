# 로또 API 참조 가이드

## 현재 프로젝트를 기반으로 한 로또 API 구현 가이드

새 프로젝트에서 로또 API를 구현할 때 참고할 수 있는 현재 프로젝트의 API 구조, 데이터 모델, 비즈니스 로직을 정리했습니다.

## 1. API 구조 및 엔드포인트

### 1.1 기본 API 구조
```kotlin
@RestController
@RequestMapping("/api/v1")
class ApiControllerV1 {

    @GetMapping("/health")
    suspend fun healthCheck(): ApiResponse {
        return ApiResponse(
            statusCode = 200,
            message = "Service is healthy",
            data = mapOf("status" to "UP")
        )
    }
}
```

### 1.2 로또 API 엔드포인트
```kotlin
@RestController
@RequestMapping("/lotto")
@Tag(name = "로또 API", description = "로또 번호 조회 및 관리 API")
class LottoController(private val lottoUseCase: LottoUseCase) {

    @GetMapping("/numbers")
    @Operation(summary = "로또 번호 조회", description = "지정된 범위 내의 로또 번호를 조회")
    suspend fun getLottoRange(
        @RequestParam firstDrwNo: Int,
        @RequestParam lastDrwNo: Int
    ): ApiResponse {
        val lottoData = lottoUseCase.getLotto(firstDrwNo, lastDrwNo)
        return ApiResponse(
            statusCode = 200,
            message = "Retrieved lotto numbers from $firstDrwNo to $lastDrwNo",
            data = lottoData
        )
    }

    @PutMapping("/numbers")
    @Operation(summary = "로또 번호 저장", description = "로또 번호를 저장합니다.")
    suspend fun saveLottoNumbers(): ApiResponse {
        lottoUseCase.putLotto()
        return ApiResponse(
            statusCode = 200,
            message = "Successfully saved lotto numbers",
            data = null
        )
    }

    @GetMapping("/numbers/{drwNo}")
    @Operation(summary = "특정 로또 번호 조회", description = "특정 회차의 로또 번호를 조회")
    suspend fun getLottoByDrawNumber(@PathVariable drwNo: Int): ApiResponse {
        val lottoData = lottoUseCase.getLotto(drwNo, drwNo)
        return ApiResponse(
            statusCode = 200,
            message = "Retrieved lotto number for draw $drwNo",
            data = lottoData.firstOrNull()
        )
    }
}
```

### 1.3 연금복권 API 엔드포인트
```kotlin
@RestController
@RequestMapping("/annuity-lotto")
@Tag(name = "연금복권 API", description = "연금복권 720+ 번호 조회 및 관리 API")
class AnnuityLottoController(private val annuityLottoUseCase: AnnuityLottoUseCase) {

    @GetMapping("/numbers")
    @Operation(summary = "연금복권 번호 조회", description = "지정된 범위 내의 연금복권 번호를 조회")
    suspend fun getAnnuityLottoRange(
        @RequestParam firstDrwNo: Int,
        @RequestParam lastDrwNo: Int
    ): ApiResponse {
        val annuityLottoData = annuityLottoUseCase.getAnnuityLotto(firstDrwNo, lastDrwNo)
        return ApiResponse(
            statusCode = 200,
            message = "Retrieved annuity lotto numbers from $firstDrwNo to $lastDrwNo",
            data = annuityLottoData
        )
    }

    @PutMapping("/numbers")
    @Operation(summary = "연금복권 번호 저장", description = "연금복권 720+ 번호를 저장합니다.")
    suspend fun saveAnnuityLottoNumbers(): ApiResponse {
        annuityLottoUseCase.putAnnuityLotto()
        return ApiResponse(
            statusCode = 200,
            message = "Successfully saved annuity lotto numbers",
            data = null
        )
    }
}
```

## 2. API 응답 구조

### 2.1 표준 응답 형식
```kotlin
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApiResponse(
    @JsonProperty("status_code")
    val statusCode: Int,

    @JsonProperty("message")
    val message: String,

    @JsonProperty("data")
    val data: Any? = null
) {
    constructor(status: HttpStatus, message: String, data: Any? = null) :
        this(status.value(), message, data)
}
```

### 2.2 성공 응답 예시
```json
{
  "status_code": 200,
  "message": "Retrieved lotto numbers from 1 to 10",
  "data": [
    {
      "drwNo": 1,
      "drwNoDate": "2002-12-07",
      "drwtNo1": 10,
      "drwtNo2": 23,
      "drwtNo3": 29,
      "drwtNo4": 33,
      "drwtNo5": 37,
      "drwtNo6": 40,
      "bnusNo": 16,
      "firstPrzwnerCo": 1,
      "firstAccumamnt": 4079293760,
      "firstWinamnt": 4079293760,
      "totSellamnt": 4079293760
    }
  ]
}
```

### 2.3 에러 응답 예시
```json
{
  "status_code": 500,
  "message": "Internal server error occurred while processing request",
  "data": null
}
```

## 3. 데이터 모델 (DTO)

### 3.1 로또 DTO
```kotlin
@JsonIgnoreProperties(ignoreUnknown = true)
data class LottoDto(
    @JsonProperty("id")
    val id: Long? = null,

    @JsonProperty("drwNo")
    val drwNo: Int,

    @JsonProperty("drwNoDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val drwNoDate: LocalDate,

    @JsonProperty("drwtNo1")
    val drwtNo1: Int,

    @JsonProperty("drwtNo2")
    val drwtNo2: Int,

    @JsonProperty("drwtNo3")
    val drwtNo3: Int,

    @JsonProperty("drwtNo4")
    val drwtNo4: Int,

    @JsonProperty("drwtNo5")
    val drwtNo5: Int,

    @JsonProperty("drwtNo6")
    val drwtNo6: Int,

    @JsonProperty("bnusNo")
    val bnusNo: Int,

    @JsonProperty("firstPrzwnerCo")
    val firstPrzwnerCo: Int,

    @JsonProperty("firstAccumamnt")
    val firstAccumamnt: Long,

    @JsonProperty("firstWinamnt")
    val firstWinamnt: Long,

    @JsonProperty("totSellamnt")
    val totSellamnt: Long,

    @JsonProperty("returnValue")
    val returnValue: String = ""
)
```

### 3.2 연금복권 DTO
```kotlin
@JsonIgnoreProperties(ignoreUnknown = true)
data class AnnuityLottoDto(
    @JsonProperty("id")
    val id: Long? = null,

    @JsonProperty("drwNo")
    val drwNo: Int,

    @JsonProperty("drwNoDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val drwNoDate: LocalDate,

    @JsonProperty("groupNumber")
    val groupNumber: Int,

    @JsonProperty("drwtNo1")
    val drwtNo1: Int,

    @JsonProperty("drwtNo2")
    val drwtNo2: Int,

    @JsonProperty("drwtNo3")
    val drwtNo3: Int,

    @JsonProperty("drwtNo4")
    val drwtNo4: Int,

    @JsonProperty("drwtNo5")
    val drwtNo5: Int,

    @JsonProperty("drwtNo6")
    val drwtNo6: Int,

    @JsonProperty("bonusNo1")
    val bonusNo1: Int,

    @JsonProperty("bonusNo2")
    val bonusNo2: Int,

    @JsonProperty("bonusNo3")
    val bonusNo3: Int,

    @JsonProperty("bonusNo4")
    val bonusNo4: Int,

    @JsonProperty("bonusNo5")
    val bonusNo5: Int,

    @JsonProperty("bonusNo6")
    val bonusNo6: Int
)
```

### 3.3 추가 유틸리티 DTO
```kotlin
// 통계 정보 DTO
data class LottoStatisticsDto(
    @JsonProperty("total_draws")
    val totalDraws: Int,

    @JsonProperty("total_sales_amount")
    val totalSalesAmount: Long,

    @JsonProperty("total_prize_amount")
    val totalPrizeAmount: Long,

    @JsonProperty("average_prize_per_draw")
    val averagePrizePerDraw: Long,

    @JsonProperty("most_frequent_numbers")
    val mostFrequentNumbers: List<Int>,

    @JsonProperty("least_frequent_numbers")
    val leastFrequentNumbers: List<Int>
)

// 당첨 분석 DTO
data class WinningAnalysisDto(
    @JsonProperty("user_numbers")
    val userNumbers: Set<Int>,

    @JsonProperty("winning_numbers")
    val winningNumbers: LottoDto,

    @JsonProperty("prize_division")
    val prizeDivision: String,

    @JsonProperty("match_count")
    val matchCount: Int,

    @JsonProperty("has_bonus")
    val hasBonus: Boolean,

    @JsonProperty("estimated_prize")
    val estimatedPrize: Long?
)
```

## 4. 서비스 레이어 구조

### 4.1 UseCase 인터페이스
```kotlin
interface LottoUseCase {
    suspend fun getLotto(firstDrwNo: Int, lastDrwNo: Int): List<LottoDto>
    suspend fun putLotto()
    suspend fun getLotto(drwNo: Int): LottoDto?
}
```

### 4.2 UseCase 구현체
```kotlin
@Component
class LottoUseCase(private val lottoService: LottoService) {

    suspend fun getLotto(firstDrwNo: Int, lastDrwNo: Int): List<LottoDto> {
        return lottoService.getLotto(firstDrwNo, lastDrwNo)
    }

    suspend fun putLotto() {
        lottoService.putLotto()
    }

    suspend fun getLotto(drwNo: Int): LottoDto? {
        return lottoService.getLotto(drwNo, drwNo).firstOrNull()
    }
}
```

## 5. 추가 API 엔드포인트 제안

### 5.1 통계 API
```kotlin
@RestController
@RequestMapping("/lotto/statistics")
class LottoStatisticsController(private val statisticsService: LottoStatisticsService) {

    @GetMapping("/summary")
    @Operation(summary = "로또 통계 요약", description = "전체 로또 통계 정보를 조회")
    suspend fun getStatisticsSummary(): ApiResponse {
        val statistics = statisticsService.getSummary()
        return ApiResponse(
            statusCode = 200,
            message = "Retrieved lotto statistics summary",
            data = statistics
        )
    }

    @GetMapping("/frequent-numbers")
    @Operation(summary = "빈번한 번호 조회", description = "가장 많이 나온 번호들을 조회")
    suspend fun getFrequentNumbers(
        @RequestParam limit: Int = 10
    ): ApiResponse {
        val frequentNumbers = statisticsService.getMostFrequentNumbers(limit)
        return ApiResponse(
            statusCode = 200,
            message = "Retrieved most frequent numbers",
            data = frequentNumbers
        )
    }

    @GetMapping("/prize-distribution")
    @Operation(summary = "상금 분포 조회", description = "등수별 상금 분포를 조회")
    suspend fun getPrizeDistribution(): ApiResponse {
        val distribution = statisticsService.getPrizeDistribution()
        return ApiResponse(
            statusCode = 200,
            message = "Retrieved prize distribution",
            data = distribution
        )
    }
}
```

### 5.2 당첨 분석 API
```kotlin
@RestController
@RequestMapping("/lotto/analysis")
class LottoAnalysisController(private val analysisService: LottoAnalysisService) {

    @PostMapping("/check-winning")
    @Operation(summary = "당첨 확인", description = "사용자의 번호가 당첨되었는지 확인")
    suspend fun checkWinning(
        @RequestBody request: WinningCheckRequest
    ): ApiResponse {
        val analysis = analysisService.analyzeWinning(request.userNumbers, request.drawNumber)
        return ApiResponse(
            statusCode = 200,
            message = "Analyzed winning numbers",
            data = analysis
        )
    }

    @GetMapping("/patterns")
    @Operation(summary = "패턴 분석", description = "로또 번호 패턴을 분석")
    suspend fun analyzePatterns(
        @RequestParam drawCount: Int = 50
    ): ApiResponse {
        val patterns = analysisService.analyzePatterns(drawCount)
        return ApiResponse(
            statusCode = 200,
            message = "Analyzed lotto patterns",
            data = patterns
        )
    }
}

data class WinningCheckRequest(
    @JsonProperty("user_numbers")
    val userNumbers: Set<Int>,

    @JsonProperty("draw_number")
    val drawNumber: Int
)
```

### 5.3 실시간 API
```kotlin
@RestController
@RequestMapping("/lotto/realtime")
class LottoRealtimeController(private val realtimeService: LottoRealtimeService) {

    @GetMapping("/latest")
    @Operation(summary = "최신 로또 정보", description = "가장 최근 로또 정보를 조회")
    suspend fun getLatestDraw(): ApiResponse {
        val latestDraw = realtimeService.getLatestDraw()
        return ApiResponse(
            statusCode = 200,
            message = "Retrieved latest lotto draw",
            data = latestDraw
        )
    }

    @GetMapping("/next-draw-date")
    @Operation(summary = "다음 추첨일 조회", description = "다음 로또 추첨일을 조회")
    suspend fun getNextDrawDate(): ApiResponse {
        val nextDrawDate = realtimeService.getNextDrawDate()
        return ApiResponse(
            statusCode = 200,
            message = "Retrieved next draw date",
            data = mapOf("next_draw_date" to nextDrawDate)
        )
    }
}
```

## 6. 설정 및 의존성

### 6.1 build.gradle.kts 주요 의존성
```kotlin
dependencies {
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")

    // JSON 처리
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // HTTP 클라이언트
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    // HTML 파싱
    implementation("org.jsoup:jsoup:1.19.1")

    // 데이터베이스
    implementation("org.jetbrains.exposed:exposed-core:0.60.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.60.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.60.0")
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:0.60.0")

    // Swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.6")

    // 로깅
    implementation("io.github.microutils:kotlin-logging-jvm:7.0.5")

    // 테스트
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.mockk:mockk:1.13.17")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
}
```

### 6.2 application.yml 설정
```yaml
spring:
  application:
    name: lotto-api
  profiles:
    active: dev

  jackson:
    serialization:
      write-dates-as-timestamps: false
    default-property-inclusion: non_null

  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/lotto}
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:postgres}

  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: ${SHOW_SQL:false}
    properties:
      hibernate:
        format_sql: ${FORMAT_SQL:false}

lotto:
  api:
    base-url: ${LOTTO_API_URL:https://www.dhlottery.co.kr}
    max-connections: 10
    timeout: 30s
    retry:
      max-attempts: 3
      initial-delay: 2s

logging:
  level:
    com.example.lotto: INFO
    org.springframework.web.reactive.function.client: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"

server:
  port: 8080
  servlet:
    context-path: /
```

## 7. 데이터베이스 스키마

### 7.1 로또 테이블
```sql
CREATE TABLE lotto (
    id BIGSERIAL PRIMARY KEY,
    drw_no INTEGER NOT NULL UNIQUE,
    drw_no_date DATE NOT NULL,
    drwt_no1 INTEGER NOT NULL,
    drwt_no2 INTEGER NOT NULL,
    drwt_no3 INTEGER NOT NULL,
    drwt_no4 INTEGER NOT NULL,
    drwt_no5 INTEGER NOT NULL,
    drwt_no6 INTEGER NOT NULL,
    bnus_no INTEGER NOT NULL,
    first_przwner_co INTEGER NOT NULL,
    first_accumamnt BIGINT NOT NULL,
    first_winamnt BIGINT NOT NULL,
    tot_sellamnt BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_lotto_drw_no ON lotto(drw_no);
CREATE INDEX idx_lotto_drw_no_date ON lotto(drw_no_date);
```

### 7.2 연금복권 테이블
```sql
CREATE TABLE annuity_lotto (
    id BIGSERIAL PRIMARY KEY,
    drw_no INTEGER NOT NULL UNIQUE,
    drw_no_date DATE NOT NULL,
    group_number INTEGER NOT NULL,
    drwt_no1 INTEGER NOT NULL,
    drwt_no2 INTEGER NOT NULL,
    drwt_no3 INTEGER NOT NULL,
    drwt_no4 INTEGER NOT NULL,
    drwt_no5 INTEGER NOT NULL,
    drwt_no6 INTEGER NOT NULL,
    bonus_no1 INTEGER NOT NULL,
    bonus_no2 INTEGER NOT NULL,
    bonus_no3 INTEGER NOT NULL,
    bonus_no4 INTEGER NOT NULL,
    bonus_no5 INTEGER NOT NULL,
    bonus_no6 INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_annuity_lotto_drw_no ON annuity_lotto(drw_no);
CREATE INDEX idx_annuity_lotto_drw_no_date ON annuity_lotto(drw_no_date);
```

## 8. 확장 제안

### 8.1 캐싱 전략
```kotlin
@Service
class LottoCacheService(
    private val cacheManager: CacheManager
) {
    fun getCachedLotto(drwNo: Int): LottoDto? {
        val cache = cacheManager.getCache("lotto")
        return cache?.get(drwNo)?.get() as LottoDto?
    }

    fun cacheLotto(lotto: LottoDto) {
        val cache = cacheManager.getCache("lotto")
        cache?.put(lotto.drwNo, lotto)
    }
}
```

### 8.2 배치 처리
```kotlin
@Service
class LottoBatchService(
    private val lottoRepository: LottoRepository
) {
    @Scheduled(cron = "0 0 9 * * SAT") // 매주 토요일 9시
    suspend fun updateWeeklyLottoData() {
        try {
            lottoRepository.updateLatestDraws()
            log.info("Weekly lotto data update completed successfully")
        } catch (e: Exception) {
            log.error("Failed to update weekly lotto data", e)
        }
    }
}
```

### 8.3 알림 서비스
```kotlin
@Service
class LottoNotificationService(
    private val lottoRepository: LottoRepository
) {
    suspend fun checkForNewDraws(): List<LottoDto> {
        val latestDraw = lottoRepository.getLatestDraw()
        val lastCheckedDraw = getLastCheckedDrawNumber()

        if (latestDraw.drwNo > lastCheckedDraw) {
            val newDraws = lottoRepository.getDrawsFrom(lastCheckedDraw + 1)
            notifySubscribers(newDraws)
            updateLastCheckedDraw(latestDraw.drwNo)
            return newDraws
        }

        return emptyList()
    }
}
```

## 결론

이 로또 API 참조 가이드는 현재 프로젝트의 경험을 바탕으로 작성되었습니다:

1. **API 구조**: RESTful 설계와 표준 응답 형식
2. **데이터 모델**: 로또와 연금복권의 상세한 DTO 구조
3. **비즈니스 로직**: 실제 운영 환경에서 검증된 로직
4. **설정 관리**: 환경별 설정과 의존성 관리
5. **확장성**: 캐싱, 배치 처리, 알림 등 추가 기능 제안

새 프로젝트에서 이 가이드를 참고하면 안정적이고 확장 가능한 로또 API를 구축할 수 있습니다.

---

**작성일**: 2025-01-21
**버전**: v1.0.0
**작성자**: Senior Technical Lead
