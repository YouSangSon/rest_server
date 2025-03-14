package yousang.rest.shared.log

import org.slf4j.LoggerFactory
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * 글로벌 로깅 유틸리티 함수
 * 이 함수들은 어디서든 import해서 사용할 수 있습니다.
 */

/**
 * 지정된 클래스에 대한 로거를 가져옵니다.
 * 
 * 사용 예시:
 * ```
 * val logger = loggerFor(MyClass::class.java)
 * ```
 */
fun loggerFor(clazz: Class<*>) = LoggerFactory.getLogger(clazz)

/**
 * 지정된 이름에 대한 로거를 가져옵니다.
 * 
 * 사용 예시:
 * ```
 * val logger = loggerFor("CustomLoggerName")
 * ```
 */
fun loggerFor(name: String) = LoggerFactory.getLogger(name)

/**
 * 로그와 함께 코드 블록 실행 - info 레벨
 * 
 * 사용 예시:
 * ```
 * withLogging("사용자 등록") {
 *     userService.register(user)
 * }
 * ```
 */
@OptIn(ExperimentalContracts::class)
inline fun <T> withLogging(message: String, block: () -> T): T {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    
    val logger = GlobalLogger.log
    logger.info("시작: $message")
    
    return try {
        val result = block()
        logger.info("완료: $message")
        result
    } catch (e: Exception) {
        logger.error("오류 발생: $message - ${e.message}", e)
        throw e
    }
}

/**
 * 실행 시간 측정과 함께 로깅
 * 
 * 사용 예시:
 * ```
 * val result = withTimingLog("데이터베이스 쿼리") {
 *     repository.findAll()
 * }
 * ```
 */
@OptIn(ExperimentalContracts::class)
inline fun <T> withTimingLog(operation: String, block: () -> T): T {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    
    val logger = GlobalLogger.log
    val startTime = System.currentTimeMillis()
    
    return try {
        val result = block()
        val elapsedTime = System.currentTimeMillis() - startTime
        logger.info("$operation 완료 (${elapsedTime}ms)")
        result
    } catch (e: Exception) {
        val elapsedTime = System.currentTimeMillis() - startTime
        logger.error("$operation 실패 (${elapsedTime}ms): ${e.message}", e)
        throw e
    }
} 