package yousang.rest_server.application.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import yousang.rest_server.application.ports.`in`.ManageRiskUseCase
import yousang.rest_server.application.ports.out.*
import yousang.rest_server.domain.event.RiskLimitExceededEvent
import yousang.rest_server.domain.event.StopLossTriggeredEvent
import yousang.rest_server.domain.model.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime

/**
 * 리스크 관리 서비스
 *
 * 포지션 크기 제한, 손절/익절, 드로우다운 관리
 */
@Service
@Transactional
class RiskManagementService(
    private val portfolioRepositoryPort: PortfolioRepositoryPort,
    private val orderRepositoryPort: OrderRepositoryPort,
    private val eventPublisherPort: EventPublisherPort
) : ManageRiskUseCase {

    companion object {
        // 리스크 파라미터 (설정 가능하도록 개선 필요)
        val MAX_POSITION_SIZE_PERCENT = BigDecimal("0.20") // 총 자산의 20%
        val MAX_DAILY_LOSS_PERCENT = BigDecimal("0.05") // 일일 최대 손실 5%
        val DEFAULT_STOP_LOSS_PERCENT = BigDecimal("0.03") // 기본 손절 3%
        val DEFAULT_TAKE_PROFIT_PERCENT = BigDecimal("0.10") // 기본 익절 10%
        val MAX_LEVERAGE = BigDecimal("3") // 최대 레버리지 3배
    }

    // ==================== Position Size Management ====================

    override fun canOpenPosition(
        userId: Long,
        symbol: String,
        quantity: BigDecimal,
        price: BigDecimal
    ): Boolean {
        // 1. 포지션 크기 제한 확인
        val positionValue = quantity * price
        val totalPortfolioValue = getTotalPortfolioValue(userId)

        if (totalPortfolioValue > BigDecimal.ZERO) {
            val positionSizePercent = positionValue.divide(totalPortfolioValue, 4, RoundingMode.HALF_UP)
            if (positionSizePercent > MAX_POSITION_SIZE_PERCENT) {
                publishRiskLimitExceeded(
                    userId, symbol,
                    "Position size ${positionSizePercent.multiply(BigDecimal(100))}% exceeds limit ${MAX_POSITION_SIZE_PERCENT.multiply(BigDecimal(100))}%"
                )
                return false
            }
        }

        // 2. 일일 손실 제한 확인
        val dailyPnL = calculateDailyPnL(userId)
        if (dailyPnL < BigDecimal.ZERO) {
            val dailyLossPercent = dailyPnL.abs().divide(totalPortfolioValue, 4, RoundingMode.HALF_UP)
            if (dailyLossPercent > MAX_DAILY_LOSS_PERCENT) {
                publishRiskLimitExceeded(
                    userId, symbol,
                    "Daily loss ${dailyLossPercent.multiply(BigDecimal(100))}% exceeds limit ${MAX_DAILY_LOSS_PERCENT.multiply(BigDecimal(100))}%"
                )
                return false
            }
        }

        // 3. 심볼별 중복 포지션 확인
        val existingPosition = portfolioRepositoryPort.findByUserIdAndSymbol(userId, symbol)
        if (existingPosition != null && existingPosition.quantity > BigDecimal.ZERO) {
            // 이미 포지션이 있으면 추가 매수 제한 (DCA 전략 제외)
            // 추후 전략별로 다르게 처리 가능
        }

        return true
    }

    override fun calculateOptimalPositionSize(
        userId: Long,
        symbol: String,
        price: BigDecimal,
        riskPercent: Double
    ): BigDecimal {
        val totalValue = getTotalPortfolioValue(userId)
        val riskAmount = totalValue * riskPercent.toBigDecimal() / BigDecimal(100)

        // Kelly Criterion 또는 Fixed Fractional Position Sizing 적용 가능
        // 여기서는 간단히 리스크 금액 기반 계산
        val positionSize = riskAmount.divide(price, 8, RoundingMode.DOWN)

        return positionSize
    }

    override fun shouldClosePosition(userId: Long, symbol: String, currentPrice: BigDecimal): Boolean {
        val portfolio = portfolioRepositoryPort.findByUserIdAndSymbol(userId, symbol)
            ?: return false

        if (portfolio.quantity <= BigDecimal.ZERO) {
            return false
        }

        // 손절 확인
        val stopLossPrice = calculateStopLossPrice(portfolio)
        if (currentPrice <= stopLossPrice) {
            eventPublisherPort.publish(
                StopLossTriggeredEvent(
                    userId = userId,
                    symbol = symbol,
                    currentPrice = currentPrice,
                    stopLossPrice = stopLossPrice,
                    quantity = portfolio.quantity,
                    timestamp = LocalDateTime.now()
                )
            )
            return true
        }

        // 익절 확인
        val takeProfitPrice = calculateTakeProfitPrice(portfolio)
        if (currentPrice >= takeProfitPrice) {
            return true
        }

        return false
    }

    // ==================== Stop Loss / Take Profit ====================

    override fun setStopLoss(userId: Long, symbol: String, stopLossPrice: BigDecimal): Boolean {
        val portfolio = portfolioRepositoryPort.findByUserIdAndSymbol(userId, symbol)
            ?: return false

        // Stop Loss 주문 생성 (STOP_LOSS 주문 타입 사용)
        // 실제 거래소에 Stop Loss 주문 전송
        return true
    }

    override fun setTakeProfit(userId: Long, symbol: String, takeProfitPrice: BigDecimal): Boolean {
        val portfolio = portfolioRepositoryPort.findByUserIdAndSymbol(userId, symbol)
            ?: return false

        // Take Profit 주문 생성
        return true
    }

    override fun calculateStopLossPrice(portfolio: Portfolio): BigDecimal {
        val avgBuyPrice = portfolio.avgBuyPrice ?: BigDecimal.ZERO
        return avgBuyPrice * (BigDecimal.ONE - DEFAULT_STOP_LOSS_PERCENT)
    }

    override fun calculateTakeProfitPrice(portfolio: Portfolio): BigDecimal {
        val avgBuyPrice = portfolio.avgBuyPrice ?: BigDecimal.ZERO
        return avgBuyPrice * (BigDecimal.ONE + DEFAULT_TAKE_PROFIT_PERCENT)
    }

    // ==================== Risk Metrics ====================

    override fun calculatePortfolioRisk(userId: Long): PortfolioRiskMetrics {
        val portfolios = portfolioRepositoryPort.findByUserId(userId)
        val totalValue = getTotalPortfolioValue(userId)

        if (totalValue == BigDecimal.ZERO) {
            return PortfolioRiskMetrics(
                totalValue = BigDecimal.ZERO,
                totalRisk = BigDecimal.ZERO,
                riskPercent = 0.0,
                maxDrawdown = 0.0,
                sharpeRatio = 0.0,
                diversificationScore = 0.0
            )
        }

        // 총 리스크 계산 (각 포지션의 잠재 손실 합계)
        val totalRisk = portfolios.sumOf { portfolio ->
            val positionValue = portfolio.quantity * (portfolio.avgBuyPrice ?: BigDecimal.ZERO)
            positionValue * DEFAULT_STOP_LOSS_PERCENT
        }

        val riskPercent = totalRisk.divide(totalValue, 4, RoundingMode.HALF_UP) * BigDecimal(100)

        // 분산 점수 (보유 심볼 수 기반 간단 계산)
        val diversificationScore = calculateDiversificationScore(portfolios)

        return PortfolioRiskMetrics(
            totalValue = totalValue,
            totalRisk = totalRisk,
            riskPercent = riskPercent.toDouble(),
            maxDrawdown = calculateMaxDrawdown(userId),
            sharpeRatio = calculateSharpeRatio(userId),
            diversificationScore = diversificationScore
        )
    }

    override fun calculateMaxDrawdown(userId: Long): Double {
        // 최대 낙폭 계산 (과거 주문 기록 기반)
        // 추후 구현: 과거 포트폴리오 가치 추적 필요
        return 0.0
    }

    override fun calculateSharpeRatio(userId: Long): Double {
        // 샤프 비율 계산 (수익률 / 변동성)
        // 추후 구현: 일별 수익률 데이터 필요
        return 0.0
    }

    override fun getVaR(userId: Long, confidenceLevel: Double, timeHorizon: Int): BigDecimal {
        // Value at Risk 계산
        // 추후 구현: 과거 수익률 분포 기반
        return BigDecimal.ZERO
    }

    // ==================== Risk Monitoring ====================

    override fun checkRiskLimits(userId: Long): List<RiskAlert> {
        val alerts = mutableListOf<RiskAlert>()

        // 1. 포지션 크기 제한 확인
        val portfolios = portfolioRepositoryPort.findByUserId(userId)
        val totalValue = getTotalPortfolioValue(userId)

        portfolios.forEach { portfolio ->
            val positionValue = portfolio.quantity * (portfolio.avgBuyPrice ?: BigDecimal.ZERO)
            val positionPercent = if (totalValue > BigDecimal.ZERO) {
                positionValue.divide(totalValue, 4, RoundingMode.HALF_UP)
            } else {
                BigDecimal.ZERO
            }

            if (positionPercent > MAX_POSITION_SIZE_PERCENT) {
                alerts.add(
                    RiskAlert(
                        type = RiskAlertType.POSITION_SIZE_EXCEEDED,
                        symbol = portfolio.symbol,
                        message = "Position size ${positionPercent.multiply(BigDecimal(100))}% exceeds limit",
                        severity = RiskAlertSeverity.HIGH
                    )
                )
            }
        }

        // 2. 일일 손실 제한 확인
        val dailyPnL = calculateDailyPnL(userId)
        if (dailyPnL < BigDecimal.ZERO && totalValue > BigDecimal.ZERO) {
            val lossPercent = dailyPnL.abs().divide(totalValue, 4, RoundingMode.HALF_UP)
            if (lossPercent > MAX_DAILY_LOSS_PERCENT) {
                alerts.add(
                    RiskAlert(
                        type = RiskAlertType.DAILY_LOSS_LIMIT,
                        symbol = "ALL",
                        message = "Daily loss ${lossPercent.multiply(BigDecimal(100))}% exceeds limit",
                        severity = RiskAlertSeverity.CRITICAL
                    )
                )
            } else if (lossPercent > MAX_DAILY_LOSS_PERCENT * BigDecimal("0.8")) {
                alerts.add(
                    RiskAlert(
                        type = RiskAlertType.DAILY_LOSS_WARNING,
                        symbol = "ALL",
                        message = "Approaching daily loss limit: ${lossPercent.multiply(BigDecimal(100))}%",
                        severity = RiskAlertSeverity.MEDIUM
                    )
                )
            }
        }

        return alerts
    }

    // ==================== Helper Methods ====================

    private fun getTotalPortfolioValue(userId: Long): BigDecimal {
        val portfolios = portfolioRepositoryPort.findByUserId(userId)
        return portfolios.sumOf { portfolio ->
            portfolio.quantity * (portfolio.avgBuyPrice ?: BigDecimal.ZERO)
        }
    }

    private fun calculateDailyPnL(userId: Long): BigDecimal {
        val today = LocalDateTime.now().toLocalDate().atStartOfDay()
        val tomorrow = today.plusDays(1)

        val orders = orderRepositoryPort.findByDateRange(today, tomorrow)
        val userOrders = orders.filter { it.userId == userId && it.status == OrderStatus.FILLED }

        return userOrders.sumOf { order ->
            when (order.side) {
                OrderSide.BUY -> -order.executedQty * (order.price ?: BigDecimal.ZERO)
                OrderSide.SELL -> order.executedQty * (order.price ?: BigDecimal.ZERO)
            }
        }
    }

    private fun calculateDiversificationScore(portfolios: List<Portfolio>): Double {
        if (portfolios.isEmpty()) return 0.0

        // 간단한 분산 점수: 보유 심볼 수와 각 포지션의 균등성 기반
        val totalValue = portfolios.sumOf { it.quantity * (it.avgBuyPrice ?: BigDecimal.ZERO) }
        if (totalValue == BigDecimal.ZERO) return 0.0

        // Herfindahl-Hirschman Index (HHI) 계산
        val hhi = portfolios.sumOf { portfolio ->
            val weight = (portfolio.quantity * (portfolio.avgBuyPrice ?: BigDecimal.ZERO)) / totalValue
            (weight * weight).toDouble()
        }

        // 분산 점수: 1 - HHI (1에 가까울수록 잘 분산됨)
        return (1.0 - hhi) * 100
    }

    private fun publishRiskLimitExceeded(userId: Long, symbol: String, reason: String) {
        eventPublisherPort.publish(
            RiskLimitExceededEvent(
                userId = userId,
                symbol = symbol,
                reason = reason,
                timestamp = LocalDateTime.now()
            )
        )
    }

    private fun Double.toBigDecimal(): BigDecimal = BigDecimal.valueOf(this)
}

/**
 * 포트폴리오 리스크 메트릭
 */
data class PortfolioRiskMetrics(
    val totalValue: BigDecimal,
    val totalRisk: BigDecimal,
    val riskPercent: Double,
    val maxDrawdown: Double,
    val sharpeRatio: Double,
    val diversificationScore: Double
)

/**
 * 리스크 알림
 */
data class RiskAlert(
    val type: RiskAlertType,
    val symbol: String,
    val message: String,
    val severity: RiskAlertSeverity,
    val timestamp: LocalDateTime = LocalDateTime.now()
)

enum class RiskAlertType {
    POSITION_SIZE_EXCEEDED,
    DAILY_LOSS_LIMIT,
    DAILY_LOSS_WARNING,
    STOP_LOSS_TRIGGERED,
    MARGIN_CALL,
    LEVERAGE_EXCEEDED
}

enum class RiskAlertSeverity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}
