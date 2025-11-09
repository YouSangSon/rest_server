package yousang.rest_server.application.ports.`in`

import yousang.rest_server.application.service.PortfolioRiskMetrics
import yousang.rest_server.application.service.RiskAlert
import yousang.rest_server.domain.model.Portfolio
import java.math.BigDecimal

/**
 * 리스크 관리 Use Case
 */
interface ManageRiskUseCase {
    // Position Management
    fun canOpenPosition(userId: Long, symbol: String, quantity: BigDecimal, price: BigDecimal): Boolean
    fun calculateOptimalPositionSize(userId: Long, symbol: String, price: BigDecimal, riskPercent: Double): BigDecimal
    fun shouldClosePosition(userId: Long, symbol: String, currentPrice: BigDecimal): Boolean

    // Stop Loss / Take Profit
    fun setStopLoss(userId: Long, symbol: String, stopLossPrice: BigDecimal): Boolean
    fun setTakeProfit(userId: Long, symbol: String, takeProfitPrice: BigDecimal): Boolean
    fun calculateStopLossPrice(portfolio: Portfolio): BigDecimal
    fun calculateTakeProfitPrice(portfolio: Portfolio): BigDecimal

    // Risk Metrics
    fun calculatePortfolioRisk(userId: Long): PortfolioRiskMetrics
    fun calculateMaxDrawdown(userId: Long): Double
    fun calculateSharpeRatio(userId: Long): Double
    fun getVaR(userId: Long, confidenceLevel: Double = 0.95, timeHorizon: Int = 1): BigDecimal

    // Risk Monitoring
    fun checkRiskLimits(userId: Long): List<RiskAlert>
}
