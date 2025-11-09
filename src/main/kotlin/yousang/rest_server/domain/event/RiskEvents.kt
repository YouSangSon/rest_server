package yousang.rest_server.domain.event

import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * 리스크 한도 초과 이벤트
 */
data class RiskLimitExceededEvent(
    val userId: Long,
    val symbol: String,
    val reason: String,
    val timestamp: LocalDateTime
) : BaseDomainEvent() {
    override val eventType: String = "alert.risk.limit_exceeded"
}

/**
 * 손절 트리거 이벤트
 */
data class StopLossTriggeredEvent(
    val userId: Long,
    val symbol: String,
    val currentPrice: BigDecimal,
    val stopLossPrice: BigDecimal,
    val quantity: BigDecimal,
    val timestamp: LocalDateTime
) : BaseDomainEvent() {
    override val eventType: String = "alert.risk.stop_loss"
}

/**
 * 익절 트리거 이벤트
 */
data class TakeProfitTriggeredEvent(
    val userId: Long,
    val symbol: String,
    val currentPrice: BigDecimal,
    val takeProfitPrice: BigDecimal,
    val quantity: BigDecimal,
    val timestamp: LocalDateTime
) : BaseDomainEvent() {
    override val eventType: String = "alert.risk.take_profit"
}

/**
 * 포트폴리오 리스크 경고 이벤트
 */
data class PortfolioRiskWarningEvent(
    val userId: Long,
    val riskPercent: Double,
    val threshold: Double,
    val message: String,
    val timestamp: LocalDateTime
) : BaseDomainEvent() {
    override val eventType: String = "alert.risk.portfolio_warning"
}
