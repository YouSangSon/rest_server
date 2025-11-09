package yousang.rest_server.application.service

import org.springframework.stereotype.Service
import yousang.rest_server.domain.model.Candle
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * 고급 기술적 분석 서비스
 *
 * MACD, Stochastic, Fibonacci, ATR, ADX 등
 */
@Service
class AdvancedTechnicalAnalysisService {

    // ==================== MACD (Moving Average Convergence Divergence) ====================

    /**
     * MACD 계산
     * @return Triple(MACD Line, Signal Line, Histogram)
     */
    fun calculateMACD(
        candles: List<Candle>,
        fastPeriod: Int = 12,
        slowPeriod: Int = 26,
        signalPeriod: Int = 9
    ): List<MACDResult> {
        if (candles.size < slowPeriod + signalPeriod) return emptyList()

        val prices = candles.map { it.close }

        // EMA 계산
        val fastEMA = calculateEMAValues(prices, fastPeriod)
        val slowEMA = calculateEMAValues(prices, slowPeriod)

        // MACD Line = Fast EMA - Slow EMA
        val macdLine = mutableListOf<BigDecimal>()
        for (i in slowEMA.indices) {
            if (i < fastEMA.size) {
                macdLine.add(fastEMA[i] - slowEMA[i])
            }
        }

        // Signal Line = EMA of MACD Line
        val signalLine = calculateEMAValues(macdLine, signalPeriod)

        // Histogram = MACD Line - Signal Line
        val results = mutableListOf<MACDResult>()
        for (i in signalLine.indices) {
            val histogram = macdLine[i] - signalLine[i]
            results.add(
                MACDResult(
                    macdLine = macdLine[i],
                    signalLine = signalLine[i],
                    histogram = histogram
                )
            )
        }

        return results
    }

    /**
     * MACD 크로스오버 신호 감지
     */
    fun detectMACDSignal(macdResults: List<MACDResult>): TradingSignal {
        if (macdResults.size < 2) return TradingSignal.HOLD

        val current = macdResults.last()
        val previous = macdResults[macdResults.size - 2]

        // Bullish: MACD가 Signal을 상향 돌파
        if (previous.macdLine <= previous.signalLine && current.macdLine > current.signalLine) {
            return TradingSignal.BUY
        }

        // Bearish: MACD가 Signal을 하향 돌파
        if (previous.macdLine >= previous.signalLine && current.macdLine < current.signalLine) {
            return TradingSignal.SELL
        }

        return TradingSignal.HOLD
    }

    // ==================== Stochastic Oscillator ====================

    /**
     * Stochastic Oscillator 계산
     * %K와 %D 라인
     */
    fun calculateStochastic(
        candles: List<Candle>,
        kPeriod: Int = 14,
        dPeriod: Int = 3
    ): List<StochasticResult> {
        if (candles.size < kPeriod) return emptyList()

        val results = mutableListOf<StochasticResult>()

        for (i in kPeriod - 1 until candles.size) {
            val window = candles.subList(i - kPeriod + 1, i + 1)
            val high = window.maxOf { it.high }
            val low = window.minOf { it.low }
            val close = candles[i].close

            // %K = 100 * (Close - Low) / (High - Low)
            val k = if (high == low) {
                BigDecimal(50)
            } else {
                ((close - low) / (high - low)) * BigDecimal(100)
            }

            results.add(StochasticResult(k = k, d = BigDecimal.ZERO))
        }

        // %D = SMA of %K
        if (results.size >= dPeriod) {
            for (i in dPeriod - 1 until results.size) {
                val kValues = results.subList(i - dPeriod + 1, i + 1).map { it.k }
                val d = kValues.reduce { acc, value -> acc + value } / dPeriod.toBigDecimal()
                results[i] = results[i].copy(d = d)
            }
        }

        return results
    }

    /**
     * Stochastic 과매수/과매도 감지
     */
    fun detectStochasticSignal(
        stochasticResults: List<StochasticResult>,
        overbought: Int = 80,
        oversold: Int = 20
    ): TradingSignal {
        if (stochasticResults.size < 2) return TradingSignal.HOLD

        val current = stochasticResults.last()

        // 과매도 영역에서 상승 → 매수
        if (current.k < oversold.toBigDecimal() && current.k > current.d) {
            return TradingSignal.BUY
        }

        // 과매수 영역에서 하락 → 매도
        if (current.k > overbought.toBigDecimal() && current.k < current.d) {
            return TradingSignal.SELL
        }

        return TradingSignal.HOLD
    }

    // ==================== ATR (Average True Range) ====================

    /**
     * ATR 계산 - 변동성 측정
     */
    fun calculateATR(candles: List<Candle>, period: Int = 14): List<BigDecimal> {
        if (candles.size < period) return emptyList()

        val trueRanges = mutableListOf<BigDecimal>()

        for (i in 1 until candles.size) {
            val current = candles[i]
            val previous = candles[i - 1]

            val tr1 = current.high - current.low
            val tr2 = (current.high - previous.close).abs()
            val tr3 = (current.low - previous.close).abs()

            val trueRange = maxOf(tr1, tr2, tr3)
            trueRanges.add(trueRange)
        }

        // ATR = EMA of True Range
        return calculateEMAValues(trueRanges, period)
    }

    // ==================== ADX (Average Directional Index) ====================

    /**
     * ADX 계산 - 트렌드 강도 측정
     */
    fun calculateADX(candles: List<Candle>, period: Int = 14): List<BigDecimal> {
        if (candles.size < period * 2) return emptyList()

        val plusDM = mutableListOf<BigDecimal>()
        val minusDM = mutableListOf<BigDecimal>()
        val tr = mutableListOf<BigDecimal>()

        for (i in 1 until candles.size) {
            val current = candles[i]
            val previous = candles[i - 1]

            // +DM, -DM 계산
            val upMove = current.high - previous.high
            val downMove = previous.low - current.low

            plusDM.add(if (upMove > downMove && upMove > BigDecimal.ZERO) upMove else BigDecimal.ZERO)
            minusDM.add(if (downMove > upMove && downMove > BigDecimal.ZERO) downMove else BigDecimal.ZERO)

            // True Range
            val tr1 = current.high - current.low
            val tr2 = (current.high - previous.close).abs()
            val tr3 = (current.low - previous.close).abs()
            tr.add(maxOf(tr1, tr2, tr3))
        }

        // Smoothed +DI, -DI
        val smoothedPlusDM = calculateEMAValues(plusDM, period)
        val smoothedMinusDM = calculateEMAValues(minusDM, period)
        val smoothedTR = calculateEMAValues(tr, period)

        val plusDI = mutableListOf<BigDecimal>()
        val minusDI = mutableListOf<BigDecimal>()

        for (i in smoothedTR.indices) {
            if (smoothedTR[i] > BigDecimal.ZERO) {
                plusDI.add((smoothedPlusDM[i] / smoothedTR[i]) * BigDecimal(100))
                minusDI.add((smoothedMinusDM[i] / smoothedTR[i]) * BigDecimal(100))
            }
        }

        // DX
        val dx = mutableListOf<BigDecimal>()
        for (i in plusDI.indices) {
            val sum = plusDI[i] + minusDI[i]
            if (sum > BigDecimal.ZERO) {
                val diff = (plusDI[i] - minusDI[i]).abs()
                dx.add((diff / sum) * BigDecimal(100))
            }
        }

        // ADX = EMA of DX
        return calculateEMAValues(dx, period)
    }

    // ==================== Fibonacci Retracement ====================

    /**
     * Fibonacci 되돌림 레벨 계산
     */
    fun calculateFibonacciLevels(high: BigDecimal, low: BigDecimal): FibonacciLevels {
        val diff = high - low

        return FibonacciLevels(
            level_0 = low,
            level_236 = low + diff * BigDecimal("0.236"),
            level_382 = low + diff * BigDecimal("0.382"),
            level_500 = low + diff * BigDecimal("0.500"),
            level_618 = low + diff * BigDecimal("0.618"),
            level_786 = low + diff * BigDecimal("0.786"),
            level_100 = high
        )
    }

    /**
     * Swing High/Low 자동 감지 후 Fibonacci 계산
     */
    fun autoFibonacci(candles: List<Candle>, lookback: Int = 50): FibonacciLevels? {
        if (candles.size < lookback) return null

        val recentCandles = candles.takeLast(lookback)
        val high = recentCandles.maxOf { it.high }
        val low = recentCandles.minOf { it.low }

        return calculateFibonacciLevels(high, low)
    }

    // ==================== Ichimoku Cloud ====================

    /**
     * Ichimoku 구름 계산
     */
    fun calculateIchimoku(candles: List<Candle>): List<IchimokuResult> {
        if (candles.size < 52) return emptyList()

        val results = mutableListOf<IchimokuResult>()

        for (i in 26 until candles.size) {
            // Tenkan-sen (Conversion Line) = (9-period high + 9-period low) / 2
            val tenkanHigh = candles.subList(max(0, i - 8), i + 1).maxOf { it.high }
            val tenkanLow = candles.subList(max(0, i - 8), i + 1).minOf { it.low }
            val tenkanSen = (tenkanHigh + tenkanLow) / BigDecimal(2)

            // Kijun-sen (Base Line) = (26-period high + 26-period low) / 2
            val kijunHigh = candles.subList(max(0, i - 25), i + 1).maxOf { it.high }
            val kijunLow = candles.subList(max(0, i - 25), i + 1).minOf { it.low }
            val kijunSen = (kijunHigh + kijunLow) / BigDecimal(2)

            // Senkou Span A = (Tenkan-sen + Kijun-sen) / 2, 26 periods ahead
            val senkouSpanA = (tenkanSen + kijunSen) / BigDecimal(2)

            // Senkou Span B = (52-period high + 52-period low) / 2, 26 periods ahead
            val senkouHigh = candles.subList(max(0, i - 51), i + 1).maxOf { it.high }
            val senkouLow = candles.subList(max(0, i - 51), i + 1).minOf { it.low }
            val senkouSpanB = (senkouHigh + senkouLow) / BigDecimal(2)

            results.add(
                IchimokuResult(
                    tenkanSen = tenkanSen,
                    kijunSen = kijunSen,
                    senkouSpanA = senkouSpanA,
                    senkouSpanB = senkouSpanB
                )
            )
        }

        return results
    }

    // ==================== Volume Analysis ====================

    /**
     * OBV (On-Balance Volume) 계산
     */
    fun calculateOBV(candles: List<Candle>): List<BigDecimal> {
        if (candles.isEmpty()) return emptyList()

        val obv = mutableListOf<BigDecimal>()
        var currentOBV = BigDecimal.ZERO

        for (i in candles.indices) {
            if (i == 0) {
                obv.add(candles[i].volume)
                currentOBV = candles[i].volume
            } else {
                currentOBV = when {
                    candles[i].close > candles[i - 1].close -> currentOBV + candles[i].volume
                    candles[i].close < candles[i - 1].close -> currentOBV - candles[i].volume
                    else -> currentOBV
                }
                obv.add(currentOBV)
            }
        }

        return obv
    }

    /**
     * VWAP (Volume Weighted Average Price) 계산
     */
    fun calculateVWAP(candles: List<Candle>): List<BigDecimal> {
        val vwap = mutableListOf<BigDecimal>()
        var cumulativeTPV = BigDecimal.ZERO
        var cumulativeVolume = BigDecimal.ZERO

        candles.forEach { candle ->
            val typicalPrice = (candle.high + candle.low + candle.close) / BigDecimal(3)
            cumulativeTPV += typicalPrice * candle.volume
            cumulativeVolume += candle.volume

            if (cumulativeVolume > BigDecimal.ZERO) {
                vwap.add(cumulativeTPV / cumulativeVolume)
            } else {
                vwap.add(BigDecimal.ZERO)
            }
        }

        return vwap
    }

    // ==================== Helper Methods ====================

    private fun calculateEMAValues(values: List<BigDecimal>, period: Int): List<BigDecimal> {
        if (values.size < period) return emptyList()

        val multiplier = BigDecimal(2) / (period + 1).toBigDecimal()
        val emaList = mutableListOf<BigDecimal>()

        // 첫 EMA는 SMA
        val firstSMA = values.take(period).reduce { acc, value -> acc + value } / period.toBigDecimal()
        emaList.add(firstSMA)

        // 나머지 EMA 계산
        for (i in period until values.size) {
            val ema = (values[i] - emaList.last()) * multiplier + emaList.last()
            emaList.add(ema)
        }

        return emaList
    }

    private fun BigDecimal.abs(): BigDecimal = this.abs()

    private fun maxOf(a: BigDecimal, b: BigDecimal, c: BigDecimal): BigDecimal {
        return if (a >= b && a >= c) a else if (b >= c) b else c
    }
}

// ==================== Result Models ====================

data class MACDResult(
    val macdLine: BigDecimal,
    val signalLine: BigDecimal,
    val histogram: BigDecimal
)

data class StochasticResult(
    val k: BigDecimal,
    val d: BigDecimal
)

data class FibonacciLevels(
    val level_0: BigDecimal,
    val level_236: BigDecimal,
    val level_382: BigDecimal,
    val level_500: BigDecimal,
    val level_618: BigDecimal,
    val level_786: BigDecimal,
    val level_100: BigDecimal
)

data class IchimokuResult(
    val tenkanSen: BigDecimal,
    val kijunSen: BigDecimal,
    val senkouSpanA: BigDecimal,
    val senkouSpanB: BigDecimal
)

enum class TradingSignal {
    BUY, SELL, HOLD
}
