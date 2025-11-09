package yousang.rest_server.application.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import yousang.rest_server.application.ports.`in`.*
import yousang.rest_server.application.ports.out.*
import yousang.rest_server.domain.event.CandleCollectedEvent
import yousang.rest_server.domain.event.MarketDataUpdatedEvent
import yousang.rest_server.domain.model.*
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * 시장 데이터 서비스
 *
 * 실시간 시장 데이터 수집, 캔들 데이터 관리
 */
@Service
@Transactional
class MarketDataService(
    private val marketDataRepositoryPort: MarketDataRepositoryPort,
    private val exchangeApiPort: List<ExchangeApiPort>,
    private val eventPublisherPort: EventPublisherPort
) : CollectMarketDataUseCase, GetMarketDataUseCase, GetCandleDataUseCase {

    // ==================== Market Data Collection ====================

    override fun collectLatestMarketData(symbol: String, exchange: String): MarketData {
        val exchangeApi = findExchangeApi(exchange)
        val marketData = exchangeApi.fetchMarketData(symbol)

        // 저장
        marketDataRepositoryPort.saveMarketData(marketData)

        // 이벤트 발행
        eventPublisherPort.publish(
            MarketDataUpdatedEvent(
                symbol = symbol,
                exchange = exchange,
                price = marketData.currentPrice,
                volume24h = marketData.volume24h,
                priceChange24h = marketData.priceChange24h,
                timestamp = marketData.timestamp
            )
        )

        return marketData
    }

    override fun collectMarketDataBatch(symbols: List<String>, exchange: String): List<MarketData> {
        val exchangeApi = findExchangeApi(exchange)
        val marketDataList = symbols.map { symbol ->
            try {
                exchangeApi.fetchMarketData(symbol)
            } catch (e: Exception) {
                println("Failed to fetch market data for $symbol: ${e.message}")
                null
            }
        }.filterNotNull()

        if (marketDataList.isNotEmpty()) {
            marketDataRepositoryPort.saveMarketDataBatch(marketDataList)

            // 이벤트 발행
            marketDataList.forEach { marketData ->
                eventPublisherPort.publish(
                    MarketDataUpdatedEvent(
                        symbol = marketData.symbol,
                        exchange = marketData.exchange,
                        price = marketData.currentPrice,
                        volume24h = marketData.volume24h,
                        priceChange24h = marketData.priceChange24h,
                        timestamp = marketData.timestamp
                    )
                )
            }
        }

        return marketDataList
    }

    override fun subscribeToRealTimeData(symbol: String, exchange: String, callback: (MarketData) -> Unit) {
        val exchangeApi = findExchangeApi(exchange)
        exchangeApi.subscribeToMarketData(symbol, callback)
    }

    override fun unsubscribeFromRealTimeData(symbol: String, exchange: String) {
        val exchangeApi = findExchangeApi(exchange)
        exchangeApi.unsubscribeFromMarketData(symbol)
    }

    // ==================== Market Data Retrieval ====================

    override fun getLatestMarketData(symbol: String, exchange: String): MarketData? {
        return marketDataRepositoryPort.findLatestMarketData(symbol, exchange)
    }

    override fun getCurrentPrice(symbol: String, exchange: String): BigDecimal {
        val marketData = getLatestMarketData(symbol, exchange)
        return marketData?.currentPrice ?: run {
            // 캐시에 없으면 실시간 조회
            val exchangeApi = findExchangeApi(exchange)
            exchangeApi.fetchMarketData(symbol).currentPrice
        }
    }

    override fun getPriceChange24h(symbol: String, exchange: String): BigDecimal {
        val marketData = getLatestMarketData(symbol, exchange)
            ?: throw IllegalStateException("No market data available for $symbol on $exchange")
        return marketData.priceChange24h
    }

    override fun getVolume24h(symbol: String, exchange: String): BigDecimal {
        val marketData = getLatestMarketData(symbol, exchange)
            ?: throw IllegalStateException("No market data available for $symbol on $exchange")
        return marketData.volume24h
    }

    // ==================== Candle Data ====================

    override fun collectCandles(
        symbol: String,
        exchange: String,
        interval: CandleInterval,
        from: LocalDateTime,
        to: LocalDateTime
    ): List<Candle> {
        val exchangeApi = findExchangeApi(exchange)
        val candles = exchangeApi.fetchCandles(symbol, interval, from, to)

        if (candles.isNotEmpty()) {
            marketDataRepositoryPort.saveCandleBatch(candles)

            // 이벤트 발행
            candles.forEach { candle ->
                eventPublisherPort.publish(
                    CandleCollectedEvent(
                        symbol = candle.symbol,
                        exchange = candle.exchange,
                        interval = candle.interval,
                        openTime = candle.openTime,
                        closeTime = candle.closeTime,
                        open = candle.open,
                        high = candle.high,
                        low = candle.low,
                        close = candle.close,
                        volume = candle.volume
                    )
                )
            }
        }

        return candles
    }

    override fun getCandles(
        symbol: String,
        exchange: String,
        interval: CandleInterval,
        from: LocalDateTime,
        to: LocalDateTime
    ): List<Candle> {
        return marketDataRepositoryPort.findCandles(symbol, exchange, interval, from, to)
    }

    override fun getRecentCandles(
        symbol: String,
        exchange: String,
        interval: CandleInterval,
        limit: Int
    ): List<Candle> {
        return marketDataRepositoryPort.findRecentCandles(symbol, exchange, interval, limit)
    }

    override fun getLatestCandle(symbol: String, exchange: String, interval: CandleInterval): Candle? {
        val candles = marketDataRepositoryPort.findRecentCandles(symbol, exchange, interval, 1)
        return candles.firstOrNull()
    }

    // ==================== Technical Analysis ====================

    override fun calculateSMA(candles: List<Candle>, period: Int): List<BigDecimal> {
        if (candles.size < period) {
            return emptyList()
        }

        return candles.windowed(period, 1) { window ->
            window.map { it.close }.reduce { acc, price -> acc + price } / period.toBigDecimal()
        }
    }

    override fun calculateEMA(candles: List<Candle>, period: Int): List<BigDecimal> {
        if (candles.size < period) {
            return emptyList()
        }

        val multiplier = BigDecimal(2) / (period + 1).toBigDecimal()
        val emaList = mutableListOf<BigDecimal>()

        // 첫 EMA는 SMA로 시작
        val firstSMA = candles.take(period).map { it.close }.reduce { acc, price -> acc + price } / period.toBigDecimal()
        emaList.add(firstSMA)

        // 나머지 EMA 계산
        for (i in period until candles.size) {
            val ema = (candles[i].close - emaList.last()) * multiplier + emaList.last()
            emaList.add(ema)
        }

        return emaList
    }

    override fun calculateRSI(candles: List<Candle>, period: Int): List<BigDecimal> {
        if (candles.size < period + 1) {
            return emptyList()
        }

        val rsiList = mutableListOf<BigDecimal>()

        for (i in period until candles.size) {
            val priceChanges = (i - period + 1..i).map { idx ->
                candles[idx].close - candles[idx - 1].close
            }

            val gains = priceChanges.filter { it > BigDecimal.ZERO }.sum()
            val losses = priceChanges.filter { it < BigDecimal.ZERO }.map { it.abs() }.sum()

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

    override fun calculateBollingerBands(
        candles: List<Candle>,
        period: Int,
        stdDevMultiplier: Double
    ): List<BollingerBands> {
        if (candles.size < period) {
            return emptyList()
        }

        return candles.windowed(period, 1) { window ->
            val prices = window.map { it.close }
            val sma = prices.reduce { acc, price -> acc + price } / period.toBigDecimal()

            // 표준편차 계산
            val variance = prices.map { price ->
                val diff = price - sma
                diff * diff
            }.reduce { acc, value -> acc + value } / period.toBigDecimal()

            val stdDev = kotlin.math.sqrt(variance.toDouble()).toBigDecimal()
            val multiplier = stdDevMultiplier.toBigDecimal()

            BollingerBands(
                middle = sma,
                upper = sma + (stdDev * multiplier),
                lower = sma - (stdDev * multiplier)
            )
        }
    }

    // ==================== Data Cleanup ====================

    override fun cleanupOldData(before: LocalDateTime) {
        marketDataRepositoryPort.deleteOldData(before)
    }

    // ==================== Helper Methods ====================

    private fun findExchangeApi(exchange: String): ExchangeApiPort {
        return exchangeApiPort.find { it.getExchangeName().equals(exchange, ignoreCase = true) }
            ?: throw IllegalArgumentException("Exchange API not found: $exchange")
    }
}

/**
 * 볼린저 밴드
 */
data class BollingerBands(
    val middle: BigDecimal,
    val upper: BigDecimal,
    val lower: BigDecimal
)

// Extension function for BigDecimal
private fun Double.toBigDecimal(): BigDecimal = BigDecimal.valueOf(this)
private fun List<BigDecimal>.sum(): BigDecimal = this.fold(BigDecimal.ZERO) { acc, value -> acc + value }
