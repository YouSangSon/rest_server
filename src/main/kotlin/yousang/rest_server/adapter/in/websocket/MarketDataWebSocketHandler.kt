package yousang.rest_server.adapter.`in`.websocket

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import yousang.rest_server.application.service.MarketDataService
import yousang.rest_server.domain.model.MarketData
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * 실시간 시장 데이터 WebSocket 핸들러
 *
 * 클라이언트에게 실시간 가격/거래량 스트리밍
 */
@Component
class MarketDataWebSocketHandler(
    private val marketDataService: MarketDataService,
    private val objectMapper: ObjectMapper
) : TextWebSocketHandler() {

    // 구독 관리: session -> List<symbol>
    private val subscriptions = ConcurrentHashMap<String, MutableSet<String>>()

    // 스케줄러
    private val scheduler = Executors.newScheduledThreadPool(1)

    init {
        // 1초마다 구독된 심볼의 최신 데이터 전송
        scheduler.scheduleAtFixedRate({
            broadcastMarketData()
        }, 1, 1, TimeUnit.SECONDS)
    }

    override fun afterConnectionEstablished(session: WebSocketSession) {
        println("WebSocket connected: ${session.id}")
        subscriptions[session.id] = mutableSetOf()
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        try {
            val payload = objectMapper.readValue(message.payload, WebSocketMessage::class.java)

            when (payload.action) {
                "subscribe" -> {
                    val symbols = payload.symbols ?: emptyList()
                    subscriptions[session.id]?.addAll(symbols)
                    session.sendMessage(
                        TextMessage(
                            objectMapper.writeValueAsString(
                                mapOf(
                                    "action" to "subscribed",
                                    "symbols" to symbols,
                                    "message" to "Successfully subscribed to ${symbols.size} symbols"
                                )
                            )
                        )
                    )
                }

                "unsubscribe" -> {
                    val symbols = payload.symbols ?: emptyList()
                    subscriptions[session.id]?.removeAll(symbols.toSet())
                    session.sendMessage(
                        TextMessage(
                            objectMapper.writeValueAsString(
                                mapOf(
                                    "action" to "unsubscribed",
                                    "symbols" to symbols
                                )
                            )
                        )
                    )
                }

                "ping" -> {
                    session.sendMessage(
                        TextMessage(
                            objectMapper.writeValueAsString(
                                mapOf("action" to "pong")
                            )
                        )
                    )
                }
            }

        } catch (e: Exception) {
            println("WebSocket message handling error: ${e.message}")
        }
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        println("WebSocket disconnected: ${session.id}")
        subscriptions.remove(session.id)
    }

    /**
     * 모든 구독자에게 시장 데이터 브로드캐스트
     */
    private fun broadcastMarketData() {
        subscriptions.forEach { (sessionId, symbols) ->
            symbols.forEach { symbol ->
                try {
                    val parts = symbol.split(":")
                    if (parts.size == 2) {
                        val (sym, exchange) = parts
                        val marketData = marketDataService.getLatestMarketData(sym, exchange)

                        if (marketData != null) {
                            val session = findSession(sessionId)
                            session?.sendMessage(
                                TextMessage(
                                    objectMapper.writeValueAsString(
                                        mapOf(
                                            "action" to "market_data",
                                            "symbol" to sym,
                                            "exchange" to exchange,
                                            "data" to mapOf(
                                                "price" to marketData.currentPrice,
                                                "volume24h" to marketData.volume24h,
                                                "priceChange24h" to marketData.priceChange24h,
                                                "priceChangePercent24h" to marketData.priceChangePercent24h,
                                                "timestamp" to marketData.timestamp.toString()
                                            )
                                        )
                                    )
                                )
                            )
                        }
                    }
                } catch (e: Exception) {
                    println("Error broadcasting market data: ${e.message}")
                }
            }
        }
    }

    private fun findSession(sessionId: String): WebSocketSession? {
        // 실제로는 WebSocketSession을 관리하는 맵 필요
        return null
    }
}

data class WebSocketMessage(
    val action: String,
    val symbols: List<String>? = null
)
