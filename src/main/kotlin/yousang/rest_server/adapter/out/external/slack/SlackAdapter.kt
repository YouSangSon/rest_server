package yousang.rest_server.adapter.out.external.slack

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import yousang.rest_server.application.ports.out.NotificationPort
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Slack 알림 어댑터
 *
 * Slack Webhook을 통한 알림 전송
 */
@Component
class SlackAdapter(
    private val restTemplate: RestTemplate,
    private val objectMapper: ObjectMapper,
    @Value("\${slack.webhook-url:}") private val webhookUrl: String
) : NotificationPort {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    override fun sendNotification(userId: Long, title: String, message: String) {
        val slackMessage = SlackMessage(
            text = "*$title*",
            blocks = listOf(
                SlackBlock(
                    type = "header",
                    text = SlackText("plain_text", title)
                ),
                SlackBlock(
                    type = "section",
                    text = SlackText("mrkdwn", message)
                ),
                SlackBlock(
                    type = "context",
                    elements = listOf(
                        SlackText("mrkdwn", "User ID: $userId | ${LocalDateTime.now().format(dateFormatter)}")
                    )
                )
            )
        )

        sendToSlack(slackMessage)
    }

    override fun sendTradeNotification(
        userId: Long,
        symbol: String,
        side: String,
        quantity: BigDecimal,
        price: BigDecimal
    ) {
        val totalValue = quantity * price
        val color = if (side == "BUY") "#36a64f" else "#ff0000" // Green for BUY, Red for SELL
        val emoji = if (side == "BUY") ":chart_with_upwards_trend:" else ":chart_with_downwards_trend:"

        val slackMessage = SlackMessage(
            text = "$emoji Trade Executed: $side $symbol",
            attachments = listOf(
                SlackAttachment(
                    color = color,
                    title = "$emoji Trade Notification",
                    fields = listOf(
                        SlackField("Symbol", symbol, short = true),
                        SlackField("Side", side, short = true),
                        SlackField("Quantity", quantity.toString(), short = true),
                        SlackField("Price", "$$price", short = true),
                        SlackField("Total Value", "$$totalValue", short = true),
                        SlackField("User ID", userId.toString(), short = true)
                    ),
                    footer = "Trading Bot",
                    ts = System.currentTimeMillis() / 1000
                )
            )
        )

        sendToSlack(slackMessage)
    }

    override fun sendRiskAlert(userId: Long, symbol: String, alertType: String, message: String) {
        val slackMessage = SlackMessage(
            text = ":warning: Risk Alert: $alertType",
            attachments = listOf(
                SlackAttachment(
                    color = "#ff0000", // Red
                    title = ":warning: Risk Alert",
                    fields = listOf(
                        SlackField("Alert Type", alertType, short = true),
                        SlackField("Symbol", symbol, short = true),
                        SlackField("User ID", userId.toString(), short = true),
                        SlackField("Message", message, short = false)
                    ),
                    footer = "Risk Management System",
                    ts = System.currentTimeMillis() / 1000
                )
            )
        )

        sendToSlack(slackMessage)
    }

    override fun sendStrategyAlert(userId: Long, strategyName: String, signal: String, reason: String) {
        val color = when (signal.uppercase()) {
            "BUY" -> "#36a64f" // Green
            "SELL" -> "#ff0000" // Red
            else -> "#ffaa00" // Orange
        }

        val emoji = when (signal.uppercase()) {
            "BUY" -> ":arrow_up:"
            "SELL" -> ":arrow_down:"
            else -> ":bulb:"
        }

        val slackMessage = SlackMessage(
            text = "$emoji Strategy Signal: $signal",
            attachments = listOf(
                SlackAttachment(
                    color = color,
                    title = "$emoji Strategy Alert",
                    fields = listOf(
                        SlackField("Strategy", strategyName, short = true),
                        SlackField("Signal", signal, short = true),
                        SlackField("User ID", userId.toString(), short = true),
                        SlackField("Reason", reason, short = false)
                    ),
                    footer = "Trading Strategy Engine",
                    ts = System.currentTimeMillis() / 1000
                )
            )
        )

        sendToSlack(slackMessage)
    }

    /**
     * 포트폴리오 요약 알림
     */
    fun sendPortfolioSummary(
        userId: Long,
        totalValue: BigDecimal,
        dailyPnL: BigDecimal,
        dailyPnLPercent: Double,
        positions: List<PositionSummary>
    ) {
        val pnlColor = if (dailyPnL >= BigDecimal.ZERO) "#36a64f" else "#ff0000"
        val pnlEmoji = if (dailyPnL >= BigDecimal.ZERO) ":chart_with_upwards_trend:" else ":chart_with_downwards_trend:"

        val positionFields = positions.map { position ->
            SlackField(
                position.symbol,
                "Qty: ${position.quantity}, P&L: ${position.pnl} (${position.pnlPercent}%)",
                short = true
            )
        }

        val slackMessage = SlackMessage(
            text = "$pnlEmoji Portfolio Summary",
            attachments = listOf(
                SlackAttachment(
                    color = pnlColor,
                    title = "Portfolio Summary",
                    fields = listOf(
                        SlackField("Total Value", "$$totalValue", short = true),
                        SlackField("Daily P&L", "$$dailyPnL ($dailyPnLPercent%)", short = true),
                        SlackField("User ID", userId.toString(), short = true)
                    ) + positionFields,
                    footer = "Portfolio Management",
                    ts = System.currentTimeMillis() / 1000
                )
            )
        )

        sendToSlack(slackMessage)
    }

    /**
     * 시스템 상태 알림
     */
    fun sendSystemStatus(
        status: String,
        activeStrategies: Int,
        activeOrders: Int,
        message: String? = null
    ) {
        val color = when (status.uppercase()) {
            "HEALTHY" -> "#36a64f"
            "WARNING" -> "#ffaa00"
            "ERROR" -> "#ff0000"
            else -> "#808080"
        }

        val slackMessage = SlackMessage(
            text = "System Status: $status",
            attachments = listOf(
                SlackAttachment(
                    color = color,
                    title = "System Status Report",
                    fields = listOf(
                        SlackField("Status", status, short = true),
                        SlackField("Active Strategies", activeStrategies.toString(), short = true),
                        SlackField("Active Orders", activeOrders.toString(), short = true)
                    ) + if (message != null) {
                        listOf(SlackField("Message", message, short = false))
                    } else {
                        emptyList()
                    },
                    footer = "System Monitor",
                    ts = System.currentTimeMillis() / 1000
                )
            )
        )

        sendToSlack(slackMessage)
    }

    private fun sendToSlack(message: SlackMessage) {
        if (webhookUrl.isBlank()) {
            println("Slack webhook URL not configured, skipping notification")
            return
        }

        try {
            val headers = HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
            }

            val json = objectMapper.writeValueAsString(message)

            restTemplate.exchange(
                webhookUrl,
                HttpMethod.POST,
                HttpEntity(json, headers),
                String::class.java
            )
        } catch (e: Exception) {
            println("Failed to send Slack notification: ${e.message}")
        }
    }
}

/**
 * Slack 메시지 모델
 */
data class SlackMessage(
    val text: String,
    val blocks: List<SlackBlock>? = null,
    val attachments: List<SlackAttachment>? = null
)

data class SlackBlock(
    val type: String,
    val text: SlackText? = null,
    val elements: List<SlackText>? = null
)

data class SlackText(
    val type: String,
    val text: String
)

data class SlackAttachment(
    val color: String,
    val title: String,
    val fields: List<SlackField>,
    val footer: String,
    val ts: Long
)

data class SlackField(
    val title: String,
    val value: String,
    val short: Boolean
)

/**
 * 포지션 요약
 */
data class PositionSummary(
    val symbol: String,
    val quantity: BigDecimal,
    val pnl: BigDecimal,
    val pnlPercent: Double
)
