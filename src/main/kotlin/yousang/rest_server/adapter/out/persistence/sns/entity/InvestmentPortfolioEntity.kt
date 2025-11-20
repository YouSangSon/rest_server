package yousang.rest_server.adapter.out.persistence.sns.entity

import jakarta.persistence.*
import yousang.rest_server.domain.sns.InvestmentPortfolio
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(
    name = "sns_investment_portfolios",
    indexes = [
        Index(name = "idx_user_id", columnList = "user_id"),
        Index(name = "idx_is_public", columnList = "is_public"),
        Index(name = "idx_created_at", columnList = "created_at")
    ]
)
class InvestmentPortfolioEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "portfolio_id")
    val portfolioId: Long = 0,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(nullable = false, length = 100)
    val name: String,

    @Column(columnDefinition = "TEXT")
    val description: String? = null,

    @Column(name = "total_value", nullable = false, precision = 20, scale = 2)
    val totalValue: BigDecimal = BigDecimal.ZERO,

    @Column(name = "total_return", nullable = false, precision = 20, scale = 2)
    val totalReturn: BigDecimal = BigDecimal.ZERO,

    @Column(name = "return_rate", nullable = false, precision = 10, scale = 4)
    val returnRate: BigDecimal = BigDecimal.ZERO,

    @Column(name = "is_public", nullable = false)
    val isPublic: Boolean = false,

    @Column(nullable = false, length = 10)
    val currency: String = "USD",

    @Column(name = "follower_count", nullable = false)
    val followerCount: Int = 0,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun toDomain(): InvestmentPortfolio = InvestmentPortfolio(
        portfolioId = portfolioId,
        userId = userId,
        name = name,
        description = description,
        totalValue = totalValue,
        totalReturn = totalReturn,
        returnRate = returnRate,
        isPublic = isPublic,
        currency = currency,
        followerCount = followerCount,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun from(domain: InvestmentPortfolio): InvestmentPortfolioEntity = InvestmentPortfolioEntity(
            portfolioId = domain.portfolioId,
            userId = domain.userId,
            name = domain.name,
            description = domain.description,
            totalValue = domain.totalValue,
            totalReturn = domain.totalReturn,
            returnRate = domain.returnRate,
            isPublic = domain.isPublic,
            currency = domain.currency,
            followerCount = domain.followerCount,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt
        )
    }

    @PreUpdate
    fun onPreUpdate() {
        updatedAt = LocalDateTime.now()
    }
}
