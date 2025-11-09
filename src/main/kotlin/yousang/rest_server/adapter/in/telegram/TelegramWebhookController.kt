package yousang.rest_server.adapter.`in`.telegram

import org.springframework.web.bind.annotation.*

/**
 * Telegram Webhook Controller
 *
 * Telegram으로부터 메시지 및 콜백 쿼리를 수신
 */
@RestController
@RequestMapping("/api/telegram/webhook")
class TelegramWebhookController(
    private val telegramBotAdapter: TelegramBotAdapter
) {

    /**
     * Telegram 웹훅 엔드포인트
     *
     * Telegram Bot API는 이 엔드포인트로 업데이트를 전송
     */
    @PostMapping
    fun handleWebhook(@RequestBody update: TelegramUpdate): Map<String, String> {
        try {
            // 메시지 처리
            update.message?.let { message ->
                val chatId = message.chat.id
                val userId = message.from.id
                val text = message.text

                when {
                    text == "/start" || text == "/menu" -> {
                        telegramBotAdapter.sendMainMenu(chatId)
                    }
                    text?.startsWith("/") == true -> {
                        handleCommand(text, chatId, userId)
                    }
                    else -> {
                        telegramBotAdapter.sendMainMenu(chatId)
                    }
                }
            }

            // 콜백 쿼리 처리 (버튼 클릭)
            update.callback_query?.let { callbackQuery ->
                val chatId = callbackQuery.message.chat.id
                val userId = callbackQuery.from.id
                val callbackData = callbackQuery.data

                if (callbackData == "main_menu") {
                    telegramBotAdapter.sendMainMenu(chatId)
                } else {
                    telegramBotAdapter.handleCallbackQuery(callbackData, chatId, userId)
                }

                // 콜백 쿼리 응답 (로딩 표시 제거)
                answerCallbackQuery(callbackQuery.id)
            }

            return mapOf("status" to "ok")
        } catch (e: Exception) {
            println("Error handling Telegram webhook: ${e.message}")
            return mapOf("status" to "error", "message" to (e.message ?: "Unknown error"))
        }
    }

    private fun handleCommand(command: String, chatId: Long, userId: Long) {
        when (command.lowercase()) {
            "/start", "/menu" -> telegramBotAdapter.sendMainMenu(chatId)
            "/portfolio" -> telegramBotAdapter.handleCallbackQuery("portfolio", chatId, userId)
            "/market" -> telegramBotAdapter.handleCallbackQuery("market_analysis", chatId, userId)
            "/strategies" -> telegramBotAdapter.handleCallbackQuery("strategies", chatId, userId)
            "/start_trading" -> telegramBotAdapter.handleCallbackQuery("auto_trade_start", chatId, userId)
            "/stop_trading" -> telegramBotAdapter.handleCallbackQuery("auto_trade_stop", chatId, userId)
            "/risk" -> telegramBotAdapter.handleCallbackQuery("risk_status", chatId, userId)
            "/news" -> telegramBotAdapter.handleCallbackQuery("latest_news", chatId, userId)
            "/help" -> telegramBotAdapter.handleCallbackQuery("help", chatId, userId)
            else -> telegramBotAdapter.sendMainMenu(chatId)
        }
    }

    private fun answerCallbackQuery(callbackQueryId: String) {
        // 실제로는 Telegram API를 호출하여 콜백 쿼리에 응답
        // 여기서는 생략
    }

    /**
     * 웹훅 설정 엔드포인트
     *
     * GET /api/telegram/webhook/setup?url=https://yourdomain.com/api/telegram/webhook
     */
    @GetMapping("/setup")
    fun setupWebhook(@RequestParam url: String): Map<String, String> {
        return try {
            // Telegram API를 통해 웹훅 설정
            // setWebhook API 호출 필요
            mapOf("status" to "ok", "webhook_url" to url)
        } catch (e: Exception) {
            mapOf("status" to "error", "message" to (e.message ?: "Unknown error"))
        }
    }
}

/**
 * Telegram Update 모델
 */
data class TelegramUpdate(
    val update_id: Long,
    val message: TelegramMessage? = null,
    val callback_query: TelegramCallbackQuery? = null
)

data class TelegramMessage(
    val message_id: Long,
    val from: TelegramUser,
    val chat: TelegramChat,
    val date: Long,
    val text: String? = null
)

data class TelegramCallbackQuery(
    val id: String,
    val from: TelegramUser,
    val message: TelegramMessage,
    val data: String
)

data class TelegramUser(
    val id: Long,
    val is_bot: Boolean,
    val first_name: String,
    val last_name: String? = null,
    val username: String? = null
)

data class TelegramChat(
    val id: Long,
    val type: String,
    val title: String? = null,
    val username: String? = null,
    val first_name: String? = null,
    val last_name: String? = null
)
