package yousang.rest.shared.log

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * 전역 로거 객체
 * 어디서든 GlobalLogger.log를 통해 로깅 가능
 */
object GlobalLogger {
    val log: Logger = LoggerFactory.getLogger("GlobalLogger")
}

/**
 * 모든 클래스에서 로거를 사용할 수 있게 해주는 확장 프로퍼티
 * 
 * 사용 예시:
 * ```
 * class MyService {
 *     fun doSomething() {
 *         log.info("작업을 시작합니다")
 *     }
 * }
 * ```
 */
val Any.log: Logger
    get() = LoggerFactory.getLogger(this.javaClass) 