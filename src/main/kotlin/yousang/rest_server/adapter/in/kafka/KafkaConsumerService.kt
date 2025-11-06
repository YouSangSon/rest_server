package yousang.rest_server.adapter.`in`.kafka

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service
import yousang.rest_server.config.KafkaConfig

/**
 * Kafka consumer service for consuming events
 */
@Service
@ConditionalOnProperty(name = ["spring.kafka.bootstrap-servers"])
class KafkaConsumerService {
    private val logger = LoggerFactory.getLogger(KafkaConsumerService::class.java)

    @KafkaListener(
        topics = [KafkaConfig.USER_EVENTS_TOPIC],
        groupId = "\${spring.kafka.consumer.group-id}"
    )
    fun consumeUserEvent(event: Map<String, Any>) {
        logger.info("Consumed user event: $event")

        // Process user event
        // For example: update search index, send notification, update analytics, etc.
        try {
            val eventType = event["eventType"]
            val username = event["username"]
            logger.info("Processing user event: $eventType for user: $username")

            // Add your business logic here
            // Example: Update Elasticsearch, send email, update cache, etc.

        } catch (e: Exception) {
            logger.error("Error processing user event", e)
        }
    }

    @KafkaListener(
        topics = [KafkaConfig.AUDIT_EVENTS_TOPIC],
        groupId = "\${spring.kafka.consumer.group-id}"
    )
    fun consumeAuditEvent(event: Map<String, Any>) {
        logger.info("Consumed audit event: $event")

        // Process audit event
        // For example: store in data warehouse, trigger alerts, etc.
        try {
            val eventType = event["eventType"]
            logger.info("Processing audit event: $eventType")

            // Add your business logic here
            // Example: Store in MongoDB, trigger security alerts, etc.

        } catch (e: Exception) {
            logger.error("Error processing audit event", e)
        }
    }

    @KafkaListener(
        topics = [KafkaConfig.NOTIFICATION_TOPIC],
        groupId = "\${spring.kafka.consumer.group-id}"
    )
    fun consumeNotification(notification: Map<String, Any>) {
        logger.info("Consumed notification: $notification")

        // Process notification
        // For example: send email, push notification, SMS, etc.
        try {
            val recipient = notification["recipient"]
            val message = notification["message"]
            logger.info("Sending notification to: $recipient - $message")

            // Add your business logic here
            // Example: Send email via SendGrid, push notification via Firebase, etc.

        } catch (e: Exception) {
            logger.error("Error processing notification", e)
        }
    }
}
