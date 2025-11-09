package yousang.rest_server.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry
import yousang.rest_server.adapter.`in`.websocket.MarketDataWebSocketHandler

/**
 * WebSocket 설정
 */
@Configuration
@EnableWebSocket
class WebSocketConfig(
    private val marketDataWebSocketHandler: MarketDataWebSocketHandler
) : WebSocketConfigurer {

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(marketDataWebSocketHandler, "/ws/market-data")
            .setAllowedOrigins("*")
    }
}
