package yousang.rest.shared.log

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Global logger object for application-wide logging
 */
object GlobalLogger {
    val log: Logger = LoggerFactory.getLogger("GlobalLogger")
}

/**
 * Extension property to get a logger for any class
 * 
 * Usage:
 * ```
 * class MyService {
 *     fun doSomething() {
 *         log.info("Starting operation...")
 *     }
 * }
 * ```
 */
val Any.log: Logger
    get() = LoggerFactory.getLogger(this.javaClass) 