package yousang.rest.shared.util

import mu.KotlinLogging
import org.springframework.stereotype.Component

@Component
object DebugHelper {
    private val logger = KotlinLogging.logger {}

    fun logException(e: Throwable, message: String? = null) {
        val msg = message ?: "Exception occurred"
        // Log message and stack trace separately
        logger.error { "$msg: ${e.message}" }
        logger.error("Full stack trace:", e)
        
        // 개발 환경에서만 스택 트레이스 출력
        if (isDevelopmentEnvironment()) {
            e.printStackTrace()
        }
    }

    fun logError(message: String, vararg args: Any?) {
        logger.error { formatMessage(message, args) }
    }

    fun logWarn(message: String, vararg args: Any?) {
        logger.warn { formatMessage(message, args) }
    }

    fun logInfo(message: String, vararg args: Any?) {
        logger.info { formatMessage(message, args) }
    }

    fun logDebug(message: String, vararg args: Any?) {
        logger.debug { formatMessage(message, args) }
    }

    fun logTrace(message: String, vararg args: Any?) {
        logger.trace { formatMessage(message, args) }
    }

    private fun formatMessage(message: String, args: Array<out Any?>): String {
        return if (args.isEmpty()) {
            message
        } else {
            String.format(message, *args)
        }
    }

    private fun isDevelopmentEnvironment(): Boolean {
        return System.getProperty("spring.profiles.active")?.contains("dev") ?: false
    }
} 