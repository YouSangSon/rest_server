package yousang.rest_server.domain.model

import java.time.LocalDateTime

/**
 * User event for Kafka streaming
 */
data class UserEvent(
    val eventId: String,
    val eventType: UserEventType,
    val userId: Long?,
    val username: String,
    val email: String,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val metadata: Map<String, Any>? = null
)

enum class UserEventType {
    USER_REGISTERED,
    USER_UPDATED,
    USER_DELETED,
    USER_LOGGED_IN,
    USER_LOGGED_OUT,
    PASSWORD_CHANGED,
    EMAIL_VERIFIED,
    OAUTH2_LINKED
}
