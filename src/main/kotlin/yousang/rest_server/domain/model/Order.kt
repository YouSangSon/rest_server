package yousang.rest_server.domain.model

import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * 주문 도메인 모델
 *
 * 거래소에 제출하는 매수/매도 주문을 나타냅니다.
 */
data class Order(
    val id: Long? = null,
    val orderId: String,                    // 거래소 주문 ID
    val userId: Long,
    val symbol: String,                     // BTC/USDT
    val exchange: String,                   // binance
    val type: OrderType,                    // MARKET, LIMIT, STOP_LOSS
    val side: OrderSide,                    // BUY, SELL
    val price: BigDecimal? = null,          // 지정가 (LIMIT 주문)
    val stopPrice: BigDecimal? = null,      // 손절가 (STOP_LOSS)
    val quantity: BigDecimal,               // 주문 수량
    val executedQty: BigDecimal = BigDecimal.ZERO,  // 체결 수량
    val cummulativeQuoteQty: BigDecimal = BigDecimal.ZERO,  // 체결 금액
    val status: OrderStatus,                // PENDING, FILLED, CANCELLED
    val strategyId: Long? = null,           // 전략 ID (자동매매)
    val timeInForce: TimeInForce = TimeInForce.GTC,
    val clientOrderId: String? = null,      // 클라이언트 주문 ID
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val filledAt: LocalDateTime? = null
) {
    init {
        require(orderId.isNotBlank()) { "주문 ID는 필수입니다" }
        require(symbol.isNotBlank()) { "심볼은 필수입니다" }
        require(quantity > BigDecimal.ZERO) { "주문 수량은 0보다 커야 합니다" }
        require(executedQty >= BigDecimal.ZERO) { "체결 수량은 0 이상이어야 합니다" }
        require(executedQty <= quantity) { "체결 수량은 주문 수량을 초과할 수 없습니다" }

        if (type == OrderType.LIMIT) {
            requireNotNull(price) { "지정가 주문은 가격이 필수입니다" }
        }
        if (type == OrderType.STOP_LOSS || type == OrderType.TAKE_PROFIT) {
            requireNotNull(stopPrice) { "손절/익절 주문은 손절가가 필수입니다" }
        }
    }

    /**
     * 주문이 완전히 체결되었는지 확인합니다.
     */
    fun isFilled(): Boolean = status == OrderStatus.FILLED

    /**
     * 주문이 부분 체결되었는지 확인합니다.
     */
    fun isPartiallyFilled(): Boolean = status == OrderStatus.PARTIALLY_FILLED

    /**
     * 주문이 취소되었는지 확인합니다.
     */
    fun isCancelled(): Boolean = status == OrderStatus.CANCELLED

    /**
     * 주문이 거부되었는지 확인합니다.
     */
    fun isRejected(): Boolean = status == OrderStatus.REJECTED

    /**
     * 남은 주문 수량을 계산합니다.
     */
    fun remainingQuantity(): BigDecimal = quantity - executedQty

    /**
     * 체결률을 계산합니다 (%).
     */
    fun fillRate(): BigDecimal {
        return if (quantity > BigDecimal.ZERO) {
            executedQty.divide(quantity, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal("100"))
        } else BigDecimal.ZERO
    }

    /**
     * 평균 체결 가격을 계산합니다.
     */
    fun averagePrice(): BigDecimal {
        return if (executedQty > BigDecimal.ZERO) {
            cummulativeQuoteQty.divide(executedQty, 8, java.math.RoundingMode.HALF_UP)
        } else BigDecimal.ZERO
    }

    /**
     * 주문 상태를 업데이트합니다.
     */
    fun updateStatus(
        newStatus: OrderStatus,
        newExecutedQty: BigDecimal = executedQty,
        newCummulativeQuoteQty: BigDecimal = cummulativeQuoteQty
    ): Order {
        val filledTime = if (newStatus == OrderStatus.FILLED) LocalDateTime.now() else filledAt
        return copy(
            status = newStatus,
            executedQty = newExecutedQty,
            cummulativeQuoteQty = newCummulativeQuoteQty,
            updatedAt = LocalDateTime.now(),
            filledAt = filledTime
        )
    }

    /**
     * 주문을 취소합니다.
     */
    fun cancel(): Order {
        return copy(status = OrderStatus.CANCELLED, updatedAt = LocalDateTime.now())
    }

    companion object {
        /**
         * 시장가 매수 주문을 생성합니다.
         */
        fun marketBuy(
            orderId: String,
            userId: Long,
            symbol: String,
            exchange: String,
            quantity: BigDecimal,
            strategyId: Long? = null
        ): Order {
            return Order(
                orderId = orderId,
                userId = userId,
                symbol = symbol,
                exchange = exchange,
                type = OrderType.MARKET,
                side = OrderSide.BUY,
                quantity = quantity,
                status = OrderStatus.PENDING,
                strategyId = strategyId
            )
        }

        /**
         * 지정가 매수 주문을 생성합니다.
         */
        fun limitBuy(
            orderId: String,
            userId: Long,
            symbol: String,
            exchange: String,
            price: BigDecimal,
            quantity: BigDecimal,
            strategyId: Long? = null
        ): Order {
            return Order(
                orderId = orderId,
                userId = userId,
                symbol = symbol,
                exchange = exchange,
                type = OrderType.LIMIT,
                side = OrderSide.BUY,
                price = price,
                quantity = quantity,
                status = OrderStatus.PENDING,
                strategyId = strategyId
            )
        }

        /**
         * 시장가 매도 주문을 생성합니다.
         */
        fun marketSell(
            orderId: String,
            userId: Long,
            symbol: String,
            exchange: String,
            quantity: BigDecimal,
            strategyId: Long? = null
        ): Order {
            return Order(
                orderId = orderId,
                userId = userId,
                symbol = symbol,
                exchange = exchange,
                type = OrderType.MARKET,
                side = OrderSide.SELL,
                quantity = quantity,
                status = OrderStatus.PENDING,
                strategyId = strategyId
            )
        }

        /**
         * 손절 주문을 생성합니다.
         */
        fun stopLoss(
            orderId: String,
            userId: Long,
            symbol: String,
            exchange: String,
            stopPrice: BigDecimal,
            quantity: BigDecimal,
            strategyId: Long? = null
        ): Order {
            return Order(
                orderId = orderId,
                userId = userId,
                symbol = symbol,
                exchange = exchange,
                type = OrderType.STOP_LOSS,
                side = OrderSide.SELL,
                stopPrice = stopPrice,
                quantity = quantity,
                status = OrderStatus.PENDING,
                strategyId = strategyId
            )
        }
    }
}

/**
 * 주문 타입
 */
enum class OrderType {
    MARKET,       // 시장가
    LIMIT,        // 지정가
    STOP_LOSS,    // 손절
    TAKE_PROFIT,  // 익절
    STOP_LIMIT    // 손절 지정가
}

/**
 * 주문 방향 (매수/매도)
 */
enum class OrderSide {
    BUY,   // 매수
    SELL   // 매도
}

/**
 * 주문 상태
 */
enum class OrderStatus {
    PENDING,           // 대기 중
    SUBMITTED,         // 제출됨
    PARTIALLY_FILLED,  // 부분 체결
    FILLED,            // 전체 체결
    CANCELLED,         // 취소됨
    REJECTED,          // 거부됨
    EXPIRED            // 만료됨
}

/**
 * 주문 유효 기간
 */
enum class TimeInForce {
    GTC,  // Good Till Cancelled (취소될 때까지 유효)
    IOC,  // Immediate Or Cancel (즉시 체결 또는 취소)
    FOK   // Fill Or Kill (전량 체결 또는 취소)
}
