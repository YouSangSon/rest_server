package yousang.rest.shared.log

import org.springframework.stereotype.Component

/**
 * 로깅 사용 예시 클래스
 * 이 클래스는 글로벌 로거를 사용한 로깅 방법을 보여줍니다.
 */
@Component
class LoggingExample {
    // 글로벌 로거 사용 - 클래스 내에서 별도의 로거 선언 필요 없음

    /**
     * 다양한 로그 레벨 사용 예시
     */
    fun demonstrateLevels() {
        log.trace("이것은 TRACE 레벨 로그입니다 - 매우 상세한 디버깅 정보")
        log.debug("이것은 DEBUG 레벨 로그입니다 - 디버깅 정보")
        log.info("이것은 INFO 레벨 로그입니다 - 일반적인 정보 메시지")
        log.warn("이것은 WARN 레벨 로그입니다 - 경고 메시지")
        log.error("이것은 ERROR 레벨 로그입니다 - 오류 메시지")
    }

    /**
     * 정적 글로벌 로거 사용 예시
     */
    fun demonstrateStaticLogger() {
        GlobalLogger.log.info("정적 글로벌 로거를 사용한 로깅")
    }

    /**
     * 예외와 함께 로그 남기기
     */
    fun logWithException() {
        try {
            throw RuntimeException("예시 예외")
        } catch (e: Exception) {
            log.error("오류 발생: ${e.message}", e)
        }
    }

    /**
     * 조건부 로깅 예시
     */
    fun conditionalLogging(shouldLog: Boolean) {
        // 조건부 로깅 - 로그를 남길지 여부를 결정
        if (log.isDebugEnabled && shouldLog) {
            // 복잡한 문자열 연산이 필요한 경우 조건부로 실행
            log.debug("상세 디버깅 정보: ${computeExpensiveString()}")
        }
        
        // 람다를 사용한 조건부 로깅 (문자열 연산이 복잡한 경우 유용)
        log.debug("이것은 일반 로깅입니다: ${computeExpensiveString()}")
        
        // 대규모 문자열 연산은 로그 레벨에 따라 실행 여부 확인 후 실행
        if (log.isInfoEnabled) {
            log.info("이것은 레벨 체크 후 로깅입니다: ${computeExpensiveString()}")
        }
    }
    
    private fun computeExpensiveString(): String {
        // 이 함수는 문자열을 생성하는데 비용이 많이 든다고 가정
        return "expensive computation result"
    }
    
    /**
     * 구조적 로깅 예시 (MDC 사용)
     */
    fun structuredLogging(userId: String, action: String) {
        // MDC context를 추가하여 로그 출력
        val context = mapOf(
            "userId" to userId,
            "action" to action,
            "timestamp" to System.currentTimeMillis().toString()
        )
        
        // infoWithContext 확장 함수를 사용하여 구조화된 로깅
        log.infoWithContext(context, "사용자 액션 수행")
    }
} 