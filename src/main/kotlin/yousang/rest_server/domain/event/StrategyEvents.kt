package yousang.rest_server.domain.event

import yousang.rest_server.application.service.StrategyType
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * 전략 실행 이벤트
 */
data class StrategyExecutedEvent(
    val strategyId: Long,
    val strategyName: String,
    val userId: Long,
    val ordersGenerated: Int,
    val timestamp: LocalDateTime
) : BaseDomainEvent() {
    override val eventType: String = "trading.strategy.executed"
}

/**
 * 트레이드 신호 생성 이벤트
 */
data class TradeSignalGeneratedEvent(
    val strategyId: Long,
    val strategyName: String,
    val symbol: String,
    val signal: String, // BUY, SELL, HOLD
    val reason: String,
    val price: BigDecimal,
    val timestamp: LocalDateTime
) : BaseDomainEvent() {
    override val eventType: String = "analysis.signal.generated"
}

/**
 * 전략 생성 이벤트
 */
data class StrategyCreatedEvent(
    val strategyId: Long,
    val userId: Long,
    val strategyName: String,
    val strategyType: StrategyType,
    val symbols: List<String>,
    val exchange: String
) : BaseDomainEvent() {
    override val eventType: String = "trading.strategy.created"
}

/**
 * 전략 활성화/비활성화 이벤트
 */
data class StrategyStatusChangedEvent(
    val strategyId: Long,
    val userId: Long,
    val strategyName: String,
    val isActive: Boolean
) : BaseDomainEvent() {
    override val eventType: String = "trading.strategy.status_changed"
}
