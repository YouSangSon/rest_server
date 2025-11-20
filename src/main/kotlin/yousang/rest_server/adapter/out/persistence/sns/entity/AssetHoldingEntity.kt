package yousang.rest_server.adapter.out.persistence.sns.entity

import jakarta.persistence.*
import yousang.rest_server.domain.sns.AssetHolding
import yousang.rest_server.domain.sns.AssetType
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(
    name = "sns_asset_holdings",
    indexes = [
        Index(name = "idx_portfolio_id", columnList = "portfolio_id"),
        Index(name = "idx_asset_symbol", columnList = "asset_symbol")
    ]
)
class AssetHoldingEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "holding_id")
    val holdingId: Long = 0,

    @Column(name = "portfolio_id", nullable = false)
    val portfolioId: Long,

    @Column(name = "asset_symbol", nullable = false, length = 20)
    val assetSymbol: String,

    @Column(name = "asset_name", nullable = false, length = 100)
    val assetName: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "asset_type", nullable = false, length = 20)
    val assetType: AssetType,

    @Column(nullable = false, precision = 20, scale = 8)
    val quantity: BigDecimal,

    @Column(name = "average_price", nullable = false, precision = 20, scale = 2)
    val averagePrice: BigDecimal,

    @Column(name = "current_price", nullable = false, precision = 20, scale = 2)
    val currentPrice: BigDecimal,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun toDomain(): AssetHolding = AssetHolding(
        holdingId = holdingId,
        portfolioId = portfolioId,
        assetSymbol = assetSymbol,
        assetName = assetName,
        assetType = assetType,
        quantity = quantity,
        averagePrice = averagePrice,
        currentPrice = currentPrice,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun from(domain: AssetHolding): AssetHoldingEntity = AssetHoldingEntity(
            holdingId = domain.holdingId,
            portfolioId = domain.portfolioId,
            assetSymbol = domain.assetSymbol,
            assetName = domain.assetName,
            assetType = domain.assetType,
            quantity = domain.quantity,
            averagePrice = domain.averagePrice,
            currentPrice = domain.currentPrice,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt
        )
    }

    @PreUpdate
    fun onPreUpdate() {
        updatedAt = LocalDateTime.now()
    }
}
