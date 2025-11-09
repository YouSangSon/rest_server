package yousang.rest_server.domain.model

import java.time.LocalDateTime

/**
 * Notification domain model
 */
data class Notification(
    val id: String,
    val recipient: String,
    val recipientType: RecipientType,
    val channel: NotificationChannel,
    val subject: String,
    val message: String,
    val priority: NotificationPriority = NotificationPriority.NORMAL,
    val data: Map<String, Any>? = null,
    val scheduledAt: LocalDateTime? = null,
    val sentAt: LocalDateTime? = null,
    val status: NotificationStatus = NotificationStatus.PENDING,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

enum class RecipientType {
    EMAIL,
    PHONE,
    USER_ID,
    DEVICE_TOKEN
}

enum class NotificationChannel {
    EMAIL,
    SMS,
    PUSH,
    IN_APP,
    WEBHOOK
}

enum class NotificationPriority {
    LOW,
    NORMAL,
    HIGH,
    URGENT
}

enum class NotificationStatus {
    PENDING,
    SENDING,
    SENT,
    FAILED,
    CANCELLED
}
