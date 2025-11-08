package yousang.rest_server.adapter.out.persistence.mongodb

import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import yousang.rest_server.application.ports.out.MarketDataRepositoryPort
import yousang.rest_server.domain.model.Candle
import yousang.rest_server.domain.model.CandleInterval
import yousang.rest_server.domain.model.MarketData
import java.time.LocalDateTime

@Component
class MarketDataRepositoryAdapter(
    private val candleMongoRepository: CandleMongoRepository
) : MarketDataRepositoryPort {

    override fun saveMarketData(marketData: MarketData) {
        // Market data는 캔들로 변환하여 저장하거나, 별도 컬렉션에 저장
        // 여기서는 캔들 저장만 구현
    }

    override fun saveMarketDataBatch(marketDataList: List<MarketData>) {
        // Batch save implementation
    }

    override fun findLatestMarketData(symbol: String, exchange: String): MarketData? {
        // Latest market data 조회
        return null
    }

    override fun saveCandle(candle: Candle) {
        val document = CandleDocument.fromDomain(candle)
        candleMongoRepository.save(document)
    }

    override fun saveCandleBatch(candles: List<Candle>) {
        val documents = candles.map { CandleDocument.fromDomain(it) }
        candleMongoRepository.saveAll(documents)
    }

    override fun findCandles(
        symbol: String,
        exchange: String,
        interval: CandleInterval,
        from: LocalDateTime,
        to: LocalDateTime
    ): List<Candle> {
        val intervalStr = interval.name.lowercase().replace("_", "")
        return candleMongoRepository.findBySymbolAndExchangeAndIntervalAndOpenTimeBetween(
            symbol, exchange, intervalStr, from, to
        ).map { it.toDomain() }
    }

    override fun findRecentCandles(
        symbol: String,
        exchange: String,
        interval: CandleInterval,
        limit: Int
    ): List<Candle> {
        val intervalStr = interval.name.lowercase().replace("_", "")
        val pageable = PageRequest.of(0, limit)
        return candleMongoRepository.findBySymbolAndExchangeAndIntervalOrderByOpenTimeDesc(
            symbol, exchange, intervalStr, pageable
        ).map { it.toDomain() }
    }

    override fun deleteOldData(before: LocalDateTime) {
        candleMongoRepository.deleteByOpenTimeBefore(before)
    }
}
