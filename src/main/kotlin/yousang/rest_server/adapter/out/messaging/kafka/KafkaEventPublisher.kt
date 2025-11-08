package yousang.rest_server.adapter.out.messaging.kafka

import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import yousang.rest_server.application.ports.out.EventPublisherPort
import yousang.rest_server.domain.event.DomainEvent

/**
 * Kafka 이벤트 발행자
 *
 * EventPublisherPort를 구현하여 Kafka로 도메인 이벤트를 발행합니다.
 */
@Component
class KafkaEventPublisher(
    private val kafkaTemplate: KafkaTemplate<String, Any>
) : EventPublisherPort {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun publish(event: DomainEvent) {
        val topic = event.eventType
        try {
            kafkaTemplate.send(topic, event.eventId, event).get()
            logger.debug("Event published to topic '{}': {}", topic, event.eventId)
        } catch (e: Exception) {
            logger.error("Failed to publish event to topic '{}': {}", topic, event.eventId, e)
            throw RuntimeException("Failed to publish event", e)
        }
    }

    override fun publishAll(events: List<DomainEvent>) {
        events.forEach { publish(it) }
    }

    override fun publishToTopic(topic: String, event: DomainEvent) {
        try {
            kafkaTemplate.send(topic, event.eventId, event).get()
            logger.debug("Event published to custom topic '{}': {}", topic, event.eventId)
        } catch (e: Exception) {
            logger.error("Failed to publish event to custom topic '{}': {}", topic, event.eventId, e)
            throw RuntimeException("Failed to publish event to custom topic", e)
        }
    }

    override fun publishWithKey(event: DomainEvent, partitionKey: String) {
        val topic = event.eventType
        try {
            kafkaTemplate.send(topic, partitionKey, event).get()
            logger.debug("Event published to topic '{}' with key '{}': {}", topic, partitionKey, event.eventId)
        } catch (e: Exception) {
            logger.error("Failed to publish event to topic '{}' with key '{}': {}",
                topic, partitionKey, event.eventId, e)
            throw RuntimeException("Failed to publish event with key", e)
        }
    }

    /**
     * 비동기로 이벤트를 발행합니다 (Future를 반환하지 않음).
     */
    fun publishAsync(event: DomainEvent) {
        val topic = event.eventType
        kafkaTemplate.send(topic, event.eventId, event)
            .whenComplete { result, ex ->
                if (ex != null) {
                    logger.error("Failed to publish event asynchronously to topic '{}': {}",
                        topic, event.eventId, ex)
                } else {
                    logger.debug("Event published asynchronously to topic '{}': {}",
                        topic, event.eventId)
                }
            }
    }
}
