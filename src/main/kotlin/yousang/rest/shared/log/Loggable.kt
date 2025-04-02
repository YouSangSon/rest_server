package yousang.rest.shared.log

/**
 * Simple annotation for automatic method logging
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Loggable(
    /**
     * Log level (default: INFO)
     */
    val level: LogLevel = LogLevel.INFO,
    
    /**
     * Whether to log method parameters (default: true)
     */
    val parameters: Boolean = true,
    
    /**
     * Whether to log return value (default: true)
     */
    val result: Boolean = true
)

/**
 * Log levels
 */
enum class LogLevel {
    TRACE, DEBUG, INFO, WARN, ERROR
} 