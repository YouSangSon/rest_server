package yousang.rest_server.application.ports.out

import yousang.rest_server.domain.sns.*
import java.time.LocalDateTime

/**
 * 투자 포트폴리오 저장소 포트
 */
interface InvestmentPortfolioRepositoryPort {
    fun save(portfolio: InvestmentPortfolio): InvestmentPortfolio
    fun findById(portfolioId: Long): InvestmentPortfolio?
    fun findByUserId(userId: Long): List<InvestmentPortfolio>
    fun findPublicPortfolios(limit: Int = 20, offset: Int = 0): List<InvestmentPortfolio>
    fun delete(portfolioId: Long): Boolean
    fun search(query: String, limit: Int = 20, offset: Int = 0): List<InvestmentPortfolio>
}

/**
 * 자산 보유 저장소 포트
 */
interface AssetHoldingRepositoryPort {
    fun save(holding: AssetHolding): AssetHolding
    fun findById(holdingId: Long): AssetHolding?
    fun findByPortfolioId(portfolioId: Long): List<AssetHolding>
    fun findBySymbol(portfolioId: Long, symbol: String): AssetHolding?
    fun delete(holdingId: Long): Boolean
    fun deleteByPortfolioId(portfolioId: Long): Int
}

/**
 * 거래 내역 저장소 포트
 */
interface TradeHistoryRepositoryPort {
    fun save(trade: TradeHistory): TradeHistory
    fun findById(tradeId: Long): TradeHistory?
    fun findByPortfolioId(
        portfolioId: Long,
        limit: Int = 50,
        offset: Int = 0
    ): List<TradeHistory>
    fun findByPortfolioIdAndSymbol(
        portfolioId: Long,
        symbol: String,
        limit: Int = 50
    ): List<TradeHistory>
    fun findByDateRange(
        portfolioId: Long,
        from: LocalDateTime,
        to: LocalDateTime
    ): List<TradeHistory>
    fun delete(tradeId: Long): Boolean
}

/**
 * 투자 게시물 저장소 포트
 */
interface InvestmentPostRepositoryPort {
    fun save(investmentPost: InvestmentPost): InvestmentPost
    fun findById(investmentPostId: Long): InvestmentPost?
    fun findByUserId(userId: Long, limit: Int = 20, offset: Int = 0): List<InvestmentPost>
    fun findByPortfolioId(portfolioId: Long, limit: Int = 20, offset: Int = 0): List<InvestmentPost>
    fun findByType(postType: InvestmentPostType, limit: Int = 20, offset: Int = 0): List<InvestmentPost>
    fun findBySymbol(symbol: String, limit: Int = 20, offset: Int = 0): List<InvestmentPost>
    fun findFeed(userId: Long, limit: Int = 20, offset: Int = 0): List<InvestmentPost>
    fun delete(investmentPostId: Long): Boolean
    fun search(query: String, limit: Int = 20, offset: Int = 0): List<InvestmentPost>
}

/**
 * 포트폴리오 팔로우 저장소 포트
 */
interface PortfolioFollowerRepositoryPort {
    fun save(portfolioFollower: PortfolioFollower): PortfolioFollower
    fun delete(portfolioId: Long, userId: Long): Boolean
    fun findByPortfolioId(portfolioId: Long, limit: Int = 50, offset: Int = 0): List<PortfolioFollower>
    fun exists(portfolioId: Long, userId: Long): Boolean
    fun countByPortfolioId(portfolioId: Long): Long
}

/**
 * 워치리스트 저장소 포트
 */
interface WatchlistRepositoryPort {
    fun save(watchlistItem: WatchlistItem): WatchlistItem
    fun findById(watchlistId: Long): WatchlistItem?
    fun findByUserId(userId: Long): List<WatchlistItem>
    fun findActiveByUserId(userId: Long): List<WatchlistItem>
    fun findBySymbol(userId: Long, symbol: String): WatchlistItem?
    fun delete(watchlistId: Long): Boolean
}
