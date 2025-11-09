package yousang.rest_server.application.service

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.stereotype.Service
import yousang.rest_server.application.ports.out.AuditLogPort
import yousang.rest_server.domain.model.AuditLog
import yousang.rest_server.domain.model.EventType
import java.time.LocalDateTime

@Service
@ConditionalOnBean(AuditLogPort::class)
class AuditLogService(
    private val auditLogPort: AuditLogPort
) {
    private val logger = LoggerFactory.getLogger(AuditLogService::class.java)

    fun logEvent(auditLog: AuditLog) {
        try {
            auditLogPort.save(auditLog)
            logger.info("Audit log saved: ${auditLog.eventType} for user ${auditLog.username}")
        } catch (e: Exception) {
            logger.error("Failed to save audit log", e)
        }
    }

    fun logUserLogin(username: String, ipAddress: String?, userAgent: String?, success: Boolean) {
        logEvent(
            AuditLog(
                eventType = EventType.USER_LOGIN,
                username = username,
                action = "login",
                resourceType = "user",
                resourceId = username,
                ipAddress = ipAddress,
                userAgent = userAgent,
                success = success
            )
        )
    }

    fun logOAuth2Login(username: String, provider: String, ipAddress: String?, userAgent: String?) {
        logEvent(
            AuditLog(
                eventType = EventType.OAUTH2_LOGIN,
                username = username,
                action = "oauth2_login",
                resourceType = "user",
                resourceId = username,
                ipAddress = ipAddress,
                userAgent = userAgent,
                details = mapOf("provider" to provider)
            )
        )
    }

    fun getUserLogs(username: String): List<AuditLog> {
        return auditLogPort.findByUsername(username)
    }

    fun getLogsByType(eventType: EventType): List<AuditLog> {
        return auditLogPort.findByEventType(eventType)
    }

    fun getLogsByDateRange(start: LocalDateTime, end: LocalDateTime): List<AuditLog> {
        return auditLogPort.findByDateRange(start, end)
    }
}
