package yousang.rest_server.adapter.out.persistence.jpa.trading

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * 포트폴리오 JPA Entity
 */
@Entity
@Table(
    name = "portfolios",
    uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "symbol"])]
)
class PortfolioJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(nullable = false, length = 20)
    val symbol: String,

    @Column(nullable = false, precision = 20, scale = 8)
    var quantity: BigDecimal,

    @Column(name = "avg_buy_price", precision = 20, scale = 8)
    var avgBuyPrice: BigDecimal? = null,

    @Column(name = "current_price", precision = 20, scale = 8)
    var currentPrice: BigDecimal? = null,

    @Column(name = "unrealized_pnl", precision = 20, scale = 8)
    var unrealizedPnl: BigDecimal? = null,

    @Column(name = "realized_pnl", precision = 20, scale = 8)
    var realizedPnl: BigDecimal = BigDecimal.ZERO,

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    /**
     * 미실현 손익을 계산합니다
     */
    fun calculateUnrealizedPnl(): BigDecimal? {
        if (currentPrice == null || avgBuyPrice == null) return null
        return (currentPrice!! - avgBuyPrice!!) * quantity
    }

    /**
     * 현재 가격을 업데이트하고 미실현 손익을 재계산합니다
     */
    fun updateCurrentPrice(newPrice: BigDecimal) {
        currentPrice = newPrice
        unrealizedPnl = calculateUnrealizedPnl()
        updatedAt = LocalDateTime.now()
    }

    /**
     * 포지션을 추가합니다 (매수)
     */
    fun addPosition(addedQty: BigDecimal, buyPrice: BigDecimal) {
        val totalCost = (avgBuyPrice ?: BigDecimal.ZERO) * quantity + buyPrice * addedQty
        quantity += addedQty
        avgBuyPrice = if (quantity > BigDecimal.ZERO) {
            totalCost / quantity
        } else {
            BigDecimal.ZERO
        }
        unrealizedPnl = calculateUnrealizedPnl()
        updatedAt = LocalDateTime.now()
    }

    /**
     * 포지션을 감소합니다 (매도)
     */
    fun reducePosition(reducedQty: BigDecimal, sellPrice: BigDecimal) {
        val pnl = (sellPrice - (avgBuyPrice ?: BigDecimal.ZERO)) * reducedQty
        realizedPnl += pnl
        quantity -= reducedQty
        unrealizedPnl = calculateUnrealizedPnl()
        updatedAt = LocalDateTime.now()
    }
}
