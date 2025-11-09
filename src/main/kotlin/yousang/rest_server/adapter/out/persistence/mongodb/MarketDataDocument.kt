package yousang.rest_server.adapter.out.persistence.mongodb

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.CompoundIndexes
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import yousang.rest_server.domain.model.Candle
import yousang.rest_server.domain.model.CandleInterval
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * 캔들 데이터 MongoDB Document (시계열 데이터)
 */
@Document(collection = "market_candles")
@CompoundIndexes(
    CompoundIndex(name = "symbol_exchange_interval_time", def = "{'symbol': 1, 'exchange': 1, 'interval': 1, 'openTime': -1}")
)
data class CandleDocument(
    @Id
    val id: String? = null,

    @Indexed
    val symbol: String,

    @Indexed
    val exchange: String,

    val interval: String,  // 1m, 5m, 1h, etc.

    @Indexed
    val openTime: LocalDateTime,

    val closeTime: LocalDateTime,
    val open: String,  // BigDecimal을 String으로 저장
    val high: String,
    val low: String,
    val close: String,
    val volume: String,
    val quoteVolume: String? = null,
    val trades: Int? = null
) {
    fun toDomain(): Candle {
        return Candle(
            symbol = symbol,
            exchange = exchange,
            interval = CandleInterval.fromString(interval),
            openTime = openTime,
            closeTime = closeTime,
            open = BigDecimal(open),
            high = BigDecimal(high),
            low = BigDecimal(low),
            close = BigDecimal(close),
            volume = BigDecimal(volume),
            quoteVolume = quoteVolume?.let { BigDecimal(it) },
            trades = trades
        )
    }

    companion object {
        fun fromDomain(candle: Candle): CandleDocument {
            return CandleDocument(
                symbol = candle.symbol,
                exchange = candle.exchange,
                interval = candle.interval.name.lowercase().replace("_", ""),
                openTime = candle.openTime,
                closeTime = candle.closeTime,
                open = candle.open.toPlainString(),
                high = candle.high.toPlainString(),
                low = candle.low.toPlainString(),
                close = candle.close.toPlainString(),
                volume = candle.volume.toPlainString(),
                quoteVolume = candle.quoteVolume?.toPlainString(),
                trades = candle.trades
            )
        }
    }
}
