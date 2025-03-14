package yousang.rest.shared.utils

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.util.*

/**
 * 구조화된 로깅을 위한 유틸리티 클래스
 */
object StructuredLogging {
    private val objectMapper = ObjectMapper()
    
    /**
     * 구조화된 로그 엔트리 생성
     */
    fun createLogEntry(
        message: String,
        level: Level = Level.INFO,
        additionalFields: Map<String, Any?> = emptyMap()
    ): Map<String, Any?> {
        val entry = mutableMapOf<String, Any?>(
            "timestamp" to System.currentTimeMillis(),
            "level" to level.name,
            "message" to message
        )
        
        // 추가 필드 병합
        entry.putAll(additionalFields)
        
        // 현재 요청 정보 추가
        getRequestInfo()?.let { entry.putAll(it) }
        
        return entry
    }
    
    /**
     * 현재 HTTP 요청 정보 가져오기
     */
    fun getRequestInfo(): Map<String, Any?>? {
        return try {
            val requestAttributes = RequestContextHolder.getRequestAttributes()
            if (requestAttributes is ServletRequestAttributes) {
                val request = requestAttributes.request
                val requestId = request.getAttribute("requestId")?.toString() 
                    ?: UUID.randomUUID().toString()
                
                mapOf(
                    "requestId" to requestId,
                    "method" to request.method,
                    "uri" to request.requestURI,
                    "userAgent" to (request.getHeader("User-Agent") ?: "Unknown")
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 구조화된 로그 출력
     */
    fun logStructured(
        logger: Logger,
        message: String,
        level: Level = Level.INFO,
        additionalFields: Map<String, Any?> = emptyMap()
    ) {
        val entry = createLogEntry(message, level, additionalFields)
        val json = try {
            objectMapper.writeValueAsString(entry)
        } catch (e: Exception) {
            "로그 직렬화 오류: ${e.message}"
        }
        
        when (level) {
            Level.ERROR -> logger.error(json)
            Level.WARN -> logger.warn(json)
            Level.INFO -> logger.info(json)
            Level.DEBUG -> logger.debug(json)
            Level.TRACE -> logger.trace(json)
        }
    }
    
    /**
     * 예외 로깅
     */
    fun logException(
        logger: Logger,
        exception: Throwable,
        message: String = exception.message ?: "에러 발생",
        additionalFields: Map<String, Any?> = emptyMap()
    ) {
        val fields = mutableMapOf<String, Any?>(
            "exception" to exception.javaClass.name,
            "stackTrace" to exception.stackTraceToString()
        ).apply { putAll(additionalFields) }
        
        logStructured(logger, message, Level.ERROR, fields)
    }
    
    /**
     * 지정된 클래스의 로거 가져오기
     */
    inline fun <reified T> getLogger(): Logger = LoggerFactory.getLogger(T::class.java)
} 