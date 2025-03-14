package yousang.rest.shared.utils

/**
 * 메소드 호출 시 자동으로 로깅하기 위한 어노테이션
 * 메소드의 실행 시간, 파라미터, 반환값 등을 로깅
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Loggable(
    /**
     * 로그 레벨 (기본값: INFO)
     */
    val level: LogLevel = LogLevel.INFO,
    
    /**
     * 메소드 파라미터를 로깅할지 여부 (기본값: true)
     */
    val parameters: Boolean = true,
    
    /**
     * 반환값을 로깅할지 여부 (기본값: true)
     */
    val result: Boolean = true,
    
    /**
     * 실행 시간을 로깅할지 여부 (기본값: true)
     */
    val executionTime: Boolean = true,
    
    /**
     * 실행 시간 경고 임계값 (밀리초) - 이 값 이상 소요되면 경고 로그를 출력
     */
    val warnThresholdMillis: Long = 1000
)

/**
 * 로그 레벨
 */
enum class LogLevel {
    TRACE, DEBUG, INFO, WARN, ERROR
} 