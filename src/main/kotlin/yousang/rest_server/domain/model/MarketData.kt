package yousang.rest_server.domain.model

import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * 시장 데이터 도메인 모델
 *
 * 실시간 가격, 거래량 등의 시장 데이터를 나타냅니다.
 */
data class MarketData(
    val symbol: String,               // BTC/USDT
    val exchange: String,             // binance
    val price: BigDecimal,            // 현재 가격
    val volume: BigDecimal,           // 24시간 거래량
    val high24h: BigDecimal,          // 24시간 최고가
    val low24h: BigDecimal,           // 24시간 최저가
    val change24h: BigDecimal,        // 24시간 변동률 (%)
    val changeAmount24h: BigDecimal,  // 24시간 변동액
    val bidPrice: BigDecimal? = null, // 매수 호가
    val askPrice: BigDecimal? = null, // 매도 호가
    val timestamp: LocalDateTime = LocalDateTime.now()
) {
    init {
        require(symbol.isNotBlank()) { "심볼은 필수입니다" }
        require(exchange.isNotBlank()) { "거래소는 필수입니다" }
        require(price > BigDecimal.ZERO) { "가격은 0보다 커야 합니다" }
        require(volume >= BigDecimal.ZERO) { "거래량은 0 이상이어야 합니다" }
    }

    /**
     * 가격이 상승 중인지 확인합니다.
     */
    fun isPriceUp(): Boolean = change24h > BigDecimal.ZERO

    /**
     * 가격이 하락 중인지 확인합니다.
     */
    fun isPriceDown(): Boolean = change24h < BigDecimal.ZERO

    /**
     * 급등 중인지 확인합니다 (24시간 변동률 > threshold%).
     */
    fun isSurging(threshold: BigDecimal = BigDecimal("5.0")): Boolean {
        return change24h > threshold
    }

    /**
     * 급락 중인지 확인합니다 (24시간 변동률 < -threshold%).
     */
    fun isPlunging(threshold: BigDecimal = BigDecimal("5.0")): Boolean {
        return change24h < threshold.negate()
    }

    /**
     * 스프레드 (매도-매수 호가 차이)를 계산합니다.
     */
    fun spread(): BigDecimal? {
        return if (bidPrice != null && askPrice != null) {
            askPrice - bidPrice
        } else null
    }

    /**
     * 스프레드 비율 (%)을 계산합니다.
     */
    fun spreadPercentage(): BigDecimal? {
        val spreadValue = spread() ?: return null
        return if (price > BigDecimal.ZERO) {
            spreadValue.divide(price, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal("100"))
        } else null
    }

    /**
     * 데이터가 최신인지 확인합니다 (5분 이내).
     */
    fun isRecent(): Boolean {
        return timestamp.isAfter(LocalDateTime.now().minusMinutes(5))
    }

    companion object {
        /**
         * 시장 데이터를 생성합니다.
         */
        fun create(
            symbol: String,
            exchange: String,
            price: BigDecimal,
            volume: BigDecimal,
            high24h: BigDecimal,
            low24h: BigDecimal,
            change24h: BigDecimal
        ): MarketData {
            val changeAmount = price.multiply(change24h).divide(BigDecimal("100"), 8, java.math.RoundingMode.HALF_UP)
            return MarketData(
                symbol = symbol,
                exchange = exchange,
                price = price,
                volume = volume,
                high24h = high24h,
                low24h = low24h,
                change24h = change24h,
                changeAmount24h = changeAmount
            )
        }
    }
}

/**
 * 캔들스틱 데이터 (OHLCV)
 */
data class Candle(
    val symbol: String,
    val exchange: String,
    val interval: CandleInterval,        // 1m, 5m, 1h, 1d
    val openTime: LocalDateTime,
    val closeTime: LocalDateTime,
    val open: BigDecimal,
    val high: BigDecimal,
    val low: BigDecimal,
    val close: BigDecimal,
    val volume: BigDecimal,
    val quoteVolume: BigDecimal? = null, // 거래대금
    val trades: Int? = null               // 거래 횟수
) {
    init {
        require(symbol.isNotBlank()) { "심볼은 필수입니다" }
        require(open > BigDecimal.ZERO) { "시가는 0보다 커야 합니다" }
        require(high >= open && high >= low && high >= close) { "고가는 OHLC 중 최대값이어야 합니다" }
        require(low <= open && low <= high && low <= close) { "저가는 OHLC 중 최소값이어야 합니다" }
        require(volume >= BigDecimal.ZERO) { "거래량은 0 이상이어야 합니다" }
    }

    /**
     * 가격 변동률을 계산합니다.
     */
    fun changePercentage(): BigDecimal {
        return if (open > BigDecimal.ZERO) {
            (close - open).divide(open, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal("100"))
        } else BigDecimal.ZERO
    }

    /**
     * 상승 캔들인지 확인합니다.
     */
    fun isBullish(): Boolean = close > open

    /**
     * 하락 캔들인지 확인합니다.
     */
    fun isBearish(): Boolean = close < open

    /**
     * 도지 캔들인지 확인합니다 (시가 = 종가).
     */
    fun isDoji(): Boolean = close == open

    /**
     * 캔들의 몸통 크기를 계산합니다.
     */
    fun bodySize(): BigDecimal = (close - open).abs()

    /**
     * 캔들의 위꼬리 크기를 계산합니다.
     */
    fun upperShadow(): BigDecimal = high - maxOf(open, close)

    /**
     * 캔들의 아래꼬리 크기를 계산합니다.
     */
    fun lowerShadow(): BigDecimal = minOf(open, close) - low
}

/**
 * 캔들 인터벌
 */
enum class CandleInterval(val minutes: Int) {
    ONE_MINUTE(1),
    FIVE_MINUTES(5),
    FIFTEEN_MINUTES(15),
    THIRTY_MINUTES(30),
    ONE_HOUR(60),
    FOUR_HOURS(240),
    ONE_DAY(1440),
    ONE_WEEK(10080);

    companion object {
        fun fromString(interval: String): CandleInterval {
            return when (interval.lowercase()) {
                "1m" -> ONE_MINUTE
                "5m" -> FIVE_MINUTES
                "15m" -> FIFTEEN_MINUTES
                "30m" -> THIRTY_MINUTES
                "1h" -> ONE_HOUR
                "4h" -> FOUR_HOURS
                "1d" -> ONE_DAY
                "1w" -> ONE_WEEK
                else -> throw IllegalArgumentException("지원하지 않는 인터벌: $interval")
            }
        }
    }
}
