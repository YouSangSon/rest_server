package yousang.rest.shared.log

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Simple delegate for lazy logger instantiation
 * Usage: private val log by LoggerDelegate()
 */
class LoggerDelegate : ReadOnlyProperty<Any?, Logger> {
    private lateinit var logger: Logger

    override fun getValue(thisRef: Any?, property: KProperty<*>): Logger {
        if (!::logger.isInitialized) {
            logger = LoggerFactory.getLogger(thisRef!!.javaClass)
        }
        return logger
    }
}

// Global logger for static contexts
val log: Logger = LoggerFactory.getLogger("GlobalLogger")