package yousang.rest_server.domain.event

import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * 손익 실현 이벤트
 */
data class PnLRealizedEvent(
    val userId: Long,
    val symbol: String,
    val realizedPnL: BigDecimal,
    val sellPrice: BigDecimal,
    val sellQty: BigDecimal,
    val timestamp: LocalDateTime = LocalDateTime.now()
) : BaseDomainEvent() {
    override val eventType: String = "trading.pnl.realized"
}

/**
 * 포지션 오픈 이벤트
 */
data class PositionOpenedEvent(
    val userId: Long,
    val symbol: String,
    val quantity: BigDecimal,
    val price: BigDecimal,
    val timestamp: LocalDateTime = LocalDateTime.now()
) : BaseDomainEvent() {
    override val eventType: String = "trading.position.opened"
}

/**
 * 포지션 클로즈 이벤트
 */
data class PositionClosedEvent(
    val userId: Long,
    val symbol: String,
    val quantity: BigDecimal,
    val exitPrice: BigDecimal,
    val pnl: BigDecimal,
    val timestamp: LocalDateTime = LocalDateTime.now()
) : BaseDomainEvent() {
    override val eventType: String = "trading.position.closed"
}
