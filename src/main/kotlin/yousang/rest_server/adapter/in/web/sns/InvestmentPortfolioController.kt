package yousang.rest_server.adapter.`in`.web.sns

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import yousang.rest_server.application.service.sns.InvestmentPortfolioService
import yousang.rest_server.application.service.sns.PortfolioAnalytics
import yousang.rest_server.domain.sns.*
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * 투자 포트폴리오 API 컨트롤러
 * /api/v1/sns/portfolios
 */
@RestController
@RequestMapping("/api/v1/sns/portfolios")
class InvestmentPortfolioController(
    private val portfolioService: InvestmentPortfolioService
) {
    /**
     * GET /api/v1/sns/portfolios
     * 사용자의 포트폴리오 목록 조회
     */
    @GetMapping
    fun getPortfolios(
        @RequestAttribute("userId") userId: Long
    ): ResponseEntity<List<PortfolioDto>> {
        val portfolios = portfolioService.getUserPortfolios(userId)
        return ResponseEntity.ok(portfolios.map { PortfolioDto.from(it) })
    }

    /**
     * GET /api/v1/sns/portfolios/public
     * 공개 포트폴리오 목록 조회
     */
    @GetMapping("/public")
    fun getPublicPortfolios(
        @RequestParam(defaultValue = "20") limit: Int,
        @RequestParam(defaultValue = "0") offset: Int
    ): ResponseEntity<PortfoliosResponse> {
        val portfolios = portfolioService.getPublicPortfolios(limit, offset)
        return ResponseEntity.ok(PortfoliosResponse(
            data = portfolios.map { PortfolioDto.from(it) },
            meta = PaginationMeta(limit, offset, portfolios.size >= limit)
        ))
    }

    /**
     * GET /api/v1/sns/portfolios/{portfolioId}
     * 포트폴리오 상세 조회
     */
    @GetMapping("/{portfolioId}")
    fun getPortfolio(@PathVariable portfolioId: Long): ResponseEntity<PortfolioDto> {
        val portfolio = portfolioService.getPortfolio(portfolioId)
        return ResponseEntity.ok(PortfolioDto.from(portfolio))
    }

    /**
     * POST /api/v1/sns/portfolios
     * 포트폴리오 생성
     */
    @PostMapping
    fun createPortfolio(
        @RequestAttribute("userId") userId: Long,
        @RequestBody request: CreatePortfolioRequest
    ): ResponseEntity<PortfolioDto> {
        val portfolio = portfolioService.createPortfolio(
            userId = userId,
            name = request.name,
            description = request.description,
            isPublic = request.isPublic
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(PortfolioDto.from(portfolio))
    }

    /**
     * PUT /api/v1/sns/portfolios/{portfolioId}
     * 포트폴리오 업데이트
     */
    @PutMapping("/{portfolioId}")
    fun updatePortfolio(
        @PathVariable portfolioId: Long,
        @RequestAttribute("userId") userId: Long,
        @RequestBody request: UpdatePortfolioRequest
    ): ResponseEntity<PortfolioDto> {
        val portfolio = portfolioService.updatePortfolio(
            portfolioId = portfolioId,
            userId = userId,
            name = request.name,
            description = request.description,
            isPublic = request.isPublic
        )
        return ResponseEntity.ok(PortfolioDto.from(portfolio))
    }

    /**
     * DELETE /api/v1/sns/portfolios/{portfolioId}
     * 포트폴리오 삭제
     */
    @DeleteMapping("/{portfolioId}")
    fun deletePortfolio(
        @PathVariable portfolioId: Long,
        @RequestAttribute("userId") userId: Long
    ): ResponseEntity<Map<String, String>> {
        portfolioService.deletePortfolio(portfolioId, userId)
        return ResponseEntity.ok(mapOf("message" to "Portfolio deleted successfully"))
    }

    /**
     * POST /api/v1/sns/portfolios/{portfolioId}/holdings
     * 자산 보유 추가
     */
    @PostMapping("/{portfolioId}/holdings")
    fun addHolding(
        @PathVariable portfolioId: Long,
        @RequestAttribute("userId") userId: Long,
        @RequestBody request: AddHoldingRequest
    ): ResponseEntity<HoldingDto> {
        val holding = portfolioService.addHolding(
            portfolioId = portfolioId,
            userId = userId,
            assetType = request.assetType,
            symbol = request.symbol,
            quantity = request.quantity,
            averagePrice = request.averagePrice,
            currentPrice = request.currentPrice
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(HoldingDto.from(holding))
    }

    /**
     * GET /api/v1/sns/portfolios/{portfolioId}/holdings
     * 포트폴리오의 보유 자산 목록 조회
     */
    @GetMapping("/{portfolioId}/holdings")
    fun getHoldings(@PathVariable portfolioId: Long): ResponseEntity<List<HoldingDto>> {
        val holdings = portfolioService.getHoldings(portfolioId)
        return ResponseEntity.ok(holdings.map { HoldingDto.from(it) })
    }

    /**
     * PUT /api/v1/sns/portfolios/{portfolioId}/holdings/{holdingId}
     * 자산 보유 업데이트
     */
    @PutMapping("/{portfolioId}/holdings/{holdingId}")
    fun updateHolding(
        @PathVariable portfolioId: Long,
        @PathVariable holdingId: Long,
        @RequestAttribute("userId") userId: Long,
        @RequestBody request: UpdateHoldingRequest
    ): ResponseEntity<HoldingDto> {
        val holding = portfolioService.updateHolding(holdingId, userId, request.currentPrice)
        return ResponseEntity.ok(HoldingDto.from(holding))
    }

    /**
     * DELETE /api/v1/sns/portfolios/{portfolioId}/holdings/{holdingId}
     * 자산 보유 삭제
     */
    @DeleteMapping("/{portfolioId}/holdings/{holdingId}")
    fun deleteHolding(
        @PathVariable portfolioId: Long,
        @PathVariable holdingId: Long,
        @RequestAttribute("userId") userId: Long
    ): ResponseEntity<Map<String, String>> {
        portfolioService.deleteHolding(holdingId, userId)
        return ResponseEntity.ok(mapOf("message" to "Holding deleted successfully"))
    }

    /**
     * GET /api/v1/sns/portfolios/{portfolioId}/analytics
     * 포트폴리오 분석 정보 조회
     */
    @GetMapping("/{portfolioId}/analytics")
    fun getAnalytics(@PathVariable portfolioId: Long): ResponseEntity<PortfolioAnalytics> {
        val analytics = portfolioService.getPortfolioAnalytics(portfolioId)
        return ResponseEntity.ok(analytics)
    }

    /**
     * GET /api/v1/sns/portfolios/{portfolioId}/trades
     * 거래 내역 조회
     */
    @GetMapping("/{portfolioId}/trades")
    fun getTradeHistory(
        @PathVariable portfolioId: Long,
        @RequestParam(defaultValue = "50") limit: Int,
        @RequestParam(defaultValue = "0") offset: Int
    ): ResponseEntity<TradesResponse> {
        val trades = portfolioService.getTradeHistory(portfolioId, limit, offset)
        return ResponseEntity.ok(TradesResponse(
            data = trades.map { TradeDto.from(it) },
            meta = PaginationMeta(limit, offset, trades.size >= limit)
        ))
    }

    /**
     * POST /api/v1/sns/portfolios/{portfolioId}/trades
     * 거래 내역 기록
     */
    @PostMapping("/{portfolioId}/trades")
    fun recordTrade(
        @PathVariable portfolioId: Long,
        @RequestAttribute("userId") userId: Long,
        @RequestBody request: RecordTradeRequest
    ): ResponseEntity<TradeDto> {
        val trade = portfolioService.recordTrade(
            portfolioId = portfolioId,
            userId = userId,
            assetType = request.assetType,
            symbol = request.symbol,
            tradeType = request.tradeType,
            quantity = request.quantity,
            price = request.price,
            fee = request.fee,
            tradeDate = LocalDateTime.parse(request.tradeDate)
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(TradeDto.from(trade))
    }
}

// DTOs
data class PortfolioDto(
    val portfolioId: Long,
    val userId: Long,
    val name: String,
    val description: String?,
    val isPublic: Boolean,
    val totalValue: BigDecimal,
    val totalReturn: BigDecimal,
    val returnRate: BigDecimal,
    val followerCount: Int
) {
    companion object {
        fun from(portfolio: InvestmentPortfolio): PortfolioDto {
            return PortfolioDto(
                portfolioId = portfolio.portfolioId,
                userId = portfolio.userId,
                name = portfolio.name,
                description = portfolio.description,
                isPublic = portfolio.isPublic,
                totalValue = portfolio.totalValue,
                totalReturn = portfolio.totalReturn,
                returnRate = portfolio.returnRate,
                followerCount = portfolio.followerCount
            )
        }
    }
}

data class HoldingDto(
    val holdingId: Long,
    val portfolioId: Long,
    val assetType: AssetType,
    val symbol: String,
    val quantity: BigDecimal,
    val averagePrice: BigDecimal,
    val currentPrice: BigDecimal,
    val totalValue: BigDecimal,
    val unrealizedGain: BigDecimal,
    val returnRate: BigDecimal
) {
    companion object {
        fun from(holding: AssetHolding): HoldingDto {
            return HoldingDto(
                holdingId = holding.holdingId,
                portfolioId = holding.portfolioId,
                assetType = holding.assetType,
                symbol = holding.symbol,
                quantity = holding.quantity,
                averagePrice = holding.averagePrice,
                currentPrice = holding.currentPrice,
                totalValue = holding.totalValue,
                unrealizedGain = holding.unrealizedGain,
                returnRate = holding.returnRate
            )
        }
    }
}

data class TradeDto(
    val tradeId: Long,
    val portfolioId: Long,
    val assetType: AssetType,
    val symbol: String,
    val tradeType: TradeType,
    val quantity: BigDecimal,
    val price: BigDecimal,
    val fee: BigDecimal,
    val tradeDate: String
) {
    companion object {
        fun from(trade: TradeHistory): TradeDto {
            return TradeDto(
                tradeId = trade.tradeId,
                portfolioId = trade.portfolioId,
                assetType = trade.assetType,
                symbol = trade.symbol,
                tradeType = trade.tradeType,
                quantity = trade.quantity,
                price = trade.price,
                fee = trade.fee,
                tradeDate = trade.tradeDate.toString()
            )
        }
    }
}

// Request DTOs
data class CreatePortfolioRequest(
    val name: String,
    val description: String?,
    val isPublic: Boolean
)

data class UpdatePortfolioRequest(
    val name: String?,
    val description: String?,
    val isPublic: Boolean?
)

data class AddHoldingRequest(
    val assetType: AssetType,
    val symbol: String,
    val quantity: BigDecimal,
    val averagePrice: BigDecimal,
    val currentPrice: BigDecimal
)

data class UpdateHoldingRequest(
    val currentPrice: BigDecimal
)

data class RecordTradeRequest(
    val assetType: AssetType,
    val symbol: String,
    val tradeType: TradeType,
    val quantity: BigDecimal,
    val price: BigDecimal,
    val fee: BigDecimal,
    val tradeDate: String
)

// Response DTOs
data class PortfoliosResponse(
    val data: List<PortfolioDto>,
    val meta: PaginationMeta
)

data class TradesResponse(
    val data: List<TradeDto>,
    val meta: PaginationMeta
)
