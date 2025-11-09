package yousang.rest_server.application.service

import org.springframework.stereotype.Service
import yousang.rest_server.domain.model.Candle
import yousang.rest_server.domain.model.CandleInterval
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * 멀티 타임프레임 분석 서비스
 *
 * 여러 시간대를 동시 분석하여 신뢰도 높은 신호 생성
 */
@Service
class MultiTimeframeAnalysisService(
    private val marketDataService: MarketDataService,
    private val advancedTechnicalAnalysisService: AdvancedTechnicalAnalysisService
) {

    /**
     * 멀티 타임프레임 트렌드 분석
     *
     * 여러 시간대의 트렌드가 일치할 때 강한 신호
     */
    fun analyzeMultiTimeframeTrend(
        symbol: String,
        exchange: String,
        timeframes: List<CandleInterval> = listOf(
            CandleInterval.FIFTEEN_MINUTES,
            CandleInterval.ONE_HOUR,
            CandleInterval.FOUR_HOURS,
            CandleInterval.ONE_DAY
        )
    ): MultiTimeframeTrendResult {
        val trendByTimeframe = mutableMapOf<CandleInterval, TrendDirection>()
        val signals = mutableListOf<TimeframeSignal>()

        timeframes.forEach { interval ->
            try {
                val candles = marketDataService.getRecentCandles(symbol, exchange, interval, 100)

                if (candles.isNotEmpty()) {
                    // MACD 기반 트렌드 판단
                    val macd = advancedTechnicalAnalysisService.calculateMACD(candles)
                    val signal = if (macd.isNotEmpty()) {
                        advancedTechnicalAnalysisService.detectMACDSignal(macd)
                    } else {
                        TradingSignal.HOLD
                    }

                    val trend = when (signal) {
                        TradingSignal.BUY -> TrendDirection.BULLISH
                        TradingSignal.SELL -> TrendDirection.BEARISH
                        TradingSignal.HOLD -> TrendDirection.NEUTRAL
                    }

                    trendByTimeframe[interval] = trend

                    // ADX로 트렌드 강도 측정
                    val adx = advancedTechnicalAnalysisService.calculateADX(candles)
                    val strength = if (adx.isNotEmpty()) {
                        val latestADX = adx.last()
                        when {
                            latestADX > BigDecimal(50) -> TrendStrength.VERY_STRONG
                            latestADX > BigDecimal(25) -> TrendStrength.STRONG
                            else -> TrendStrength.WEAK
                        }
                    } else {
                        TrendStrength.WEAK
                    }

                    signals.add(
                        TimeframeSignal(
                            interval = interval,
                            trend = trend,
                            strength = strength,
                            signal = signal
                        )
                    )
                }

            } catch (e: Exception) {
                println("Failed to analyze timeframe $interval: ${e.message}")
            }
        }

        // 컨플루언스 점수 계산 (모든 시간대가 같은 방향 → 높은 점수)
        val confluenceScore = calculateConfluenceScore(signals)

        // 종합 신호
        val overallTrend = determineOverallTrend(signals)

        return MultiTimeframeTrendResult(
            symbol = symbol,
            timeframeSignals = signals,
            overallTrend = overallTrend,
            confluenceScore = confluenceScore,
            recommendation = generateRecommendation(overallTrend, confluenceScore)
        )
    }

    /**
     * 타임프레임 간 수렴/발산 감지
     */
    fun detectDivergence(
        symbol: String,
        exchange: String,
        shortTF: CandleInterval = CandleInterval.FIFTEEN_MINUTES,
        longTF: CandleInterval = CandleInterval.FOUR_HOURS
    ): DivergenceResult {
        val shortCandles = marketDataService.getRecentCandles(symbol, exchange, shortTF, 50)
        val longCandles = marketDataService.getRecentCandles(symbol, exchange, longTF, 50)

        if (shortCandles.isEmpty() || longCandles.isEmpty()) {
            return DivergenceResult(
                hasDivergence = false,
                type = null,
                description = "Insufficient data"
            )
        }

        // 단기 트렌드
        val shortRSI = advancedTechnicalAnalysisService.calculateMACD(shortCandles)
        val shortSignal = if (shortRSI.isNotEmpty()) {
            advancedTechnicalAnalysisService.detectMACDSignal(shortRSI)
        } else {
            TradingSignal.HOLD
        }

        // 장기 트렌드
        val longRSI = advancedTechnicalAnalysisService.calculateMACD(longCandles)
        val longSignal = if (longRSI.isNotEmpty()) {
            advancedTechnicalAnalysisService.detectMACDSignal(longRSI)
        } else {
            TradingSignal.HOLD
        }

        // 발산 감지
        val hasDivergence = shortSignal != longSignal && shortSignal != TradingSignal.HOLD && longSignal != TradingSignal.HOLD

        val divergenceType = if (hasDivergence) {
            when {
                shortSignal == TradingSignal.BUY && longSignal == TradingSignal.SELL -> "BULLISH_DIVERGENCE"
                shortSignal == TradingSignal.SELL && longSignal == TradingSignal.BUY -> "BEARISH_DIVERGENCE"
                else -> "NEUTRAL"
            }
        } else {
            null
        }

        return DivergenceResult(
            hasDivergence = hasDivergence,
            type = divergenceType,
            description = if (hasDivergence) {
                "Short-term: $shortSignal, Long-term: $longSignal"
            } else {
                "Trends are aligned"
            }
        )
    }

    /**
     * 최적 진입 타이밍 찾기
     */
    fun findOptimalEntry(
        symbol: String,
        exchange: String,
        targetSide: OrderSide
    ): OptimalEntryResult {
        val mtfResult = analyzeMultiTimeframeTrend(symbol, exchange)

        val isAligned = when (targetSide) {
            OrderSide.BUY -> mtfResult.overallTrend == TrendDirection.BULLISH
            OrderSide.SELL -> mtfResult.overallTrend == TrendDirection.BEARISH
        }

        val confidence = mtfResult.confluenceScore

        // 피보나치 레벨로 진입점 찾기
        val candles = marketDataService.getRecentCandles(symbol, exchange, CandleInterval.ONE_HOUR, 100)
        val fibonacci = advancedTechnicalAnalysisService.autoFibonacci(candles, 50)

        val suggestedEntry = when (targetSide) {
            OrderSide.BUY -> fibonacci?.level_382 // 38.2% 되돌림에서 매수
            OrderSide.SELL -> fibonacci?.level_618 // 61.8% 되돌림에서 매도
        }

        return OptimalEntryResult(
            symbol = symbol,
            side = targetSide,
            isAligned = isAligned,
            confidence = confidence,
            suggestedEntryPrice = suggestedEntry,
            reasoning = if (isAligned) {
                "All timeframes aligned ${mtfResult.overallTrend}. Confluence score: $confidence%"
            } else {
                "Timeframes not aligned. Wait for better setup."
            }
        )
    }

    // ==================== Helper Methods ====================

    private fun calculateConfluenceScore(signals: List<TimeframeSignal>): Double {
        if (signals.isEmpty()) return 0.0

        val bullishCount = signals.count { it.trend == TrendDirection.BULLISH }
        val bearishCount = signals.count { it.trend == TrendDirection.BEARISH }

        val maxCount = maxOf(bullishCount, bearishCount)
        return (maxCount.toDouble() / signals.size) * 100
    }

    private fun determineOverallTrend(signals: List<TimeframeSignal>): TrendDirection {
        if (signals.isEmpty()) return TrendDirection.NEUTRAL

        val bullishCount = signals.count { it.trend == TrendDirection.BULLISH }
        val bearishCount = signals.count { it.trend == TrendDirection.BEARISH }

        return when {
            bullishCount > bearishCount -> TrendDirection.BULLISH
            bearishCount > bullishCount -> TrendDirection.BEARISH
            else -> TrendDirection.NEUTRAL
        }
    }

    private fun generateRecommendation(trend: TrendDirection, confidence: Double): String {
        return when {
            confidence >= 80 -> "STRONG ${trend.name} - High confidence setup"
            confidence >= 60 -> "MODERATE ${trend.name} - Good setup"
            confidence >= 40 -> "WEAK ${trend.name} - Wait for confirmation"
            else -> "NO CLEAR TREND - Stay out or use smaller position"
        }
    }
}

// ==================== Result Models ====================

data class TimeframeSignal(
    val interval: CandleInterval,
    val trend: TrendDirection,
    val strength: TrendStrength,
    val signal: TradingSignal
)

data class MultiTimeframeTrendResult(
    val symbol: String,
    val timeframeSignals: List<TimeframeSignal>,
    val overallTrend: TrendDirection,
    val confluenceScore: Double, // 0-100
    val recommendation: String
)

data class DivergenceResult(
    val hasDivergence: Boolean,
    val type: String?, // BULLISH_DIVERGENCE, BEARISH_DIVERGENCE
    val description: String
)

data class OptimalEntryResult(
    val symbol: String,
    val side: OrderSide,
    val isAligned: Boolean,
    val confidence: Double,
    val suggestedEntryPrice: BigDecimal?,
    val reasoning: String
)

enum class TrendDirection {
    BULLISH, BEARISH, NEUTRAL
}

enum class TrendStrength {
    VERY_STRONG, STRONG, WEAK
}
