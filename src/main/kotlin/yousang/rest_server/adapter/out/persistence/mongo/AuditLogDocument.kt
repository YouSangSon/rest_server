package yousang.rest_server.adapter.out.persistence.mongo

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import yousang.rest_server.domain.model.AuditLog
import yousang.rest_server.domain.model.EventType
import java.time.LocalDateTime

@Document(collection = "audit_logs")
data class AuditLogDocument(
    @Id
    val id: String? = null,
    val eventType: EventType,
    val username: String?,
    val action: String,
    val resourceType: String,
    val resourceId: String?,
    val ipAddress: String?,
    val userAgent: String?,
    val details: Map<String, Any>? = null,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val success: Boolean = true,
    val errorMessage: String? = null
) {
    fun toDomain(): AuditLog {
        return AuditLog(
            id = id,
            eventType = eventType,
            username = username,
            action = action,
            resourceType = resourceType,
            resourceId = resourceId,
            ipAddress = ipAddress,
            userAgent = userAgent,
            details = details,
            timestamp = timestamp,
            success = success,
            errorMessage = errorMessage
        )
    }

    companion object {
        fun fromDomain(auditLog: AuditLog): AuditLogDocument {
            return AuditLogDocument(
                id = auditLog.id,
                eventType = auditLog.eventType,
                username = auditLog.username,
                action = auditLog.action,
                resourceType = auditLog.resourceType,
                resourceId = auditLog.resourceId,
                ipAddress = auditLog.ipAddress,
                userAgent = auditLog.userAgent,
                details = auditLog.details,
                timestamp = auditLog.timestamp,
                success = auditLog.success,
                errorMessage = auditLog.errorMessage
            )
        }
    }
}
