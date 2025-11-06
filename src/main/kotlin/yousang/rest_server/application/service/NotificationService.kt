package yousang.rest_server.application.service

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.stereotype.Service
import yousang.rest_server.adapter.out.kafka.KafkaProducerService
import yousang.rest_server.domain.model.*
import java.time.LocalDateTime
import java.util.*

/**
 * Notification service for sending various types of notifications
 */
@Service
@ConditionalOnBean(KafkaProducerService::class)
class NotificationService(
    private val kafkaProducerService: KafkaProducerService
) {
    private val logger = LoggerFactory.getLogger(NotificationService::class.java)

    fun sendWelcomeEmail(user: User) {
        val notification = Notification(
            id = UUID.randomUUID().toString(),
            recipient = user.email,
            recipientType = RecipientType.EMAIL,
            channel = NotificationChannel.EMAIL,
            subject = "Welcome to REST Server!",
            message = """
                Hello ${user.username},

                Welcome to our REST Server!

                Thank you for registering.

                Best regards,
                The REST Server Team
            """.trimIndent(),
            priority = NotificationPriority.NORMAL,
            data = mapOf(
                "userId" to user.id,
                "username" to user.username
            )
        )

        publishNotification(notification)
    }

    fun sendOAuth2WelcomeEmail(user: User, provider: OAuth2Provider) {
        val notification = Notification(
            id = UUID.randomUUID().toString(),
            recipient = user.email,
            recipientType = RecipientType.EMAIL,
            channel = NotificationChannel.EMAIL,
            subject = "Welcome via ${provider.name}!",
            message = """
                Hello ${user.username},

                You've successfully logged in via ${provider.name}.

                Welcome to our REST Server!

                Best regards,
                The REST Server Team
            """.trimIndent(),
            priority = NotificationPriority.NORMAL,
            data = mapOf(
                "userId" to user.id,
                "username" to user.username,
                "provider" to provider.name
            )
        )

        publishNotification(notification)
    }

    fun sendPasswordResetEmail(email: String, resetToken: String) {
        val notification = Notification(
            id = UUID.randomUUID().toString(),
            recipient = email,
            recipientType = RecipientType.EMAIL,
            channel = NotificationChannel.EMAIL,
            subject = "Password Reset Request",
            message = """
                You have requested to reset your password.

                Please use the following token to reset your password:
                $resetToken

                This token will expire in 1 hour.

                If you didn't request this, please ignore this email.

                Best regards,
                The REST Server Team
            """.trimIndent(),
            priority = NotificationPriority.HIGH,
            data = mapOf(
                "resetToken" to resetToken,
                "expiresAt" to LocalDateTime.now().plusHours(1).toString()
            )
        )

        publishNotification(notification)
    }

    fun sendAccountDeletedEmail(user: User) {
        val notification = Notification(
            id = UUID.randomUUID().toString(),
            recipient = user.email,
            recipientType = RecipientType.EMAIL,
            channel = NotificationChannel.EMAIL,
            subject = "Account Deleted",
            message = """
                Hello ${user.username},

                Your account has been successfully deleted.

                If you didn't request this, please contact support immediately.

                Best regards,
                The REST Server Team
            """.trimIndent(),
            priority = NotificationPriority.HIGH,
            data = mapOf(
                "userId" to user.id,
                "username" to user.username,
                "deletedAt" to LocalDateTime.now().toString()
            )
        )

        publishNotification(notification)
    }

    fun sendSecurityAlert(user: User, eventType: String, details: Map<String, Any>) {
        val notification = Notification(
            id = UUID.randomUUID().toString(),
            recipient = user.email,
            recipientType = RecipientType.EMAIL,
            channel = NotificationChannel.EMAIL,
            subject = "Security Alert: $eventType",
            message = """
                Hello ${user.username},

                We detected unusual activity on your account:
                Event: $eventType

                If this was you, you can ignore this message.
                If not, please secure your account immediately.

                Best regards,
                The REST Server Team
            """.trimIndent(),
            priority = NotificationPriority.URGENT,
            data = mapOf(
                "userId" to user.id,
                "username" to user.username,
                "eventType" to eventType,
                "details" to details
            )
        )

        publishNotification(notification)
    }

    private fun publishNotification(notification: Notification) {
        try {
            val notificationMap = mapOf(
                "id" to notification.id,
                "recipient" to notification.recipient,
                "recipientType" to notification.recipientType.name,
                "channel" to notification.channel.name,
                "subject" to notification.subject,
                "message" to notification.message,
                "priority" to notification.priority.name,
                "data" to notification.data,
                "status" to notification.status.name,
                "createdAt" to notification.createdAt.toString()
            )

            kafkaProducerService.publishNotification(notificationMap)
            logger.info("Published notification: ${notification.id} to ${notification.recipient} via ${notification.channel}")
        } catch (e: Exception) {
            logger.error("Failed to publish notification: ${notification.id}", e)
        }
    }
}
