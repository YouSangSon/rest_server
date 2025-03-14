package yousang.rest.shared.log

import org.slf4j.Logger
import org.slf4j.MDC
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * 로그에 추가 속성을 포함하여 출력
 */
fun Logger.infoWithContext(ctx: Map<String, String>, message: String) {
    val previous = mutableMapOf<String, String>()
    try {
        // 기존 MDC 값 저장
        ctx.keys.forEach { key ->
            MDC.get(key)?.let { previous[key] = it }
        }
        
        // 새 MDC 값 설정
        ctx.forEach { (key, value) -> MDC.put(key, value) }
        
        // 로그 출력
        info(message)
    } finally {
        // MDC 값 복원
        ctx.keys.forEach { key -> MDC.remove(key) }
        previous.forEach { (key, value) -> MDC.put(key, value) }
    }
}

/**
 * 로그에 추가 속성을 포함하여 출력
 */
fun Logger.debugWithContext(ctx: Map<String, String>, message: String) {
    val previous = mutableMapOf<String, String>()
    try {
        // 기존 MDC 값 저장
        ctx.keys.forEach { key ->
            MDC.get(key)?.let { previous[key] = it }
        }
        
        // 새 MDC 값 설정
        ctx.forEach { (key, value) -> MDC.put(key, value) }
        
        // 로그 출력
        debug(message)
    } finally {
        // MDC 값 복원
        ctx.keys.forEach { key -> MDC.remove(key) }
        previous.forEach { (key, value) -> MDC.put(key, value) }
    }
}

/**
 * 로그에 추가 속성을 포함하여 출력
 */
fun Logger.errorWithContext(ctx: Map<String, String>, message: String, throwable: Throwable? = null) {
    val previous = mutableMapOf<String, String>()
    try {
        // 기존 MDC 값 저장
        ctx.keys.forEach { key ->
            MDC.get(key)?.let { previous[key] = it }
        }
        
        // 새 MDC 값 설정
        ctx.forEach { (key, value) -> MDC.put(key, value) }
        
        // 로그 출력
        if (throwable != null) {
            error(message, throwable)
        } else {
            error(message)
        }
    } finally {
        // MDC 값 복원
        ctx.keys.forEach { key -> MDC.remove(key) }
        previous.forEach { (key, value) -> MDC.put(key, value) }
    }
}

/**
 * 시간 측정과 함께 람다 실행
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
 * 로깅과 함께 범위 함수 실행 (info 레벨)
 */
@OptIn(ExperimentalContracts::class)
inline fun <T> Logger.withInfoScope(message: String, block: () -> T): T {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    
    info("START: $message")
    return try {
        block().also {
            info("END: $message")
        }
    } catch (e: Exception) {
        error("ERROR IN: $message", e)
        throw e
    }
}

/**
 * 로깅과 함께 범위 함수 실행 (debug 레벨)
 */
@OptIn(ExperimentalContracts::class)
inline fun <T> Logger.withDebugScope(message: String, block: () -> T): T {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    
    if (!isDebugEnabled) return block()
    
    debug("START: $message")
    return try {
        block().also {
            debug("END: $message")
        }
    } catch (e: Exception) {
        error("ERROR IN: $message", e)
        throw e
    }
}

/**
 * 디버그 로깅과 함께 람다 실행
 */
inline fun Logger.ifDebug(block: Logger.() -> Unit) {
    if (isDebugEnabled) {
        block()
    }
}

/**
 * 트레이스 로깅과 함께 람다 실행
 */
inline fun Logger.ifTrace(block: Logger.() -> Unit) {
    if (isTraceEnabled) {
        block()
    }
} 