package yousang.rest_server.application.scheduler

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import yousang.rest_server.adapter.`in`.telegram.TelegramBotAdapter
import yousang.rest_server.application.service.*
import yousang.rest_server.domain.model.CandleInterval
import java.time.LocalDateTime

/**
 * 자동 매매 스케줄러
 *
 * 주기적으로 전략을 실행하고 시장 데이터를 수집
 */
@Component
class TradingScheduler(
    private val tradingStrategyService: TradingStrategyService,
    private val marketDataService: MarketDataService,
    private val newsService: NewsService,
    private val riskManagementService: RiskManagementService,
    private val telegramBotAdapter: TelegramBotAdapter
) {

    // 사용자별 자동 매매 활성화 상태 (실제로는 DB에서 관리)
    private val enabledUsers = mutableSetOf<Long>()

    /**
     * 전략 실행 스케줄러 - 1분마다
     */
    @Scheduled(fixedRate = 60000) // 1분
    fun executeStrategies() {
        println("🤖 [${LocalDateTime.now()}] Executing active trading strategies...")

        try {
            // 모든 활성 전략 조회 및 실행
            enabledUsers.forEach { userId ->
                try {
                    if (telegramBotAdapter.isAutoTradingEnabled(userId)) {
                        val results = tradingStrategyService.executeAllActiveStrategies(userId)

                        val totalOrders = results.values.sumOf { it.size }
                        if (totalOrders > 0) {
                            println("✅ User $userId: Generated $totalOrders orders from ${results.size} strategies")
                        }
                    }
                } catch (e: Exception) {
                    println("❌ Error executing strategies for user $userId: ${e.message}")
                }
            }
        } catch (e: Exception) {
            println("❌ Strategy execution failed: ${e.message}")
        }
    }

    /**
     * 시장 데이터 수집 - 30초마다
     */
    @Scheduled(fixedRate = 30000) // 30초
    fun collectMarketData() {
        try {
            val symbols = listOf("BTC/USDT", "ETH/USDT", "BTC/KRW", "ETH/KRW")

            // Binance 데이터 수집
            val binanceSymbols = symbols.filter { it.endsWith("USDT") }
            marketDataService.collectMarketDataBatch(binanceSymbols, "Binance")

            // Upbit 데이터 수집
            val upbitSymbols = symbols.filter { it.endsWith("KRW") }
            marketDataService.collectMarketDataBatch(upbitSymbols, "Upbit")

        } catch (e: Exception) {
            println("❌ Market data collection failed: ${e.message}")
        }
    }

    /**
     * 뉴스 수집 - 5분마다
     */
    @Scheduled(fixedRate = 300000) // 5분
    fun collectNews() {
        println("📰 [${LocalDateTime.now()}] Collecting news...")

        try {
            val keywords = listOf("Bitcoin", "Ethereum", "Cryptocurrency", "BTC", "ETH")
            val news = newsService.collectNews(keywords, "en")

            if (news.isNotEmpty()) {
                println("✅ Collected ${news.size} news articles")

                // 감성 분석 실행
                news.forEach { article ->
                    try {
                        newsService.analyzeSentiment(article)
                    } catch (e: Exception) {
                        println("❌ Sentiment analysis failed for article ${article.id}: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            println("❌ News collection failed: ${e.message}")
        }
    }

    /**
     * 리스크 체크 - 1분마다
     */
    @Scheduled(fixedRate = 60000) // 1분
    fun checkRiskLimits() {
        try {
            enabledUsers.forEach { userId ->
                try {
                    val alerts = riskManagementService.checkRiskLimits(userId)

                    // Critical 알림만 즉시 전송
                    alerts.filter { it.severity == RiskAlertSeverity.CRITICAL }.forEach { alert ->
                        println("🚨 CRITICAL RISK ALERT for user $userId: ${alert.message}")
                    }
                } catch (e: Exception) {
                    println("❌ Risk check failed for user $userId: ${e.message}")
                }
            }
        } catch (e: Exception) {
            println("❌ Risk check failed: ${e.message}")
        }
    }

    /**
     * 손절/익절 체크 - 10초마다 (더 빠른 응답 필요)
     */
    @Scheduled(fixedRate = 10000) // 10초
    fun checkStopLossAndTakeProfit() {
        try {
            enabledUsers.forEach { userId ->
                try {
                    val portfolios = riskManagementService.calculatePortfolioRisk(userId)
                    // 실제로는 각 포지션별로 손절/익절 체크 필요
                } catch (e: Exception) {
                    // Silent fail
                }
            }
        } catch (e: Exception) {
            // Silent fail
        }
    }

    /**
     * 캔들 데이터 수집 - 1분마다
     */
    @Scheduled(fixedRate = 60000) // 1분
    fun collectCandles() {
        try {
            val symbols = listOf("BTC/USDT", "ETH/USDT")
            val to = LocalDateTime.now()
            val from = to.minusHours(1)

            symbols.forEach { symbol ->
                try {
                    marketDataService.collectCandles(
                        symbol = symbol,
                        exchange = "Binance",
                        interval = CandleInterval.ONE_MINUTE,
                        from = from,
                        to = to
                    )
                } catch (e: Exception) {
                    // Silent fail
                }
            }
        } catch (e: Exception) {
            // Silent fail
        }
    }

    /**
     * 오래된 데이터 정리 - 매일 자정
     */
    @Scheduled(cron = "0 0 0 * * *") // 매일 00:00
    fun cleanupOldData() {
        println("🧹 [${LocalDateTime.now()}] Cleaning up old data...")

        try {
            val cutoffDate = LocalDateTime.now().minusDays(30)
            marketDataService.cleanupOldData(cutoffDate)
            println("✅ Old data cleanup completed")
        } catch (e: Exception) {
            println("❌ Data cleanup failed: ${e.message}")
        }
    }

    /**
     * 헬스 체크 - 10분마다
     */
    @Scheduled(fixedRate = 600000) // 10분
    fun healthCheck() {
        println("💚 [${LocalDateTime.now()}] System health check - Active users: ${enabledUsers.size}")
    }

    // ==================== 사용자 관리 메서드 ====================

    fun enableAutoTradingForUser(userId: Long) {
        enabledUsers.add(userId)
        println("✅ Auto-trading enabled for user $userId")
    }

    fun disableAutoTradingForUser(userId: Long) {
        enabledUsers.remove(userId)
        println("⏸️ Auto-trading disabled for user $userId")
    }

    fun isAutoTradingEnabled(userId: Long): Boolean {
        return enabledUsers.contains(userId)
    }
}
