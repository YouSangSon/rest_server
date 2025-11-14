package yousang.rest_server.application.service.sns

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import yousang.rest_server.application.ports.out.*
import yousang.rest_server.domain.sns.*
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * 투자 포트폴리오 서비스
 */
@Service
@Transactional
class InvestmentPortfolioService(
    private val portfolioRepository: InvestmentPortfolioRepositoryPort,
    private val holdingRepository: AssetHoldingRepositoryPort,
    private val tradeHistoryRepository: TradeHistoryRepositoryPort
) {
    /**
     * 포트폴리오 생성
     */
    fun createPortfolio(
        userId: Long,
        name: String,
        description: String? = null,
        isPublic: Boolean = false
    ): InvestmentPortfolio {
        val portfolio = InvestmentPortfolio(
            portfolioId = 0,
            userId = userId,
            name = name,
            description = description,
            isPublic = isPublic,
            createdAt = LocalDateTime.now()
        )

        return portfolioRepository.save(portfolio)
    }

    /**
     * 포트폴리오 조회
     */
    @Transactional(readOnly = true)
    fun getPortfolio(portfolioId: Long): InvestmentPortfolio {
        return portfolioRepository.findById(portfolioId)
            ?: throw IllegalArgumentException("Portfolio not found")
    }

    /**
     * 사용자의 포트폴리오 목록 조회
     */
    @Transactional(readOnly = true)
    fun getUserPortfolios(userId: Long): List<InvestmentPortfolio> {
        return portfolioRepository.findByUserId(userId)
    }

    /**
     * 공개 포트폴리오 목록 조회
     */
    @Transactional(readOnly = true)
    fun getPublicPortfolios(limit: Int = 20, offset: Int = 0): List<InvestmentPortfolio> {
        return portfolioRepository.findPublicPortfolios(limit, offset)
    }

    /**
     * 포트폴리오 업데이트
     */
    fun updatePortfolio(
        portfolioId: Long,
        userId: Long,
        name: String? = null,
        description: String? = null,
        isPublic: Boolean? = null
    ): InvestmentPortfolio {
        val portfolio = portfolioRepository.findById(portfolioId)
            ?: throw IllegalArgumentException("Portfolio not found")

        if (portfolio.userId != userId) {
            throw IllegalArgumentException("Not authorized")
        }

        val updatedPortfolio = portfolio.update(name, description, isPublic)
        return portfolioRepository.save(updatedPortfolio)
    }

    /**
     * 포트폴리오 삭제
     */
    fun deletePortfolio(portfolioId: Long, userId: Long) {
        val portfolio = portfolioRepository.findById(portfolioId)
            ?: throw IllegalArgumentException("Portfolio not found")

        if (portfolio.userId != userId) {
            throw IllegalArgumentException("Not authorized")
        }

        // 관련 보유 자산 삭제
        holdingRepository.deleteByPortfolioId(portfolioId)

        portfolioRepository.delete(portfolioId)
    }

    /**
     * 자산 보유 추가
     */
    fun addHolding(
        portfolioId: Long,
        userId: Long,
        assetType: AssetType,
        symbol: String,
        quantity: BigDecimal,
        averagePrice: BigDecimal,
        currentPrice: BigDecimal
    ): AssetHolding {
        val portfolio = portfolioRepository.findById(portfolioId)
            ?: throw IllegalArgumentException("Portfolio not found")

        if (portfolio.userId != userId) {
            throw IllegalArgumentException("Not authorized")
        }

        // 기존 보유 자산 확인
        val existingHolding = holdingRepository.findBySymbol(portfolioId, symbol)
        if (existingHolding != null) {
            // 기존 보유 자산에 추가 매수
            val updatedHolding = existingHolding.buy(quantity, averagePrice)
                .updatePrice(currentPrice)
            val saved = holdingRepository.save(updatedHolding)

            // 포트폴리오 가치 재계산
            recalculatePortfolio(portfolioId)

            return saved
        }

        // 새 보유 자산 추가
        val holding = AssetHolding(
            holdingId = 0,
            portfolioId = portfolioId,
            assetType = assetType,
            symbol = symbol,
            quantity = quantity,
            averagePrice = averagePrice,
            currentPrice = currentPrice,
            createdAt = LocalDateTime.now()
        )

        val savedHolding = holdingRepository.save(holding)

        // 포트폴리오 가치 재계산
        recalculatePortfolio(portfolioId)

        return savedHolding
    }

    /**
     * 자산 보유 업데이트
     */
    fun updateHolding(
        holdingId: Long,
        userId: Long,
        currentPrice: BigDecimal
    ): AssetHolding {
        val holding = holdingRepository.findById(holdingId)
            ?: throw IllegalArgumentException("Holding not found")

        val portfolio = portfolioRepository.findById(holding.portfolioId)
            ?: throw IllegalArgumentException("Portfolio not found")

        if (portfolio.userId != userId) {
            throw IllegalArgumentException("Not authorized")
        }

        val updatedHolding = holding.updatePrice(currentPrice)
        val saved = holdingRepository.save(updatedHolding)

        // 포트폴리오 가치 재계산
        recalculatePortfolio(portfolio.portfolioId)

        return saved
    }

    /**
     * 자산 보유 삭제
     */
    fun deleteHolding(holdingId: Long, userId: Long) {
        val holding = holdingRepository.findById(holdingId)
            ?: throw IllegalArgumentException("Holding not found")

        val portfolio = portfolioRepository.findById(holding.portfolioId)
            ?: throw IllegalArgumentException("Portfolio not found")

        if (portfolio.userId != userId) {
            throw IllegalArgumentException("Not authorized")
        }

        holdingRepository.delete(holdingId)

        // 포트폴리오 가치 재계산
        recalculatePortfolio(portfolio.portfolioId)
    }

    /**
     * 포트폴리오의 보유 자산 목록 조회
     */
    @Transactional(readOnly = true)
    fun getHoldings(portfolioId: Long): List<AssetHolding> {
        return holdingRepository.findByPortfolioId(portfolioId)
    }

    /**
     * 거래 내역 기록
     */
    fun recordTrade(
        portfolioId: Long,
        userId: Long,
        assetType: AssetType,
        symbol: String,
        tradeType: TradeType,
        quantity: BigDecimal,
        price: BigDecimal,
        fee: BigDecimal = BigDecimal.ZERO,
        tradeDate: LocalDateTime
    ): TradeHistory {
        val portfolio = portfolioRepository.findById(portfolioId)
            ?: throw IllegalArgumentException("Portfolio not found")

        if (portfolio.userId != userId) {
            throw IllegalArgumentException("Not authorized")
        }

        val trade = TradeHistory(
            tradeId = 0,
            portfolioId = portfolioId,
            assetType = assetType,
            symbol = symbol,
            tradeType = tradeType,
            quantity = quantity,
            price = price,
            fee = fee,
            tradeDate = tradeDate,
            createdAt = LocalDateTime.now()
        )

        return tradeHistoryRepository.save(trade)
    }

    /**
     * 거래 내역 조회
     */
    @Transactional(readOnly = true)
    fun getTradeHistory(
        portfolioId: Long,
        limit: Int = 50,
        offset: Int = 0
    ): List<TradeHistory> {
        return tradeHistoryRepository.findByPortfolioId(portfolioId, limit, offset)
    }

    /**
     * 포트폴리오 가치 재계산
     */
    private fun recalculatePortfolio(portfolioId: Long) {
        val portfolio = portfolioRepository.findById(portfolioId) ?: return
        val holdings = holdingRepository.findByPortfolioId(portfolioId)

        val recalculated = portfolio.recalculate(holdings)
        portfolioRepository.save(recalculated)
    }

    /**
     * 포트폴리오 분석 정보 조회
     */
    @Transactional(readOnly = true)
    fun getPortfolioAnalytics(portfolioId: Long): PortfolioAnalytics {
        val portfolio = getPortfolio(portfolioId)
        val holdings = getHoldings(portfolioId)

        return PortfolioAnalytics(
            portfolioId = portfolio.portfolioId,
            totalValue = portfolio.totalValue,
            totalCost = portfolio.totalCost,
            totalReturn = portfolio.totalReturn,
            returnRate = portfolio.returnRate,
            holdingsCount = holdings.size,
            assetAllocation = calculateAssetAllocation(holdings)
        )
    }

    /**
     * 자산 배분 계산
     */
    private fun calculateAssetAllocation(holdings: List<AssetHolding>): Map<AssetType, BigDecimal> {
        val totalValue = holdings.sumOf { it.totalValue }

        if (totalValue == BigDecimal.ZERO) {
            return emptyMap()
        }

        return holdings.groupBy { it.assetType }
            .mapValues { (_, assets) ->
                val typeValue = assets.sumOf { it.totalValue }
                (typeValue / totalValue) * BigDecimal(100)
            }
    }
}

/**
 * 포트폴리오 분석 정보
 */
data class PortfolioAnalytics(
    val portfolioId: Long,
    val totalValue: BigDecimal,
    val totalCost: BigDecimal,
    val totalReturn: BigDecimal,
    val returnRate: BigDecimal,
    val holdingsCount: Int,
    val assetAllocation: Map<AssetType, BigDecimal>
)
