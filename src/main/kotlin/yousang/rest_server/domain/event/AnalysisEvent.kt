package yousang.rest_server.domain.event

import java.math.BigDecimal

/**
 * 가격 예측 완료 이벤트
 */
data class PricePredictionCompletedEvent(
    val symbol: String,
    val currentPrice: BigDecimal,
    val predictions: Map<String, PredictionData>,  // "1h" -> PredictionData
    val modelVersion: String
) : BaseDomainEvent() {
    override val eventType: String = "analysis.prediction.completed"
}

data class PredictionData(
    val predictedPrice: BigDecimal,
    val confidence: Double,
    val changePercentage: BigDecimal
)

/**
 * 연관성 분석 업데이트 이벤트
 */
data class CorrelationUpdatedEvent(
    val symbol1: String,
    val symbol2: String,
    val correlationCoefficient: Double,  // -1.0 ~ +1.0
    val period: String  // "24h", "7d", "30d"
) : BaseDomainEvent() {
    override val eventType: String = "analysis.correlation.updated"
}

/**
 * 기술적 지표 업데이트 이벤트
 */
data class TechnicalIndicatorUpdatedEvent(
    val symbol: String,
    val exchange: String,
    val indicators: Map<String, BigDecimal>  // RSI, MACD, BB 등
) : BaseDomainEvent() {
    override val eventType: String = "analysis.technical.updated"
}
