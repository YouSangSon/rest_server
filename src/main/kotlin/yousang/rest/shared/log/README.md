# 로깅 가이드

이 문서는 프로젝트에서 로깅을 사용하는 방법에 대한 가이드입니다.

## 로깅 기본 사용법

### 방법 1: 전역 로거 사용 (권장)

전역 로거를 사용하면 각 클래스에서 로거를 선언할 필요가 없습니다:

```kotlin
import yousang.rest.shared.log.log

class MyService {
    fun doSomething() {
        log.info("작업을 시작합니다")
        
        try {
            // 비즈니스 로직 수행
            log.info("작업이 성공적으로 완료되었습니다")
        } catch (e: Exception) {
            log.error("작업 중 오류가 발생했습니다: ${e.message}", e)
            throw e
        }
    }
}
```

또는 정적 로거를 사용할 수 있습니다:

```kotlin
import yousang.rest.shared.log.GlobalLogger

// 어디서든 접근 가능한 정적 로거
fun someFunction() {
    GlobalLogger.log.info("GlobalLogger를 사용한 로깅")
}
```

### 방법 2: LoggerDelegate 사용

기존 방식으로 `LoggerDelegate`를 사용할 수도 있습니다:

```kotlin
import yousang.rest.shared.log.LoggerDelegate

class MyService {
    private val log by LoggerDelegate()
    
    fun doSomething() {
        log.info("작업을 시작합니다")
        
        try {
            // 비즈니스 로직 수행
            log.info("작업이 성공적으로 완료되었습니다")
        } catch (e: Exception) {
            log.error("작업 중 오류가 발생했습니다: ${e.message}", e)
            throw e
        }
    }
}
```

## 글로벌 유틸리티 함수

### withLogging - 코드 블록 실행 전/후 로깅

```kotlin
import yousang.rest.shared.log.withLogging

fun registerUser(user: User) {
    withLogging("사용자 등록") {
        userRepository.save(user)
        emailService.sendWelcomeEmail(user)
    }
}
```

### withTimingLog - 실행 시간 측정 및 로깅

```kotlin
import yousang.rest.shared.log.withTimingLog

fun processLargeData() {
    val result = withTimingLog("대용량 데이터 처리") {
        dataProcessor.processData()
    }
}
```

### 커스텀 로거 생성

```kotlin
import yousang.rest.shared.log.loggerFor

// 클래스 기반 로거
val logger1 = loggerFor(MyClass::class.java)

// 이름 기반 로거
val logger2 = loggerFor("CustomLogger")
```

## 로그 레벨

다음과 같은 로그 레벨을 사용할 수 있습니다:

1. **TRACE**: 가장 상세한 정보, 개발 중에만 사용
2. **DEBUG**: 디버깅을 위한 상세 정보
3. **INFO**: 일반적인 애플리케이션 정보
4. **WARN**: 잠재적인 오류 상황
5. **ERROR**: 오류 및 예외 상황

로그 레벨은 application.yml 또는 logback-spring.xml 파일에서 설정할 수 있습니다.

## 성능 최적화 팁

로그 생성 비용이 높은 경우(예: 복잡한 문자열 연산) 조건부 로깅을 사용하세요:

```kotlin
// 비용이 많이 드는 문자열 연산을 로깅할 때
if (log.isDebugEnabled) {
    log.debug("상세 정보: ${expensiveOperation()}")
}
```

## 구조화된 로깅 (MDC)

추가 컨텍스트 정보가 필요한 경우 infoWithContext 확장 함수를 사용하세요:

```kotlin
val context = mapOf(
    "userId" to "12345",
    "action" to "PAYMENT"
)
log.infoWithContext(context, "사용자 액션 수행")
```

## 예제

더 많은 예제는 `LoggingExample.kt` 파일을 참조하세요. 