package yousang.rest_server.adapter.out.persistence.jpa.trading

import jakarta.persistence.*
import yousang.rest_server.domain.model.TradingPair
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * 거래 쌍 JPA Entity
 */
@Entity
@Table(
    name = "trading_pairs",
    uniqueConstraints = [UniqueConstraint(columnNames = ["symbol", "exchange"])]
)
class TradingPairJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, length = 20)
    val symbol: String,

    @Column(name = "base_asset", nullable = false, length = 10)
    val baseAsset: String,

    @Column(name = "quote_asset", nullable = false, length = 10)
    val quoteAsset: String,

    @Column(nullable = false, length = 50)
    val exchange: String,

    @Column(name = "min_order_size", nullable = false, precision = 20, scale = 8)
    val minOrderSize: BigDecimal,

    @Column(name = "max_order_size", precision = 20, scale = 8)
    val maxOrderSize: BigDecimal? = null,

    @Column(name = "tick_size", nullable = false, precision = 20, scale = 8)
    val tickSize: BigDecimal,

    @Column(name = "maker_fee", precision = 10, scale = 6)
    val makerFee: BigDecimal = BigDecimal.ZERO,

    @Column(name = "taker_fee", precision = 10, scale = 6)
    val takerFee: BigDecimal = BigDecimal.ZERO,

    @Column(name = "is_active")
    val isActive: Boolean = true,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    /**
     * JPA Entity를 Domain Model로 변환
     */
    fun toDomain(): TradingPair {
        return TradingPair(
            id = id,
            symbol = symbol,
            baseAsset = baseAsset,
            quoteAsset = quoteAsset,
            exchange = exchange,
            minOrderSize = minOrderSize,
            maxOrderSize = maxOrderSize,
            tickSize = tickSize,
            makerFee = makerFee,
            takerFee = takerFee,
            isActive = isActive,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        /**
         * Domain Model을 JPA Entity로 변환
         */
        fun fromDomain(tradingPair: TradingPair): TradingPairJpaEntity {
            return TradingPairJpaEntity(
                id = tradingPair.id,
                symbol = tradingPair.symbol,
                baseAsset = tradingPair.baseAsset,
                quoteAsset = tradingPair.quoteAsset,
                exchange = tradingPair.exchange,
                minOrderSize = tradingPair.minOrderSize,
                maxOrderSize = tradingPair.maxOrderSize,
                tickSize = tradingPair.tickSize,
                makerFee = tradingPair.makerFee,
                takerFee = tradingPair.takerFee,
                isActive = tradingPair.isActive,
                createdAt = tradingPair.createdAt,
                updatedAt = tradingPair.updatedAt
            )
        }
    }
}
