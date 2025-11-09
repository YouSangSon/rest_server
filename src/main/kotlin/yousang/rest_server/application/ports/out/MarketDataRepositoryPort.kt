package yousang.rest_server.application.ports.out

import yousang.rest_server.domain.model.Candle
import yousang.rest_server.domain.model.CandleInterval
import yousang.rest_server.domain.model.MarketData
import java.time.LocalDateTime

/**
 * 시장 데이터 Repository Port (Outbound Port)
 *
 * 시장 데이터 저장 및 조회를 위한 포트.
 * MongoDB로 구현됩니다 (대용량 시계열 데이터).
 */
interface MarketDataRepositoryPort {
    /**
     * 시장 데이터를 저장합니다.
     */
    fun saveMarketData(marketData: MarketData)

    /**
     * 여러 시장 데이터를 일괄 저장합니다.
     */
    fun saveMarketDataBatch(marketDataList: List<MarketData>)

    /**
     * 최신 시장 데이터를 조회합니다.
     */
    fun findLatestMarketData(symbol: String, exchange: String): MarketData?

    /**
     * 캔들 데이터를 저장합니다.
     */
    fun saveCandle(candle: Candle)

    /**
     * 여러 캔들 데이터를 일괄 저장합니다.
     */
    fun saveCandleBatch(candles: List<Candle>)

    /**
     * 캔들 데이터를 조회합니다.
     */
    fun findCandles(
        symbol: String,
        exchange: String,
        interval: CandleInterval,
        from: LocalDateTime,
        to: LocalDateTime
    ): List<Candle>

    /**
     * 최근 N개의 캔들을 조회합니다.
     */
    fun findRecentCandles(
        symbol: String,
        exchange: String,
        interval: CandleInterval,
        limit: Int = 100
    ): List<Candle>

    /**
     * 오래된 데이터를 삭제합니다 (정리 작업).
     */
    fun deleteOldData(before: LocalDateTime)
}
