package yousang.rest_server.domain.event

import yousang.rest_server.domain.model.OrderSide
import yousang.rest_server.domain.model.OrderType
import java.math.BigDecimal

/**
 * 매매 신호 생성 이벤트
 */
data class TradingSignalGeneratedEvent(
    val strategyId: Long,
    val strategyName: String,
    val symbol: String,
    val signal: SignalType,
    val strength: Double,  // 0.0 ~ 1.0
    val reason: String
) : BaseDomainEvent() {
    override val eventType: String = "trading.signal.generated"
}

enum class SignalType {
    BUY,
    SELL,
    HOLD
}

/**
 * 주문 생성 이벤트
 */
data class OrderCreatedEvent(
    val orderId: String,
    val userId: Long,
    val symbol: String,
    val exchange: String,
    val type: OrderType,
    val side: OrderSide,
    val price: BigDecimal?,
    val quantity: BigDecimal,
    val strategyId: Long?
) : BaseDomainEvent() {
    override val eventType: String = "trading.order.created"
}

/**
 * 주문 체결 이벤트
 */
data class OrderFilledEvent(
    val orderId: String,
    val userId: Long,
    val symbol: String,
    val exchange: String,
    val side: OrderSide,
    val executedPrice: BigDecimal,
    val executedQty: BigDecimal,
    val totalAmount: BigDecimal,
    val strategyId: Long?
) : BaseDomainEvent() {
    override val eventType: String = "trading.order.filled"
}

/**
 * 주문 취소 이벤트
 */
data class OrderCancelledEvent(
    val orderId: String,
    val userId: Long,
    val symbol: String,
    val reason: String
) : BaseDomainEvent() {
    override val eventType: String = "trading.order.cancelled"
}

/**
 * 포지션 오픈 이벤트
 */
data class PositionOpenedEvent(
    val userId: Long,
    val symbol: String,
    val side: OrderSide,
    val quantity: BigDecimal,
    val entryPrice: BigDecimal,
    val strategyId: Long?
) : BaseDomainEvent() {
    override val eventType: String = "trading.position.opened"
}

/**
 * 포지션 청산 이벤트
 */
data class PositionClosedEvent(
    val userId: Long,
    val symbol: String,
    val side: OrderSide,
    val quantity: BigDecimal,
    val entryPrice: BigDecimal,
    val exitPrice: BigDecimal,
    val pnl: BigDecimal,
    val pnlPercentage: BigDecimal,
    val reason: String  // take_profit, stop_loss, manual
) : BaseDomainEvent() {
    override val eventType: String = "trading.position.closed"
}
