package yousang.rest_server.application.service

import kotlinx.coroutines.*
import org.springframework.stereotype.Service
import yousang.rest_server.application.ports.out.EventPublisherPort
import yousang.rest_server.application.ports.out.ExchangeApiPort
import yousang.rest_server.application.ports.out.OrderRepositoryPort
import yousang.rest_server.domain.model.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap

/**
 * 고급 주문 전략 서비스
 *
 * TWAP, VWAP, Iceberg, Trailing Stop 등
 */
@Service
class AdvancedOrderService(
    private val orderRepositoryPort: OrderRepositoryPort,
    private val exchangeApiPorts: List<ExchangeApiPort>,
    private val marketDataService: MarketDataService,
    private val eventPublisherPort: EventPublisherPort
) {

    // 활성 Trailing Stop 주문 추적
    private val activeTrailingStops = ConcurrentHashMap<String, TrailingStopOrder>()

    // 활성 TWAP 주문 추적
    private val activeTWAPOrders = ConcurrentHashMap<String, TWAPOrderContext>()

    // ==================== TWAP (Time-Weighted Average Price) ====================

    /**
     * TWAP 주문 생성 및 실행
     *
     * 대량 주문을 일정 시간에 걸쳐 분할 실행
     */
    suspend fun executeTWAP(
        userId: Long,
        symbol: String,
        exchange: String,
        side: OrderSide,
        totalQuantity: BigDecimal,
        durationMinutes: Int,
        sliceCount: Int = 10
    ): TWAPOrderResult = coroutineScope {
        val orderId = "TWAP_${System.currentTimeMillis()}"
        val sliceQuantity = totalQuantity.divide(sliceCount.toBigDecimal(), 8, RoundingMode.DOWN)
        val intervalMs = (durationMinutes * 60 * 1000) / sliceCount

        val context = TWAPOrderContext(
            orderId = orderId,
            userId = userId,
            symbol = symbol,
            exchange = exchange,
            side = side,
            totalQuantity = totalQuantity,
            sliceQuantity = sliceQuantity,
            sliceCount = sliceCount,
            intervalMs = intervalMs.toLong(),
            startTime = LocalDateTime.now()
        )

        activeTWAPOrders[orderId] = context

        // 비동기로 슬라이스 주문 실행
        val job = launch {
            executeTWAPSlices(context)
        }

        TWAPOrderResult(
            orderId = orderId,
            totalQuantity = totalQuantity,
            sliceCount = sliceCount,
            intervalSeconds = intervalMs / 1000,
            status = "STARTED"
        )
    }

    private suspend fun executeTWAPSlices(context: TWAPOrderContext) {
        val exchangeApi = findExchangeApi(context.exchange)
        val executedOrders = mutableListOf<Order>()

        repeat(context.sliceCount) { i ->
            try {
                // 현재 시장 가격 조회
                val currentPrice = marketDataService.getCurrentPrice(context.symbol, context.exchange)

                // 슬라이스 주문 생성
                val order = Order.create(
                    userId = context.userId,
                    symbol = context.symbol,
                    exchange = context.exchange,
                    type = OrderType.MARKET, // 또는 LIMIT with 현재가
                    side = context.side,
                    quantity = context.sliceQuantity,
                    price = currentPrice
                )

                // 주문 제출
                val saved = orderRepositoryPort.save(order)
                val submitted = exchangeApi.submitOrder(saved)
                executedOrders.add(submitted)

                println("TWAP slice ${i + 1}/${context.sliceCount} executed: ${context.sliceQuantity} @ $currentPrice")

                // 마지막 슬라이스가 아니면 대기
                if (i < context.sliceCount - 1) {
                    delay(context.intervalMs)
                }

            } catch (e: Exception) {
                println("TWAP slice ${i + 1} failed: ${e.message}")
            }
        }

        // TWAP 완료
        activeTWAPOrders.remove(context.orderId)
        println("TWAP order ${context.orderId} completed: ${executedOrders.size}/${context.sliceCount} slices filled")
    }

    // ==================== VWAP (Volume-Weighted Average Price) ====================

    /**
     * VWAP 주문 실행
     *
     * 거래량에 비례하여 주문 분할
     */
    fun executeVWAP(
        userId: Long,
        symbol: String,
        exchange: String,
        side: OrderSide,
        totalQuantity: BigDecimal,
        targetVWAP: BigDecimal? = null
    ): Order {
        // VWAP 계산
        val candles = marketDataService.getRecentCandles(symbol, exchange, CandleInterval.FIVE_MINUTES, 50)
        val vwap = marketDataService.calculateVWAP(candles)

        val currentVWAP = vwap.lastOrNull() ?: throw IllegalStateException("Cannot calculate VWAP")

        // VWAP보다 유리한 가격으로 주문
        val limitPrice = when (side) {
            OrderSide.BUY -> currentVWAP * BigDecimal("0.999") // VWAP보다 0.1% 낮게
            OrderSide.SELL -> currentVWAP * BigDecimal("1.001") // VWAP보다 0.1% 높게
        }

        return Order.create(
            userId = userId,
            symbol = symbol,
            exchange = exchange,
            type = OrderType.LIMIT,
            side = side,
            quantity = totalQuantity,
            price = limitPrice
        )
    }

    // ==================== Iceberg Order (빙산 주문) ====================

    /**
     * Iceberg 주문 실행
     *
     * 대량 주문을 숨기고 일부만 공개
     */
    suspend fun executeIceberg(
        userId: Long,
        symbol: String,
        exchange: String,
        side: OrderSide,
        totalQuantity: BigDecimal,
        visibleQuantity: BigDecimal,
        limitPrice: BigDecimal
    ): IcebergOrderResult = coroutineScope {
        val orderId = "ICEBERG_${System.currentTimeMillis()}"
        var remaining = totalQuantity
        val executedOrders = mutableListOf<Order>()

        while (remaining > BigDecimal.ZERO) {
            val orderQuantity = if (remaining > visibleQuantity) visibleQuantity else remaining

            try {
                val order = Order.create(
                    userId = userId,
                    symbol = symbol,
                    exchange = exchange,
                    type = OrderType.LIMIT,
                    side = side,
                    quantity = orderQuantity,
                    price = limitPrice
                )

                val exchangeApi = findExchangeApi(exchange)
                val saved = orderRepositoryPort.save(order)
                val submitted = exchangeApi.submitOrder(saved)

                executedOrders.add(submitted)
                remaining -= orderQuantity

                // 주문이 체결될 때까지 대기 (실제로는 WebSocket으로 체결 확인)
                delay(1000)

            } catch (e: Exception) {
                println("Iceberg order slice failed: ${e.message}")
                break
            }
        }

        IcebergOrderResult(
            orderId = orderId,
            totalQuantity = totalQuantity,
            visibleQuantity = visibleQuantity,
            executedQuantity = totalQuantity - remaining,
            status = if (remaining == BigDecimal.ZERO) "COMPLETED" else "PARTIAL"
        )
    }

    // ==================== Trailing Stop ====================

    /**
     * Trailing Stop 주문 생성
     *
     * 가격이 유리하게 움직이면 손절가도 따라 이동
     */
    fun createTrailingStop(
        userId: Long,
        symbol: String,
        exchange: String,
        quantity: BigDecimal,
        trailingPercent: Double = 3.0 // 3%
    ): TrailingStopOrder {
        val currentPrice = marketDataService.getCurrentPrice(symbol, exchange)
        val stopPrice = currentPrice * (BigDecimal.ONE - (trailingPercent / 100).toBigDecimal())

        val order = TrailingStopOrder(
            orderId = "TRAILING_${System.currentTimeMillis()}",
            userId = userId,
            symbol = symbol,
            exchange = exchange,
            quantity = quantity,
            trailingPercent = trailingPercent,
            highestPrice = currentPrice,
            currentStopPrice = stopPrice,
            isActive = true
        )

        activeTrailingStops[order.orderId] = order
        return order
    }

    /**
     * Trailing Stop 업데이트 (가격 변동 시 호출)
     */
    fun updateTrailingStops(symbol: String, exchange: String, currentPrice: BigDecimal) {
        activeTrailingStops.values
            .filter { it.symbol == symbol && it.exchange == exchange && it.isActive }
            .forEach { order ->
                // 새로운 최고가 갱신
                if (currentPrice > order.highestPrice) {
                    val newStopPrice = currentPrice * (BigDecimal.ONE - (order.trailingPercent / 100).toBigDecimal())

                    val updated = order.copy(
                        highestPrice = currentPrice,
                        currentStopPrice = newStopPrice
                    )

                    activeTrailingStops[order.orderId] = updated
                    println("Trailing stop updated: ${order.symbol} - New stop: $newStopPrice (High: $currentPrice)")
                }

                // 손절가 도달 체크
                if (currentPrice <= order.currentStopPrice) {
                    triggerTrailingStop(order, currentPrice)
                }
            }
    }

    /**
     * Trailing Stop 트리거 (손절 실행)
     */
    private fun triggerTrailingStop(order: TrailingStopOrder, currentPrice: BigDecimal) {
        try {
            println("Trailing stop triggered: ${order.symbol} @ $currentPrice")

            // 시장가 매도 주문 생성
            val sellOrder = Order.create(
                userId = order.userId,
                symbol = order.symbol,
                exchange = order.exchange,
                type = OrderType.MARKET,
                side = OrderSide.SELL,
                quantity = order.quantity
            )

            val exchangeApi = findExchangeApi(order.exchange)
            val saved = orderRepositoryPort.save(sellOrder)
            exchangeApi.submitOrder(saved)

            // Trailing Stop 비활성화
            activeTrailingStops[order.orderId] = order.copy(isActive = false)

        } catch (e: Exception) {
            println("Failed to trigger trailing stop: ${e.message}")
        }
    }

    // ==================== Kelly Criterion Position Sizing ====================

    /**
     * Kelly Criterion 기반 최적 포지션 크기 계산
     */
    fun calculateKellyPosition(
        winRate: Double,
        avgWinPercent: Double,
        avgLossPercent: Double,
        totalCapital: BigDecimal
    ): BigDecimal {
        // Kelly % = (Win Rate * Avg Win - (1 - Win Rate) * Avg Loss) / Avg Win
        val winProb = winRate / 100
        val loseProb = 1 - winProb

        val kelly = (winProb * avgWinPercent - loseProb * avgLossPercent) / avgWinPercent

        // Kelly의 절반 사용 (안전하게)
        val fractionalKelly = kelly * 0.5

        // 0~25% 사이로 제한
        val clampedKelly = fractionalKelly.coerceIn(0.0, 0.25)

        return totalCapital * clampedKelly.toBigDecimal()
    }

    // ==================== Helper Methods ====================

    private fun findExchangeApi(exchange: String): ExchangeApiPort {
        return exchangeApiPorts.find { it.getExchangeName().equals(exchange, ignoreCase = true) }
            ?: throw IllegalArgumentException("Exchange API not found: $exchange")
    }

    private fun Double.toBigDecimal(): BigDecimal = BigDecimal.valueOf(this)
}

// ==================== Result Models ====================

data class TWAPOrderContext(
    val orderId: String,
    val userId: Long,
    val symbol: String,
    val exchange: String,
    val side: OrderSide,
    val totalQuantity: BigDecimal,
    val sliceQuantity: BigDecimal,
    val sliceCount: Int,
    val intervalMs: Long,
    val startTime: LocalDateTime
)

data class TWAPOrderResult(
    val orderId: String,
    val totalQuantity: BigDecimal,
    val sliceCount: Int,
    val intervalSeconds: Long,
    val status: String
)

data class IcebergOrderResult(
    val orderId: String,
    val totalQuantity: BigDecimal,
    val visibleQuantity: BigDecimal,
    val executedQuantity: BigDecimal,
    val status: String
)

data class TrailingStopOrder(
    val orderId: String,
    val userId: Long,
    val symbol: String,
    val exchange: String,
    val quantity: BigDecimal,
    val trailingPercent: Double,
    val highestPrice: BigDecimal,
    val currentStopPrice: BigDecimal,
    val isActive: Boolean
)
