package yousang.rest_server.domain.sns

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime

/**
 * 투자 포트폴리오 도메인 모델
 * 사용자의 투자 자산을 관리하고 공유할 수 있는 포트폴리오
 */
data class InvestmentPortfolio(
    val portfolioId: Long,
    val userId: Long,
    val name: String,
    val description: String? = null,
    val isPublic: Boolean = false,
    val totalValue: BigDecimal = BigDecimal.ZERO,
    val totalCost: BigDecimal = BigDecimal.ZERO,
    val totalReturn: BigDecimal = BigDecimal.ZERO,
    val returnRate: BigDecimal = BigDecimal.ZERO,
    val followerCount: Int = 0,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    init {
        require(name.isNotBlank()) { "Portfolio name cannot be blank" }
        require(name.length <= 100) { "Portfolio name cannot exceed 100 characters" }
    }

    /**
     * 포트폴리오 정보 업데이트
     */
    fun update(
        name: String? = null,
        description: String? = null,
        isPublic: Boolean? = null
    ): InvestmentPortfolio {
        return copy(
            name = name ?: this.name,
            description = description ?: this.description,
            isPublic = isPublic ?: this.isPublic,
            updatedAt = LocalDateTime.now()
        )
    }

    /**
     * 포트폴리오 가치 재계산
     */
    fun recalculate(holdings: List<AssetHolding>): InvestmentPortfolio {
        val newTotalValue = holdings.sumOf { it.totalValue }
        val newTotalCost = holdings.sumOf { it.totalCost }
        val newTotalReturn = newTotalValue - newTotalCost
        val newReturnRate = if (newTotalCost > BigDecimal.ZERO) {
            (newTotalReturn.divide(newTotalCost, 4, RoundingMode.HALF_UP) * BigDecimal(100))
                .setScale(2, RoundingMode.HALF_UP)
        } else {
            BigDecimal.ZERO
        }

        return copy(
            totalValue = newTotalValue,
            totalCost = newTotalCost,
            totalReturn = newTotalReturn,
            returnRate = newReturnRate,
            updatedAt = LocalDateTime.now()
        )
    }

    /**
     * 팔로워 수 증가
     */
    fun incrementFollowers(): InvestmentPortfolio = copy(followerCount = followerCount + 1)

    /**
     * 팔로워 수 감소
     */
    fun decrementFollowers(): InvestmentPortfolio = copy(followerCount = maxOf(0, followerCount - 1))
}

/**
 * 자산 보유 도메인 모델
 */
data class AssetHolding(
    val holdingId: Long,
    val portfolioId: Long,
    val assetType: AssetType,
    val symbol: String,
    val quantity: BigDecimal,
    val averagePrice: BigDecimal,
    val currentPrice: BigDecimal,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    /**
     * 총 가치 계산 (현재 가격 * 수량)
     */
    val totalValue: BigDecimal
        get() = currentPrice * quantity

    /**
     * 총 비용 계산 (평균 매입 가격 * 수량)
     */
    val totalCost: BigDecimal
        get() = averagePrice * quantity

    /**
     * 미실현 손익 계산
     */
    val unrealizedGain: BigDecimal
        get() = totalValue - totalCost

    /**
     * 수익률 계산 (%)
     */
    val returnRate: BigDecimal
        get() = if (totalCost > BigDecimal.ZERO) {
            ((unrealizedGain / totalCost) * BigDecimal(100))
                .setScale(2, RoundingMode.HALF_UP)
        } else {
            BigDecimal.ZERO
        }

    init {
        require(symbol.isNotBlank()) { "Symbol cannot be blank" }
        require(quantity > BigDecimal.ZERO) { "Quantity must be greater than zero" }
        require(averagePrice >= BigDecimal.ZERO) { "Average price cannot be negative" }
        require(currentPrice >= BigDecimal.ZERO) { "Current price cannot be negative" }
    }

    /**
     * 현재 가격 업데이트
     */
    fun updatePrice(newPrice: BigDecimal): AssetHolding {
        require(newPrice >= BigDecimal.ZERO) { "Price cannot be negative" }
        return copy(currentPrice = newPrice, updatedAt = LocalDateTime.now())
    }

    /**
     * 매수 거래 추가
     */
    fun buy(quantity: BigDecimal, price: BigDecimal): AssetHolding {
        val totalQuantity = this.quantity + quantity
        val newAveragePrice = ((this.totalCost + (price * quantity)) / totalQuantity)
            .setScale(2, RoundingMode.HALF_UP)

        return copy(
            quantity = totalQuantity,
            averagePrice = newAveragePrice,
            updatedAt = LocalDateTime.now()
        )
    }

    /**
     * 매도 거래 추가
     */
    fun sell(quantity: BigDecimal): AssetHolding {
        require(quantity <= this.quantity) { "Cannot sell more than owned" }
        return copy(
            quantity = this.quantity - quantity,
            updatedAt = LocalDateTime.now()
        )
    }
}

/**
 * 자산 유형
 */
enum class AssetType {
    STOCK,      // 주식
    CRYPTO,     // 암호화폐
    ETF,        // 상장지수펀드
    BOND,       // 채권
    COMMODITY,  // 원자재
    FOREX       // 외환
}

/**
 * 거래 내역 도메인 모델
 */
data class TradeHistory(
    val tradeId: Long,
    val portfolioId: Long,
    val assetType: AssetType,
    val symbol: String,
    val tradeType: TradeType,
    val quantity: BigDecimal,
    val price: BigDecimal,
    val fee: BigDecimal = BigDecimal.ZERO,
    val tradeDate: LocalDateTime,
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    /**
     * 거래 총액 계산 (수수료 포함)
     */
    val totalAmount: BigDecimal
        get() = (price * quantity) + fee

    init {
        require(symbol.isNotBlank()) { "Symbol cannot be blank" }
        require(quantity > BigDecimal.ZERO) { "Quantity must be greater than zero" }
        require(price >= BigDecimal.ZERO) { "Price cannot be negative" }
        require(fee >= BigDecimal.ZERO) { "Fee cannot be negative" }
    }
}

/**
 * 거래 유형
 */
enum class TradeType {
    BUY,    // 매수
    SELL    // 매도
}
