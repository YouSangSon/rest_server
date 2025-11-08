package yousang.rest_server.adapter.out.external.upbit

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
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
import java.math.BigInteger
import java.security.MessageDigest
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Upbit 거래소 어댑터
 *
 * Upbit REST API 통합 (한국 원화 시장)
 */
@Component
class UpbitAdapter(
    private val restTemplate: RestTemplate,
    private val objectMapper: ObjectMapper,
    @Value("\${upbit.access-key:}") private val accessKey: String,
    @Value("\${upbit.secret-key:}") private val secretKey: String,
    @Value("\${upbit.base-url:https://api.upbit.com}") private val baseUrl: String
) : ExchangeApiPort {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

    // ==================== Market Data ====================

    override fun fetchMarketData(symbol: String): MarketData {
        val upbitSymbol = convertSymbol(symbol)
        val url = "$baseUrl/v1/ticker?markets=$upbitSymbol"

        val response = restTemplate.getForObject(url, Array<UpbitTicker>::class.java)
            ?: throw IllegalStateException("Failed to fetch market data for $symbol")

        if (response.isEmpty()) {
            throw IllegalStateException("No ticker data for $symbol")
        }

        val ticker = response[0]

        return MarketData.create(
            symbol = symbol,
            exchange = "Upbit",
            currentPrice = ticker.trade_price.toBigDecimal(),
            volume24h = ticker.acc_trade_volume_24h.toBigDecimal(),
            high24h = ticker.high_price.toBigDecimal(),
            low24h = ticker.low_price.toBigDecimal(),
            priceChange24h = ticker.change_price.toBigDecimal(),
            priceChangePercent24h = ticker.change_rate * 100,
            timestamp = LocalDateTime.now()
        )
    }

    override fun fetchCandles(
        symbol: String,
        interval: CandleInterval,
        from: LocalDateTime,
        to: LocalDateTime
    ): List<Candle> {
        val upbitSymbol = convertSymbol(symbol)
        val (candleType, unit) = convertInterval(interval)

        val url = UriComponentsBuilder.fromHttpUrl("$baseUrl/v1/candles/$candleType")
            .apply {
                if (unit != null) {
                    queryParam("unit", unit)
                }
            }
            .queryParam("market", upbitSymbol)
            .queryParam("count", 200)
            .queryParam("to", to.format(DateTimeFormatter.ISO_DATE_TIME))
            .build()
            .toUriString()

        val response = restTemplate.getForObject(url, Array<UpbitCandle>::class.java)
            ?: return emptyList()

        return response
            .filter {
                val candleTime = parseUpbitDateTime(it.candle_date_time_kst)
                candleTime.isAfter(from) && candleTime.isBefore(to)
            }
            .map { candle ->
                Candle.create(
                    symbol = symbol,
                    exchange = "Upbit",
                    interval = interval,
                    openTime = parseUpbitDateTime(candle.candle_date_time_kst),
                    closeTime = parseUpbitDateTime(candle.candle_date_time_kst).plusMinutes(getIntervalMinutes(interval)),
                    open = candle.opening_price.toBigDecimal(),
                    high = candle.high_price.toBigDecimal(),
                    low = candle.low_price.toBigDecimal(),
                    close = candle.trade_price.toBigDecimal(),
                    volume = candle.candle_acc_trade_volume.toBigDecimal()
                )
            }
    }

    override fun subscribeToMarketData(symbol: String, callback: (MarketData) -> Unit) {
        println("Subscribing to $symbol on Upbit (WebSocket implementation needed)")
    }

    override fun unsubscribeFromMarketData(symbol: String) {
        println("Unsubscribing from $symbol on Upbit")
    }

    // ==================== Trading ====================

    override fun submitOrder(order: Order): Order {
        if (accessKey.isBlank() || secretKey.isBlank()) {
            throw IllegalStateException("Upbit API credentials not configured")
        }

        val upbitSymbol = convertSymbol(order.symbol)

        val params = mutableMapOf(
            "market" to upbitSymbol,
            "side" to convertOrderSide(order.side),
            "ord_type" to convertOrderType(order.type)
        )

        // 주문 수량/가격 설정
        when (order.type) {
            OrderType.MARKET -> {
                if (order.side == OrderSide.BUY) {
                    // 시장가 매수는 주문 총액 지정
                    order.price?.let { price ->
                        val totalPrice = price * order.quantity
                        params["price"] = totalPrice.toPlainString()
                    }
                } else {
                    // 시장가 매도는 수량 지정
                    params["volume"] = order.quantity.toPlainString()
                }
            }
            OrderType.LIMIT -> {
                params["volume"] = order.quantity.toPlainString()
                params["price"] = order.price?.toPlainString() ?: throw IllegalArgumentException("Price required for LIMIT order")
            }
            else -> throw UnsupportedOperationException("Order type ${order.type} not supported on Upbit")
        }

        val queryString = params.entries.joinToString("&") { "${it.key}=${it.value}" }
        val authToken = generateAuthToken(queryString)

        val url = "$baseUrl/v1/orders"
        val headers = HttpHeaders().apply {
            set("Authorization", "Bearer $authToken")
            set("Content-Type", "application/json")
        }

        try {
            val response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                HttpEntity(params, headers),
                UpbitOrderResponse::class.java
            )

            val upbitOrder = response.body
                ?: throw IllegalStateException("No response from Upbit")

            return order.submit(upbitOrder.uuid)
        } catch (e: Exception) {
            throw IllegalStateException("Failed to submit order to Upbit: ${e.message}", e)
        }
    }

    override fun cancelOrder(orderId: String): Boolean {
        if (accessKey.isBlank() || secretKey.isBlank()) {
            throw IllegalStateException("Upbit API credentials not configured")
        }

        val queryString = "uuid=$orderId"
        val authToken = generateAuthToken(queryString)

        val url = "$baseUrl/v1/order?$queryString"
        val headers = HttpHeaders().apply {
            set("Authorization", "Bearer $authToken")
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
            println("Failed to cancel order on Upbit: ${e.message}")
            false
        }
    }

    override fun getOrderStatus(orderId: String, symbol: String): Order? {
        if (accessKey.isBlank() || secretKey.isBlank()) {
            return null
        }

        val queryString = "uuid=$orderId"
        val authToken = generateAuthToken(queryString)

        val url = "$baseUrl/v1/order?$queryString"
        val headers = HttpHeaders().apply {
            set("Authorization", "Bearer $authToken")
        }

        return try {
            val response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                HttpEntity<Any>(headers),
                UpbitOrderResponse::class.java
            )

            // Response를 Order 도메인 모델로 변환 (구현 필요)
            null
        } catch (e: Exception) {
            println("Failed to get order status: ${e.message}")
            null
        }
    }

    override fun getExchangeName(): String = "Upbit"

    // ==================== Helper Methods ====================

    private fun convertSymbol(symbol: String): String {
        // BTC/KRW -> KRW-BTC
        val parts = symbol.split("/")
        return if (parts.size == 2) {
            "${parts[1]}-${parts[0]}"
        } else {
            symbol
        }
    }

    private fun convertInterval(interval: CandleInterval): Pair<String, Int?> {
        return when (interval) {
            CandleInterval.ONE_MINUTE -> "minutes/1" to 1
            CandleInterval.FIVE_MINUTES -> "minutes/5" to 5
            CandleInterval.FIFTEEN_MINUTES -> "minutes/15" to 15
            CandleInterval.THIRTY_MINUTES -> "minutes/30" to 30
            CandleInterval.ONE_HOUR -> "minutes/60" to 60
            CandleInterval.FOUR_HOURS -> "minutes/240" to 240
            CandleInterval.ONE_DAY -> "days" to null
            CandleInterval.ONE_WEEK -> "weeks" to null
            CandleInterval.ONE_MONTH -> "months" to null
        }
    }

    private fun convertOrderSide(side: OrderSide): String {
        return when (side) {
            OrderSide.BUY -> "bid"
            OrderSide.SELL -> "ask"
        }
    }

    private fun convertOrderType(type: OrderType): String {
        return when (type) {
            OrderType.MARKET -> "price" // Upbit uses 'price' for market orders
            OrderType.LIMIT -> "limit"
            else -> throw UnsupportedOperationException("Order type $type not supported on Upbit")
        }
    }

    private fun getIntervalMinutes(interval: CandleInterval): Long {
        return when (interval) {
            CandleInterval.ONE_MINUTE -> 1
            CandleInterval.FIVE_MINUTES -> 5
            CandleInterval.FIFTEEN_MINUTES -> 15
            CandleInterval.THIRTY_MINUTES -> 30
            CandleInterval.ONE_HOUR -> 60
            CandleInterval.FOUR_HOURS -> 240
            CandleInterval.ONE_DAY -> 1440
            CandleInterval.ONE_WEEK -> 10080
            CandleInterval.ONE_MONTH -> 43200
        }
    }

    private fun parseUpbitDateTime(dateTimeStr: String): LocalDateTime {
        return try {
            LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_DATE_TIME)
        } catch (e: Exception) {
            LocalDateTime.now()
        }
    }

    private fun generateAuthToken(queryString: String? = null): String {
        val algorithm = Algorithm.HMAC256(secretKey)
        val jwtBuilder = JWT.create()
            .withClaim("access_key", accessKey)
            .withClaim("nonce", UUID.randomUUID().toString())

        if (queryString != null) {
            val md = MessageDigest.getInstance("SHA-512")
            md.update(queryString.toByteArray())
            val queryHash = String.format("%0128x", BigInteger(1, md.digest()))
            jwtBuilder.withClaim("query_hash", queryHash)
            jwtBuilder.withClaim("query_hash_alg", "SHA512")
        }

        return jwtBuilder.sign(algorithm)
    }
}

/**
 * Upbit API 응답 모델
 */
data class UpbitTicker(
    val market: String,
    val trade_date: String,
    val trade_time: String,
    val trade_date_kst: String,
    val trade_time_kst: String,
    val trade_timestamp: Long,
    val opening_price: Double,
    val high_price: Double,
    val low_price: Double,
    val trade_price: Double,
    val prev_closing_price: Double,
    val change: String,
    val change_price: Double,
    val change_rate: Double,
    val signed_change_price: Double,
    val signed_change_rate: Double,
    val trade_volume: Double,
    val acc_trade_price: Double,
    val acc_trade_price_24h: Double,
    val acc_trade_volume: Double,
    val acc_trade_volume_24h: Double,
    val highest_52_week_price: Double,
    val highest_52_week_date: String,
    val lowest_52_week_price: Double,
    val lowest_52_week_date: String,
    val timestamp: Long
)

data class UpbitCandle(
    val market: String,
    val candle_date_time_utc: String,
    val candle_date_time_kst: String,
    val opening_price: Double,
    val high_price: Double,
    val low_price: Double,
    val trade_price: Double,
    val timestamp: Long,
    val candle_acc_trade_price: Double,
    val candle_acc_trade_volume: Double,
    val unit: Int?
)

data class UpbitOrderResponse(
    val uuid: String,
    val side: String,
    val ord_type: String,
    val price: String?,
    val state: String,
    val market: String,
    val created_at: String,
    val volume: String?,
    val remaining_volume: String?,
    val reserved_fee: String,
    val remaining_fee: String,
    val paid_fee: String,
    val locked: String,
    val executed_volume: String,
    val trades_count: Int
)
