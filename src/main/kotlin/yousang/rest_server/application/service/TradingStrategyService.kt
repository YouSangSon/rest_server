package yousang.rest_server.application.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import yousang.rest_server.application.ports.`in`.*
import yousang.rest_server.application.ports.out.*
import yousang.rest_server.domain.event.StrategyExecutedEvent
import yousang.rest_server.domain.event.TradeSignalGeneratedEvent
import yousang.rest_server.domain.model.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime

/**
 * 트레이딩 전략 서비스
 *
 * 다양한 트레이딩 전략 실행 및 관리
 */
@Service
@Transactional
class TradingStrategyService(
    private val tradingStrategyRepositoryPort: TradingStrategyRepositoryPort,
    private val marketDataService: MarketDataService,
    private val newsService: NewsService,
    private val tradingService: TradingService,
    private val riskManagementService: RiskManagementService,
    private val eventPublisherPort: EventPublisherPort
) : ExecuteTradingStrategyUseCase, ManageTradingStrategyUseCase {

    // ==================== Strategy Execution ====================

    override fun executeStrategy(strategyId: Long, userId: Long): List<Order> {
        val strategy = tradingStrategyRepositoryPort.findById(strategyId)
            ?: throw IllegalArgumentException("Strategy not found: $strategyId")

        if (!strategy.isActive) {
            throw IllegalStateException("Strategy is not active: ${strategy.name}")
        }

        if (strategy.userId != userId) {
            throw IllegalArgumentException("Strategy does not belong to user")
        }

        // 전략 타입에 따라 실행
        val orders = when (strategy.strategyType) {
            StrategyType.MOMENTUM -> executeMomentumStrategy(strategy)
            StrategyType.MEAN_REVERSION -> executeMeanReversionStrategy(strategy)
            StrategyType.SENTIMENT_BASED -> executeSentimentStrategy(strategy)
            StrategyType.GRID_TRADING -> executeGridTradingStrategy(strategy)
            StrategyType.DCA -> executeDCAStrategy(strategy)
            StrategyType.ARBITRAGE -> executeArbitrageStrategy(strategy)
            StrategyType.CUSTOM -> executeCustomStrategy(strategy)
        }

        // 이벤트 발행
        eventPublisherPort.publish(
            StrategyExecutedEvent(
                strategyId = strategy.id!!,
                strategyName = strategy.name,
                userId = userId,
                ordersGenerated = orders.size,
                timestamp = LocalDateTime.now()
            )
        )

        return orders
    }

    override fun executeAllActiveStrategies(userId: Long): Map<Long, List<Order>> {
        val strategies = tradingStrategyRepositoryPort.findByUserIdAndIsActive(userId, true)
        return strategies.associate { strategy ->
            strategy.id!! to try {
                executeStrategy(strategy.id!!, userId)
            } catch (e: Exception) {
                println("Failed to execute strategy ${strategy.name}: ${e.message}")
                emptyList()
            }
        }
    }

    // ==================== Momentum Strategy ====================

    private fun executeMomentumStrategy(strategy: TradingStrategy): List<Order> {
        val orders = mutableListOf<Order>()
        val symbols = strategy.symbols

        symbols.forEach { symbol ->
            try {
                val candles = marketDataService.getRecentCandles(symbol, strategy.exchange, CandleInterval.ONE_HOUR, 50)
                if (candles.size < 20) return@forEach

                val sma20 = marketDataService.calculateSMA(candles, 20)
                val sma50 = marketDataService.calculateSMA(candles, 50)
                val rsi = marketDataService.calculateRSI(candles, 14)

                if (sma20.isEmpty() || sma50.isEmpty() || rsi.isEmpty()) return@forEach

                val currentPrice = candles.last().close
                val latestSMA20 = sma20.last()
                val latestSMA50 = sma50.last()
                val latestRSI = rsi.last()

                // 매수 신호: SMA20이 SMA50을 상향 돌파 + RSI < 70
                if (latestSMA20 > latestSMA50 && latestRSI < BigDecimal(70)) {
                    val quantity = calculatePositionSize(strategy, currentPrice)
                    if (riskManagementService.canOpenPosition(strategy.userId, symbol, quantity, currentPrice)) {
                        val order = tradingService.placeOrder(
                            PlaceOrderCommand(
                                userId = strategy.userId,
                                symbol = symbol,
                                exchange = strategy.exchange,
                                type = OrderType.MARKET,
                                side = OrderSide.BUY,
                                quantity = quantity,
                                strategyId = strategy.id
                            )
                        )
                        orders.add(order)

                        publishTradeSignal(strategy, symbol, "BUY", "Momentum bullish", currentPrice)
                    }
                }

                // 매도 신호: SMA20이 SMA50을 하향 돌파 OR RSI > 80
                if (latestSMA20 < latestSMA50 || latestRSI > BigDecimal(80)) {
                    val portfolio = tradingService.getPortfolioBySymbol(strategy.userId, symbol)
                    if (portfolio != null && portfolio.quantity > BigDecimal.ZERO) {
                        val order = tradingService.placeOrder(
                            PlaceOrderCommand(
                                userId = strategy.userId,
                                symbol = symbol,
                                exchange = strategy.exchange,
                                type = OrderType.MARKET,
                                side = OrderSide.SELL,
                                quantity = portfolio.quantity,
                                strategyId = strategy.id
                            )
                        )
                        orders.add(order)

                        publishTradeSignal(strategy, symbol, "SELL", "Momentum bearish", currentPrice)
                    }
                }
            } catch (e: Exception) {
                println("Error executing momentum strategy for $symbol: ${e.message}")
            }
        }

        return orders
    }

    // ==================== Mean Reversion Strategy ====================

    private fun executeMeanReversionStrategy(strategy: TradingStrategy): List<Order> {
        val orders = mutableListOf<Order>()
        val symbols = strategy.symbols

        symbols.forEach { symbol ->
            try {
                val candles = marketDataService.getRecentCandles(symbol, strategy.exchange, CandleInterval.ONE_HOUR, 30)
                if (candles.size < 20) return@forEach

                val bollingerBands = marketDataService.calculateBollingerBands(candles, 20, 2.0)
                if (bollingerBands.isEmpty()) return@forEach

                val currentPrice = candles.last().close
                val latestBB = bollingerBands.last()

                // 매수 신호: 가격이 하단 밴드 아래
                if (currentPrice < latestBB.lower) {
                    val quantity = calculatePositionSize(strategy, currentPrice)
                    if (riskManagementService.canOpenPosition(strategy.userId, symbol, quantity, currentPrice)) {
                        val order = tradingService.placeOrder(
                            PlaceOrderCommand(
                                userId = strategy.userId,
                                symbol = symbol,
                                exchange = strategy.exchange,
                                type = OrderType.LIMIT,
                                side = OrderSide.BUY,
                                quantity = quantity,
                                price = currentPrice,
                                strategyId = strategy.id
                            )
                        )
                        orders.add(order)

                        publishTradeSignal(strategy, symbol, "BUY", "Price below lower BB", currentPrice)
                    }
                }

                // 매도 신호: 가격이 상단 밴드 위
                if (currentPrice > latestBB.upper) {
                    val portfolio = tradingService.getPortfolioBySymbol(strategy.userId, symbol)
                    if (portfolio != null && portfolio.quantity > BigDecimal.ZERO) {
                        val order = tradingService.placeOrder(
                            PlaceOrderCommand(
                                userId = strategy.userId,
                                symbol = symbol,
                                exchange = strategy.exchange,
                                type = OrderType.LIMIT,
                                side = OrderSide.SELL,
                                quantity = portfolio.quantity,
                                price = currentPrice,
                                strategyId = strategy.id
                            )
                        )
                        orders.add(order)

                        publishTradeSignal(strategy, symbol, "SELL", "Price above upper BB", currentPrice)
                    }
                }
            } catch (e: Exception) {
                println("Error executing mean reversion strategy for $symbol: ${e.message}")
            }
        }

        return orders
    }

    // ==================== Sentiment-Based Strategy ====================

    private fun executeSentimentStrategy(strategy: TradingStrategy): List<Order> {
        val orders = mutableListOf<Order>()
        val symbols = strategy.symbols

        symbols.forEach { symbol ->
            try {
                val sentiment = newsService.getAggregateSentiment(symbol, 24)
                val currentPrice = marketDataService.getCurrentPrice(symbol, strategy.exchange)

                // 강한 긍정 감성 + 충분한 기사 수
                if (sentiment.sentimentType == SentimentType.POSITIVE &&
                    sentiment.averageScore > 0.5 &&
                    sentiment.totalArticles >= 5) {

                    val quantity = calculatePositionSize(strategy, currentPrice)
                    if (riskManagementService.canOpenPosition(strategy.userId, symbol, quantity, currentPrice)) {
                        val order = tradingService.placeOrder(
                            PlaceOrderCommand(
                                userId = strategy.userId,
                                symbol = symbol,
                                exchange = strategy.exchange,
                                type = OrderType.MARKET,
                                side = OrderSide.BUY,
                                quantity = quantity,
                                strategyId = strategy.id
                            )
                        )
                        orders.add(order)

                        publishTradeSignal(
                            strategy, symbol, "BUY",
                            "Positive sentiment: ${sentiment.averageScore} (${sentiment.totalArticles} articles)",
                            currentPrice
                        )
                    }
                }

                // 강한 부정 감성
                if (sentiment.sentimentType == SentimentType.NEGATIVE &&
                    sentiment.averageScore < -0.5 &&
                    sentiment.totalArticles >= 5) {

                    val portfolio = tradingService.getPortfolioBySymbol(strategy.userId, symbol)
                    if (portfolio != null && portfolio.quantity > BigDecimal.ZERO) {
                        val order = tradingService.placeOrder(
                            PlaceOrderCommand(
                                userId = strategy.userId,
                                symbol = symbol,
                                exchange = strategy.exchange,
                                type = OrderType.MARKET,
                                side = OrderSide.SELL,
                                quantity = portfolio.quantity,
                                strategyId = strategy.id
                            )
                        )
                        orders.add(order)

                        publishTradeSignal(
                            strategy, symbol, "SELL",
                            "Negative sentiment: ${sentiment.averageScore} (${sentiment.totalArticles} articles)",
                            currentPrice
                        )
                    }
                }
            } catch (e: Exception) {
                println("Error executing sentiment strategy for $symbol: ${e.message}")
            }
        }

        return orders
    }

    // ==================== Grid Trading Strategy ====================

    private fun executeGridTradingStrategy(strategy: TradingStrategy): List<Order> {
        // Grid trading: 가격 범위를 여러 구간으로 나누어 각 구간에서 매수/매도
        // 추후 구현
        return emptyList()
    }

    // ==================== DCA (Dollar Cost Averaging) Strategy ====================

    private fun executeDCAStrategy(strategy: TradingStrategy): List<Order> {
        val orders = mutableListOf<Order>()
        val symbols = strategy.symbols

        symbols.forEach { symbol ->
            try {
                val currentPrice = marketDataService.getCurrentPrice(symbol, strategy.exchange)
                val fixedAmount = BigDecimal(100) // 고정 금액 (설정 가능하도록 개선 필요)
                val quantity = fixedAmount.divide(currentPrice, 8, RoundingMode.DOWN)

                if (riskManagementService.canOpenPosition(strategy.userId, symbol, quantity, currentPrice)) {
                    val order = tradingService.placeOrder(
                        PlaceOrderCommand(
                            userId = strategy.userId,
                            symbol = symbol,
                            exchange = strategy.exchange,
                            type = OrderType.MARKET,
                            side = OrderSide.BUY,
                            quantity = quantity,
                            strategyId = strategy.id
                        )
                    )
                    orders.add(order)

                    publishTradeSignal(strategy, symbol, "BUY", "DCA periodic buy", currentPrice)
                }
            } catch (e: Exception) {
                println("Error executing DCA strategy for $symbol: ${e.message}")
            }
        }

        return orders
    }

    // ==================== Arbitrage Strategy ====================

    private fun executeArbitrageStrategy(strategy: TradingStrategy): List<Order> {
        // 거래소간 차익거래 (추후 구현)
        return emptyList()
    }

    // ==================== Custom Strategy ====================

    private fun executeCustomStrategy(strategy: TradingStrategy): List<Order> {
        // 사용자 정의 전략 (추후 구현)
        return emptyList()
    }

    // ==================== Strategy Management ====================

    override fun createStrategy(command: CreateStrategyCommand): TradingStrategy {
        val strategy = TradingStrategy.create(
            userId = command.userId,
            name = command.name,
            strategyType = command.strategyType,
            symbols = command.symbols,
            exchange = command.exchange,
            parameters = command.parameters
        )
        return tradingStrategyRepositoryPort.save(strategy)
    }

    override fun updateStrategy(strategyId: Long, command: UpdateStrategyCommand): TradingStrategy {
        val strategy = tradingStrategyRepositoryPort.findById(strategyId)
            ?: throw IllegalArgumentException("Strategy not found: $strategyId")

        val updated = strategy.copy(
            name = command.name ?: strategy.name,
            isActive = command.isActive ?: strategy.isActive,
            symbols = command.symbols ?: strategy.symbols,
            parameters = command.parameters ?: strategy.parameters
        )

        return tradingStrategyRepositoryPort.save(updated)
    }

    override fun deleteStrategy(strategyId: Long, userId: Long) {
        val strategy = tradingStrategyRepositoryPort.findById(strategyId)
            ?: throw IllegalArgumentException("Strategy not found: $strategyId")

        if (strategy.userId != userId) {
            throw IllegalArgumentException("Strategy does not belong to user")
        }

        tradingStrategyRepositoryPort.deleteById(strategyId)
    }

    override fun getStrategy(strategyId: Long): TradingStrategy? {
        return tradingStrategyRepositoryPort.findById(strategyId)
    }

    override fun getStrategiesByUser(userId: Long): List<TradingStrategy> {
        return tradingStrategyRepositoryPort.findByUserId(userId)
    }

    override fun getActiveStrategies(userId: Long): List<TradingStrategy> {
        return tradingStrategyRepositoryPort.findByUserIdAndIsActive(userId, true)
    }

    // ==================== Helper Methods ====================

    private fun calculatePositionSize(strategy: TradingStrategy, currentPrice: BigDecimal): BigDecimal {
        // 간단한 position sizing (고정 금액 기반)
        val investmentAmount = BigDecimal(1000) // 1000 USD/KRW per trade
        return investmentAmount.divide(currentPrice, 8, RoundingMode.DOWN)
    }

    private fun publishTradeSignal(
        strategy: TradingStrategy,
        symbol: String,
        signal: String,
        reason: String,
        price: BigDecimal
    ) {
        eventPublisherPort.publish(
            TradeSignalGeneratedEvent(
                strategyId = strategy.id!!,
                strategyName = strategy.name,
                symbol = symbol,
                signal = signal,
                reason = reason,
                price = price,
                timestamp = LocalDateTime.now()
            )
        )
    }
}

/**
 * 전략 타입
 */
enum class StrategyType {
    MOMENTUM,          // 모멘텀 전략
    MEAN_REVERSION,    // 평균회귀 전략
    SENTIMENT_BASED,   // 감성 기반 전략
    GRID_TRADING,      // 그리드 트레이딩
    DCA,               // 적립식 투자
    ARBITRAGE,         // 차익거래
    CUSTOM             // 사용자 정의
}

/**
 * 전략 생성 커맨드
 */
data class CreateStrategyCommand(
    val userId: Long,
    val name: String,
    val strategyType: StrategyType,
    val symbols: List<String>,
    val exchange: String,
    val parameters: Map<String, String> = emptyMap()
)

/**
 * 전략 수정 커맨드
 */
data class UpdateStrategyCommand(
    val name: String? = null,
    val isActive: Boolean? = null,
    val symbols: List<String>? = null,
    val parameters: Map<String, String>? = null
)
