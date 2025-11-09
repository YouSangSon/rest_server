package yousang.rest_server.adapter.out.kafka

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.stereotype.Service
import yousang.rest_server.config.KafkaConfig
import yousang.rest_server.domain.model.UserEvent
import java.util.concurrent.CompletableFuture

/**
 * Kafka producer service for publishing events
 */
@Service
@ConditionalOnProperty(name = ["spring.kafka.bootstrap-servers"])
class KafkaProducerService(
    private val kafkaTemplate: KafkaTemplate<String, Any>
) {
    private val logger = LoggerFactory.getLogger(KafkaProducerService::class.java)

    fun publishUserEvent(event: UserEvent) {
        try {
            val future: CompletableFuture<SendResult<String, Any>> = kafkaTemplate.send(
                KafkaConfig.USER_EVENTS_TOPIC,
                event.eventId,
                event
            )

            future.whenComplete { result, ex ->
                if (ex == null) {
                    logger.info(
                        "Published user event: ${event.eventType} for user ${event.username} " +
                                "to partition ${result.recordMetadata.partition()} with offset ${result.recordMetadata.offset()}"
                    )
                } else {
                    logger.error("Failed to publish user event: ${event.eventType}", ex)
                }
            }
        } catch (e: Exception) {
            logger.error("Error publishing user event", e)
        }
    }

    fun publishAuditEvent(event: Map<String, Any>) {
        try {
            val future: CompletableFuture<SendResult<String, Any>> = kafkaTemplate.send(
                KafkaConfig.AUDIT_EVENTS_TOPIC,
                event["eventId"].toString(),
                event
            )

            future.whenComplete { result, ex ->
                if (ex == null) {
                    logger.info("Published audit event to partition ${result.recordMetadata.partition()}")
                } else {
                    logger.error("Failed to publish audit event", ex)
                }
            }
        } catch (e: Exception) {
            logger.error("Error publishing audit event", e)
        }
    }

    fun publishNotification(notification: Map<String, Any>) {
        try {
            val future: CompletableFuture<SendResult<String, Any>> = kafkaTemplate.send(
                KafkaConfig.NOTIFICATION_TOPIC,
                notification["id"].toString(),
                notification
            )

            future.whenComplete { result, ex ->
                if (ex == null) {
                    logger.info("Published notification to partition ${result.recordMetadata.partition()}")
                } else {
                    logger.error("Failed to publish notification", ex)
                }
            }
        } catch (e: Exception) {
            logger.error("Error publishing notification", e)
        }
    }
}
