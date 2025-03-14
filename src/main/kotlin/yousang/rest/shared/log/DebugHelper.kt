package yousang.rest.shared.utils

import org.jetbrains.exposed.sql.Transaction
import org.slf4j.Logger
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder
import java.util.Stack
import kotlin.reflect.KClass

/**
 * 디버깅을 위한 도우미 함수들
 */
object DebugHelper {
    private val debugLevelStack = ThreadLocal.withInitial { Stack<Boolean>() }
    
    /**
     * 디버그 로그 그룹을 시작합니다.
     * 로그 그룹 내의 모든 로그는 함께 그룹화됩니다.
     */
    fun startLogGroup(logger: Logger, name: String, enabled: Boolean = true) {
        debugLevelStack.get().push(enabled)
        if (enabled && logger.isDebugEnabled) {
            logger.debug("┌─ START: $name ─".padEnd(100, '─'))
        }
    }
    
    /**
     * 디버그 로그 그룹을 종료합니다.
     */
    fun endLogGroup(logger: Logger, name: String) {
        val enabled = if (debugLevelStack.get().isNotEmpty()) debugLevelStack.get().pop() else true
        if (enabled && logger.isDebugEnabled) {
            logger.debug("└─ END: $name ─".padEnd(100, '─'))
        }
    }
    
    /**
     * 디버그 로그를 그룹 내에 기록합니다.
     */
    fun logInGroup(logger: Logger, message: String) {
        val enabled = if (debugLevelStack.get().isNotEmpty()) debugLevelStack.get().peek() else true
        if (enabled && logger.isDebugEnabled) {
            logger.debug("│ $message")
        }
    }
    
    /**
     * 객체를 디버그 로그로 출력합니다.
     */
    fun logObject(logger: Logger, name: String, obj: Any?) {
        if (logger.isDebugEnabled) {
            when (obj) {
                null -> logger.debug("Object [$name] is null")
                is Collection<*> -> {
                    logger.debug("Collection [$name] has ${obj.size} items:")
                    obj.forEachIndexed { index, item ->
                        logger.debug("  [$name][$index] = $item")
                    }
                }
                is Map<*, *> -> {
                    logger.debug("Map [$name] has ${obj.size} entries:")
                    obj.entries.forEachIndexed { index, entry ->
                        logger.debug("  [$name][${entry.key}] = ${entry.value}")
                    }
                }
                else -> logger.debug("Object [$name] = $obj")
            }
        }
    }
    
    /**
     * 예외의 스택 트레이스 정보를 더 읽기 쉽게 로깅합니다.
     */
    fun logException(logger: Logger, message: String, exception: Throwable) {
        if (logger.isDebugEnabled) {
            logger.debug("┌─ Exception: $message ─".padEnd(100, '─'))
            logger.debug("│ Type: ${exception.javaClass.name}")
            logger.debug("│ Message: ${exception.message}")
            logger.debug("│ Stack trace:")
            exception.stackTrace.forEachIndexed { index, element ->
                // 프로젝트 코드에 속한 스택 트레이스만 강조 표시
                val highlight = element.className.startsWith("yousang.rest")
                val prefix = if (highlight) "│ => " else "│    "
                logger.debug("$prefix($index) $element")
                
                // 너무 긴 스택 트레이스는 제한
                if (index > 15 && !highlight) {
                    logger.debug("│    ... (${exception.stackTrace.size - index - 1} more)")
                    return@forEachIndexed
                }
            }
            // 원인 예외가 있으면 재귀적으로 로깅
            if (exception.cause != null && exception.cause != exception) {
                logger.debug("│")
                logger.debug("│ Caused by:")
                logException(logger, "Cause", exception.cause!!)
            }
            logger.debug("└" + "─".repeat(98))
        } else {
            // 디버그 모드가 아닐 때는 간단하게 로깅
            logger.error("$message: ${exception.message}", exception)
        }
    }
    
    /**
     * 현재 요청의 속성 정보를 로깅합니다.
     */
    fun logRequestAttributes(logger: Logger) {
        if (logger.isDebugEnabled) {
            try {
                val attributes = RequestContextHolder.getRequestAttributes()
                if (attributes != null) {
                    logger.debug("Request attributes:")
                    
                    // 요청 스코프 속성 로깅
                    logRequestScope(logger, attributes, RequestAttributes.SCOPE_REQUEST, "REQUEST")
                    
                    // 세션 스코프 속성 로깅
                    logRequestScope(logger, attributes, RequestAttributes.SCOPE_SESSION, "SESSION")
                }
            } catch (e: Exception) {
                logger.warn("Failed to log request attributes", e)
            }
        }
    }
    
    /**
     * 특정 스코프의 요청 속성을 로깅합니다.
     */
    private fun logRequestScope(logger: Logger, attributes: RequestAttributes, scope: Int, scopeName: String) {
        try {
            // 여기서는 Jakarta Servlet API를 사용하는 경우를 가정
            // 실제 구현은 사용 중인 RequestAttributes 구현체에 따라 다를 수 있음
            val httpRequest = when (scope) {
                RequestAttributes.SCOPE_REQUEST -> attributes.getAttribute("org.springframework.web.servlet.DispatcherServlet.REQUEST", scope)
                RequestAttributes.SCOPE_SESSION -> attributes.getAttribute("javax.servlet.http.HttpSession", scope)
                else -> null
            }
            
            if (httpRequest != null) {
                logger.debug("[$scopeName scope]:")
                logger.debug("  Found $scopeName object: ${httpRequest.javaClass.name}")
            }
        } catch (e: Exception) {
            logger.warn("Failed to log $scopeName attributes", e)
        }
    }
    
    /**
     * Exposed SQL 트랜잭션의 SQL 쿼리를 로깅합니다.
     */
    fun logSqlQueries(logger: Logger, transaction: Transaction) {
        if (logger.isDebugEnabled) {
            try {
                startLogGroup(logger, "SQL Queries")
                transaction.statementCount.let { count ->
                    logInGroup(logger, "Transaction has executed $count SQL statements")
                }
                endLogGroup(logger, "SQL Queries")
            } catch (e: Exception) {
                logger.warn("Failed to log SQL queries", e)
            }
        }
    }
} 