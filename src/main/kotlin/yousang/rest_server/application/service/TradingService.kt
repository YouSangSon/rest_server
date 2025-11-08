package yousang.rest_server.application.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import yousang.rest_server.application.ports.`in`.*
import yousang.rest_server.application.ports.out.*
import yousang.rest_server.domain.event.*
import yousang.rest_server.domain.model.*
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * 트레이딩 서비스
 *
 * 주문, 포트폴리오, 거래쌍 관리 통합 서비스
 */
@Service
@Transactional
class TradingService(
    private val orderRepositoryPort: OrderRepositoryPort,
    private val portfolioRepositoryPort: PortfolioRepositoryPort,
    private val tradingPairRepositoryPort: TradingPairRepositoryPort,
    private val exchangeApiPort: List<ExchangeApiPort>,
    private val eventPublisherPort: EventPublisherPort
) : PlaceOrderUseCase, ManagePortfolioUseCase, GetTradingPairUseCase {

    // ==================== Order Management ====================

    override fun placeOrder(command: PlaceOrderCommand): Order {
        // 거래쌍 검증
        val tradingPair = tradingPairRepositoryPort.findBySymbolAndExchange(command.symbol, command.exchange)
            ?: throw IllegalArgumentException("Trading pair not found: ${command.symbol} on ${command.exchange}")

        if (!tradingPair.isActive) {
            throw IllegalStateException("Trading pair is not active: ${command.symbol}")
        }

        // 주문 수량 검증
        if (command.quantity < tradingPair.minOrderSize) {
            throw IllegalArgumentException("Order quantity ${command.quantity} is below minimum ${tradingPair.minOrderSize}")
        }

        // 포트폴리오 조회 (매도시 잔고 확인)
        if (command.side == OrderSide.SELL) {
            val portfolio = portfolioRepositoryPort.findByUserIdAndSymbol(command.userId, command.symbol)
            if (portfolio == null || portfolio.quantity < command.quantity) {
                throw IllegalArgumentException("Insufficient balance for SELL order")
            }
        }

        // 주문 생성
        val order = Order.create(
            userId = command.userId,
            symbol = command.symbol,
            exchange = command.exchange,
            type = command.type,
            side = command.side,
            quantity = command.quantity,
            price = command.price,
            stopPrice = command.stopPrice,
            timeInForce = command.timeInForce,
            strategyId = command.strategyId
        )

        // 주문 저장
        val saved = orderRepositoryPort.save(order)

        // 거래소에 주문 제출
        try {
            val exchangeApi = findExchangeApi(command.exchange)
            val submittedOrder = exchangeApi.submitOrder(saved)
            val updated = orderRepositoryPort.save(submittedOrder)

            // 이벤트 발행
            eventPublisherPort.publish(
                OrderSubmittedEvent(
                    orderId = updated.orderId,
                    userId = updated.userId,
                    symbol = updated.symbol,
                    exchange = updated.exchange,
                    side = updated.side,
                    quantity = updated.quantity,
                    price = updated.price
                )
            )

            return updated
        } catch (e: Exception) {
            // 주문 실패 처리
            val failed = saved.fail(e.message ?: "Order submission failed")
            orderRepositoryPort.save(failed)

            eventPublisherPort.publish(
                OrderFailedEvent(
                    orderId = failed.orderId,
                    userId = failed.userId,
                    symbol = failed.symbol,
                    reason = e.message ?: "Unknown error"
                )
            )

            throw e
        }
    }

    override fun cancelOrder(orderId: String, userId: Long): Order {
        val order = orderRepositoryPort.findByOrderId(orderId)
            ?: throw IllegalArgumentException("Order not found: $orderId")

        if (order.userId != userId) {
            throw IllegalArgumentException("Order does not belong to user")
        }

        if (!order.canCancel()) {
            throw IllegalStateException("Order cannot be cancelled: current status is ${order.status}")
        }

        // 거래소에 취소 요청
        val exchangeApi = findExchangeApi(order.exchange)
        exchangeApi.cancelOrder(orderId)

        // 주문 상태 업데이트
        val cancelled = order.cancel()
        val updated = orderRepositoryPort.save(cancelled)

        // 이벤트 발행
        eventPublisherPort.publish(
            OrderCancelledEvent(
                orderId = updated.orderId,
                userId = updated.userId,
                symbol = updated.symbol
            )
        )

        return updated
    }

    override fun getOrderById(orderId: String): Order? {
        return orderRepositoryPort.findByOrderId(orderId)
    }

    override fun getOrdersByUser(userId: Long, limit: Int): List<Order> {
        return orderRepositoryPort.findByUserId(userId, limit)
    }

    override fun getOrdersBySymbol(userId: Long, symbol: String, limit: Int): List<Order> {
        return orderRepositoryPort.findByUserIdAndSymbol(userId, symbol, limit)
    }

    override fun getOrdersByStatus(status: OrderStatus, limit: Int): List<Order> {
        return orderRepositoryPort.findByStatus(status, limit)
    }

    override fun getOrdersByDateRange(from: LocalDateTime, to: LocalDateTime): List<Order> {
        return orderRepositoryPort.findByDateRange(from, to)
    }

    /**
     * 주문 체결 처리 (Kafka 이벤트 또는 WebSocket에서 호출)
     */
    fun processOrderFilled(orderId: String, executedPrice: BigDecimal, executedQty: BigDecimal) {
        val order = orderRepositoryPort.findByOrderId(orderId)
            ?: throw IllegalArgumentException("Order not found: $orderId")

        // 주문 상태 업데이트
        val filled = order.fill(executedQty, executedPrice)
        val updated = orderRepositoryPort.save(filled)

        // 포트폴리오 업데이트
        updatePortfolioFromOrder(updated, executedQty, executedPrice)

        // 이벤트 발행
        eventPublisherPort.publish(
            OrderFilledEvent(
                orderId = updated.orderId,
                userId = updated.userId,
                symbol = updated.symbol,
                side = updated.side,
                executedPrice = executedPrice,
                executedQty = executedQty,
                totalExecutedQty = updated.executedQty
            )
        )
    }

    // ==================== Portfolio Management ====================

    override fun getPortfolio(userId: Long): List<Portfolio> {
        return portfolioRepositoryPort.findByUserId(userId)
    }

    override fun getPortfolioBySymbol(userId: Long, symbol: String): Portfolio? {
        return portfolioRepositoryPort.findByUserIdAndSymbol(userId, symbol)
    }

    override fun getTotalPortfolioValue(userId: Long, currentPrices: Map<String, BigDecimal>): BigDecimal {
        val portfolios = portfolioRepositoryPort.findByUserId(userId)
        return portfolios.sumOf { portfolio ->
            val currentPrice = currentPrices[portfolio.symbol] ?: BigDecimal.ZERO
            portfolio.quantity * currentPrice
        }
    }

    override fun getPortfolioPnL(userId: Long, symbol: String, currentPrice: BigDecimal): BigDecimal {
        val portfolio = portfolioRepositoryPort.findByUserIdAndSymbol(userId, symbol)
            ?: return BigDecimal.ZERO
        return portfolio.calculatePnL(currentPrice)
    }

    override fun getPortfolioPnLPercentage(userId: Long, symbol: String, currentPrice: BigDecimal): Double {
        val portfolio = portfolioRepositoryPort.findByUserIdAndSymbol(userId, symbol)
            ?: return 0.0
        return portfolio.calculatePnLPercentage(currentPrice)
    }

    private fun updatePortfolioFromOrder(order: Order, executedQty: BigDecimal, executedPrice: BigDecimal) {
        val existing = portfolioRepositoryPort.findByUserIdAndSymbol(order.userId, order.symbol)

        val updated = if (existing == null) {
            // 신규 포지션 생성 (BUY만 가능)
            if (order.side != OrderSide.BUY) {
                throw IllegalStateException("Cannot create position with SELL order")
            }
            Portfolio.create(
                userId = order.userId,
                symbol = order.symbol,
                exchange = order.exchange,
                quantity = executedQty,
                avgBuyPrice = executedPrice
            )
        } else {
            // 기존 포지션 업데이트
            when (order.side) {
                OrderSide.BUY -> existing.addPosition(executedQty, executedPrice)
                OrderSide.SELL -> {
                    val pnl = existing.calculatePnL(executedPrice)
                    val updated = existing.reducePosition(executedQty, executedPrice)

                    // 실현 손익 이벤트 발행
                    eventPublisherPort.publish(
                        PnLRealizedEvent(
                            userId = order.userId,
                            symbol = order.symbol,
                            realizedPnL = pnl,
                            sellPrice = executedPrice,
                            sellQty = executedQty
                        )
                    )

                    updated
                }
            }
        }

        portfolioRepositoryPort.save(updated)
    }

    // ==================== Trading Pair Management ====================

    override fun getAllTradingPairs(): List<TradingPair> {
        return tradingPairRepositoryPort.findAll()
    }

    override fun getActiveTradingPairs(): List<TradingPair> {
        return tradingPairRepositoryPort.findByIsActive(true)
    }

    override fun getTradingPairsByExchange(exchange: String): List<TradingPair> {
        return tradingPairRepositoryPort.findByExchange(exchange)
    }

    override fun getTradingPair(symbol: String, exchange: String): TradingPair? {
        return tradingPairRepositoryPort.findBySymbolAndExchange(symbol, exchange)
    }

    override fun updateTradingPairPrice(symbol: String, exchange: String, currentPrice: BigDecimal) {
        val tradingPair = tradingPairRepositoryPort.findBySymbolAndExchange(symbol, exchange)
            ?: throw IllegalArgumentException("Trading pair not found: $symbol on $exchange")

        val updated = tradingPair.updatePrice(currentPrice)
        tradingPairRepositoryPort.save(updated)

        // 가격 업데이트 이벤트 발행
        eventPublisherPort.publish(
            PriceUpdatedEvent(
                symbol = symbol,
                exchange = exchange,
                price = currentPrice,
                timestamp = LocalDateTime.now()
            )
        )
    }

    // ==================== Helper Methods ====================

    private fun findExchangeApi(exchange: String): ExchangeApiPort {
        return exchangeApiPort.find { it.getExchangeName().equals(exchange, ignoreCase = true) }
            ?: throw IllegalArgumentException("Exchange API not found: $exchange")
    }
}

/**
 * 주문 생성 커맨드
 */
data class PlaceOrderCommand(
    val userId: Long,
    val symbol: String,
    val exchange: String,
    val type: OrderType,
    val side: OrderSide,
    val quantity: BigDecimal,
    val price: BigDecimal? = null,
    val stopPrice: BigDecimal? = null,
    val timeInForce: TimeInForce = TimeInForce.GTC,
    val strategyId: Long? = null
)
