package yousang.rest_server.application.ports.`in`

import yousang.rest_server.domain.model.Candle
import yousang.rest_server.domain.model.CandleInterval
import yousang.rest_server.domain.model.MarketData
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * 시장 데이터 수집 Use Case
 */
interface CollectMarketDataUseCase {
    fun collectLatestMarketData(symbol: String, exchange: String): MarketData
    fun collectMarketDataBatch(symbols: List<String>, exchange: String): List<MarketData>
    fun subscribeToRealTimeData(symbol: String, exchange: String, callback: (MarketData) -> Unit)
    fun unsubscribeFromRealTimeData(symbol: String, exchange: String)
}

/**
 * 시장 데이터 조회 Use Case
 */
interface GetMarketDataUseCase {
    fun getLatestMarketData(symbol: String, exchange: String): MarketData?
    fun getCurrentPrice(symbol: String, exchange: String): BigDecimal
    fun getPriceChange24h(symbol: String, exchange: String): BigDecimal
    fun getVolume24h(symbol: String, exchange: String): BigDecimal
}

/**
 * 캔들 데이터 Use Case
 */
interface GetCandleDataUseCase {
    fun collectCandles(
        symbol: String,
        exchange: String,
        interval: CandleInterval,
        from: LocalDateTime,
        to: LocalDateTime
    ): List<Candle>

    fun getCandles(
        symbol: String,
        exchange: String,
        interval: CandleInterval,
        from: LocalDateTime,
        to: LocalDateTime
    ): List<Candle>

    fun getRecentCandles(
        symbol: String,
        exchange: String,
        interval: CandleInterval,
        limit: Int
    ): List<Candle>

    fun getLatestCandle(symbol: String, exchange: String, interval: CandleInterval): Candle?

    // Technical Analysis
    fun calculateSMA(candles: List<Candle>, period: Int): List<BigDecimal>
    fun calculateEMA(candles: List<Candle>, period: Int): List<BigDecimal>
    fun calculateRSI(candles: List<Candle>, period: Int): List<BigDecimal>
    fun calculateBollingerBands(
        candles: List<Candle>,
        period: Int,
        stdDevMultiplier: Double
    ): List<yousang.rest_server.application.service.BollingerBands>

    fun cleanupOldData(before: LocalDateTime)
}
