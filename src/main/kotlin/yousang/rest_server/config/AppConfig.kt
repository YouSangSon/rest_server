package yousang.rest_server.config

import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Configuration
import yousang.rest_server.adapter.`in`.telegram.TelegramBotAdapter
import yousang.rest_server.application.scheduler.TradingScheduler

/**
 * 애플리케이션 설정
 *
 * 순환 참조 방지를 위해 초기화 후 상호 참조 설정
 */
@Configuration
class AppConfig(
    private val telegramBotAdapter: TelegramBotAdapter,
    private val tradingScheduler: TradingScheduler
) {

    @PostConstruct
    fun init() {
        // TelegramBotAdapter에 TradingScheduler 주입
        telegramBotAdapter.setTradingScheduler(tradingScheduler)
    }
}
