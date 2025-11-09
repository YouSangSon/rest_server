package yousang.rest_server.adapter.out.kafka

import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.kafka.core.KafkaTemplate
import yousang.rest_server.domain.model.UserEvent
import yousang.rest_server.domain.model.UserEventType
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.CompletableFuture

class KafkaProducerServiceTest {

    @Test
    fun `should publish user event successfully`() {
        // Given
        val kafkaTemplate: KafkaTemplate<String, Any> = mock()
        val producerService = KafkaProducerService(kafkaTemplate)

        val event = UserEvent(
            eventId = UUID.randomUUID().toString(),
            eventType = UserEventType.USER_REGISTERED,
            userId = 1L,
            username = "testuser",
            email = "test@example.com",
            timestamp = LocalDateTime.now()
        )

        val future = CompletableFuture<org.springframework.kafka.support.SendResult<String, Any>>()
        whenever(kafkaTemplate.send(any(), any(), any())).thenReturn(future)

        // When
        producerService.publishUserEvent(event)

        // Then
        verify(kafkaTemplate).send(
            eq("user-events"),
            eq(event.eventId),
            eq(event)
        )
    }

    @Test
    fun `should publish audit event successfully`() {
        // Given
        val kafkaTemplate: KafkaTemplate<String, Any> = mock()
        val producerService = KafkaProducerService(kafkaTemplate)

        val event = mapOf(
            "eventId" to UUID.randomUUID().toString(),
            "eventType" to "USER_LOGIN",
            "username" to "testuser",
            "ipAddress" to "127.0.0.1"
        )

        val future = CompletableFuture<org.springframework.kafka.support.SendResult<String, Any>>()
        whenever(kafkaTemplate.send(any(), any(), any())).thenReturn(future)

        // When
        producerService.publishAuditEvent(event)

        // Then
        verify(kafkaTemplate).send(
            eq("audit-events"),
            eq(event["eventId"].toString()),
            eq(event)
        )
    }

    @Test
    fun `should publish notification successfully`() {
        // Given
        val kafkaTemplate: KafkaTemplate<String, Any> = mock()
        val producerService = KafkaProducerService(kafkaTemplate)

        val notification = mapOf(
            "id" to UUID.randomUUID().toString(),
            "recipient" to "test@example.com",
            "message" to "Welcome to our service!"
        )

        val future = CompletableFuture<org.springframework.kafka.support.SendResult<String, Any>>()
        whenever(kafkaTemplate.send(any(), any(), any())).thenReturn(future)

        // When
        producerService.publishNotification(notification)

        // Then
        verify(kafkaTemplate).send(
            eq("notifications"),
            eq(notification["id"].toString()),
            eq(notification)
        )
    }
}
