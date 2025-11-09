package yousang.rest_server.application.service

import org.springframework.stereotype.Service
import yousang.rest_server.application.ports.out.TradingStrategyRepositoryPort
import yousang.rest_server.domain.model.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * 백테스팅 엔진
 *
 * 과거 데이터로 전략 성과 시뮬레이션
 */
@Service
class BacktestingService(
    private val marketDataService: MarketDataService,
    private val tradingStrategyRepositoryPort: TradingStrategyRepositoryPort
) {

    /**
     * 전략 백테스트 실행
     */
    fun runBacktest(
        strategyId: Long,
        symbol: String,
        exchange: String,
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        initialCapital: BigDecimal = BigDecimal(10000),
        commission: Double = 0.001 // 0.1%
    ): BacktestResult {
        val strategy = tradingStrategyRepositoryPort.findById(strategyId)
            ?: throw IllegalArgumentException("Strategy not found: $strategyId")

        // 과거 캔들 데이터 가져오기
        val candles = marketDataService.getCandles(
            symbol = symbol,
            exchange = exchange,
            interval = CandleInterval.ONE_HOUR,
            from = startDate,
            to = endDate
        )

        if (candles.isEmpty()) {
            throw IllegalStateException("No historical data available")
        }

        // 백테스트 실행
        val trades = mutableListOf<BacktestTrade>()
        var capital = initialCapital
        var position: BacktestPosition? = null
        val equityCurve = mutableListOf<EquityPoint>()

        for (i in 50 until candles.size) { // 최소 50개 캔들로 지표 계산
            val currentCandle = candles[i]
            val historicalCandles = candles.subList(0, i + 1)

            // 전략별 매수/매도 신호 생성
            val signal = generateSignal(strategy.strategyType, historicalCandles)

            // 포지션이 없고 매수 신호
            if (position == null && signal == TradingSignal.BUY) {
                val entryPrice = currentCandle.close
                val quantity = (capital * BigDecimal("0.95")) / entryPrice // 95% 투자
                val commissionCost = quantity * entryPrice * commission.toBigDecimal()

                position = BacktestPosition(
                    entryPrice = entryPrice,
                    quantity = quantity,
                    entryTime = currentCandle.closeTime
                )

                capital -= (quantity * entryPrice + commissionCost)
            }
            // 포지션이 있고 매도 신호
            else if (position != null && signal == TradingSignal.SELL) {
                val exitPrice = currentCandle.close
                val exitValue = position.quantity * exitPrice
                val commissionCost = exitValue * commission.toBigDecimal()
                val profit = exitValue - (position.quantity * position.entryPrice)
                val profitPercent = (profit / (position.quantity * position.entryPrice)) * BigDecimal(100)

                capital += (exitValue - commissionCost)

                trades.add(
                    BacktestTrade(
                        entryTime = position.entryTime,
                        entryPrice = position.entryPrice,
                        exitTime = currentCandle.closeTime,
                        exitPrice = exitPrice,
                        quantity = position.quantity,
                        profit = profit,
                        profitPercent = profitPercent.toDouble(),
                        commission = commissionCost * BigDecimal(2) // 매수+매도
                    )
                )

                position = null
            }

            // Equity Curve 기록
            val currentEquity = if (position != null) {
                capital + (position.quantity * currentCandle.close)
            } else {
                capital
            }

            equityCurve.add(
                EquityPoint(
                    timestamp = currentCandle.closeTime,
                    equity = currentEquity
                )
            )
        }

        // 미청산 포지션 처리
        if (position != null) {
            val finalPrice = candles.last().close
            capital += position.quantity * finalPrice
        }

        // 성과 분석
        return analyzePerformance(
            trades = trades,
            initialCapital = initialCapital,
            finalCapital = capital,
            equityCurve = equityCurve,
            strategyName = strategy.name
        )
    }

    /**
     * 전략별 신호 생성
     */
    private fun generateSignal(strategyType: StrategyType, candles: List<Candle>): TradingSignal {
        return when (strategyType) {
            StrategyType.MOMENTUM -> {
                val sma20 = calculateSMA(candles, 20)
                val sma50 = calculateSMA(candles, 50)
                val rsi = calculateRSI(candles, 14)

                if (sma20.isNotEmpty() && sma50.isNotEmpty() && rsi.isNotEmpty()) {
                    val latestSMA20 = sma20.last()
                    val latestSMA50 = sma50.last()
                    val latestRSI = rsi.last()

                    when {
                        latestSMA20 > latestSMA50 && latestRSI < BigDecimal(70) -> TradingSignal.BUY
                        latestSMA20 < latestSMA50 || latestRSI > BigDecimal(80) -> TradingSignal.SELL
                        else -> TradingSignal.HOLD
                    }
                } else {
                    TradingSignal.HOLD
                }
            }
            StrategyType.MEAN_REVERSION -> {
                val bb = calculateBollingerBands(candles, 20, 2.0)
                if (bb.isNotEmpty()) {
                    val currentPrice = candles.last().close
                    val latestBB = bb.last()

                    when {
                        currentPrice < latestBB.lower -> TradingSignal.BUY
                        currentPrice > latestBB.upper -> TradingSignal.SELL
                        else -> TradingSignal.HOLD
                    }
                } else {
                    TradingSignal.HOLD
                }
            }
            else -> TradingSignal.HOLD
        }
    }

    /**
     * 성과 분석
     */
    private fun analyzePerformance(
        trades: List<BacktestTrade>,
        initialCapital: BigDecimal,
        finalCapital: BigDecimal,
        equityCurve: List<EquityPoint>,
        strategyName: String
    ): BacktestResult {
        val totalReturn = ((finalCapital - initialCapital) / initialCapital) * BigDecimal(100)
        val totalTrades = trades.size
        val winningTrades = trades.count { it.profit > BigDecimal.ZERO }
        val losingTrades = trades.count { it.profit < BigDecimal.ZERO }
        val winRate = if (totalTrades > 0) (winningTrades.toDouble() / totalTrades) * 100 else 0.0

        val avgWin = if (winningTrades > 0) {
            trades.filter { it.profit > BigDecimal.ZERO }.map { it.profit }.average().toBigDecimal()
        } else {
            BigDecimal.ZERO
        }

        val avgLoss = if (losingTrades > 0) {
            trades.filter { it.profit < BigDecimal.ZERO }.map { it.profit }.average().toBigDecimal().abs()
        } else {
            BigDecimal.ZERO
        }

        val profitFactor = if (avgLoss > BigDecimal.ZERO) {
            avgWin / avgLoss
        } else {
            BigDecimal.ZERO
        }

        // Maximum Drawdown 계산
        val maxDrawdown = calculateMaxDrawdown(equityCurve)

        // Sharpe Ratio 계산
        val sharpeRatio = calculateSharpeRatio(trades)

        return BacktestResult(
            strategyName = strategyName,
            initialCapital = initialCapital,
            finalCapital = finalCapital,
            totalReturn = totalReturn.toDouble(),
            totalTrades = totalTrades,
            winningTrades = winningTrades,
            losingTrades = losingTrades,
            winRate = winRate,
            averageWin = avgWin,
            averageLoss = avgLoss,
            profitFactor = profitFactor.toDouble(),
            maxDrawdown = maxDrawdown,
            sharpeRatio = sharpeRatio,
            trades = trades,
            equityCurve = equityCurve
        )
    }

    /**
     * Maximum Drawdown 계산
     */
    private fun calculateMaxDrawdown(equityCurve: List<EquityPoint>): Double {
        if (equityCurve.isEmpty()) return 0.0

        var maxEquity = equityCurve.first().equity
        var maxDrawdown = 0.0

        equityCurve.forEach { point ->
            if (point.equity > maxEquity) {
                maxEquity = point.equity
            }

            val drawdown = ((maxEquity - point.equity) / maxEquity) * BigDecimal(100)
            if (drawdown.toDouble() > maxDrawdown) {
                maxDrawdown = drawdown.toDouble()
            }
        }

        return maxDrawdown
    }

    /**
     * Sharpe Ratio 계산
     */
    private fun calculateSharpeRatio(trades: List<BacktestTrade>, riskFreeRate: Double = 0.02): Double {
        if (trades.isEmpty()) return 0.0

        val returns = trades.map { it.profitPercent / 100 }
        val avgReturn = returns.average()
        val stdDev = calculateStdDev(returns)

        return if (stdDev > 0) {
            (avgReturn - riskFreeRate) / stdDev
        } else {
            0.0
        }
    }

    /**
     * 표준편차 계산
     */
    private fun calculateStdDev(values: List<Double>): Double {
        if (values.isEmpty()) return 0.0

        val mean = values.average()
        val variance = values.map { (it - mean).pow(2) }.average()
        return sqrt(variance)
    }

    // Helper methods for indicators (simplified versions)
    private fun calculateSMA(candles: List<Candle>, period: Int): List<BigDecimal> {
        if (candles.size < period) return emptyList()
        return candles.windowed(period, 1) { window ->
            window.map { it.close }.reduce { acc, price -> acc + price } / period.toBigDecimal()
        }
    }

    private fun calculateRSI(candles: List<Candle>, period: Int): List<BigDecimal> {
        if (candles.size < period + 1) return emptyList()

        val rsiList = mutableListOf<BigDecimal>()
        for (i in period until candles.size) {
            val changes = (i - period + 1..i).map { idx ->
                candles[idx].close - candles[idx - 1].close
            }
            val gains = changes.filter { it > BigDecimal.ZERO }.sumOf { it }
            val losses = changes.filter { it < BigDecimal.ZERO }.sumOf { it.abs() }

            val avgGain = gains / period.toBigDecimal()
            val avgLoss = losses / period.toBigDecimal()

            val rsi = if (avgLoss == BigDecimal.ZERO) {
                BigDecimal(100)
            } else {
                val rs = avgGain / avgLoss
                BigDecimal(100) - (BigDecimal(100) / (BigDecimal.ONE + rs))
            }
            rsiList.add(rsi)
        }
        return rsiList
    }

    private fun calculateBollingerBands(candles: List<Candle>, period: Int, stdDevMultiplier: Double): List<BollingerBands> {
        if (candles.size < period) return emptyList()

        return candles.windowed(period, 1) { window ->
            val prices = window.map { it.close }
            val sma = prices.reduce { acc, price -> acc + price } / period.toBigDecimal()

            val variance = prices.map { price ->
                val diff = price - sma
                diff * diff
            }.reduce { acc, value -> acc + value } / period.toBigDecimal()

            val stdDev = sqrt(variance.toDouble()).toBigDecimal()
            val multiplier = stdDevMultiplier.toBigDecimal()

            BollingerBands(
                middle = sma,
                upper = sma + (stdDev * multiplier),
                lower = sma - (stdDev * multiplier)
            )
        }
    }

    private fun Double.toBigDecimal(): BigDecimal = BigDecimal.valueOf(this)
    private fun List<BigDecimal>.average(): Double = this.map { it.toDouble() }.average()
}

// ==================== Backtest Models ====================

data class BacktestPosition(
    val entryPrice: BigDecimal,
    val quantity: BigDecimal,
    val entryTime: LocalDateTime
)

data class BacktestTrade(
    val entryTime: LocalDateTime,
    val entryPrice: BigDecimal,
    val exitTime: LocalDateTime,
    val exitPrice: BigDecimal,
    val quantity: BigDecimal,
    val profit: BigDecimal,
    val profitPercent: Double,
    val commission: BigDecimal
)

data class EquityPoint(
    val timestamp: LocalDateTime,
    val equity: BigDecimal
)

data class BacktestResult(
    val strategyName: String,
    val initialCapital: BigDecimal,
    val finalCapital: BigDecimal,
    val totalReturn: Double, // %
    val totalTrades: Int,
    val winningTrades: Int,
    val losingTrades: Int,
    val winRate: Double, // %
    val averageWin: BigDecimal,
    val averageLoss: BigDecimal,
    val profitFactor: Double,
    val maxDrawdown: Double, // %
    val sharpeRatio: Double,
    val trades: List<BacktestTrade>,
    val equityCurve: List<EquityPoint>
)
