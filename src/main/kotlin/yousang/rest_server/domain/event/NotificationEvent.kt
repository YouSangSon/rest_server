package yousang.rest_server.domain.event

/**
 * 알림 이벤트
 */
data class AlertNotificationEvent(
    val userId: Long,
    val title: String,
    val message: String,
    val priority: AlertPriority,
    val category: AlertCategory,
    val metadata: Map<String, Any> = emptyMap()
) : BaseDomainEvent() {
    override val eventType: String = "alert.notification"
}

enum class AlertPriority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

enum class AlertCategory {
    TRADE_EXECUTED,      // 거래 체결
    PRICE_ALERT,         // 가격 알림
    RISK_WARNING,        // 리스크 경고
    SYSTEM_ERROR,        // 시스템 오류
    DAILY_REPORT,        // 일일 리포트
    STRATEGY_UPDATE      // 전략 업데이트
}

/**
 * 리스크 경고 이벤트
 */
data class RiskWarningEvent(
    val userId: Long,
    val riskType: RiskType,
    val currentValue: Double,
    val threshold: Double,
    val message: String
) : BaseDomainEvent() {
    override val eventType: String = "alert.risk.warning"
}

enum class RiskType {
    DAILY_LOSS_LIMIT,       // 일일 손실 한도
    POSITION_SIZE_LIMIT,    // 포지션 크기 한도
    DRAWDOWN_LIMIT,         // 최대 손실률 한도
    LEVERAGE_LIMIT          // 레버리지 한도
}

/**
 * 거래 완료 알림 이벤트
 */
data class TradeCompletedNotificationEvent(
    val userId: Long,
    val symbol: String,
    val side: String,  // BUY, SELL
    val quantity: String,
    val price: String,
    val totalAmount: String,
    val strategyName: String?
) : BaseDomainEvent() {
    override val eventType: String = "alert.trade.completed"
}
