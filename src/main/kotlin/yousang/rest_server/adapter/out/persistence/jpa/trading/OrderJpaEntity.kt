package yousang.rest_server.adapter.out.persistence.jpa.trading

import jakarta.persistence.*
import yousang.rest_server.domain.model.*
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * 주문 JPA Entity
 */
@Entity
@Table(name = "orders")
class OrderJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "order_id", nullable = false, unique = true, length = 100)
    val orderId: String,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(nullable = false, length = 20)
    val symbol: String,

    @Column(nullable = false, length = 50)
    val exchange: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false, length = 20)
    val type: OrderType,

    @Enumerated(EnumType.STRING)
    @Column(name = "order_side", nullable = false, length = 10)
    val side: OrderSide,

    @Column(precision = 20, scale = 8)
    val price: BigDecimal? = null,

    @Column(name = "stop_price", precision = 20, scale = 8)
    val stopPrice: BigDecimal? = null,

    @Column(nullable = false, precision = 20, scale = 8)
    val quantity: BigDecimal,

    @Column(name = "executed_qty", precision = 20, scale = 8)
    val executedQty: BigDecimal = BigDecimal.ZERO,

    @Column(name = "cummulative_quote_qty", precision = 20, scale = 8)
    val cummulativeQuoteQty: BigDecimal = BigDecimal.ZERO,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val status: OrderStatus,

    @Column(name = "strategy_id")
    val strategyId: Long? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "time_in_force", length = 10)
    val timeInForce: TimeInForce = TimeInForce.GTC,

    @Column(name = "client_order_id", length = 100)
    val clientOrderId: String? = null,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "filled_at")
    val filledAt: LocalDateTime? = null
) {
    /**
     * JPA Entity를 Domain Model로 변환
     */
    fun toDomain(): Order {
        return Order(
            id = id,
            orderId = orderId,
            userId = userId,
            symbol = symbol,
            exchange = exchange,
            type = type,
            side = side,
            price = price,
            stopPrice = stopPrice,
            quantity = quantity,
            executedQty = executedQty,
            cummulativeQuoteQty = cummulativeQuoteQty,
            status = status,
            strategyId = strategyId,
            timeInForce = timeInForce,
            clientOrderId = clientOrderId,
            createdAt = createdAt,
            updatedAt = updatedAt,
            filledAt = filledAt
        )
    }

    companion object {
        /**
         * Domain Model을 JPA Entity로 변환
         */
        fun fromDomain(order: Order): OrderJpaEntity {
            return OrderJpaEntity(
                id = order.id,
                orderId = order.orderId,
                userId = order.userId,
                symbol = order.symbol,
                exchange = order.exchange,
                type = order.type,
                side = order.side,
                price = order.price,
                stopPrice = order.stopPrice,
                quantity = order.quantity,
                executedQty = order.executedQty,
                cummulativeQuoteQty = order.cummulativeQuoteQty,
                status = order.status,
                strategyId = order.strategyId,
                timeInForce = order.timeInForce,
                clientOrderId = order.clientOrderId,
                createdAt = order.createdAt,
                updatedAt = order.updatedAt,
                filledAt = order.filledAt
            )
        }
    }
}
