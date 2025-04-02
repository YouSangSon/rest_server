package yousang.rest.shared.log

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.slf4j.Logger
import org.springframework.stereotype.Component
import java.time.LocalDateTime

/**
 * Simple aspect for automatic method logging
 */
@Aspect
@Component
class LoggingAspect {
    private val log by LoggerDelegate()

    @Around("@annotation(yousang.rest.shared.log.Loggable) || @within(yousang.rest.shared.log.Loggable)")
    fun logAround(joinPoint: ProceedingJoinPoint): Any? {
        val signature = joinPoint.signature as MethodSignature
        val method = signature.method
        val loggable = method.getAnnotation(Loggable::class.java)
            ?: joinPoint.target.javaClass.getAnnotation(Loggable::class.java)

        val methodName = signature.declaringType.simpleName + "." + method.name

        // Method start logging
        log(loggable.level, "Starting method: $methodName | Time: ${LocalDateTime.now()}")

        // Parameter logging
        if (loggable.parameters && joinPoint.args.isNotEmpty()) {
            log(loggable.level, "Parameters:")
            val parameterNames = signature.parameterNames
            val args = joinPoint.args

            for (i in args.indices) {
                val argName = if (i < parameterNames.size) parameterNames[i] else "arg$i"
                log(loggable.level, "  $argName: ${formatValue(args[i])}")
            }
        }

        // Method execution
        val startTime = System.currentTimeMillis()
        val result = try {
            joinPoint.proceed()
        } catch (e: Exception) {
            log(LogLevel.ERROR, "Exception in method: $methodName", e)
            throw e
        }
        val executionTime = System.currentTimeMillis() - startTime

        // Execution time logging
        log(loggable.level, "Method $methodName execution time: $executionTime ms")

        // Result logging
        if (loggable.result && method.returnType != Void.TYPE) {
            log(loggable.level, "Result: ${formatValue(result)}")
        }

        // Method completion logging
        log(loggable.level, "Completed method: $methodName | Time: ${LocalDateTime.now()}")

        return result
    }

    /**
     * Log according to the specified level
     */
    private fun log(level: LogLevel, message: String, throwable: Throwable? = null) {
        when (level) {
            LogLevel.TRACE -> if (throwable != null) log.trace(message, throwable) else log.trace(message)
            LogLevel.DEBUG -> if (throwable != null) log.debug(message, throwable) else log.debug(message)
            LogLevel.INFO -> if (throwable != null) log.info(message, throwable) else log.info(message)
            LogLevel.WARN -> if (throwable != null) log.warn(message, throwable) else log.warn(message)
            LogLevel.ERROR -> if (throwable != null) log.error(message, throwable) else log.error(message)
        }
    }

    /**
     * Format value for logging
     */
    private fun formatValue(value: Any?): String {
        return when {
            value == null -> "null"
            value is Array<*> -> "Array(size=${value.size}): ${value.contentToString()}"
            value is Collection<*> -> "Collection(size=${value.size}): $value"
            value is Map<*, *> -> "Map(size=${value.size}): $value"
            else -> value.toString()
        }
    }
}