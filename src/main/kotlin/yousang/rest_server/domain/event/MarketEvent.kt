package yousang.rest_server.domain.event

import java.math.BigDecimal

/**
 * 시장 가격 업데이트 이벤트
 */
data class MarketPriceUpdatedEvent(
    val symbol: String,
    val exchange: String,
    val price: BigDecimal,
    val volume: BigDecimal,
    val change24h: BigDecimal
) : BaseDomainEvent() {
    override val eventType: String = "trading.market.price"
}

/**
 * 호가창 업데이트 이벤트
 */
data class OrderBookUpdatedEvent(
    val symbol: String,
    val exchange: String,
    val bidPrice: BigDecimal,
    val askPrice: BigDecimal,
    val spread: BigDecimal
) : BaseDomainEvent() {
    override val eventType: String = "trading.market.orderbook"
}

/**
 * 거래 체결 이벤트
 */
data class TradeExecutedEvent(
    val symbol: String,
    val exchange: String,
    val price: BigDecimal,
    val quantity: BigDecimal,
    val isBuyerMaker: Boolean
) : BaseDomainEvent() {
    override val eventType: String = "trading.market.trade"
}

/**
 * 가격 급등락 알림 이벤트
 */
data class PriceAlertEvent(
    val symbol: String,
    val exchange: String,
    val currentPrice: BigDecimal,
    val change24h: BigDecimal,
    val alertType: PriceAlertType
) : BaseDomainEvent() {
    override val eventType: String = "alert.price.threshold"
}

enum class PriceAlertType {
    SURGE,      // 급등
    PLUNGE,     // 급락
    THRESHOLD   // 임계값 도달
}
