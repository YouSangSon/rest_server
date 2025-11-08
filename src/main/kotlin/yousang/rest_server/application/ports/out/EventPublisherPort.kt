package yousang.rest_server.application.ports.out

import yousang.rest_server.domain.event.DomainEvent

/**
 * 이벤트 발행 Port (Outbound Port)
 *
 * Kafka로 도메인 이벤트를 발행합니다.
 */
interface EventPublisherPort {
    /**
     * 단일 이벤트를 발행합니다.
     *
     * @param event 도메인 이벤트
     */
    fun publish(event: DomainEvent)

    /**
     * 여러 이벤트를 일괄 발행합니다.
     *
     * @param events 도메인 이벤트 목록
     */
    fun publishAll(events: List<DomainEvent>)

    /**
     * 특정 토픽으로 이벤트를 발행합니다.
     *
     * @param topic Kafka 토픽 이름
     * @param event 도메인 이벤트
     */
    fun publishToTopic(topic: String, event: DomainEvent)

    /**
     * 파티션 키를 지정하여 이벤트를 발행합니다.
     * (같은 키는 같은 파티션으로 전송되어 순서 보장)
     *
     * @param event 도메인 이벤트
     * @param partitionKey 파티션 키
     */
    fun publishWithKey(event: DomainEvent, partitionKey: String)
}
