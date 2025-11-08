package yousang.rest_server.adapter.out.external.binance

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import yousang.rest_server.application.ports.out.ExchangeApiPort
import yousang.rest_server.domain.model.*
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Binance 거래소 어댑터
 *
 * Binance REST API 및 WebSocket 통합
 */
@Component
class BinanceAdapter(
    private val restTemplate: RestTemplate,
    private val objectMapper: ObjectMapper,
    @Value("\${binance.api-key:}") private val apiKey: String,
    @Value("\${binance.secret-key:}") private val secretKey: String,
    @Value("\${binance.base-url:https://api.binance.com}") private val baseUrl: String
) : ExchangeApiPort {

    private val hmacSha256 = "HmacSHA256"

    // ==================== Market Data ====================

    override fun fetchMarketData(symbol: String): MarketData {
        val binanceSymbol = convertSymbol(symbol)
        val url = "$baseUrl/api/v3/ticker/24hr?symbol=$binanceSymbol"

        val response = restTemplate.getForObject(url, BinanceTicker24hr::class.java)
            ?: throw IllegalStateException("Failed to fetch market data for $symbol")

        return MarketData.create(
            symbol = symbol,
            exchange = "Binance",
            currentPrice = response.lastPrice.toBigDecimal(),
            volume24h = response.volume.toBigDecimal(),
            high24h = response.highPrice.toBigDecimal(),
            low24h = response.lowPrice.toBigDecimal(),
            priceChange24h = response.priceChange.toBigDecimal(),
            priceChangePercent24h = response.priceChangePercent.toDouble(),
            timestamp = LocalDateTime.now()
        )
    }

    override fun fetchCandles(
        symbol: String,
        interval: CandleInterval,
        from: LocalDateTime,
        to: LocalDateTime
    ): List<Candle> {
        val binanceSymbol = convertSymbol(symbol)
        val binanceInterval = convertInterval(interval)

        val url = UriComponentsBuilder.fromHttpUrl("$baseUrl/api/v3/klines")
            .queryParam("symbol", binanceSymbol)
            .queryParam("interval", binanceInterval)
            .queryParam("startTime", from.toEpochMilli())
            .queryParam("endTime", to.toEpochMilli())
            .queryParam("limit", 1000)
            .build()
            .toUriString()

        val response = restTemplate.getForObject(url, Array<Array<Any>>::class.java)
            ?: return emptyList()

        return response.map { kline ->
            Candle.create(
                symbol = symbol,
                exchange = "Binance",
                interval = interval,
                openTime = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli((kline[0] as Number).toLong()),
                    ZoneId.systemDefault()
                ),
                closeTime = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli((kline[6] as Number).toLong()),
                    ZoneId.systemDefault()
                ),
                open = BigDecimal(kline[1].toString()),
                high = BigDecimal(kline[2].toString()),
                low = BigDecimal(kline[3].toString()),
                close = BigDecimal(kline[4].toString()),
                volume = BigDecimal(kline[5].toString())
            )
        }
    }

    override fun subscribeToMarketData(symbol: String, callback: (MarketData) -> Unit) {
        // WebSocket 구독 구현 (실제로는 WebSocket 클라이언트 필요)
        println("Subscribing to $symbol on Binance (WebSocket implementation needed)")
    }

    override fun unsubscribeFromMarketData(symbol: String) {
        println("Unsubscribing from $symbol on Binance")
    }

    // ==================== Trading ====================

    override fun submitOrder(order: Order): Order {
        if (apiKey.isBlank() || secretKey.isBlank()) {
            throw IllegalStateException("Binance API credentials not configured")
        }

        val binanceSymbol = convertSymbol(order.symbol)
        val timestamp = System.currentTimeMillis()

        val params = mutableMapOf(
            "symbol" to binanceSymbol,
            "side" to order.side.name,
            "type" to convertOrderType(order.type),
            "quantity" to order.quantity.toPlainString(),
            "timestamp" to timestamp.toString()
        )

        // LIMIT 주문인 경우 가격 추가
        if (order.type == OrderType.LIMIT && order.price != null) {
            params["price"] = order.price.toPlainString()
            params["timeInForce"] = order.timeInForce.name
        }

        // STOP_LOSS, STOP_LOSS_LIMIT 주문인 경우 stopPrice 추가
        if (order.stopPrice != null) {
            params["stopPrice"] = order.stopPrice.toPlainString()
        }

        // 서명 생성
        val queryString = params.entries.joinToString("&") { "${it.key}=${it.value}" }
        val signature = generateSignature(queryString)

        val url = "$baseUrl/api/v3/order?$queryString&signature=$signature"

        val headers = HttpHeaders().apply {
            set("X-MBX-APIKEY", apiKey)
        }

        try {
            val response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                HttpEntity<Any>(headers),
                BinanceOrderResponse::class.java
            )

            val binanceOrder = response.body
                ?: throw IllegalStateException("No response from Binance")

            // 주문 상태 업데이트
            return order.submit(binanceOrder.orderId.toString())
        } catch (e: Exception) {
            throw IllegalStateException("Failed to submit order to Binance: ${e.message}", e)
        }
    }

    override fun cancelOrder(orderId: String): Boolean {
        if (apiKey.isBlank() || secretKey.isBlank()) {
            throw IllegalStateException("Binance API credentials not configured")
        }

        val timestamp = System.currentTimeMillis()
        val queryString = "orderId=$orderId&timestamp=$timestamp"
        val signature = generateSignature(queryString)

        val url = "$baseUrl/api/v3/order?$queryString&signature=$signature"

        val headers = HttpHeaders().apply {
            set("X-MBX-APIKEY", apiKey)
        }

        return try {
            restTemplate.exchange(
                url,
                HttpMethod.DELETE,
                HttpEntity<Any>(headers),
                Map::class.java
            )
            true
        } catch (e: Exception) {
            println("Failed to cancel order on Binance: ${e.message}")
            false
        }
    }

    override fun getOrderStatus(orderId: String, symbol: String): Order? {
        if (apiKey.isBlank() || secretKey.isBlank()) {
            return null
        }

        val binanceSymbol = convertSymbol(symbol)
        val timestamp = System.currentTimeMillis()
        val queryString = "symbol=$binanceSymbol&orderId=$orderId&timestamp=$timestamp"
        val signature = generateSignature(queryString)

        val url = "$baseUrl/api/v3/order?$queryString&signature=$signature"

        val headers = HttpHeaders().apply {
            set("X-MBX-APIKEY", apiKey)
        }

        return try {
            val response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                HttpEntity<Any>(headers),
                BinanceOrderResponse::class.java
            )

            // Response를 Order 도메인 모델로 변환 (구현 필요)
            null
        } catch (e: Exception) {
            println("Failed to get order status: ${e.message}")
            null
        }
    }

    override fun getExchangeName(): String = "Binance"

    // ==================== Helper Methods ====================

    private fun convertSymbol(symbol: String): String {
        // BTC/USDT -> BTCUSDT
        return symbol.replace("/", "")
    }

    private fun convertInterval(interval: CandleInterval): String {
        return when (interval) {
            CandleInterval.ONE_MINUTE -> "1m"
            CandleInterval.FIVE_MINUTES -> "5m"
            CandleInterval.FIFTEEN_MINUTES -> "15m"
            CandleInterval.THIRTY_MINUTES -> "30m"
            CandleInterval.ONE_HOUR -> "1h"
            CandleInterval.FOUR_HOURS -> "4h"
            CandleInterval.ONE_DAY -> "1d"
            CandleInterval.ONE_WEEK -> "1w"
            CandleInterval.ONE_MONTH -> "1M"
        }
    }

    private fun convertOrderType(type: OrderType): String {
        return when (type) {
            OrderType.MARKET -> "MARKET"
            OrderType.LIMIT -> "LIMIT"
            OrderType.STOP_LOSS -> "STOP_LOSS"
            OrderType.STOP_LOSS_LIMIT -> "STOP_LOSS_LIMIT"
            OrderType.TAKE_PROFIT -> "TAKE_PROFIT"
            OrderType.TAKE_PROFIT_LIMIT -> "TAKE_PROFIT_LIMIT"
        }
    }

    private fun generateSignature(data: String): String {
        val sha256Hmac = Mac.getInstance(hmacSha256)
        val secretKeySpec = SecretKeySpec(secretKey.toByteArray(), hmacSha256)
        sha256Hmac.init(secretKeySpec)
        val hash = sha256Hmac.doFinal(data.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }

    private fun LocalDateTime.toEpochMilli(): Long {
        return this.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
}

/**
 * Binance API 응답 모델
 */
data class BinanceTicker24hr(
    val symbol: String,
    val priceChange: String,
    val priceChangePercent: String,
    val weightedAvgPrice: String,
    val prevClosePrice: String,
    val lastPrice: String,
    val lastQty: String,
    val bidPrice: String,
    val askPrice: String,
    val openPrice: String,
    val highPrice: String,
    val lowPrice: String,
    val volume: String,
    val quoteVolume: String,
    val openTime: Long,
    val closeTime: Long,
    val firstId: Long,
    val lastId: Long,
    val count: Long
)

data class BinanceOrderResponse(
    val symbol: String,
    val orderId: Long,
    val orderListId: Long,
    val clientOrderId: String,
    val transactTime: Long,
    val price: String,
    val origQty: String,
    val executedQty: String,
    val cummulativeQuoteQty: String,
    val status: String,
    val timeInForce: String,
    val type: String,
    val side: String
)
