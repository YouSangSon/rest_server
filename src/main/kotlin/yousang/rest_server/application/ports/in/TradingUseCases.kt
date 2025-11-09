package yousang.rest_server.application.ports.`in`

import yousang.rest_server.domain.model.*
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * 주문 관리 Use Case
 */
interface PlaceOrderUseCase {
    fun placeOrder(command: yousang.rest_server.application.service.PlaceOrderCommand): Order
    fun cancelOrder(orderId: String, userId: Long): Order
    fun getOrderById(orderId: String): Order?
    fun getOrdersByUser(userId: Long, limit: Int = 100): List<Order>
    fun getOrdersBySymbol(userId: Long, symbol: String, limit: Int = 100): List<Order>
    fun getOrdersByStatus(status: OrderStatus, limit: Int = 100): List<Order>
    fun getOrdersByDateRange(from: LocalDateTime, to: LocalDateTime): List<Order>
}

/**
 * 포트폴리오 관리 Use Case
 */
interface ManagePortfolioUseCase {
    fun getPortfolio(userId: Long): List<Portfolio>
    fun getPortfolioBySymbol(userId: Long, symbol: String): Portfolio?
    fun getTotalPortfolioValue(userId: Long, currentPrices: Map<String, BigDecimal>): BigDecimal
    fun getPortfolioPnL(userId: Long, symbol: String, currentPrice: BigDecimal): BigDecimal
    fun getPortfolioPnLPercentage(userId: Long, symbol: String, currentPrice: BigDecimal): Double
}

/**
 * 거래쌍 조회 Use Case
 */
interface GetTradingPairUseCase {
    fun getAllTradingPairs(): List<TradingPair>
    fun getActiveTradingPairs(): List<TradingPair>
    fun getTradingPairsByExchange(exchange: String): List<TradingPair>
    fun getTradingPair(symbol: String, exchange: String): TradingPair?
    fun updateTradingPairPrice(symbol: String, exchange: String, currentPrice: BigDecimal)
}
