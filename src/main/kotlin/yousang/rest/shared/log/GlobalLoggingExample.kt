package yousang.rest.shared.log

import org.springframework.stereotype.Component

/**
 * 글로벌 로깅 유틸리티 함수 사용 예시
 */
@Component
class GlobalLoggingExample {
    
    /**
     * withLogging 함수 사용 예시
     */
    fun demonstrateWithLogging(id: String) {
        withLogging("ID $id 처리") {
            // 비즈니스 로직 수행
            Thread.sleep(100) // 작업 시뮬레이션
            "처리 결과"
        }
    }
    
    /**
     * withTimingLog 함수 사용 예시
     */
    fun demonstrateWithTimingLog(dataSize: Int) {
        val result = withTimingLog("${dataSize}개 데이터 처리") {
            // 비즈니스 로직 수행
            Thread.sleep(dataSize.toLong()) // 작업 시뮬레이션
            "처리 완료"
        }
        
        // 결과 사용
        log.info("처리 결과: $result")
    }
    
    /**
     * 커스텀 로거 사용 예시
     */
    fun demonstrateCustomLogger() {
        // 클래스 기반 로거
        val classLogger = loggerFor(GlobalLoggingExample::class.java)
        classLogger.info("클래스 기반 로거 사용")
        
        // 이름 기반 로거
        val namedLogger = loggerFor("CustomLogger")
        namedLogger.info("이름 기반 로거 사용")
    }
    
    /**
     * 중첩된 로깅 유틸리티 함수 사용 예시
     */
    fun demonstrateNestedLogging() {
        withLogging("외부 작업") {
            log.info("외부 작업 중간 단계")
            
            withTimingLog("내부 작업") {
                // 내부 작업 수행
                Thread.sleep(200)
                "내부 작업 결과"
            }
            
            "외부 작업 결과"
        }
    }
    
    /**
     * 예외 처리 예시
     */
    fun demonstrateExceptionHandling(shouldFail: Boolean) {
        try {
            withLogging("예외 발생 가능 작업") {
                if (shouldFail) {
                    throw RuntimeException("의도적인 예외 발생")
                }
                "성공"
            }
        } catch (e: Exception) {
            // 예외는 이미 withLogging에서 로깅되었으므로 추가로 로깅할 필요 없음
            // 예외 처리 로직만 구현
        }
    }
} 