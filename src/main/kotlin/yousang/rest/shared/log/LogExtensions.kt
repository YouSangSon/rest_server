package yousang.rest.shared.log

import org.slf4j.Logger
import org.slf4j.MDC
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Simple logger extension functions for common use cases
 */

/**
 * Log with context data added to MDC
 */
fun Logger.logWithContext(level: String, message: String, context: Map<String, String>, throwable: Throwable? = null) {
    val previous = mutableMapOf<String, String>()
    try {
        // Save existing MDC values
        context.keys.forEach { key ->
            MDC.get(key)?.let { previous[key] = it }
        }
        
        // Set new MDC values
        context.forEach { (key, value) -> MDC.put(key, value) }
        
        // Log the message
        when (level) {
            "trace" -> if (throwable != null) trace(message, throwable) else trace(message)
            "debug" -> if (throwable != null) debug(message, throwable) else debug(message)
            "info" -> if (throwable != null) info(message, throwable) else info(message)
            "warn" -> if (throwable != null) warn(message, throwable) else warn(message)
            "error" -> if (throwable != null) error(message, throwable) else error(message)
        }
    } finally {
        // Restore MDC values
        context.keys.forEach { key -> MDC.remove(key) }
        previous.forEach { (key, value) -> MDC.put(key, value) }
    }
}

/**
 * Measure execution time of a code block
 */
@OptIn(ExperimentalContracts::class)
inline fun <T> Logger.withTiming(message: String, block: () -> T): T {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    
    val start = System.currentTimeMillis()
    return try {
        block().also {
            val time = System.currentTimeMillis() - start
            info("$message completed in $time ms")
        }
    } catch (e: Exception) {
        val time = System.currentTimeMillis() - start
        error("$message failed after $time ms", e)
        throw e
    }
}

/**
 * Execute code block only if debug is enabled
 */
inline fun Logger.ifDebug(block: Logger.() -> Unit) {
    if (isDebugEnabled) {
        block()
    }
}

/**
 * Execute code block only if trace is enabled
 */
inline fun Logger.ifTrace(block: Logger.() -> Unit) {
    if (isTraceEnabled) {
        block()
    }
} 