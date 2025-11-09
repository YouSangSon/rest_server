package yousang.rest_server.adapter.`in`.kafka

import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import yousang.rest_server.adapter.`in`.telegram.TelegramBotAdapter
import yousang.rest_server.adapter.out.external.slack.SlackAdapter
import yousang.rest_server.domain.event.*

/**
 * 알림 이벤트 리스너
 *
 * Kafka에서 도메인 이벤트를 수신하여 Telegram 및 Slack으로 알림 전송
 */
@Component
class NotificationEventListener(
    private val telegramBotAdapter: TelegramBotAdapter,
    private val slackAdapter: SlackAdapter
) {

    // ==================== Trading Events ====================

    @KafkaListener(topics = ["trading.order.submitted"], groupId = "notification-group")
    fun handleOrderSubmitted(event: OrderSubmittedEvent) {
        telegramBotAdapter.sendNotification(
            event.userId,
            "주문 제출",
            """
            |${event.symbol} ${event.side} 주문이 제출되었습니다.
            |수량: ${event.quantity}
            |가격: ${event.price ?: "시장가"}
            """.trimMargin()
        )

        slackAdapter.sendTradeNotification(
            event.userId,
            event.symbol,
            event.side.name,
            event.quantity,
            event.price ?: event.quantity
        )
    }

    @KafkaListener(topics = ["trading.order.filled"], groupId = "notification-group")
    fun handleOrderFilled(event: OrderFilledEvent) {
        telegramBotAdapter.sendTradeNotification(
            event.userId,
            event.symbol,
            event.side.name,
            event.executedQty,
            event.executedPrice
        )

        slackAdapter.sendTradeNotification(
            event.userId,
            event.symbol,
            event.side.name,
            event.executedQty,
            event.executedPrice
        )
    }

    @KafkaListener(topics = ["trading.order.cancelled"], groupId = "notification-group")
    fun handleOrderCancelled(event: OrderCancelledEvent) {
        telegramBotAdapter.sendNotification(
            event.userId,
            "주문 취소",
            "${event.symbol} 주문이 취소되었습니다."
        )
    }

    @KafkaListener(topics = ["trading.order.failed"], groupId = "notification-group")
    fun handleOrderFailed(event: OrderFailedEvent) {
        telegramBotAdapter.sendNotification(
            event.userId,
            "주문 실패",
            """
            |${event.symbol} 주문이 실패했습니다.
            |사유: ${event.reason}
            """.trimMargin()
        )

        slackAdapter.sendRiskAlert(
            event.userId,
            event.symbol,
            "ORDER_FAILED",
            event.reason
        )
    }

    // ==================== Strategy Events ====================

    @KafkaListener(topics = ["analysis.signal.generated"], groupId = "notification-group")
    fun handleTradeSignal(event: TradeSignalGeneratedEvent) {
        telegramBotAdapter.sendStrategyAlert(
            0L, // 전략의 userId 필요 (이벤트에 추가 필요)
            event.strategyName,
            event.signal,
            event.reason
        )

        slackAdapter.sendStrategyAlert(
            0L,
            event.strategyName,
            event.signal,
            event.reason
        )
    }

    @KafkaListener(topics = ["trading.strategy.executed"], groupId = "notification-group")
    fun handleStrategyExecuted(event: StrategyExecutedEvent) {
        if (event.ordersGenerated > 0) {
            telegramBotAdapter.sendNotification(
                event.userId,
                "전략 실행",
                """
                |전략 '${event.strategyName}'이 실행되었습니다.
                |생성된 주문: ${event.ordersGenerated}개
                """.trimMargin()
            )
        }
    }

    // ==================== Risk Events ====================

    @KafkaListener(topics = ["alert.risk.limit_exceeded"], groupId = "notification-group")
    fun handleRiskLimitExceeded(event: RiskLimitExceededEvent) {
        telegramBotAdapter.sendRiskAlert(
            event.userId,
            event.symbol,
            "RISK_LIMIT_EXCEEDED",
            event.reason
        )

        slackAdapter.sendRiskAlert(
            event.userId,
            event.symbol,
            "RISK_LIMIT_EXCEEDED",
            event.reason
        )
    }

    @KafkaListener(topics = ["alert.risk.stop_loss"], groupId = "notification-group")
    fun handleStopLossTriggered(event: StopLossTriggeredEvent) {
        telegramBotAdapter.sendRiskAlert(
            event.userId,
            event.symbol,
            "STOP_LOSS_TRIGGERED",
            """
            |손절가 도달
            |현재가: ${event.currentPrice}
            |손절가: ${event.stopLossPrice}
            |수량: ${event.quantity}
            """.trimMargin()
        )

        slackAdapter.sendRiskAlert(
            event.userId,
            event.symbol,
            "STOP_LOSS_TRIGGERED",
            "Price: ${event.currentPrice}, Stop Loss: ${event.stopLossPrice}"
        )
    }

    // ==================== Portfolio Events ====================

    @KafkaListener(topics = ["trading.pnl.realized"], groupId = "notification-group")
    fun handlePnLRealized(event: PnLRealizedEvent) {
        val emoji = if (event.realizedPnL >= event.realizedPnL.toBigInteger().toBigDecimal()) "🎉" else "📉"

        telegramBotAdapter.sendNotification(
            event.userId,
            "$emoji 손익 실현",
            """
            |${event.symbol} 포지션 청산
            |매도가: ${event.sellPrice}
            |수량: ${event.sellQty}
            |실현 손익: ${event.realizedPnL}
            """.trimMargin()
        )
    }

    // ==================== Market Events ====================

    @KafkaListener(topics = ["market.price.updated"], groupId = "notification-group")
    fun handlePriceUpdated(event: PriceUpdatedEvent) {
        // 가격 업데이트는 너무 빈번하므로 특정 조건에서만 알림
        // 예: 급격한 가격 변동 시
    }

    @KafkaListener(topics = ["market.data.updated"], groupId = "notification-group")
    fun handleMarketDataUpdated(event: MarketDataUpdatedEvent) {
        // 시장 데이터 업데이트 (필요시 알림)
    }

    // ==================== News Events ====================

    @KafkaListener(topics = ["news.article.collected"], groupId = "notification-group")
    fun handleNewsCollected(event: NewsCollectedEvent) {
        // 뉴스 수집 완료 (중요 뉴스만 알림 가능)
    }

    @KafkaListener(topics = ["news.sentiment.analyzed"], groupId = "notification-group")
    fun handleSentimentAnalyzed(event: SentimentAnalyzedEvent) {
        // 감성 분석 완료 (강한 감성 신호 시 알림 가능)
        if (event.sentimentScore.abs() > event.sentimentScore.toBigInteger().toBigDecimal()) {
            // 강한 감성 신호 알림
        }
    }
}
