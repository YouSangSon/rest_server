package yousang.rest_server.domain.event

import java.time.LocalDateTime

/**
 * 기본 도메인 이벤트
 *
 * 모든 이벤트의 기본 인터페이스
 */
interface DomainEvent {
    val eventId: String
    val occurredAt: LocalDateTime
    val eventType: String
}

/**
 * 추상 도메인 이벤트
 */
abstract class BaseDomainEvent : DomainEvent {
    override val eventId: String = java.util.UUID.randomUUID().toString()
    override val occurredAt: LocalDateTime = LocalDateTime.now()
}
