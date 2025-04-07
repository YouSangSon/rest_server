package yousang.rest.shared.log

import io.github.oshai.kotlinlogging.KotlinLogging
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.stereotype.Component
import yousang.rest.shared.util.DebugHelper

/**
 * Simple aspect for automatic method logging
 */
@Aspect
@Component
class LoggingAspect {
    private val logger = KotlinLogging.logger {}

    @Around("@within(org.springframework.web.bind.annotation.RestController) || " +
            "@within(org.springframework.stereotype.Service) || " +
            "@within(org.springframework.stereotype.Repository)")
    fun logExecutionTime(joinPoint: ProceedingJoinPoint): Any? {
        val start = System.currentTimeMillis()
        val signature = joinPoint.signature as? MethodSignature ?: return joinPoint.proceed()
        val className = signature.declaringType.simpleName
        val methodName = signature.name

        try {
            val result = joinPoint.proceed()
            val executionTime = System.currentTimeMillis() - start
            
            logger.info { "Executed $className.$methodName in $executionTime ms" }
            return result
        } catch (e: Exception) {
            try {
                logger.error { "Exception in $className.$methodName: ${e.message ?: "No message"}" }
                logger.error("Full stack trace:", e)
            } catch (loggingEx: Exception) {
                // Fallback if logging fails
                System.err.println("Error during logging: ${loggingEx.message}")
                e.printStackTrace()
            }
            throw e
        }
    }
}