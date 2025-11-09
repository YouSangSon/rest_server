package yousang.rest_server.domain.model

import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * 거래 쌍 도메인 모델
 *
 * 거래소에서 거래 가능한 자산 쌍 정보 (예: BTC/USDT)
 */
data class TradingPair(
    val id: Long? = null,
    val symbol: String,                    // BTC/USDT, ETH/USDT
    val baseAsset: String,                 // BTC, ETH
    val quoteAsset: String,                // USDT, KRW
    val exchange: String,                  // binance, upbit, bithumb
    val minOrderSize: BigDecimal,          // 최소 주문 수량
    val maxOrderSize: BigDecimal? = null,  // 최대 주문 수량
    val tickSize: BigDecimal,              // 최소 가격 단위
    val makerFee: BigDecimal = BigDecimal.ZERO,  // Maker 수수료
    val takerFee: BigDecimal = BigDecimal.ZERO,  // Taker 수수료
    val isActive: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    init {
        require(symbol.isNotBlank()) { "심볼은 필수입니다" }
        require(baseAsset.isNotBlank()) { "기준 자산은 필수입니다" }
        require(quoteAsset.isNotBlank()) { "견적 자산은 필수입니다" }
        require(exchange.isNotBlank()) { "거래소는 필수입니다" }
        require(minOrderSize > BigDecimal.ZERO) { "최소 주문 수량은 0보다 커야 합니다" }
        require(tickSize > BigDecimal.ZERO) { "최소 가격 단위는 0보다 커야 합니다" }
    }

    /**
     * 심볼을 정규화합니다 (예: BTC/USDT -> BTCUSDT).
     */
    fun normalizedSymbol(): String = symbol.replace("/", "")

    /**
     * 가격을 Tick Size에 맞춰 반올림합니다.
     */
    fun roundPrice(price: BigDecimal): BigDecimal {
        val divided = price.divide(tickSize, 0, java.math.RoundingMode.HALF_UP)
        return divided.multiply(tickSize)
    }

    /**
     * 수량을 최소 단위에 맞춰 반올림합니다.
     */
    fun roundQuantity(quantity: BigDecimal): BigDecimal {
        return quantity.setScale(8, java.math.RoundingMode.HALF_DOWN)
    }

    /**
     * 주문 수량이 유효한지 확인합니다.
     */
    fun isValidQuantity(quantity: BigDecimal): Boolean {
        if (quantity < minOrderSize) return false
        if (maxOrderSize != null && quantity > maxOrderSize) return false
        return true
    }

    /**
     * 수수료를 계산합니다.
     */
    fun calculateFee(amount: BigDecimal, isMaker: Boolean): BigDecimal {
        val feeRate = if (isMaker) makerFee else takerFee
        return amount.multiply(feeRate)
    }

    companion object {
        /**
         * 거래 쌍을 생성합니다.
         */
        fun create(
            symbol: String,
            baseAsset: String,
            quoteAsset: String,
            exchange: String,
            minOrderSize: BigDecimal,
            tickSize: BigDecimal
        ): TradingPair {
            return TradingPair(
                symbol = symbol,
                baseAsset = baseAsset,
                quoteAsset = quoteAsset,
                exchange = exchange,
                minOrderSize = minOrderSize,
                tickSize = tickSize
            )
        }

        /**
         * Binance BTC/USDT
         */
        fun btcUsdt(): TradingPair {
            return create(
                symbol = "BTC/USDT",
                baseAsset = "BTC",
                quoteAsset = "USDT",
                exchange = "binance",
                minOrderSize = BigDecimal("0.00001"),
                tickSize = BigDecimal("0.01")
            )
        }

        /**
         * Binance ETH/USDT
         */
        fun ethUsdt(): TradingPair {
            return create(
                symbol = "ETH/USDT",
                baseAsset = "ETH",
                quoteAsset = "USDT",
                exchange = "binance",
                minOrderSize = BigDecimal("0.0001"),
                tickSize = BigDecimal("0.01")
            )
        }

        /**
         * Upbit KRW-BTC
         */
        fun btcKrw(): TradingPair {
            return create(
                symbol = "BTC/KRW",
                baseAsset = "BTC",
                quoteAsset = "KRW",
                exchange = "upbit",
                minOrderSize = BigDecimal("0.0001"),
                tickSize = BigDecimal("1000")
            )
        }
    }
}
