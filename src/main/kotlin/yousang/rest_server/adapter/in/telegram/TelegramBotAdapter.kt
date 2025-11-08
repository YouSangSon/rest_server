package yousang.rest_server.adapter.`in`.telegram

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import yousang.rest_server.application.ports.out.NotificationPort
import yousang.rest_server.application.service.*
import yousang.rest_server.domain.model.OrderSide
import yousang.rest_server.domain.model.OrderType
import java.math.BigDecimal

/**
 * Telegram Bot 어댑터
 *
 * Telegram Bot API를 통한 알림 및 인터랙티브 버튼 제공
 * 사용자는 버튼을 통해 자동 매매 시작/중지, 시황 분석, 포트폴리오 조회 등을 수행
 */
@Component
class TelegramBotAdapter(
    private val restTemplate: RestTemplate,
    private val objectMapper: ObjectMapper,
    private val tradingStrategyService: TradingStrategyService,
    private val marketDataService: MarketDataService,
    private val tradingService: TradingService,
    private val newsService: NewsService,
    private val riskManagementService: RiskManagementService,
    @Value("\${telegram.bot-token:}") private val botToken: String,
    @Value("\${telegram.base-url:https://api.telegram.org}") private val baseUrl: String
) : NotificationPort {

    private val apiUrl = "$baseUrl/bot$botToken"

    // 사용자별 자동 매매 활성화 상태 (실제로는 DB나 Redis에 저장)
    private val autoTradingStatus = mutableMapOf<Long, Boolean>()

    // ==================== Notification Implementation ====================

    override fun sendNotification(userId: Long, title: String, message: String) {
        sendMessage(userId, "**$title**\n\n$message")
    }

    override fun sendTradeNotification(
        userId: Long,
        symbol: String,
        side: String,
        quantity: BigDecimal,
        price: BigDecimal
    ) {
        val message = """
            |🔔 **거래 알림**
            |
            |심볼: $symbol
            |방향: ${if (side == "BUY") "매수 🟢" else "매도 🔴"}
            |수량: $quantity
            |가격: $$price
            |총액: $${quantity * price}
        """.trimMargin()

        sendMessage(userId, message)
    }

    override fun sendRiskAlert(userId: Long, symbol: String, alertType: String, message: String) {
        val alertMessage = """
            |⚠️ **리스크 알림**
            |
            |심볼: $symbol
            |타입: $alertType
            |메시지: $message
        """.trimMargin()

        sendMessage(userId, alertMessage, urgent = true)
    }

    override fun sendStrategyAlert(userId: Long, strategyName: String, signal: String, reason: String) {
        val message = """
            |📊 **전략 신호**
            |
            |전략: $strategyName
            |신호: $signal
            |이유: $reason
        """.trimMargin()

        sendMessage(userId, message)
    }

    // ==================== Interactive Buttons ====================

    fun sendMainMenu(chatId: Long) {
        val keyboard = createInlineKeyboard(
            listOf(
                listOf(
                    InlineKeyboardButton("📈 현재 시황 분석", "market_analysis"),
                    InlineKeyboardButton("💼 포트폴리오", "portfolio")
                ),
                listOf(
                    InlineKeyboardButton("🤖 자동매매 시작", "auto_trade_start"),
                    InlineKeyboardButton("⏸️ 자동매매 중지", "auto_trade_stop")
                ),
                listOf(
                    InlineKeyboardButton("📋 전략 관리", "strategies"),
                    InlineKeyboardButton("⚠️ 리스크 현황", "risk_status")
                ),
                listOf(
                    InlineKeyboardButton("📰 최신 뉴스", "latest_news"),
                    InlineKeyboardButton("💡 트레이드 신호", "trade_signals")
                ),
                listOf(
                    InlineKeyboardButton("⚙️ 설정", "settings"),
                    InlineKeyboardButton("ℹ️ 도움말", "help")
                )
            )
        )

        sendMessageWithKeyboard(
            chatId,
            "안녕하세요! 자동 트레이딩 봇입니다. 원하시는 기능을 선택하세요:",
            keyboard
        )
    }

    fun handleCallbackQuery(callbackData: String, chatId: Long, userId: Long) {
        when (callbackData) {
            "market_analysis" -> handleMarketAnalysis(chatId, userId)
            "portfolio" -> handlePortfolio(chatId, userId)
            "auto_trade_start" -> handleAutoTradeStart(chatId, userId)
            "auto_trade_stop" -> handleAutoTradeStop(chatId, userId)
            "strategies" -> handleStrategies(chatId, userId)
            "risk_status" -> handleRiskStatus(chatId, userId)
            "latest_news" -> handleLatestNews(chatId, userId)
            "trade_signals" -> handleTradeSignals(chatId, userId)
            "settings" -> handleSettings(chatId, userId)
            "help" -> handleHelp(chatId, userId)
            else -> {
                // 동적 콜백 처리 (예: strategy_execute_123)
                when {
                    callbackData.startsWith("strategy_execute_") -> {
                        val strategyId = callbackData.substringAfter("strategy_execute_").toLong()
                        handleStrategyExecute(chatId, userId, strategyId)
                    }
                    callbackData.startsWith("symbol_") -> {
                        val symbol = callbackData.substringAfter("symbol_")
                        handleSymbolAnalysis(chatId, userId, symbol)
                    }
                }
            }
        }
    }

    // ==================== Command Handlers ====================

    private fun handleMarketAnalysis(chatId: Long, userId: Long) {
        try {
            val symbols = listOf("BTC/USDT", "ETH/USDT", "BTC/KRW", "ETH/KRW")
            val analysis = StringBuilder("📈 **현재 시황 분석**\n\n")

            symbols.forEach { symbol ->
                try {
                    val exchange = if (symbol.endsWith("USDT")) "Binance" else "Upbit"
                    val marketData = marketDataService.getLatestMarketData(symbol, exchange)

                    if (marketData != null) {
                        val changeEmoji = if (marketData.priceChange24h >= BigDecimal.ZERO) "🟢" else "🔴"
                        analysis.append("**$symbol** $changeEmoji\n")
                        analysis.append("가격: $${marketData.currentPrice}\n")
                        analysis.append("24h 변동: ${marketData.priceChangePercent24h}%\n")
                        analysis.append("거래량: ${marketData.volume24h}\n\n")
                    }
                } catch (e: Exception) {
                    analysis.append("**$symbol**: 데이터 조회 실패\n\n")
                }
            }

            // 감성 분석 추가
            analysis.append("📰 **뉴스 감성**\n\n")
            listOf("BTC/USDT", "ETH/USDT").forEach { symbol ->
                try {
                    val sentiment = newsService.getAggregateSentiment(symbol, 24)
                    val sentimentEmoji = when {
                        sentiment.averageScore > 0.3 -> "😊"
                        sentiment.averageScore < -0.3 -> "😟"
                        else -> "😐"
                    }
                    analysis.append("$symbol $sentimentEmoji: ${sentiment.sentimentType} (${sentiment.totalArticles}개 기사)\n")
                } catch (e: Exception) {
                    // Skip
                }
            }

            sendMessage(chatId, analysis.toString())

            // 심볼 선택 버튼 제공
            val keyboard = createInlineKeyboard(
                listOf(
                    listOf(
                        InlineKeyboardButton("BTC 분석", "symbol_BTC/USDT"),
                        InlineKeyboardButton("ETH 분석", "symbol_ETH/USDT")
                    ),
                    listOf(InlineKeyboardButton("🔙 메인 메뉴", "main_menu"))
                )
            )
            sendMessageWithKeyboard(chatId, "더 자세한 분석을 보려면 심볼을 선택하세요:", keyboard)
        } catch (e: Exception) {
            sendMessage(chatId, "❌ 시황 분석 중 오류 발생: ${e.message}")
        }
    }

    private fun handlePortfolio(chatId: Long, userId: Long) {
        try {
            val portfolios = tradingService.getPortfolio(userId)

            if (portfolios.isEmpty()) {
                sendMessage(chatId, "💼 **포트폴리오**\n\n포지션이 없습니다.")
                return
            }

            val message = StringBuilder("💼 **포트폴리오**\n\n")
            var totalValue = BigDecimal.ZERO

            portfolios.forEach { portfolio ->
                val exchange = portfolio.exchange
                val currentPrice = marketDataService.getCurrentPrice(portfolio.symbol, exchange)
                val positionValue = portfolio.quantity * currentPrice
                val pnl = portfolio.calculatePnL(currentPrice)
                val pnlPercent = portfolio.calculatePnLPercentage(currentPrice)

                totalValue += positionValue

                val pnlEmoji = if (pnl >= BigDecimal.ZERO) "🟢" else "🔴"
                message.append("**${portfolio.symbol}** $pnlEmoji\n")
                message.append("수량: ${portfolio.quantity}\n")
                message.append("평균 매수가: $${portfolio.avgBuyPrice}\n")
                message.append("현재가: $$currentPrice\n")
                message.append("평가액: $$positionValue\n")
                message.append("손익: $$pnl ($pnlPercent%)\n\n")
            }

            message.append("**총 평가액: $$totalValue**")

            sendMessage(chatId, message.toString())
        } catch (e: Exception) {
            sendMessage(chatId, "❌ 포트폴리오 조회 중 오류 발생: ${e.message}")
        }
    }

    private fun handleAutoTradeStart(chatId: Long, userId: Long) {
        try {
            // 모든 활성 전략 실행 시작
            autoTradingStatus[userId] = true

            val strategies = tradingStrategyService.getActiveStrategies(userId)
            if (strategies.isEmpty()) {
                sendMessage(chatId, "⚠️ 활성화된 전략이 없습니다. 먼저 전략을 생성하고 활성화하세요.")
                return
            }

            sendMessage(
                chatId,
                """
                |🤖 **자동 매매 시작**
                |
                |${strategies.size}개의 전략이 활성화되었습니다:
                |${strategies.joinToString("\n") { "- ${it.name} (${it.strategyType})" }}
                |
                |실시간으로 시장을 모니터링하고 거래 신호를 생성합니다.
            """.trimMargin()
            )
        } catch (e: Exception) {
            sendMessage(chatId, "❌ 자동 매매 시작 중 오류 발생: ${e.message}")
        }
    }

    private fun handleAutoTradeStop(chatId: Long, userId: Long) {
        autoTradingStatus[userId] = false
        sendMessage(
            chatId,
            """
            |⏸️ **자동 매매 중지**
            |
            |모든 자동 매매가 중지되었습니다.
            |기존 주문은 계속 유효하며, 새로운 주문은 생성되지 않습니다.
        """.trimMargin()
        )
    }

    private fun handleStrategies(chatId: Long, userId: Long) {
        try {
            val strategies = tradingStrategyService.getStrategiesByUser(userId)

            if (strategies.isEmpty()) {
                sendMessage(chatId, "📋 **전략 관리**\n\n등록된 전략이 없습니다.")
                return
            }

            val message = StringBuilder("📋 **전략 관리**\n\n")
            val buttons = mutableListOf<List<InlineKeyboardButton>>()

            strategies.forEach { strategy ->
                val statusEmoji = if (strategy.isActive) "✅" else "⏸️"
                message.append("$statusEmoji **${strategy.name}**\n")
                message.append("타입: ${strategy.strategyType}\n")
                message.append("심볼: ${strategy.symbols.joinToString(", ")}\n")
                message.append("거래소: ${strategy.exchange}\n\n")

                buttons.add(
                    listOf(
                        InlineKeyboardButton("실행: ${strategy.name}", "strategy_execute_${strategy.id}")
                    )
                )
            }

            buttons.add(listOf(InlineKeyboardButton("🔙 메인 메뉴", "main_menu")))

            val keyboard = InlineKeyboardMarkup(buttons)
            sendMessageWithKeyboard(chatId, message.toString(), keyboard)
        } catch (e: Exception) {
            sendMessage(chatId, "❌ 전략 조회 중 오류 발생: ${e.message}")
        }
    }

    private fun handleRiskStatus(chatId: Long, userId: Long) {
        try {
            val riskMetrics = riskManagementService.calculatePortfolioRisk(userId)
            val alerts = riskManagementService.checkRiskLimits(userId)

            val message = StringBuilder("⚠️ **리스크 현황**\n\n")
            message.append("총 자산: $${riskMetrics.totalValue}\n")
            message.append("총 리스크: $${riskMetrics.totalRisk}\n")
            message.append("리스크 비율: ${riskMetrics.riskPercent}%\n")
            message.append("분산 점수: ${riskMetrics.diversificationScore}\n\n")

            if (alerts.isNotEmpty()) {
                message.append("🚨 **활성 알림**\n\n")
                alerts.forEach { alert ->
                    val emoji = when (alert.severity) {
                        RiskAlertSeverity.CRITICAL -> "🔴"
                        RiskAlertSeverity.HIGH -> "🟠"
                        RiskAlertSeverity.MEDIUM -> "🟡"
                        RiskAlertSeverity.LOW -> "🟢"
                    }
                    message.append("$emoji ${alert.message}\n")
                }
            } else {
                message.append("✅ 리스크 한도 내에서 안전하게 운영 중입니다.")
            }

            sendMessage(chatId, message.toString())
        } catch (e: Exception) {
            sendMessage(chatId, "❌ 리스크 현황 조회 중 오류 발생: ${e.message}")
        }
    }

    private fun handleLatestNews(chatId: Long, userId: Long) {
        try {
            val news = newsService.getLatestNews(10)

            if (news.isEmpty()) {
                sendMessage(chatId, "📰 **최신 뉴스**\n\n뉴스가 없습니다.")
                return
            }

            val message = StringBuilder("📰 **최신 뉴스**\n\n")
            news.take(5).forEach { article ->
                val sentimentEmoji = when {
                    article.sentiment?.name == "POSITIVE" -> "😊"
                    article.sentiment?.name == "NEGATIVE" -> "😟"
                    else -> "😐"
                }
                message.append("$sentimentEmoji **${article.title}**\n")
                message.append("출처: ${article.source}\n")
                message.append("URL: ${article.url}\n\n")
            }

            sendMessage(chatId, message.toString())
        } catch (e: Exception) {
            sendMessage(chatId, "❌ 뉴스 조회 중 오류 발생: ${e.message}")
        }
    }

    private fun handleTradeSignals(chatId: Long, userId: Long) {
        sendMessage(chatId, "💡 **트레이드 신호**\n\n실시간 신호는 자동 매매 활성화 시 자동으로 알림됩니다.")
    }

    private fun handleSettings(chatId: Long, userId: Long) {
        val autoTradeStatus = if (autoTradingStatus[userId] == true) "활성화 ✅" else "비활성화 ⏸️"

        sendMessage(
            chatId,
            """
            |⚙️ **설정**
            |
            |자동 매매: $autoTradeStatus
            |알림: 활성화 ✅
            |
            |더 많은 설정은 웹 대시보드에서 가능합니다.
        """.trimMargin()
        )
    }

    private fun handleHelp(chatId: Long, userId: Long) {
        sendMessage(
            chatId,
            """
            |ℹ️ **도움말**
            |
            |**주요 기능:**
            |• 📈 현재 시황 분석: 실시간 가격 및 감성 분석
            |• 💼 포트폴리오: 보유 포지션 및 손익 현황
            |• 🤖 자동 매매: 전략 기반 자동 거래
            |• 📋 전략 관리: 거래 전략 생성 및 관리
            |• ⚠️ 리스크 현황: 포트폴리오 리스크 모니터링
            |• 📰 최신 뉴스: 암호화폐 관련 뉴스
            |
            |**문의:** @your_support
        """.trimMargin()
        )
    }

    private fun handleStrategyExecute(chatId: Long, userId: Long, strategyId: Long) {
        try {
            val orders = tradingStrategyService.executeStrategy(strategyId, userId)

            if (orders.isEmpty()) {
                sendMessage(chatId, "전략을 실행했지만 생성된 주문이 없습니다. (진입 조건 미충족)")
            } else {
                val message = StringBuilder("✅ 전략 실행 완료\n\n")
                message.append("${orders.size}개의 주문이 생성되었습니다:\n\n")
                orders.forEach { order ->
                    message.append("${order.symbol} ${order.side} ${order.quantity}\n")
                }
                sendMessage(chatId, message.toString())
            }
        } catch (e: Exception) {
            sendMessage(chatId, "❌ 전략 실행 중 오류 발생: ${e.message}")
        }
    }

    private fun handleSymbolAnalysis(chatId: Long, userId: Long, symbol: String) {
        try {
            val exchange = if (symbol.endsWith("USDT")) "Binance" else "Upbit"
            val candles = marketDataService.getRecentCandles(symbol, exchange, yousang.rest_server.domain.model.CandleInterval.ONE_HOUR, 50)

            if (candles.isEmpty()) {
                sendMessage(chatId, "데이터를 가져올 수 없습니다.")
                return
            }

            val sma20 = marketDataService.calculateSMA(candles, 20)
            val rsi = marketDataService.calculateRSI(candles, 14)

            val message = StringBuilder("📊 **$symbol 상세 분석**\n\n")
            message.append("현재가: ${candles.last().close}\n")
            if (sma20.isNotEmpty()) {
                message.append("SMA20: ${sma20.last()}\n")
            }
            if (rsi.isNotEmpty()) {
                message.append("RSI: ${rsi.last()}\n")
            }

            sendMessage(chatId, message.toString())
        } catch (e: Exception) {
            sendMessage(chatId, "❌ 분석 중 오류 발생: ${e.message}")
        }
    }

    // ==================== Telegram API Methods ====================

    private fun sendMessage(chatId: Long, text: String, urgent: Boolean = false) {
        if (botToken.isBlank()) {
            println("Telegram bot token not configured, skipping message: $text")
            return
        }

        val payload = mapOf(
            "chat_id" to chatId,
            "text" to text,
            "parse_mode" to "Markdown"
        )

        try {
            val headers = HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
            }

            restTemplate.exchange(
                "$apiUrl/sendMessage",
                HttpMethod.POST,
                HttpEntity(payload, headers),
                Map::class.java
            )
        } catch (e: Exception) {
            println("Failed to send Telegram message: ${e.message}")
        }
    }

    private fun sendMessageWithKeyboard(chatId: Long, text: String, keyboard: InlineKeyboardMarkup) {
        if (botToken.isBlank()) {
            println("Telegram bot token not configured")
            return
        }

        val payload = mapOf(
            "chat_id" to chatId,
            "text" to text,
            "parse_mode" to "Markdown",
            "reply_markup" to keyboard
        )

        try {
            val headers = HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
            }

            restTemplate.exchange(
                "$apiUrl/sendMessage",
                HttpMethod.POST,
                HttpEntity(payload, headers),
                Map::class.java
            )
        } catch (e: Exception) {
            println("Failed to send Telegram message with keyboard: ${e.message}")
        }
    }

    private fun createInlineKeyboard(buttons: List<List<InlineKeyboardButton>>): InlineKeyboardMarkup {
        return InlineKeyboardMarkup(buttons)
    }

    fun isAutoTradingEnabled(userId: Long): Boolean {
        return autoTradingStatus[userId] ?: false
    }
}

/**
 * Telegram 인라인 키보드
 */
data class InlineKeyboardMarkup(
    val inline_keyboard: List<List<InlineKeyboardButton>>
)

data class InlineKeyboardButton(
    val text: String,
    val callback_data: String
)
