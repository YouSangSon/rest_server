package yousang.rest_server.domain.model

import java.time.LocalDateTime

/**
 * Audit log domain model for tracking system events
 * Stored in MongoDB for flexible schema
 */
data class AuditLog(
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
)

enum class EventType {
    USER_REGISTRATION,
    USER_LOGIN,
    USER_LOGOUT,
    USER_UPDATE,
    USER_DELETE,
    OAUTH2_LOGIN,
    API_ACCESS,
    PERMISSION_DENIED,
    SYSTEM_ERROR,
    DATA_CREATED,
    DATA_UPDATED,
    DATA_DELETED
}
